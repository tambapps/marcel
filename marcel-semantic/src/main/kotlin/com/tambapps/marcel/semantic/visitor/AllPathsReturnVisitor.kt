package com.tambapps.marcel.semantic.visitor

import com.tambapps.marcel.semantic.ast.statement.BlockStatementNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNodeVisitor

object AllPathsReturnVisitor: StatementNodeVisitor<Boolean> {


  override fun visit(node: ExpressionStatementNode) = false

  override fun visit(node: ReturnStatementNode) = true

  override fun visit(node: BlockStatementNode) = test(node.statements)

  fun test(statements: List<StatementNode>) = statements.any { it.accept(this) }
}