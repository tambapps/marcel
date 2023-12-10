package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeNode

class TruthyVariableDeclarationNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val type: TypeNode,
  override val value: String,
  val expression: ExpressionNode
) :
  AbstractExpressionNode(parent, tokenStart, tokenEnd) {
  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)
}