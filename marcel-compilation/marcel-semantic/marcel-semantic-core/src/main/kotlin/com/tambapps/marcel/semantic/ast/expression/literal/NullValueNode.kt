package com.tambapps.marcel.semantic.ast.expression.literal

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness

class NullValueNode(token: LexToken, type: JavaType?) :
  AbstractExpressionNode(token),
  JavaConstantExpression {

    override val nullness: Nullness
    get() = Nullness.NULLABLE

  override val value = null
  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)

  override var type = type ?: JavaType.Anything

  constructor(token: LexToken) : this(token, null)

  override fun equals(other: Any?): Boolean {
    return other is NullValueNode
  }

  override fun hashCode(): Int {
    return 0
  }

  override fun toString(): String {
    return if (type != JavaType.Object) "($type) null" else "null"
  }
}
