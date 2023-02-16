package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.ast.statement.BlockStatement
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.scope.MethodScope
import com.tambapps.marcel.parser.scope.Scope

class WhenNode constructor(override var scope: Scope, val branches: MutableList<WhenBranchNode>,
                           val elseStatement: StatementNode?): ExpressionNode, ScopedNode<Scope> {


  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>): T {
    return astNodeVisitor.visit(this)
  }


  override fun toString(): String {
    var branchesString = branches.joinToString(separator = "\n")
    if (elseStatement != null) branchesString += "else -> $elseStatement"
    return "when {\n$branchesString\n}"
  }
}

class WhenBranchNode(
   val conditionExpressionNode: ExpressionNode,
   var statementNode: StatementNode): ExpressionNode {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>): T {
    return astNodeVisitor.visit(this)
  }


  override fun toString(): String {
    return "$conditionExpressionNode -> $statementNode"
  }
}

