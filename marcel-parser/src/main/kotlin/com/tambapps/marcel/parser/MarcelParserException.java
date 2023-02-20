package com.tambapps.marcel.parser;

import com.tambapps.marcel.lexer.LexToken;

public class MarcelParserException extends RuntimeException {
  public MarcelParserException(LexToken token, String message) {
    super(String.format("Parser error at token %s (line %d, column %d): %s",
        token.getType(), token.getLine(), token.getColumn(), message));
  }

  public static MarcelParserException malformedNumber(LexToken token, NumberFormatException e) {
    return new MarcelParserException(token, "Malformed number (" + e.getMessage() + ")");
  }
}
