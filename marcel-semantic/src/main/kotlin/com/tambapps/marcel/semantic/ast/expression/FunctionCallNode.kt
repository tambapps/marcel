package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.AstNodeVisitor
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.JavaType

open class FunctionCallNode(
  val javaMethod: JavaMethod,
  val owner: ExpressionNode?,
  val castType: JavaType?,
  val arguments: List<ExpressionNode>,
  token: LexToken
) : AbstractExpressionNode(javaMethod.returnType, token) {
  override fun <T> accept(visitor: AstNodeVisitor<T>) = visitor.visit(this)


  override fun toString() = StringBuilder().apply {
    if (javaMethod.isStatic) {
      append(javaMethod.ownerClass.simpleName)
      append(".")
    } else if (owner != null) {
      append(owner)
      append(".")
    }
    append(javaMethod.name)
    if (castType != null) {
      append("<$castType>")
    }
    append("(")
    arguments.joinTo(buffer = this, separator = ", ")
    append(")")
  }.toString()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is FunctionCallNode) return false

    if (javaMethod != other.javaMethod) return false
    if (owner != other.owner) return false
    if (castType != other.castType) return false
    if (arguments != other.arguments) return false

    return true
  }

  override fun hashCode(): Int {
    var result = javaMethod.hashCode()
    result = 31 * result + (owner?.hashCode() ?: 0)
    result = 31 * result + (castType?.hashCode() ?: 0)
    result = 31 * result + arguments.hashCode()
    return result
  }

}