package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeNode

class NewInstanceNode(
  parent: CstNode?,
  val type: TypeNode,
  val positionalArgumentNodes: List<ExpressionNode>,
  val namedArgumentNodes: List<Pair<String, ExpressionNode>>,
  tokenStart: LexToken,
  tokenEnd: LexToken
) : AbstractExpressionNode(parent, tokenStart, tokenEnd) {

  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

}