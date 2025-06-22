package com.tambapps.marcel.semantic.ast.expression.literal

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.symbol.type.JavaType

class ShortConstantNode(token: LexToken, override val value: Short) :
  AbstractExpressionNode(JavaType.short, token), JavaConstantExpression {

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)

  override fun toString() = value.toString()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ShortConstantNode

    return value == other.value
  }

  override fun hashCode() = value.hashCode()
}
