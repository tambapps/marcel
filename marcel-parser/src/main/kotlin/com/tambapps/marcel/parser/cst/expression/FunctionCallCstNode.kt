package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeCstNode

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
    positionalArgumentNodes.joinTo(buffer = this, separator = ", ")
    namedArgumentNodes.joinTo(buffer = this, separator = ", ", transform = { it.first + ": " + it.second })
    append(")")
  }.toString()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is FunctionCallCstNode) return false
    if (!super.equals(other)) return false

    if (value != other.value) return false
    if (castType != other.castType) return false
    if (positionalArgumentNodes != other.positionalArgumentNodes) return false
    if (namedArgumentNodes != other.namedArgumentNodes) return false

    return true
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + value.hashCode()
    if (castType != null) result = 31 * result + castType.hashCode()
    result = 31 * result + positionalArgumentNodes.hashCode()
    result = 31 * result + namedArgumentNodes.hashCode()
    return result
  }


}