package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.AstNodeVisitor

class FloatConstantNode(token: LexToken, val value: Float): AbstractExpressionNode(token) {

  override fun <T> accept(visitor: AstNodeVisitor<T>) = visitor.visit(this)

  override fun toString(): String {
    return value.toString()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as FloatConstantNode

    if (value != other.value) return false

    return true
  }

  override fun hashCode(): Int {
    return value.hashCode()
  }
}
