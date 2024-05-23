package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.MethodCstNode
import com.tambapps.marcel.semantic.CompilationPurpose
import com.tambapps.marcel.semantic.SemanticHelper
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.exception.MarcelSyntaxTreeTransformationException
import com.tambapps.marcel.semantic.extensions.javaType

import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.SourceJavaType
import marcel.lang.Script
import java.util.concurrent.ConcurrentHashMap

/**
 * AST Transformations caching results of the annotated method
 */
class CachedAstTransformation : GenerateMethodAstTransformation() {

  companion object {
    const val THREAD_SAFE_OPTION = "threadSafe"
  }

  override fun generateSignatures(
    node: CstNode,
    javaType: SourceJavaType,
    annotation: AnnotationNode
  ): List<JavaMethod> {
    if ((node as? MethodCstNode)?.isAsync == true) {
      throw MarcelSyntaxTreeTransformationException(this, node.token, "Cannot cache async methods")
    }
    // as the generated method is private and only called for this caching usecase, no need to define it here
    //  (it would be anyway hard to do so as we don't have the AST node here)
    return emptyList()
  }

  /**
   * Generates a new method doCompute whose body is the body of the original annotated method and modifies the original method
   * so that it uses the cache and call doCompute if the result wasn't already computed.
   *
   * When the 'threadSafe' option is not provided we use a simple HashMap. When this option is enabled, we use a ConcurrentHashMap
   * and call map.computeIfAbsent(key, (k) -> doCompute(k)).
   *
   * In case the method has multiple parameter, the key becomes the list of all those parameters
   */
  override fun generateMethodNodes(node: AstNode, classNode: ClassNode, annotation: AnnotationNode): List<MethodNode> {
    val originalMethod = node as MethodNode
    if (originalMethod.isConstructor) throw MarcelSyntaxTreeTransformationException(
      this,
      node.token,
      "Annotated method is a constructor"
    )
    if (originalMethod.returnType == JavaType.void) throw MarcelSyntaxTreeTransformationException(
      this,
      node.token,
      "Cannot cache void methods"
    )
    if (originalMethod.parameters.isEmpty()) throw MarcelSyntaxTreeTransformationException(
      this,
      node.token,
      "Cannot cache methods with no parameters"
    )
    val newMethodName = "doCompute" + originalMethod.name.first().uppercase() + originalMethod.name.substring(1)

    val doComputeMethod = methodNode(
      visibility = Visibility.INTERNAL,
      name = newMethodName,
      parameters = originalMethod.parameters,
      returnType = originalMethod.returnType
    ) {
      addAllStmt(originalMethod.blockStatement.statements)
    }

    val threadSafe = annotation.getAttribute(THREAD_SAFE_OPTION)?.value == true
    val cacheExpr = getCacheExpression(classNode, originalMethod, threadSafe)

    // rewrite the original method
    originalMethod.blockStatement.statements.clear()
    addStatements(originalMethod) {
      val isMultiParams = originalMethod.parameters.size > 1
      val cacheKeyRef = if (!isMultiParams) argRef(0)
      else {
        val lv = currentMethodScope.addLocalVariable(List::class.javaType)
        varAssignStmt(
          lv,
          cast(
            array(JavaType.objectArray, originalMethod.parameters.map { cast(ref(it), JavaType.Object) }),
            List::class.javaType
          )
        )
        ref(lv)
      }
      val cacheKeyObjectRef = cast(cacheKeyRef, JavaType.Object)
      if (threadSafe) {
        // generate code cache.computeIfAbsent(key, k -> doCompute(k))
        val ciaLambda = newLambda(
          classNode,
          parameters = listOf(parameter(cacheKeyObjectRef.type, "param")),
          returnType = doComputeMethod.returnType.objectType,
          interfaceType = java.util.function.Function::class.javaType,
        ) {
          val doComputeMethodCall = if (isMultiParams) {
            val listLv = currentMethodScope.addLocalVariable(List::class.javaType)
            varAssignStmt(listLv, argRef(0))

            fCall(owner = outerRef(), method = doComputeMethod,
              arguments = doComputeMethod.parameters.mapIndexed { index, methodParameter ->
                cast(
                  fCall(
                    owner = ref(
                      listLv
                    ), name = "get", arguments = listOf(int(index))
                  ), methodParameter.type
                )
              })
          } else fCall(
            owner = outerRef(),
            method = doComputeMethod,
            arguments = listOf(cast(argRef(0), cacheKeyRef.type))
          )
          returnStmt(doComputeMethodCall)
        }
        returnStmt(
          fCall(
            owner = cacheExpr,
            name = "computeIfAbsent",
            arguments = listOf(cacheKeyObjectRef, ciaLambda)
          )
        )
      } else {
        // generate code if (!cache.containsKey(key)) cache[key] = doCompute(key); return cache[key]
        ifStmt(notExpr(fCall(owner = cacheExpr, name = "containsKey", arguments = listOf(cacheKeyObjectRef)))) {
          stmt(
            fCall(
              owner = cacheExpr, name = "put",
              arguments = listOf(
                cacheKeyObjectRef,
                cast(
                  fCall(
                    owner = thisRef(),
                    method = doComputeMethod,
                    arguments = originalMethod.parameters.map { ref(it) }), JavaType.Object
                )
              )
            )
          )
        }
        returnStmt(fCall(owner = cacheExpr, name = "get", arguments = listOf(cacheKeyObjectRef)))
      }
    }
    return listOf(doComputeMethod)
  }

  private fun getCacheExpression(
    classNode: ClassNode,
    originalMethod: MethodNode,
    threadSafe: Boolean
  ): ExpressionNode {
    val fieldName = originalMethod.name + "\$cache"
    val cacheInitValueExpr = constructorCall(
      method = symbolResolver.findMethodOrThrow(
        if (threadSafe) ConcurrentHashMap::class.javaType
        else HashMap::class.javaType, JavaMethod.CONSTRUCTOR_NAME, emptyList()
      ), arguments = emptyList()
    )
    if (purpose == CompilationPurpose.REPL && classNode.type.implements(Script::class.javaType)) {
      // init field in constructor
      classNode.constructors.forEach {
        SemanticHelper.addStatementLast(
          ExpressionStatementNode(fCall("setVariable", listOf(string(fieldName), cacheInitValueExpr), thisRef())),
          it.blockStatement
        )
      }
      return fCall(name = "getVariable", arguments = listOf(string(fieldName)), owner = thisRef(), castType = java.util.Map::class.javaType)
    } else {
      val cacheField = fieldNode(Map::class.javaType, originalMethod.name + "\$cache")
      addField(classNode, cacheField, cacheInitValueExpr)

      return ref(cacheField)
    }
  }
}