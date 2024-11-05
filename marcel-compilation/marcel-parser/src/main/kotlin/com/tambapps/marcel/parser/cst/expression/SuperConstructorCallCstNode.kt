package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor

class SuperConstructorCallCstNode(
  parent: CstNode?,
  val positionalArgumentNodes: List<ExpressionCstNode>,
  val namedArgumentNodes: List<Pair<String, ExpressionCstNode>>,
  tokenStart: LexToken,
  tokenEnd: LexToken
) : AbstractExpressionCstNode(parent, tokenStart, tokenEnd) {

  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

  override fun toString() = StringBuilder().apply {
    append("super(")
    (positionalArgumentNodes + namedArgumentNodes.map { it.first + ": " + it.second })
      .joinTo(this, separator = ",")
    append(")")
  }.toString()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is SuperConstructorCallCstNode) return false
    if (!super.equals(other)) return false

    if (positionalArgumentNodes != other.positionalArgumentNodes) return false

    return true
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + positionalArgumentNodes.hashCode()
    return result
  }


}