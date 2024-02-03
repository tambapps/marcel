package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.type.JavaType

class ClassReferenceNode(val classType: JavaType, token: LexToken) : com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode(JavaType.Clazz, token) {

  override fun <T> accept(visitor: com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor<T>) = visitor.visit(this)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is com.tambapps.marcel.semantic.ast.expression.ClassReferenceNode) return false

    if (classType != other.classType) return false

    return true
  }

  override fun hashCode(): Int {
    return classType.hashCode()
  }

  override fun toString(): String {
    return "$classType.class"
  }

}