package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeCstNode

abstract class InOperationCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val varType: TypeCstNode,
  val varName: String,
  var inExpr: ExpressionCstNode?,
  open val filterExpr: ExpressionCstNode?,
) : AbstractExpressionCstNode(parent, tokenStart, tokenEnd) {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is InOperationCstNode) return false

    if (varType != other.varType) return false
    if (varName != other.varName) return false
    if (inExpr != other.inExpr) return false
    if (filterExpr != other.filterExpr) return false

    return true
  }

  override fun hashCode(): Int {
    var result = varType.hashCode()
    result = 31 * result + varName.hashCode()
    result = 31 * result + (inExpr?.hashCode() ?: 0)
    result = 31 * result + (filterExpr?.hashCode() ?: 0)
    return result
  }
}