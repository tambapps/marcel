package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode

class FieldCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val access: CstAccessNode,
  val annotations: List<AnnotationCstNode>,
  val type: TypeCstNode,
  val name: String,
  val initialValue: ExpressionCstNode?
) :
  AbstractCstNode(parent, tokenStart, tokenEnd) {

}