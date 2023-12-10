package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.statement.StatementNode

open class WhenNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val branches: MutableList<Pair<ExpressionNode, StatementNode>>,
  val elseStatement: StatementNode?
) : AbstractExpressionNode(parent, tokenStart, tokenEnd) {
  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)
}