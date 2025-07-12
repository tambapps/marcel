package com.tambapps.marcel.parser.cst.expression.reference

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.AbstractExpressionCstNode
import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor

class IncrCstNode(
  parent: CstNode?,
  override val value: String,
  val amount: Int,
  val returnValueBefore: Boolean,
  tokenStart: LexToken, tokenEnd: LexToken) : AbstractExpressionCstNode(parent, tokenStart, tokenEnd) {
  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

  override fun toString() = if (returnValueBefore) "$value++" else "++$value"
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is IncrCstNode) return false

    if (amount != other.amount) return false
    if (returnValueBefore != other.returnValueBefore) return false
    if (value != other.value) return false

    return true
  }

  override fun hashCode(): Int {
    var result = amount
    result = 31 * result + returnValueBefore.hashCode()
    result = 31 * result + value.hashCode()
    return result
  }


}