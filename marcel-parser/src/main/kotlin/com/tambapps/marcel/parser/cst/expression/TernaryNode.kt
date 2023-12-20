package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode

class TernaryNode(
  val testExpressionNode: ExpressionNode,
  val trueExpressionNode: ExpressionNode,
  val falseExpressionNode: ExpressionNode,
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken
) :
  AbstractExpressionNode(parent, tokenStart, tokenEnd) {

  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)
}