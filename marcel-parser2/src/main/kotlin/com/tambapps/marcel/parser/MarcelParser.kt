package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType

class MarcelParser constructor(tokens: List<LexToken>) {

  private val tokens = tokens.filter { it.type != TokenType.WHITE_SPACE }

}