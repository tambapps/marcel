package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode

class FieldCstNode(
  val parentClassNode: ClassCstNode,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val access: AccessCstNode,
  val annotations: List<AnnotationCstNode>,
  val type: TypeCstNode,
  var name: String,
  val initialValue: ExpressionCstNode?
) :
  AbstractCstNode(parentClassNode, tokenStart, tokenEnd) {

  override fun toString(): String {
    return "$type $name"
  }
}