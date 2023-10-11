package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.expression.CstExpressionNode

class MethodParameterCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val name: String,
  val type: TypeCstNode,
  val defaultValue: CstExpressionNode?,
  val annotations: List<AnnotationCstNode>,
  val thisParameter: Boolean
  ) :
  AbstractCstNode(parent, tokenStart, tokenEnd) {
}