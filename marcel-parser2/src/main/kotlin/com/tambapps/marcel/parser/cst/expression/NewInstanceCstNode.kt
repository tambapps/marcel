package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeCstNode

class NewInstanceCstNode(
  parent: CstNode?,
  val type: TypeCstNode,
  val positionalArgumentNodes: List<CstExpressionNode>,
  val namedArgumentNodes: List<Pair<String, CstExpressionNode>>,
  tokenStart: LexToken,
  tokenEnd: LexToken
) : AbstractExpressionCstNode(parent, tokenStart, tokenEnd) {

  override fun <T> accept(visitor: ExpressionCstNodeVisitor<T>) = visitor.visit(this)

}