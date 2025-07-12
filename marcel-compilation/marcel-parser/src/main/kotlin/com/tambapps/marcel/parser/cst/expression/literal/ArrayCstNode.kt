package com.tambapps.marcel.parser.cst.expression.literal

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.AbstractExpressionCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor

class ArrayCstNode(
  val elements: List<ExpressionCstNode>,
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken
) : AbstractExpressionCstNode(parent, tokenStart, tokenEnd) {
  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ArrayCstNode) return false

    if (elements != other.elements) return false

    return true
  }

  override fun hashCode(): Int {
    return elements.hashCode()
  }

  override fun toString() = elements.joinToString(", ", prefix = "[", postfix = "]")
}