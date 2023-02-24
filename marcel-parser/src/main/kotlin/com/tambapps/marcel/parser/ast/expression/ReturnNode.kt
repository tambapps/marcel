package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.scope.MethodScope
import com.tambapps.marcel.parser.scope.Scope

class ReturnNode(token: LexToken, override var scope: MethodScope, expression: ExpressionNode) : StatementNode,
  ScopedNode<MethodScope>, ExpressionStatementNode(token, expression) {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ReturnNode) return false

    if (expression != other.expression) return false

    return true
  }

  override fun hashCode(): Int {
    return expression.hashCode()
  }

  override fun toString(): String {
    return "return $expression"
  }

}