package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.ComparisonOperator
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.ast.statement.IfStatementNode
import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.scope.Scope

sealed class ConditionalBranchFlowNode<T: ConditionalBranchNode>(
  override var scope: Scope, val branches: MutableList<T>,
  val elseStatement: StatementNode?
): ExpressionNode, ScopedNode<Scope>
open class WhenNode constructor(scope: Scope,
                                branches: MutableList<WhenBranchNode>,
                                elseStatement: StatementNode?): ConditionalBranchFlowNode<WhenBranchNode>(scope, branches, elseStatement) {


  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>): T {
    return astNodeVisitor.visit(this)
  }


  override fun toString(): String {
    var branchesString = branches.joinToString(separator = "\n")
    if (elseStatement != null) branchesString += "else -> $elseStatement"
    return "when {\n$branchesString\n}"
  }
}

sealed class ConditionalBranchNode(val conditionExpressionNode: ExpressionNode,
                                   var statementNode: StatementNode): ExpressionNode {

  fun toIf(): IfStatementNode {
    return IfStatementNode(conditionExpressionNode, statementNode, null)
  }

}
class WhenBranchNode(conditionExpressionNode: ExpressionNode,
                     statementNode: StatementNode): ConditionalBranchNode(conditionExpressionNode, statementNode) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>): T {
    return astNodeVisitor.visit(this)
  }

  override fun toString(): String {
    return "$conditionExpressionNode -> $statementNode"
  }
}

// switch nodes
class SwitchNode constructor(override var scope: Scope,
                             val expressionNode: ExpressionNode, branches: MutableList<SwitchBranchNode>,
                             elseStatement: StatementNode?): ConditionalBranchFlowNode<SwitchBranchNode>(scope, branches, elseStatement) {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    var branchesString = branches.joinToString(separator = "\n")
    if (elseStatement != null) branchesString += "else -> $elseStatement"
    return "switch($expressionNode) {\n$branchesString\n}"
  }

}
class SwitchBranchNode private constructor(
  val valueExpression: ExpressionNode,
  val itReference: ReferenceExpression,
  statementNode: StatementNode)
  : ConditionalBranchNode(ComparisonOperatorNode(ComparisonOperator.EQUAL, valueExpression,
  // using reference "it" to avoid evaluating more than once the value (e.g. executing more than once functon calls)
  itReference), statementNode) {

  constructor(scope: Scope,
    valueExpression: ExpressionNode,
    statementNode: StatementNode): this(valueExpression, ReferenceExpression(scope, "it"), statementNode)

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return "$valueExpression -> $statementNode"
  }
}

