package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.lexer.TokenType
import org.objectweb.asm.MethodVisitor

class BinaryOperatorNode(val type: TokenType): ExpressionNode {
  lateinit var leftOperand: ExpressionNode
  lateinit var rightOperand: ExpressionNode
  override fun writeInstructions(mv: MethodVisitor) {
    TODO("Not yet implemented")
  }
}