package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.scope.Scope

class SwitchNode constructor(override var scope: Scope,
                             val expressionNode: ExpressionNode, val branches: MutableList<SwitchBranchNode>,
                             val elseStatement: StatementNode?): ExpressionNode, ScopedNode<Scope> {


  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>): T {
    return astNodeVisitor.visit(this)
  }

  override fun toString(): String {
    var branchesString = branches.joinToString(separator = "\n")
    if (elseStatement != null) branchesString += "else -> $elseStatement"
    return "switch($expressionNode) {\n$branchesString\n}"
  }

}
class SwitchBranchNode(val valueExpression: ExpressionNode, var statementNode: StatementNode): ExpressionNode {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>): T {
    return astNodeVisitor.visit(this)
  }

  override fun toString(): String {
    return "$valueExpression -> $statementNode"
  }
}
