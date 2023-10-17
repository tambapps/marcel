package com.tambapps.marcel.semantic.visitor

import com.tambapps.marcel.semantic.ast.statement.BlockStatementNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.ForInIteratorStatementNode
import com.tambapps.marcel.semantic.ast.statement.IfStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNodeVisitor

object AllPathsReturnVisitor: StatementNodeVisitor<Boolean> {

  override fun visit(node: ExpressionStatementNode) = false

  override fun visit(node: ReturnStatementNode) = true

  override fun visit(node: BlockStatementNode) = test(node.statements)

  override fun visit(node: IfStatementNode) = node.trueStatementNode.accept(this)
      && node.falseStatementNode != null && node.falseStatementNode.accept(this)

  override fun visit(node: ForInIteratorStatementNode) = false
  fun test(statements: List<StatementNode>) = statements.any { it.accept(this) }
}