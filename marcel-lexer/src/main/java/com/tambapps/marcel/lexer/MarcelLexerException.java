package com.tambapps.marcel.lexer;

import lombok.Getter;

@Getter
public class MarcelLexerException extends RuntimeException {

  private final int line;
  private final int column;
  public MarcelLexerException(int line, int column, String message) {
    super(String.format("Lexer error at line %d, column %d: %s", line, column, message));
    this.line = line;
    this.column = column;
  }
}
