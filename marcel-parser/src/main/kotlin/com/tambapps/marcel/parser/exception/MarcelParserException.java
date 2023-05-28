package com.tambapps.marcel.parser.exception;

import com.tambapps.marcel.lexer.LexToken;

public class MarcelParserException extends RuntimeException {

  private final boolean eof;
  private final int line;
  private final int column;

  public MarcelParserException(LexToken token, String message) {
    this(token, message, false);
  }

  public MarcelParserException(LexToken token, String message, boolean eof) {
    super(String.format("Parser error at token %s (line %d, column %d): %s",
        token.getType(), token.getLine(), token.getColumn(), message));
    this.eof = eof;
    this.line = token.getLine();
    this.column = token.getColumn();
  }

  public MarcelParserException(String message, boolean eof) {
    this(LexToken.dummy(), message, eof);
  }

  public static MarcelParserException malformedNumber(LexToken token, NumberFormatException e) {
    return new MarcelParserException(token, "Malformed number (" + e.getMessage() + ")");
  }

  public boolean isEof() {
    return eof;
  }

  public int getLine() {
    return line;
  }

  public int getColumn() {
    return column;
  }
}
