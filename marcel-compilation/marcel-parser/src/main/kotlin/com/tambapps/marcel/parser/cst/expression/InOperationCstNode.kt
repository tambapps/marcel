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
}