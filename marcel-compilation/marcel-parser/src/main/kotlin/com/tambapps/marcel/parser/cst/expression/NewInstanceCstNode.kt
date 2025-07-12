package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor

class NewInstanceCstNode(
  parent: CstNode?,
  val type: TypeCstNode,
  val positionalArgumentNodes: List<ExpressionCstNode>,
  val namedArgumentNodes: List<Pair<String, ExpressionCstNode>>,
  tokenStart: LexToken,
  tokenEnd: LexToken
) : AbstractExpressionCstNode(parent, tokenStart, tokenEnd) {

  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is NewInstanceCstNode) return false

    if (type != other.type) return false
    if (positionalArgumentNodes != other.positionalArgumentNodes) return false
    if (namedArgumentNodes != other.namedArgumentNodes) return false

    return true
  }

  override fun hashCode(): Int {
    var result = type.hashCode()
    result = 31 * result + positionalArgumentNodes.hashCode()
    result = 31 * result + namedArgumentNodes.hashCode()
    return result
  }

}