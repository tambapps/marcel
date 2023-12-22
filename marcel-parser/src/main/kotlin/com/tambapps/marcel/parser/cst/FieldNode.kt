package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.expression.ExpressionNode

class FieldNode(
  val parentClassNode: ClassNode,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val access: AccessNode,
  val annotations: List<AnnotationNode>,
  val type: TypeNode,
  val name: String,
  val initialValue: ExpressionNode?
) :
  AbstractCstNode(parentClassNode, tokenStart, tokenEnd) {

  override fun toString(): String {
    return "$type $name"
  }
}