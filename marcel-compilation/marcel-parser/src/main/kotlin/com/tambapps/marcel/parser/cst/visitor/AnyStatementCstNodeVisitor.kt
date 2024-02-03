package com.tambapps.marcel.parser.cst.visitor

import com.tambapps.marcel.parser.cst.statement.BlockCstNode
import com.tambapps.marcel.parser.cst.statement.BreakCstNode
import com.tambapps.marcel.parser.cst.statement.ContinueCstNode
import com.tambapps.marcel.parser.cst.statement.DoWhileStatementCstNode
import com.tambapps.marcel.parser.cst.statement.ExpressionStatementCstNode
import com.tambapps.marcel.parser.cst.statement.ForInCstNode
import com.tambapps.marcel.parser.cst.statement.ForInMultiVarCstNode
import com.tambapps.marcel.parser.cst.statement.ForVarCstNode
import com.tambapps.marcel.parser.cst.statement.IfStatementCstNode
import com.tambapps.marcel.parser.cst.statement.MultiVarDeclarationCstNode
import com.tambapps.marcel.parser.cst.statement.ReturnCstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNodeVisitor
import com.tambapps.marcel.parser.cst.statement.ThrowCstNode
import com.tambapps.marcel.parser.cst.statement.TryCatchCstNode
import com.tambapps.marcel.parser.cst.statement.VariableDeclarationCstNode
import com.tambapps.marcel.parser.cst.statement.WhileCstNode

/**
 * Visitor that computes if any node of the provided tree matches the provided predicate
 */
class AnyStatementCstNodeVisitor(
  private val predicate: (StatementCstNode) -> Boolean
): StatementCstNodeVisitor<Boolean> {
  override fun visit(node: ExpressionStatementCstNode) = predicate(node)

  override fun visit(node: ReturnCstNode) = predicate(node)

  override fun visit(node: VariableDeclarationCstNode) = predicate(node)

  override fun visit(node: MultiVarDeclarationCstNode) = predicate(node)

  override fun visit(node: IfStatementCstNode) = predicate(node)
      || node.trueStatementNode.accept(this)
      || node.falseStatementNode?.accept(this) == true

  override fun visit(node: ForInCstNode) = predicate(node)
      || node.statementNode.accept(this)

  override fun visit(node: ForInMultiVarCstNode) = predicate(node)
      || node.statementNode.accept(this)

  override fun visit(node: ForVarCstNode) = predicate(node)
      || node.varDecl.accept(this) || node.iteratorStatement.accept(this) || node.bodyStatement.accept(this)

  override fun visit(node: WhileCstNode) = predicate(node) || node.statement.accept(this)

  override fun visit(node: DoWhileStatementCstNode) = predicate(node) || node.statement.accept(this)

  override fun visit(node: BlockCstNode) = predicate(node) || node.statements.any { it.accept(this) }

  override fun visit(node: BreakCstNode) = predicate(node)

  override fun visit(node: ContinueCstNode) = predicate(node)

  override fun visit(node: ThrowCstNode) = predicate(node)

  override fun visit(node: TryCatchCstNode) = predicate(node) || node.tryNode.accept(this)
      || node.catchNodes.any { it.third.accept(this) } || node.finallyNode?.accept(this) == true
}