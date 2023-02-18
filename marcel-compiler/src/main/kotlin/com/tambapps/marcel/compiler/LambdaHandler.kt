package com.tambapps.marcel.compiler

import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.ast.AstNodeTypeResolver
import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.ast.ConstructorNode
import com.tambapps.marcel.parser.ast.MethodNode
import com.tambapps.marcel.parser.ast.expression.FunctionBlockNode
import com.tambapps.marcel.parser.ast.expression.FunctionCallNode
import com.tambapps.marcel.parser.ast.expression.LambdaNode
import com.tambapps.marcel.parser.ast.expression.ReferenceExpression
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.scope.LambdaScope
import com.tambapps.marcel.parser.scope.MethodScope
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import org.objectweb.asm.Opcodes

class LambdaHandler(private val classNode: ClassNode, private val typeResolver: JavaTypeResolver) {
  private var lambdasCount = 0

  fun defineLambda(lambdaNode: LambdaNode): JavaType {
    val scope = lambdaNode.scope
    val className = generateLambdaName(scope)
    val interfaceType = lambdaNode.interfaceType
    tryMatchParametersWithLambda(lambdaNode)
    val lambdaInterfaceType = AstNodeTypeResolver.getLambdaType(typeResolver, lambdaNode)
    val type = scope.typeResolver.defineClass(className, JavaType.Object, false,
        if (interfaceType != null) listOf(interfaceType, lambdaInterfaceType)
        else listOf(lambdaInterfaceType))
    val methods = mutableListOf<MethodNode>()
    val lambdaClassNode = ClassNode(scope.copy(type), Opcodes.ACC_PRIVATE, type, type.superType!!, false, methods, emptyList(), mutableListOf())
    // adding default constructor
    lambdaClassNode.methods.add(ConstructorNode.emptyConstructor(lambdaClassNode))

    // define lambda interfaceType method
    val lambdaMethod = scope.typeResolver.getDeclaredMethods(lambdaInterfaceType).first()
    val lambdaReturnType = lambdaInterfaceType.genericTypes.lastOrNull() ?: JavaType.Object
    var parameters = lambdaMethod.parameters.toMutableList()
    if (parameters.size == 1 && lambdaNode.parameters.isEmpty()) {
      parameters = mutableListOf(MethodParameter(lambdaMethod.parameters.first().type, lambdaMethod.parameters.first().type, "it"))
    } else if (lambdaNode.parameters.isNotEmpty()) {
      if (lambdaMethod.parameters.size != lambdaNode.parameters.size) {
        throw SemanticException("Lambda method parameters count is not consistent")
      }
      for (i in lambdaMethod.parameters.indices) {
        val methodParameter = lambdaMethod.parameters[i]
        val nodeParameter = lambdaNode.parameters[i]
        if (!nodeParameter.type.isAssignableFrom(methodParameter.type)) {
          throw SemanticException("Bad parameter lambda ${nodeParameter.type} ${nodeParameter.name}")
        }
        parameters[i] = nodeParameter
      }
    }

    val lambdaMethodScope = MethodScope(lambdaClassNode.scope, lambdaMethod.name, parameters, lambdaReturnType)
    val fblock = FunctionBlockNode(lambdaMethodScope, lambdaNode.blockNode.statements)
    fblock.setTreeScope(lambdaMethodScope)
    lambdaClassNode.addMethod(
      MethodNode(Opcodes.ACC_PUBLIC, type, lambdaMethod.name,
        fblock,
        parameters.toMutableList(),
        lambdaReturnType, lambdaMethodScope,
        false)
    )

    // define the interface method if any
    if (interfaceType != null) {
      val declaredMethods = scope.typeResolver.getDeclaredMethods(interfaceType)
        .filter { it.isAbstract }
      if (declaredMethods.size != 1) {
        throw SemanticException("Cannot make a lambda out of interface $interfaceType")
      }

      val interfaceMethod = declaredMethods.first()
      val interfaceMethodScope = MethodScope(lambdaClassNode.scope, interfaceMethod.name, parameters, interfaceMethod.returnType)


      val lambdaMethodCall = FunctionCallNode(interfaceMethodScope, lambdaMethod.name,
        parameters.map {
          ReferenceExpression(interfaceMethodScope, it.name)
        }.toMutableList(), ReferenceExpression.thisRef(interfaceMethodScope))

      lambdaClassNode.addMethod(
        MethodNode(Opcodes.ACC_PUBLIC, type, interfaceMethod.name,
          FunctionBlockNode(interfaceMethodScope, mutableListOf(ExpressionStatementNode(lambdaMethodCall))),
          parameters.toMutableList(),
          interfaceMethod.returnType, interfaceMethodScope,
          false)
      )
    }
    lambdaClassNode.methods.forEach { scope.typeResolver.defineMethod(type, it) }
    // add lambda class as an inner class of the class it was defined in
    classNode.innerClasses.add(lambdaClassNode)
    return type
  }

  private fun tryMatchParametersWithLambda(lambdaNode: LambdaNode) {
    if (lambdaNode.explicit0Parameters || lambdaNode.parameters.isNotEmpty()) return
    val interfaceType = lambdaNode.interfaceType ?: return
    val method = typeResolver.getInterfaceLambdaMethod(interfaceType)
    lambdaNode.parameters.clear()
    lambdaNode.parameters.addAll(method.parameters)
    if (lambdaNode.parameters.size == 1) {
      val p = lambdaNode.parameters[0]
      lambdaNode.parameters[0] = MethodParameter(p.type, p.rawType, "it", p.isFinal)
    }
  }

  private fun generateLambdaName(scope: LambdaScope): String {
    val prefix = scope.classType.simpleName + "\$"
    return if (scope.parentScope is MethodScope) "${prefix}_" + (scope.parentScope as MethodScope).methodName + "_closure" + lambdasCount++
    else "${prefix}_" + scope.classType.simpleName + "_lambda" + lambdasCount++
  }
}