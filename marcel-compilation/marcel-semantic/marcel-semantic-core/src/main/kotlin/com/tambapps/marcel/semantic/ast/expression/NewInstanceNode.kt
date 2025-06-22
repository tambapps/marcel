package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.symbol.method.MarcelMethod
import com.tambapps.marcel.semantic.symbol.type.JavaType

open class NewInstanceNode(
  type: JavaType,
  val javaMethod: MarcelMethod,
  open val arguments: List<ExpressionNode>,
  token: LexToken
) : AbstractExpressionNode(type, token) {

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)

}