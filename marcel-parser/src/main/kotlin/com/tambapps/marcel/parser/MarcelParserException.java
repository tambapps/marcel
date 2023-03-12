package com.tambapps.marcel.parser;

import com.tambapps.marcel.lexer.LexToken;

public class MarcelParserException extends RuntimeException {

  public final boolean eof;
  public MarcelParserException(LexToken token, String message) {
    this(token, message, false);
  }

  public MarcelParserException(LexToken token, String message, boolean eof) {
    this(String.format("Parser error at token %s (line %d, column %d): %s",
        token.getType(), token.getLine(), token.getColumn(), message), eof);
  }

  public MarcelParserException(String message, boolean eof) {
    super(message);
    this.eof = eof;
  }

  public static MarcelParserException malformedNumber(LexToken token, NumberFormatException e) {
    return new MarcelParserException(token, "Malformed number (" + e.getMessage() + ")");
  }
}
