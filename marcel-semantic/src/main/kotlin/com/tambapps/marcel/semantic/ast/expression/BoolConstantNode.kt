package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.AstNodeVisitor

class BoolConstantNode(token: LexToken, val value: Boolean): AbstractExpressionNode(token) {

  override fun <T> accept(visitor: AstNodeVisitor<T>) = visitor.visit(this)

  override fun toString() = value.toString()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as BoolConstantNode

    if (value != other.value) return false

    return true
  }

  override fun hashCode() = value.hashCode()
}
