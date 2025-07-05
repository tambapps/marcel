package com.tambapps.marcel.semantic.processor.visitor

import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.statement.BlockStatementNode
import com.tambapps.marcel.semantic.ast.statement.BreakNode
import com.tambapps.marcel.semantic.ast.statement.ContinueNode
import com.tambapps.marcel.semantic.ast.statement.DoWhileNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.ForInIteratorStatementNode
import com.tambapps.marcel.semantic.ast.statement.ForStatementNode
import com.tambapps.marcel.semantic.ast.statement.IfStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNodeVisitor
import com.tambapps.marcel.semantic.ast.statement.ThrowNode
import com.tambapps.marcel.semantic.ast.statement.TryNode
import com.tambapps.marcel.semantic.ast.statement.WhileNode

/**
 * Visitor of a switch/when branch allowing to separate the statements (if any) from the yield value
 */
class WhenStatementExpressionSeparator(
  val exprError: (AstNode, String) -> ExpressionNode,
): StatementNodeVisitor<Pair<StatementNode?, ExpressionNode>> {

  override fun visit(node: ExpressionStatementNode) = null to node.expressionNode

  override fun visit(node: ReturnStatementNode) = error(node)

  override fun visit(node: BlockStatementNode): Pair<StatementNode?, ExpressionNode> {
    if (node.statements.isEmpty()) error(node)
    val lastStatement = node.statements.last()
    if (lastStatement !is ExpressionStatementNode) return error(lastStatement)
    return if (node.statements.size == 1) null to lastStatement.expressionNode
    else BlockStatementNode(node.statements.toMutableList().apply {
      removeLast()
    }) to lastStatement.expressionNode
  }

  override fun visit(node: IfStatementNode) = error(node)

  override fun visit(node: ForInIteratorStatementNode) = error(node)

  override fun visit(node: WhileNode) = error(node)

  override fun visit(node: DoWhileNode) = error(node)

  override fun visit(node: ForStatementNode) = error(node)

  override fun visit(node: BreakNode) = error(node)

  override fun visit(node: ContinueNode) = error(node)

  override fun visit(node: ThrowNode) = error(node)

  override fun visit(node: TryNode) = error(node)

  private fun error(node: AstNode, msg: String = "When/switch branch doesn't yield a value"): Pair<StatementNode?, ExpressionNode> {
    return null to exprError(node, msg)
  }
}