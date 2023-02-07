package com.tambapps.marcel.compiler

import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.ast.MethodNode
import com.tambapps.marcel.parser.ast.expression.LambdaNode
import com.tambapps.marcel.parser.scope.MethodScope
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaType
import jdk.internal.org.objectweb.asm.Opcodes
import marcel.lang.lambda.Lambda1

class LambdaHandler(private val classNode: ClassNode, private val methodNode: MethodNode) {
  private var lambdasCount = 0

  fun defineLambda(lambdaNode: LambdaNode) {
    val scope = lambdaNode.scope
    val className = generateLambdaName(scope)
    val lambdaInterfaceType = findLambdaInterface(lambdaNode)
    val interfaceType = lambdaNode.interfaceType
    val type = scope.typeResolver.defineClass(className, lambdaInterfaceType, false,
        if (interfaceType != null) listOf(interfaceType)
        else listOf())
    val methods = mutableListOf<MethodNode>()
    val classNode = ClassNode(scope.copy(), Opcodes.ACC_PRIVATE, type, type.superType!!, false, methods, mutableListOf())
    // define lambda interfaceType method


    // TODO generate class implementing the right interface(s)
  }

  private fun findLambdaInterface(lambdaNode: LambdaNode): JavaType {
    return when (lambdaNode.parameters.size) {
      1 -> JavaType.of(Lambda1::class.java)
      else -> TODO("Doesn't handle lambda with such parameters for now")
    }
  }
  private fun generateLambdaName(scope: Scope): String {
    return if (scope is MethodScope) "_" + scope.methodName + "_closure" + lambdasCount++
    else "_" + scope.classType.simpleName + "_closure" + lambdasCount++

  }
}