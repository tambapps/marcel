package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.AstNodeVisitor
import com.tambapps.marcel.semantic.type.JavaType

class NullValueNode(token: LexToken, var type: JavaType?): AbstractExpressionNode(token) {

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
