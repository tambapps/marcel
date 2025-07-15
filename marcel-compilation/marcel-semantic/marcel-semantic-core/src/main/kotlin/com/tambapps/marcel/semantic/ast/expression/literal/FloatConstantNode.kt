package com.tambapps.marcel.semantic.ast.expression.literal

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.visitor.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.symbol.type.JavaType

class FloatConstantNode(token: LexToken, override val value: Float) :
  AbstractExpressionNode(token), JavaConstantExpression {

  override val type = JavaType.float
  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)

  override fun toString(): String {
    return value.toString()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as FloatConstantNode

    return value == other.value
  }

  override fun hashCode(): Int {
    return value.hashCode()
  }
}
