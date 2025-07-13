package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor

class TruthyVariableDeclarationCstNode constructor(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val type: TypeCstNode,
  val identifierToken: LexToken,
  val expression: ExpressionCstNode
) :
  AbstractExpressionCstNode(parent, tokenStart, tokenEnd) {
  override val value: String = identifierToken.value

  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

}