package com.tambapps.marcel.semantic.ast.expression.literal

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.type.JavaType

class BoolConstantNode(token: LexToken, override val value: Boolean) :
  AbstractExpressionNode(JavaType.boolean, token), JavaConstantExpression {

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)

  override fun toString() = value.toString()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as BoolConstantNode

    return value == other.value
  }

  override fun hashCode() = value.hashCode()
}
