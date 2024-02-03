package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.JavaType

open class FunctionCallNode constructor(
  val javaMethod: JavaMethod,
  override val owner: com.tambapps.marcel.semantic.ast.expression.ExpressionNode?,
  val arguments: List<com.tambapps.marcel.semantic.ast.expression.ExpressionNode>,
  tokenStart: LexToken,
  tokenEnd: LexToken
) : com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode(javaMethod.returnType, tokenStart, tokenEnd),
  com.tambapps.marcel.semantic.ast.expression.OwnableAstNode {

  override fun <T> accept(visitor: com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor<T>) = visitor.visit(this)

  override fun withOwner(owner: com.tambapps.marcel.semantic.ast.expression.ExpressionNode) =
    com.tambapps.marcel.semantic.ast.expression.FunctionCallNode(javaMethod, owner, arguments, tokenStart, tokenEnd)

  override fun toString() = StringBuilder().apply {
    if (javaMethod.isStatic) {
      append(javaMethod.ownerClass.simpleName)
      append(".")
    } else if (owner != null) {
      append(owner)
      append(".")
    }
    append(javaMethod.name)
    append("(")
    arguments.joinTo(buffer = this, separator = ", ")
    append(")")
  }.toString()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is com.tambapps.marcel.semantic.ast.expression.FunctionCallNode) return false

    if (javaMethod != other.javaMethod) return false
    if (owner != other.owner) return false
    if (arguments != other.arguments) return false

    return true
  }

  override fun hashCode(): Int {
    var result = javaMethod.hashCode()
    result = 31 * result + (owner?.hashCode() ?: 0)
    result = 31 * result + arguments.hashCode()
    return result
  }

}