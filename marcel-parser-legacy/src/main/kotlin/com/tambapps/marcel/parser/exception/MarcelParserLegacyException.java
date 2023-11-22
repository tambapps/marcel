package com.tambapps.marcel.parser.exception;

import com.tambapps.marcel.lexer.LexToken;

public class MarcelParserLegacyException extends RuntimeException {

  private final boolean eof;
  private final int line;
  private final int column;

  public MarcelParserLegacyException(LexToken token, String message) {
    this(token, message, false);
  }

  public MarcelParserLegacyException(LexToken token, String message, boolean eof) {
    super(String.format("Parser error at token %s (line %d, column %d): %s",
        token.getType(), token.getLine() + 1, token.getColumn(), message));
    this.eof = eof;
    this.line = token.getLine() + 1;
    this.column = token.getColumn();
  }

  public MarcelParserLegacyException(String message, boolean eof) {
    this(LexToken.dummy(), message, eof);
  }

  public static MarcelParserLegacyException malformedNumber(LexToken token, NumberFormatException e) {
    return new MarcelParserLegacyException(token, "Malformed number (" + e.getMessage() + ")");
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
