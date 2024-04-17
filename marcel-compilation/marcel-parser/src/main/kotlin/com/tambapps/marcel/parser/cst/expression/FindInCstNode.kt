package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeCstNode

// TODO document me.
// TODO test me
class FindInCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val varType: TypeCstNode,
  val varName: String,
  // TODO make this optional and allow right shift operators on all in nodes
  val inExpr: ExpressionCstNode,
  val filterExpr: ExpressionCstNode,
) : AbstractExpressionCstNode(parent, tokenStart, tokenEnd) {

  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) =  TODO() //visitor.visit(this, arg)

}