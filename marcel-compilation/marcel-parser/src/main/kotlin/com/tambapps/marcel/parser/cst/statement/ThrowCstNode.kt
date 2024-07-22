package com.tambapps.marcel.parser.cst.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.visitor.StatementCstNodeVisitor

class ThrowCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val expression: ExpressionCstNode) :
  AbstractStatementCstNode(parent, tokenStart, tokenEnd) {
  override fun <T> accept(visitor: StatementCstNodeVisitor<T>) = visitor.visit(this)
}