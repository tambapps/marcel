package com.tambapps.marcel.semantic.ast.statement

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode

class IfStatementNode(
  val conditionNode: ExpressionNode,
  var trueStatementNode: StatementNode,
  var falseStatementNode: StatementNode?,
  node: CstNode
) : AbstractStatementNode(node) {

  override fun <T> accept(visitor: StatementNodeVisitor<T>) = visitor.visit(this)
}