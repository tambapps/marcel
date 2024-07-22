package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor

class TernaryCstNode(
  val testExpressionNode: ExpressionCstNode,
  val trueExpressionNode: ExpressionCstNode,
  val falseExpressionNode: ExpressionCstNode,
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken
) :
  AbstractExpressionCstNode(parent, tokenStart, tokenEnd) {

  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)
}