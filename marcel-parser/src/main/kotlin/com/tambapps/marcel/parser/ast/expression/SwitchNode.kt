package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.scope.Scope

class SwitchNode constructor(override var scope: Scope,
                 val expressionNode: ExpressionNode, val branches: List<SwitchBranchNode>): ExpressionNode, ScopedNode<Scope> {


  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>): T {
    return astNodeVisitor.visit(this)
  }

  override fun toString(): String {
    return "switch($expressionNode) {\n" + branches.joinToString(separator = "\n", prefix = "{", postfix = "}")
  }
}

sealed class SwitchBranchNode(val statementNode: StatementNode): ExpressionNode {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>): T {
    return astNodeVisitor.visit(this)
  }
}

class EqSwitchBranchNode(val valueExpression: ExpressionNode, statementNode: StatementNode): SwitchBranchNode(statementNode) {


  override fun toString(): String {
    return "$valueExpression -> $statementNode"
  }
}

class ElseBranchNode(statementNode: StatementNode): SwitchBranchNode(statementNode) {


  override fun toString(): String {
    return "else -> $statementNode"
  }
}
