package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType

class CstAccessNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  var isStatic: Boolean,
  val isInline: Boolean,
  val isFinal: Boolean,
  val visibility: TokenType,
  // whether some access were explicitly written or we're just
  // using the default values because nothing was specified
  val isExplicit: Boolean,
) :
  AbstractCstNode(parent, tokenStart, tokenEnd) {
}