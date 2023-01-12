package com.tambapps.marcel.lexer

// TODO use flex
// https://jflex.de/manual.html#Example
class MarcelLexer {
  private var index = 0


  fun lex(content: String): List<LexToken> {
    return listOf(LexToken(TokenTypes.IDENTIFIER, "println"),
      LexToken(TokenTypes.QUOTE, null),
      LexToken(TokenTypes.INTEGER, "1"),
      LexToken(TokenTypes.QUOTE, null)
    )
  }
}