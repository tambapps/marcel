package com.tambapps.marcel.semantic.ast.statement

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.variable.LocalVariable

class ForInIteratorStatementNode(
  node: CstNode,
  val variable: LocalVariable,
  val iteratorVariable: LocalVariable,
  val iteratorExpression: com.tambapps.marcel.semantic.ast.expression.ExpressionNode,
  val nextMethodCall: com.tambapps.marcel.semantic.ast.expression.ExpressionNode,
  var bodyStatement: StatementNode
) : AbstractStatementNode(node) {
  override fun <T> accept(visitor: StatementNodeVisitor<T>) = visitor.visit(this)
}