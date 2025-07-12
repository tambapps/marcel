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

  override fun isEqualTo(node: CstNode): Boolean {
    if (node !is AnnotationCstNode) return false

    if (!typeNode.isEqualTo(node.typeNode)) return false
    if (attributes.size != node.attributes.size) return false
    for (i in attributes.indices) {
      val (name1, value1) = attributes[i]
      val (name2, value2) = node.attributes[i]
      if (name1 != name2 || !value1.isEqualTo(value2)) return false
    }
    return true
  }
}