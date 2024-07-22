package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor

class AnyInCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  varType: TypeCstNode,
  varName: String,
  inExpr: ExpressionCstNode?,
  filterExpr: ExpressionCstNode,
  val negate: Boolean,
  ) : InOperationCstNode(parent, tokenStart, tokenEnd, varType, varName, inExpr, filterExpr) {

  override val filterExpr: ExpressionCstNode get() = super.filterExpr!!
  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

}