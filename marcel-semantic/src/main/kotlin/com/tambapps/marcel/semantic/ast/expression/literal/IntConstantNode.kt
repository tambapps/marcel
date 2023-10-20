package com.tambapps.marcel.semantic.ast.expression.literal

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.type.JavaType

class IntConstantNode(token: LexToken, val value: Int): AbstractExpressionNode(JavaType.int, token) {

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) = visitor.visit(this)

  override fun toString(): String {
    return value.toString()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as IntConstantNode

    if (value != other.value) return false

    return true
  }

  override fun hashCode(): Int {
    return value
  }
}
