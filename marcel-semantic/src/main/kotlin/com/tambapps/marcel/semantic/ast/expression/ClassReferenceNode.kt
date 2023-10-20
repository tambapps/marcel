package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.extensions.javaType

class ClassReferenceNode(val name: String, token: LexToken) : AbstractExpressionNode(Class::class.javaType, token) {

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) = visitor.visit(this)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ClassReferenceNode) return false

    if (name != other.name) return false

    return true
  }

  override fun hashCode(): Int {
    return name.hashCode()
  }

  override fun toString(): String {
    return "$name.class"
  }

}