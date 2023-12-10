package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.expression.ExpressionNode

class AnnotationNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val typeNode: TypeNode,
  val attributes: List<Pair<String, ExpressionNode>>
) :
  AbstractCstNode(parent, tokenStart, tokenEnd) {
}