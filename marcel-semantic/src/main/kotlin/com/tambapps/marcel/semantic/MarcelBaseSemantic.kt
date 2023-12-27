package com.tambapps.marcel.semantic

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.FieldNode
import com.tambapps.marcel.semantic.ast.LambdaClassNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.cast.AstNodeCaster
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.FunctionCallNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.ThisReferenceNode
import com.tambapps.marcel.semantic.ast.expression.literal.ArrayNode
import com.tambapps.marcel.semantic.ast.expression.operator.VariableAssignmentNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.method.MethodParameter
import com.tambapps.marcel.semantic.scope.MethodInnerScope
import com.tambapps.marcel.semantic.scope.MethodScope
import com.tambapps.marcel.semantic.scope.Scope
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import com.tambapps.marcel.semantic.variable.LocalVariable
import java.util.*

abstract class MarcelBaseSemantic {

  protected abstract val typeResolver: JavaTypeResolver
  protected abstract val caster: AstNodeCaster

  val scopeQueue = LinkedList<Scope>()
  protected val currentScope: Scope get() = scopeQueue.peek() // FIFO
  protected val currentMethodScope get() = currentScope as? MethodScope ?: throw MarcelSemanticException(LexToken.DUMMY, "Not in a method")
  protected val currentInnerMethodScope get() = currentScope as? MethodInnerScope ?: throw MarcelSemanticException(LexToken.DUMMY, "Not in a inner scope")

  protected inline fun <T: Scope, U> useScope(scope: T, consumer: (T) -> U): U {
    scopeQueue.push(scope)
    val u = consumer.invoke(scope)
    scope.dispose()
    scopeQueue.pop()
    return u
  }

  protected fun newInnerScope() = MethodInnerScope(currentMethodScope)
  protected inline fun <U> useInnerScope(consumer: (MethodInnerScope) -> U)
      = useScope(newInnerScope(), consumer)

  fun visit(node: TypeCstNode): JavaType = currentScope.resolveTypeOrThrow(node)

  /**
   * Cast method arguments if necessary and transform them to handle varags if necessary
   *
   * @param method the methods
   * @param arguments the arguments
   * @return the list of arguments to call the provided method
   */
  protected fun castedArguments(method: JavaMethod, arguments: List<ExpressionNode>): List<ExpressionNode> {
    if (!method.isVarArgs
      // in case the provider did provide the array
      || typeResolver.matchesMethod(method, arguments)) return arguments.mapIndexed { index, expressionNode -> caster.cast(method.parameters[index].type, expressionNode) }
    val castedArguments = mutableListOf<ExpressionNode>()

    var i = 0
    while (i < method.parameters.size - 1) {
      castedArguments.add(
        caster.cast(method.parameters[i].type, arguments[i])
      )
      i++
    }
    val varArgType = method.varArgType
    val arrayArgs = mutableListOf<ExpressionNode>()
    while (i < arguments.size) {
      arrayArgs.add(caster.cast(varArgType, arguments[i]))
      i++
    }
    castedArguments.add(ArrayNode(arrayArgs, LexToken.DUMMY, LexToken.DUMMY, method.varArgsType))
    return castedArguments
  }

  protected fun fCall(node: CstNode, name: String, arguments: List<ExpressionNode>,
                    owner: ExpressionNode,
                    castType: JavaType? = null): ExpressionNode {
    return fCall(node, owner.type, name, arguments, owner, castType)
  }

  protected fun fCall(node: CstNode, ownerType: JavaType, name: String, arguments: List<ExpressionNode>,
                    owner: ExpressionNode? = null,
                    castType: JavaType? = null): ExpressionNode {
    val method = typeResolver.findMethodOrThrow(ownerType, name, arguments, node.token)
    return fCall(node, method, arguments, owner, castType)
  }

  protected fun fCall(
    node: CstNode,
    method: JavaMethod,
    arguments: List<ExpressionNode>,
    owner: ExpressionNode? = null,
    castType: JavaType? = null) = fCall(node.tokenStart,
    LexToken.DUMMY, // passing dummy to inform code highlight that this is not a fCall from the real marcel source code
    method, arguments, owner, castType)
  protected fun fCall(
    tokenStart: LexToken,
    tokenEnd: LexToken,
    method: JavaMethod,
    arguments: List<ExpressionNode>,
    owner: ExpressionNode? = null,
    castType: JavaType? = null): ExpressionNode {
    if (owner != null && method.isMarcelStatic) throw MarcelSemanticException(tokenStart, "Method $method is static but was call from an instance")
    val node = FunctionCallNode(method, owner, castedArguments(method, arguments), tokenStart, tokenEnd)
    return if (castType != null) caster.cast(castType, node) else node
  }

