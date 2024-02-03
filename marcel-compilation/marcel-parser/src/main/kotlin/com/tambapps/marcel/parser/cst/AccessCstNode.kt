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
  AbstractCstNode(parent, tokenStart, tokenEnd) {
}