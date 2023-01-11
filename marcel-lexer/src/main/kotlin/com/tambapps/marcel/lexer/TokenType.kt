package com.tambapps.marcel.lexer

enum class TokenType(val string: String?) {
  IDENTIFIER,
  TEXT,
  QUOTE("\"");

  constructor() : this(null)


}