package com.tambapps.marcel.semantic.ast.expression.literal

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.AstNodeVisitor
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.type.JavaType

class NullValueNode(token: LexToken, type: JavaType?): AbstractExpressionNode(type ?: JavaType.Anything, token) {

  override var type = super.type

  override fun <T> accept(visitor: AstNodeVisitor<T>) = visitor.visit(this)

  constructor(token: LexToken): this(token, null)

  override fun equals(other: Any?): Boolean {
    return other is NullValueNode
  }

  override fun hashCode(): Int {
    return 0
  }

  override fun toString(): String {
    return if (type != null) "($type) null" else "null"
  }
}
