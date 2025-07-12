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


}