package com.tambapps.marcel.parser.ast

import org.objectweb.asm.MethodVisitor

class TernaryNode(boolExpression: ExpressionNode,
                  trueExpression: ExpressionNode,
                  falseExpression: ExpressionNode): ExpressionNode {
  override fun writeInstructions(mv: MethodVisitor) {
    TODO("Not yet implemented")
  }
}