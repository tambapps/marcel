package com.tambapps.marcel.semantic.ast.statement

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode

class ForStatementNode(
  node: CstNode,
  val initStatement: StatementNode,
  val condition: ExpressionNode,
  val iteratorStatement: StatementNode,
  var bodyStatement: StatementNode
) : AbstractStatementNode(node) {
  override fun <T> accept(visitor: StatementNodeVisitor<T>) = visitor.visit(this)
}