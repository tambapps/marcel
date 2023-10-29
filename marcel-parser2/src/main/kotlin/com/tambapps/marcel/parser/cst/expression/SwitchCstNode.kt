package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNode

class SwitchCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  branches: MutableList<Pair<ExpressionCstNode, StatementCstNode>>,
  elseStatement: StatementCstNode?,
  val switchExpression: ExpressionCstNode
) : WhenCstNode(parent, tokenStart, tokenEnd, branches, elseStatement) {
  override fun <T> accept(visitor: ExpressionCstNodeVisitor<T>) = visitor.visit(this)
}