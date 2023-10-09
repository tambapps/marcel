package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.expression.CstExpressionNode

class AnnotationCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val typeCstNode: TypeCstNode,
  val attributes: List<Pair<String, CstExpressionNode>>
) :
  AbstractCstNode(parent, tokenStart, tokenEnd) {
}