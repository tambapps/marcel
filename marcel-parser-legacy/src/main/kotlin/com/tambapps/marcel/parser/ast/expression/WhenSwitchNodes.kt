package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.ComparisonOperator
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.ast.statement.IfStatementNode
import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.scope.Scope

sealed class ConditionalBranchFlowNode<T: ConditionalBranchNode>(
  token: LexToken,
  override var scope: Scope, val branches: MutableList<T>,
  val elseStatement: StatementNode?
): AbstractExpressionNode(token), ScopedNode<Scope>
open class WhenNode constructor(token: LexToken, scope: Scope,
                                branches: MutableList<WhenBranchNode>,
                                elseStatement: StatementNode?): ConditionalBranchFlowNode<WhenBranchNode>(token, scope, branches, elseStatement) {


  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>): T {
    return astNodeVisitor.visit(this)
  }


  override fun toString(): String {
    var branchesString = branches.joinToString(separator = "\n")
    if (elseStatement != null) branchesString += "else -> $elseStatement"
    return "when {\n$branchesString\n}"
  }
}

sealed class ConditionalBranchNode(token: LexToken, val conditionExpressionNode: ExpressionNode,
                                   var statementNode: StatementNode): AbstractExpressionNode(token) {

  fun toIf(): IfStatementNode {
    return IfStatementNode(conditionExpressionNode, statementNode, null)
  }

}
class WhenBranchNode(token: LexToken, conditionExpressionNode: ExpressionNode,
                     statementNode: StatementNode): ConditionalBranchNode(token, conditionExpressionNode, statementNode) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>): T {
    return astNodeVisitor.visit(this)
  }

  override fun toString(): String {
    return "$conditionExpressionNode -> $statementNode"
  }
}

// switch nodes
class SwitchNode constructor(token: LexToken, override var scope: Scope,
                             val expressionNode: ExpressionNode, branches: MutableList<SwitchBranchNode>,
                             elseStatement: StatementNode?): ConditionalBranchFlowNode<SwitchBranchNode>(token, scope, branches, elseStatement) {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    var branchesString = branches.joinToString(separator = "\n")
    if (elseStatement != null) branchesString += "else -> $elseStatement"
    return "switch($expressionNode) {\n$branchesString\n}"
  }

}
class SwitchBranchNode private constructor(
  token: LexToken,
  val valueExpression: ExpressionNode,
  itReference: ReferenceExpression,
  statementNode: StatementNode)
  : ConditionalBranchNode(token, ComparisonOperatorNode(token, ComparisonOperator.EQUAL, valueExpression,
  // using reference "it" to avoid evaluating more than once the value (e.g. executing more than once functon calls)
  itReference), statementNode) {

  constructor(token: LexToken, scope: Scope,
    valueExpression: ExpressionNode,
    statementNode: StatementNode): this(token, valueExpression, ReferenceExpression(token, scope, "it"), statementNode)

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return "$valueExpression -> $statementNode"
  }
}

