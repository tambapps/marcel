package com.tambapps.marcel.semantic.ast.statement

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.variable.LocalVariable

class ForInIteratorStatementNode(
  node: CstNode,
  val variable: LocalVariable,
  val iteratorVariable: LocalVariable,
  val iteratorExpression: ExpressionNode,
  val nextMethodCall: ExpressionNode,
  val bodyStatement: StatementNode
) : AbstractStatementNode(node) {
  override fun <T> accept(visitor: StatementNodeVisitor<T>) = visitor.visit(this)
}