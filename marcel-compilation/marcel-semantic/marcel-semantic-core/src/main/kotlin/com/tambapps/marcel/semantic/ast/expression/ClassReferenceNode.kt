package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.expression.literal.JavaConstantExpression
import com.tambapps.marcel.semantic.type.JavaType

class ClassReferenceNode(val classType: JavaType, token: LexToken) :
  AbstractExpressionNode(JavaType.Clazz, token), JavaConstantExpression {

  override val value = classType

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ClassReferenceNode) return false

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