package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType

class CstAccessNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val isStatic: Boolean,
  val isInline: Boolean,
  val isFinal: Boolean,
  val visibility: TokenType
) :
  AbstractCstNode(parent, tokenStart, tokenEnd) {
}