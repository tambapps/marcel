package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.type.JavaType

class ThisReferenceNode(
  type: JavaType,
  // java have synthetic fields to references outer classes
  // this$0 for the outer class this$1 for the outer-outer class, and so on.
  // starting from outerLevel=1, this thisReference must reference outerclass.
  // meaning outerLevel=1 equals to this$0
  val outerLevel: Int,
  // in such case we have to provide the referenced outer class in the descriptor when writting asm instruction
  val descriptorType: JavaType,
  token: LexToken
) : AbstractExpressionNode(type, token) {

  constructor(type: JavaType, token: LexToken) : this(type, 0, type, token)

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