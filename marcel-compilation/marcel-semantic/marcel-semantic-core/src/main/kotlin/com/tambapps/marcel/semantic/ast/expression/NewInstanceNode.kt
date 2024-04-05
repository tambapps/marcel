package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.JavaType

open class NewInstanceNode(
  type: JavaType,
  val javaMethod: JavaMethod,
  open val arguments: List<ExpressionNode>,
  token: LexToken
) : AbstractExpressionNode(type, token) {

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)

}