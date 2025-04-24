package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode

class FieldCstNode(
  val parentClassNode: ClassCstNode,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  var access: AccessCstNode,
  val annotations: List<AnnotationCstNode>,
  val type: TypeCstNode,
  val identifierToken: LexToken,
  var initialValue: ExpressionCstNode?
) :
  AbstractCstNode(parentClassNode, tokenStart, tokenEnd) {

  // var because names can be modified in CstTransformation
  var name: String = identifierToken.value

  override fun toString(): String {
    return "$type $name"
  }
}