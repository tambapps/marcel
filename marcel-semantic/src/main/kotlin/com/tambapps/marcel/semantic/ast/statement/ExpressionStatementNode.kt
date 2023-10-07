package com.tambapps.marcel.semantic.ast.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.AstNodeVisitor
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode

open class ExpressionStatementNode(
  val expressionNode: ExpressionNode,
  tokenStart: LexToken, tokenEnd: LexToken) : AbstractStatementNode(tokenStart, tokenEnd) {

  override fun <T> accept(visitor: AstNodeVisitor<T>) = visitor.visit(this)
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ExpressionStatementNode) return false

    if (expressionNode != other.expressionNode) return false

    return true
  }

  override fun hashCode(): Int {
    return expressionNode.hashCode()
  }
}