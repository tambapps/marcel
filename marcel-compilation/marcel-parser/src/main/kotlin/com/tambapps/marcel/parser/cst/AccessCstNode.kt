package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType

class AccessCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  var isStatic: Boolean,
  val isInline: Boolean,
  val isFinal: Boolean,
  var visibility: TokenType,
  // whether some access were explicitly written or we're just
  // using the default values because nothing was specified
  // it is only useful while parsing
  val isExplicit: Boolean,
) :
  AbstractCstNode(parent, tokenStart, tokenEnd), IdentifiableCstNode {

  override fun isEqualTo(node: CstNode): Boolean {

    if (node !is AccessCstNode) return false

    if (isStatic != node.isStatic) return false
    if (isInline != node.isInline) return false
    if (isFinal != node.isFinal) return false
    if (isExplicit != node.isExplicit) return false
    if (visibility != node.visibility) return false
    return true
  }
}