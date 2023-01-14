package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.visitor.ExpressionVisitor

data class BinaryOperatorNode(val type: TokenType): ExpressionNode {
  lateinit var leftOperand: ExpressionNode
  lateinit var rightOperand: ExpressionNode

  override fun accept(expressionVisitor: ExpressionVisitor) {
    expressionVisitor.visit(this)
  }
}