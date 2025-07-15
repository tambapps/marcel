package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.visitor.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.symbol.method.MarcelMethod
import com.tambapps.marcel.semantic.symbol.type.Nullness

open class FunctionCallNode(
  val javaMethod: MarcelMethod,
  override val owner: ExpressionNode?,
  val arguments: List<ExpressionNode>,
  tokenStart: LexToken,
  tokenEnd: LexToken
) : AbstractExpressionNode(tokenStart, tokenEnd),
  OwnableAstNode {

  override val type = javaMethod.returnType

  override val nullness: Nullness
    get() = javaMethod.nullness
  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)

  override fun withOwner(owner: ExpressionNode) =
    FunctionCallNode(javaMethod, owner, arguments, tokenStart, tokenEnd)

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
    if (other !is FunctionCallNode) return false

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