package com.tambapps.marcel.semantic.ast.statement

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.visitor.StatementNodeVisitor

class DoWhileNode(
  node: CstNode,
  val condition: ExpressionNode,
  var statement: StatementNode
) : AbstractStatementNode(node) {

  override fun <T> accept(visitor: StatementNodeVisitor<T>) = visitor.visit(this)

}