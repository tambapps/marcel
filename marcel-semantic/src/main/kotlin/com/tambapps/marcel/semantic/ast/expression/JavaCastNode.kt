package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.AstNodeVisitor
import com.tambapps.marcel.semantic.type.JavaType

/**
 * Java cast node
 */
class JavaCastNode(type: JavaType, val expressionNode: ExpressionNode, token: LexToken) : AbstractExpressionNode(type, token) {

  override fun <T> accept(visitor: AstNodeVisitor<T>) = visitor.visit(this)
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