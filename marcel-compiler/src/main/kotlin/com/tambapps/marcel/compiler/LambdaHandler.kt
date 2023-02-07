package com.tambapps.marcel.compiler

import com.tambapps.marcel.parser.ast.AstNodeTypeResolver
import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.ast.MethodNode
import com.tambapps.marcel.parser.ast.expression.LambdaNode
import com.tambapps.marcel.parser.scope.MethodScope
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaType
import marcel.lang.lambda.Lambda1
import org.objectweb.asm.Opcodes

class LambdaHandler(private val classNode: ClassNode, private val methodNode: MethodNode) {
  private var lambdasCount = 0

  fun defineLambda(lambdaNode: LambdaNode) {
    val scope = lambdaNode.scope
    val className = generateLambdaName(scope)
    val lambdaInterfaceType = AstNodeTypeResolver.getLambdaType(lambdaNode)
    val interfaceType = lambdaNode.interfaceType
    val type = scope.typeResolver.defineClass(className, lambdaInterfaceType, false,
        if (interfaceType != null) listOf(interfaceType)
        else listOf())
    val methods = mutableListOf<MethodNode>()
    val classNode = ClassNode(scope.copy(), Opcodes.ACC_PRIVATE, type, type.superType!!, false, methods, mutableListOf())
    // define lambda interfaceType method

    val lambdaMethod = scope.typeResolver.getDeclaredMethods(type)
    TODO()

    // TODO generate class implementing the right interface(s)
  }

  private fun generateLambdaName(scope: Scope): String {
    return if (scope is MethodScope) "_" + scope.methodName + "_closure" + lambdasCount++
    else "_" + scope.classType.simpleName + "_closure" + lambdasCount++
  }
}