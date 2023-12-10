package com.tambapps.marcel.parser.cst.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionNode

class WhileNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val condition: ExpressionNode,
  val statement: StatementNode
) :
  AbstractStatementNode(parent, tokenStart, tokenEnd) {
  override fun <T> accept(visitor: StatementCstNodeVisitor<T>) = visitor.visit(this)
}