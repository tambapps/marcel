package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor

class ElvisThrowCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val expression: ExpressionCstNode,
  val throwableException: ExpressionCstNode
) :
  AbstractExpressionCstNode(parent, tokenStart, tokenEnd) {

  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ElvisThrowCstNode) return false

    if (expression != other.expression) return false
    if (throwableException != other.throwableException) return false

    return true
  }

  override fun hashCode(): Int {
    var result = expression.hashCode()
    result = 31 * result + throwableException.hashCode()
    return result
  }

  override fun toString() = "$expression ?: $throwableException"
}