  protected fun generateLambdaClassName(lambdaOuterClassNode: ClassNode): String {
    return (
        if (currentMethodScope.method.isConstructor) "init"
        else currentMethodScope.method.name
        ) + "_lambda" + (lambdaOuterClassNode.innerClasses.count { it is LambdaClassNode } + 1)
  }

  protected fun generateOutClassFields(classType: JavaType, classNode: ClassNode): List<FieldNode> {
    val fields = mutableListOf<FieldNode>()
    if (!classNode.isStatic && classType.outerTypeName != null) {
      // java generates fields to reference outer class(es) from inner class. So does marcel.
      var outerLevel = 0
      var levelType: JavaType? = classType.outerTypeName?.let { typeResolver.of(classNode.token, it) }
      while (levelType != null) {
        val outerFieldName = "this$$outerLevel"
        val fieldNode = FieldNode(
          type = levelType,
          name = outerFieldName,
          owner = classNode.type,
          annotations = emptyList(),
          isFinal = true,
          visibility = Visibility.INTERNAL,
          isStatic = false,
          isSynthetic = true,
          tokenStart = classNode.tokenStart,
          tokenEnd = classNode.tokenEnd
        )
        fields.add(fieldNode)
        classNode.fields.add(fieldNode)
        typeResolver.defineField(classType, fieldNode)

        outerLevel++
        levelType = levelType.outerTypeName?.let { typeResolver.of(classNode.token, it, emptyList()) }
      }
    }
    return fields
  }

  protected fun handleLambdaInnerClassFields(
    lambdaNode: ClassNode,
    lambdaConstructor: MethodNode,
    constructorArguments: MutableList<ExpressionNode>,
    token: LexToken,
  ) {
    val outerClassFields = generateOutClassFields(lambdaNode.type, lambdaNode)
    for (i in outerClassFields.indices) {
      // if we're here we know the context is not static as the above method only generates fields if it is not static
      val outerClassField = outerClassFields[i]
      // adding at the beginning
      lambdaConstructor.parameters.add(i, MethodParameter(outerClassField.type, outerClassField.name))
      val (outerLevel, _) = outerLevel(token, lambdaNode.type, outerClassField.type)
        ?: throw MarcelSemanticException(token, "Lambda cannot be generated in this context")

      val argument = getInnerOuterReference(token, outerLevel)
        ?: throw MarcelSemanticException(token, "Lambda cannot be generated in this context")
      constructorArguments.add(i, argument)
      lambdaConstructor.blockStatement.statements.add(i + 1, // +1 because first statement should be super call
        ExpressionStatementNode(
          VariableAssignmentNode(
            owner = ThisReferenceNode(lambdaNode.type, lambdaNode.token),
            variable = outerClassField,
            expression = ReferenceNode(variable = LocalVariable(outerClassField.type, outerClassField.name, nbSlots = 1, index = i + 1, isFinal = false),  token = token),
            tokenStart = token,
            tokenEnd = token,
          )
        )
      )
    }
  }

  // get the reference to pass to an inner class constructor for the provided outerLevel
  protected fun getInnerOuterReference(token: LexToken, outerLevel: Int): ExpressionNode? {
    val thisNode = ThisReferenceNode(currentScope.classType, token)
    return if (outerLevel == 0) thisNode
    else currentScope.findField("this$${outerLevel - 1}")?.let { ReferenceNode(owner = thisNode, variable = it, token = token) }
  }


  // -1 means self, 0 means outer, 1 means outer.outer and so on
  // this is in order to be coherent with this$0 which corresponds to the outer, and so on
  protected fun outerLevel(token: LexToken, innerClass: JavaType, outerClass: JavaType): Pair<Int, JavaType>? {
    var outerLevel = -1
    var levelType: JavaType? = innerClass
    while (levelType != null && !outerClass.isAssignableFrom(levelType)) {
      outerLevel++
      levelType = levelType.outerTypeName?.let { typeResolver.of(token, it, emptyList()) }
    }
    return if (levelType == null) null
    else Pair(outerLevel, levelType)
  }
}