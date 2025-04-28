package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode

class AnnotationCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val typeNode: TypeCstNode,
  val attributes: List<Pair<String, ExpressionCstNode>>,
  val identifierToken: LexToken? = null
) :
  AbstractCstNode(parent, tokenStart, tokenEnd) {
}