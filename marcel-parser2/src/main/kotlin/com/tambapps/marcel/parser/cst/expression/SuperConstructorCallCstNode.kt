package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode

class SuperConstructorCallCstNode(
  parent: CstNode?,
  val arguments: List<ExpressionCstNode>,
  tokenStart: LexToken,
  tokenEnd: LexToken
) : AbstractExpressionCstNode(parent, tokenStart, tokenEnd) {

  override fun <T> accept(visitor: ExpressionCstNodeVisitor<T>) = visitor.visit(this)

  override fun toString() = StringBuilder().apply {
    append("super(")
    arguments.joinTo(buffer = this, separator = ", ")
    append(")")
  }.toString()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is SuperConstructorCallCstNode) return false
    if (!super.equals(other)) return false

    if (arguments != other.arguments) return false

    return true
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + arguments.hashCode()
    return result
  }


}