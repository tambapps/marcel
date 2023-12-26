package com.tambapps.marcel.lexer


class MarcelLexerException(val line: Int, val column: Int, message: String?) :
  RuntimeException(String.format("Lexer error at line %d, column %d: %s", line, column, message))
