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
    return "when " + branches.joinToString(separator = "\n", prefix = "{\n", postfix = "\n}")
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

