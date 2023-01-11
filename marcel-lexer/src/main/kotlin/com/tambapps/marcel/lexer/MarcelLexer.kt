package com.tambapps.marcel.lexer

// TODO use flex
// https://jflex.de/manual.html#Example
class MarcelLexer {
  private var index = 0


  fun lex(content: String): List<Token> {
    return listOf(Token(TokenType.IDENTIFIER, "println"),
      Token(TokenType.QUOTE, null),
      Token(TokenType.TEXT, "Hello World"),
      Token(TokenType.QUOTE, null)
    )
  }
}