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
  AbstractCstNode(parent, tokenStart, tokenEnd), IdentifiableCstNode {

  override fun isSyntaxEqualTo(other: CstNode): Boolean {
    if (other !is AnnotationCstNode) return false

    if (!typeNode.isSyntaxEqualTo(other.typeNode)) return false
    if (attributes.size != other.attributes.size) return false
    for (i in attributes.indices) {
      val (name1, value1) = attributes[i]
      val (name2, value2) = other.attributes[i]
      if (name1 != name2 || !value1.isSyntaxEqualTo(value2)) return false
    }
    return true
  }
}