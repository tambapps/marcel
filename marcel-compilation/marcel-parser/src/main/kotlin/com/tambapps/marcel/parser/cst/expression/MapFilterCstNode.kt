package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor

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
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is MapFilterCstNode) return false
    if (!super.equals(other)) return false

    if (mapExpr != other.mapExpr) return false

    return true
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + (mapExpr?.hashCode() ?: 0)
    return result
  }


}