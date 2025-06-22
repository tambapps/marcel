package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.symbol.method.MarcelMethod
import com.tambapps.marcel.semantic.symbol.type.JavaType

class SuperConstructorCallNode(
  val classType: JavaType,
  val javaMethod: MarcelMethod,
  val arguments: List<ExpressionNode>,
  tokenStart: LexToken, tokenEnd: LexToken
) : AbstractExpressionNode(JavaType.void, tokenStart, tokenEnd) {

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)

  override fun toString() = StringBuilder().apply {
    append("super(")
    arguments.joinTo(buffer = this, separator = ", ")
    append(")")
  }.toString()
}