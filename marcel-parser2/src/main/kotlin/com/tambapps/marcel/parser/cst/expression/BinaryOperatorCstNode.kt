package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.cst.CstNode

class BinaryOperatorCstNode(
  val tokenType: TokenType,
  val leftOperand: CstExpressionNode,
  val rightOperand: CstExpressionNode,
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken
) : AbstractExpressionCstNode(parent, tokenStart, tokenEnd) {

  override fun <T> accept(visitor: ExpressionCstNodeVisitor<T>) = visitor.visit(this)
}