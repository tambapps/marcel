package com.tambapps.marcel.semantic.ast.expression.literal

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.type.JavaType

class NullValueNode(token: LexToken, type: JavaType?): com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode(type ?: JavaType.Anything, token), JavaConstantExpression {

  override val value = null
  override fun <T> accept(visitor: com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor<T>) = visitor.visit(this)

  override var type = super.type

  constructor(token: LexToken): this(token, null)

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
