package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode

class ElvisThrowNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val expression: ExpressionNode,
  val throwableException: ExpressionNode
) :
  AbstractExpressionNode(parent, tokenStart, tokenEnd) {

  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)
}