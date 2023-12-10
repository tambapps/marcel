package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode

class NotNode(val expression: ExpressionNode, parent: CstNode?, tokenStart: LexToken, tokenEnd: LexToken) :
  AbstractExpressionNode(parent, tokenStart, tokenEnd) {
  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

  override fun toString() = "!($expression)"
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is NotNode) return false
    if (!super.equals(other)) return false

    if (expression != other.expression) return false

    return true
  }

  override fun hashCode(): Int {
    return expression.hashCode()
  }

}