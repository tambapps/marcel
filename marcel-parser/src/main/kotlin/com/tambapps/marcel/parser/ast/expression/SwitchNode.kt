package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.statement.StatementNode

class SwitchNode(val expressionNode: ExpressionNode, val branches: List<SwitchBranchNode>): ExpressionNode {


  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>): T {
    return astNodeVisitor.visit(this)
  }
}

abstract class SwitchBranchNode(val statementNode: StatementNode): ExpressionNode {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>): T {
    return astNodeVisitor.visit(this)
  }
}

class EqSwitchBranchNode(val valueExpression: ExpressionNode, statementNode: StatementNode): SwitchBranchNode(statementNode) {


}

class ElseBranchNode(statementNode: StatementNode): SwitchBranchNode(statementNode) {


}
