package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.JavaType

class SuperConstructorCallNode(
  val classType: JavaType,
  val javaMethod: JavaMethod,
  val arguments: List<com.tambapps.marcel.semantic.ast.expression.ExpressionNode>,
  tokenStart: LexToken, tokenEnd: LexToken): com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode(JavaType.void, tokenStart, tokenEnd) {

  override fun <T> accept(visitor: com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor<T>) = visitor.visit(this)

  override fun toString() = StringBuilder().apply {
    append("super(")
    arguments.joinTo(buffer = this, separator = ", ")
    append(")")
  }.toString()
}