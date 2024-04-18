package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeCstNode

class MapFilterCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  varType: TypeCstNode,
  varName: String,
  inExpr: ExpressionCstNode?,
  val mapExpr: ExpressionCstNode?,
  filterExpr: ExpressionCstNode?,
) : InOperationCstNode(parent, tokenStart, tokenEnd, varType, varName, inExpr, filterExpr) {

  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

}