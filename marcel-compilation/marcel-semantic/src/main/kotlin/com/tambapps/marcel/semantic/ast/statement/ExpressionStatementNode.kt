package com.tambapps.marcel.semantic.ast.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode

/**
 * Expression as a statement
 */
open class ExpressionStatementNode(
  val expressionNode: ExpressionNode,
  tokenStart: LexToken, tokenEnd: LexToken) : AbstractStatementNode(tokenStart, tokenEnd) {

    constructor(expressionNode: ExpressionNode): this(expressionNode, expressionNode.tokenStart, expressionNode.tokenEnd)

  override fun <T> accept(visitor: StatementNodeVisitor<T>) = visitor.visit(this)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ExpressionStatementNode) return false

    if (expressionNode != other.expressionNode) return false

    return true
  }

  override fun hashCode(): Int {
    return expressionNode.hashCode()
  }

  override fun toString() = "$expressionNode;"
}