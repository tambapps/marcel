package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness

// must pass the actual superType
class SuperReferenceNode(override val type: JavaType, token: LexToken) :
  AbstractExpressionNode(token) {

  override val nullness: Nullness
    get() = Nullness.NOT_NULL

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is SuperReferenceNode) return false
    return true
  }

  override fun toString(): String {
    return "super"
  }

  override fun hashCode(): Int {
    return javaClass.hashCode()
  }
}