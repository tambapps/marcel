package com.tambapps.marcel.compiler

import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.ast.MethodNode
import com.tambapps.marcel.parser.ast.expression.LambdaNode

class LambdaHandler(private val classNode: ClassNode, private val methodNode: MethodNode) {
  private var lambdasCount = 0

  fun defineLambda(lambdaNode: LambdaNode) {
    val className = generateLambdaName()
    // TODO generate class implementing the right interface(s)
  }

  private fun generateLambdaName(): String {
    return "_" + methodNode.name + "_closure" + lambdasCount++
  }
}