package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.Token

class MarcelParser {
  private var index = 0


  fun parse(tokens: List<Token>): List<Token> {
    return listOf(Token(TokenType.IDENTIFIER, "println"),
      Token(TokenType.QUOTE, null),
      Token(TokenType.TEXT, "Hello World"),
      Token(TokenType.QUOTE, null)
    )
  }
}