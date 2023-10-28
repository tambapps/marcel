package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode

class AnnotationCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val typeCstNode: TypeCstNode,
  val attributes: List<Pair<String, ExpressionCstNode>>
) :
  AbstractCstNode(parent, tokenStart, tokenEnd) {
}