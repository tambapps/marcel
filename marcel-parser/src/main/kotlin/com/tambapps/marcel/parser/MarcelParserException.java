package com.tambapps.marcel.parser;

import com.tambapps.marcel.lexer.LexToken;
import lombok.Getter;
import lombok.Value;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class MarcelParserException extends RuntimeException {

  public static MarcelParserException.Error error(String message, boolean eof, LexToken token) {
    return new Error(message, eof, token);
  }

  @Value
  public static class Error {
    String message;
    boolean eof;
    LexToken token;
  }

  private final List<Error> errors;

  public MarcelParserException(LexToken token, String message) {
    this(token, message, false);
  }

  public MarcelParserException(LexToken token, String message, boolean eof) {
    this(new Error(message, eof, token));
  }

  private MarcelParserException(Error error) {
    this(Collections.singletonList(error));
  }

  public MarcelParserException(List<Error> errors) {
    super(generateErrorMessage(errors));
    this.errors = errors;
  }

  private static String generateErrorMessage(List<Error> errors) {
    if (errors.size() == 1) return generateErrorMessage(errors.get(0));
    return "Multiple parser errors were found:" + errors.stream()
            .map(e -> "\n  -" + generateErrorMessage(e))
            .collect(Collectors.joining());
  }

  private static String generateErrorMessage(Error error) {
    return String.format("Parser error at token %s (line %d, column %d): %s",
            error.token.getType(), error.token.getLine() + 1, error.token.getColumn(), error.message);
  }

  public static MarcelParserException.Error malformedNumber(NumberFormatException e, LexToken token, boolean eof) {
    return new MarcelParserException.Error("Malformed number (" + e.getMessage() + ")", eof, token);
  }

}