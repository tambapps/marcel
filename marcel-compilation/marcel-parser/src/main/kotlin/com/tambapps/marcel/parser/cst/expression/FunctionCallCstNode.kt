package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor

class FunctionCallCstNode(
  parent: CstNode?,
  override val value: String,
  val castType: TypeCstNode?,
  val positionalArgumentNodes: List<ExpressionCstNode>,
  val namedArgumentNodes: List<Pair<String, ExpressionCstNode>>,
  tokenStart: LexToken,
  tokenEnd: LexToken
) : AbstractExpressionCstNode(parent, tokenStart, tokenEnd) {

  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

  override fun toString() = StringBuilder().apply {
    append(value)
    if (castType != null) {
      append("<$castType>")
    }
    append("(")
    (positionalArgumentNodes + namedArgumentNodes.map { it.first + ": " + it.second })
      .joinTo(this, separator = ",")
    append(")")
  }.toString()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is FunctionCallCstNode) return false

    if (value != other.value) return false
    if (castType != other.castType) return false
    if (positionalArgumentNodes != other.positionalArgumentNodes) return false
    if (namedArgumentNodes != other.namedArgumentNodes) return false

    return true
  }

  override fun hashCode(): Int {
    var result = value.hashCode()
    result = 31 * result + (castType?.hashCode() ?: 0)
    result = 31 * result + positionalArgumentNodes.hashCode()
    result = 31 * result + namedArgumentNodes.hashCode()
    return result
  }


}