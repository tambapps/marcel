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

  override fun isEqualTo(other: CstNode): Boolean {

    if (other !is AccessCstNode) return false

    if (isStatic != other.isStatic) return false
    if (isInline != other.isInline) return false
    if (isFinal != other.isFinal) return false
    if (isExplicit != other.isExplicit) return false
    if (visibility != other.visibility) return false
    return true
  }
}