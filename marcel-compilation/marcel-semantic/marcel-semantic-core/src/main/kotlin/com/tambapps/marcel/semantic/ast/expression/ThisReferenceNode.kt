package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.symbol.type.JavaType

class ThisReferenceNode(
  type: JavaType,
  token: LexToken
) : AbstractExpressionNode(type, token) {

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ThisReferenceNode) return false
    return true
  }

  override fun toString(): String {
    return "this"
  }

  override fun hashCode(): Int {
    return javaClass.hashCode()
  }
}