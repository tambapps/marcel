package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness

/**
 * Java cast node
 */
class JavaCastNode(
  type: JavaType,
  val expressionNode: ExpressionNode,
  token: LexToken
) : AbstractExpressionNode(type, token) {
  override val nullness: Nullness
    get() = expressionNode.nullness

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is JavaCastNode) return false

    if (expressionNode != other.expressionNode) return false

    return true
  }

  override fun hashCode(): Int {
    return expressionNode.hashCode()
  }

  override fun toString(): String {
    return "($type) $expressionNode"
  }
}