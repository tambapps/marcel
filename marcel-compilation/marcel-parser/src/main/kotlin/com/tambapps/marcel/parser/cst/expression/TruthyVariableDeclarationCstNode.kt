package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor

class TruthyVariableDeclarationCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val type: TypeCstNode,
  val variableToken: LexToken,
  val expression: ExpressionCstNode
) :
  AbstractExpressionCstNode(parent, tokenStart, tokenEnd) {
  override val value: String = variableToken.value

  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)
}