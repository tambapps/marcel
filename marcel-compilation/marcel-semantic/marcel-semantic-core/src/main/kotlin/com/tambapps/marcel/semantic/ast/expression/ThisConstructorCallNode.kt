package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.method.MarcelMethod
import com.tambapps.marcel.semantic.type.JavaType

class ThisConstructorCallNode(
  val classType: JavaType,
  val javaMethod: MarcelMethod,
  val arguments: List<ExpressionNode>,
  tokenStart: LexToken, tokenEnd: LexToken
) : AbstractExpressionNode(JavaType.void, tokenStart, tokenEnd) {

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)

}