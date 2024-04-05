package com.tambapps.marcel.semantic.ast.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode

class IfStatementNode(
  val conditionNode: ExpressionNode,
  var trueStatementNode: StatementNode,
  var falseStatementNode: StatementNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
) : AbstractStatementNode(tokenStart, tokenEnd) {

  constructor(
    conditionNode: ExpressionNode,
    trueStatementNode: StatementNode,
    falseStatementNode: StatementNode?,
    node: CstNode
  ) : this(conditionNode, trueStatementNode, falseStatementNode, node.tokenStart, node.tokenEnd)

  override fun <T> accept(visitor: StatementNodeVisitor<T>) = visitor.visit(this)
}