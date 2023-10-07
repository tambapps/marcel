package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.AstNodeVisitor
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.JavaType

class FunctionCallNode(
  val javaMethod: JavaMethod,
  val castType: JavaType?,
  val arguments: List<ExpressionNode>,
  token: LexToken
) : AbstractExpressionNode(javaMethod.returnType, token) {
  override fun <T> accept(visitor: AstNodeVisitor<T>) = visitor.visit(this)


  override fun toString() = StringBuilder().apply {
    append(javaMethod.name)
    if (castType != null) {
      append("<$castType>")
    }
    append("(")
    arguments.joinTo(buffer = this, separator = ", ")
    append(")")
  }.toString()

}