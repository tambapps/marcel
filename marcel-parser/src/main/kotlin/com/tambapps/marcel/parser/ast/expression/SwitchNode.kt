package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.ast.statement.BlockStatement
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.scope.MethodScope
import com.tambapps.marcel.parser.scope.Scope

class SwitchNode constructor(override var scope: Scope,
                 val expressionNode: ExpressionNode, val branches: MutableList<SwitchBranchNode>): ExpressionNode, ScopedNode<Scope> {


  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>): T {
    return astNodeVisitor.visit(this)
  }

  override fun toString(): String {
    return "switch($expressionNode) " + branches.joinToString(separator = "\n", prefix = "{\n", postfix = "\n}")
  }

  override fun trySetScope(scope: Scope) {
    super.trySetScope(scope)
    branches.forEach { it.trySetTreeScope(scope) }
  }
}

sealed class SwitchBranchNode(var statementNode: StatementNode): ExpressionNode {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>): T {
    return astNodeVisitor.visit(this)
  }

  fun returningLastStatement(methodScope: MethodScope) {
    if (statementNode is BlockStatement) {
      val blockStatement = statementNode as BlockStatement
      val lastStatement = blockStatement.block.statements.lastOrNull()
      if (lastStatement == null) blockStatement.block.addStatement(ReturnNode(methodScope, NullValueNode()))
      else if (lastStatement is ExpressionStatementNode) blockStatement.block.statements.set(
        blockStatement.block.statements.size - 1, ReturnNode(methodScope, lastStatement.expression)
      ) else blockStatement.block.addStatement(ReturnNode(methodScope, NullValueNode()))
    } else if (statementNode is ExpressionStatementNode) {
      statementNode = ReturnNode(methodScope, (statementNode as ExpressionStatementNode).expression)
    } else {
      statementNode = BlockStatement(
        BlockNode(
          // scope don't really matters here because it will be overriden in lambda
          methodScope,
          mutableListOf(
            statementNode, ReturnNode(methodScope, NullValueNode())
          )
      )
      )
    }
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
