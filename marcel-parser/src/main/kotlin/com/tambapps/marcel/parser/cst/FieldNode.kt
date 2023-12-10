package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.expression.ExpressionNode

class FieldNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val access: AccessNode,
  val annotations: List<AnnotationNode>,
  val type: TypeNode,
  val name: String,
  val initialValue: ExpressionNode?
) :
  AbstractCstNode(parent, tokenStart, tokenEnd) {

}