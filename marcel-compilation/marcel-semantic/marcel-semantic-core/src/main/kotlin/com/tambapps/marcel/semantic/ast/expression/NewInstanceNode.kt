package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.JavaType

open class NewInstanceNode constructor(
  type: JavaType,
  val javaMethod: JavaMethod,
  open val arguments: List<com.tambapps.marcel.semantic.ast.expression.ExpressionNode>,
  token: LexToken
) : com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode(type, token) {

  override fun <T> accept(visitor: com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor<T>) = visitor.visit(this)

}