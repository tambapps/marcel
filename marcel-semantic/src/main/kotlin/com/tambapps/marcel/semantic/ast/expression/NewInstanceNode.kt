package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.AstNodeVisitor
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.JavaType


class NewInstanceNode(
  type: JavaType,
  val javaMethod: JavaMethod,
  val arguments: List<ExpressionNode>,
  token: LexToken
) : AbstractExpressionNode(type, token) {

  override fun <T> accept(visitor: AstNodeVisitor<T>) = visitor.visit(this)

}