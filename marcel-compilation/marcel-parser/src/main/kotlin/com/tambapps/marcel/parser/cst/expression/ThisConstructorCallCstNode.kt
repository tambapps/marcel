package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor

class ThisConstructorCallCstNode(
  parent: CstNode?,
  val positionalArgumentNodes: List<ExpressionCstNode>,
  val namedArgumentNodes: List<Pair<String, ExpressionCstNode>>,
  tokenStart: LexToken,
  tokenEnd: LexToken
) : AbstractExpressionCstNode(parent, tokenStart, tokenEnd) {

  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

  override fun toString() = StringBuilder().apply {
    append("this(")
    (positionalArgumentNodes + namedArgumentNodes.map { it.first + ": " + it.second })
      .joinTo(this, separator = ",")
    append(")")
  }.toString()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ThisConstructorCallCstNode) return false

    if (positionalArgumentNodes != other.positionalArgumentNodes) return false
    if (namedArgumentNodes != other.namedArgumentNodes) return false

    return true
  }

  override fun hashCode(): Int {
    var result = positionalArgumentNodes.hashCode()
    result = 31 * result + namedArgumentNodes.hashCode()
    return result
  }


}