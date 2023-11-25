package com.tambapps.marcel.semantic.exception;

import com.tambapps.marcel.lexer.LexToken;
import com.tambapps.marcel.lexer.TokenType;
import com.tambapps.marcel.parser.cst.CstNode;
import lombok.Getter;
import lombok.Value;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MarcelSemanticException extends RuntimeException {

  public static MarcelSemanticException.Error error(String message, boolean eof, LexToken token) {
    return new Error(message, eof, token);
  }

  @Value
  public static class Error {
    String message;
    boolean eof;
    LexToken token;
  }

  @Getter
  private final List<Error> errors;

  // TODO should remove this constructor
  public MarcelSemanticException(String message) {
    this(new LexToken(0, 0, 0, 0, TokenType.END_OF_FILE, null), message, false);
  }
  public MarcelSemanticException(CstNode node, String message) {
    this(node.getToken(), message);
  }

  public MarcelSemanticException(LexToken token, String message) {
    this(token, message, false);
  }

  public MarcelSemanticException(LexToken token, String message, boolean eof) {
    this(new Error(message, eof, token));
  }

  private MarcelSemanticException(Error error) {
    this(Collections.singletonList(error));
  }

  public MarcelSemanticException(List<Error> errors) {
    super(generateErrorMessage(errors));
    this.errors = errors;
  }

  private static String generateErrorMessage(List<Error> errors) {
    if (errors.size() == 1) return generateErrorMessage(errors.get(0));
    return "Multiple semantic errors were found:" + errors.stream()
            .map(e -> "\n  -" + generateErrorMessage(e))
            .collect(Collectors.joining());
  }

  private static String generateErrorMessage(Error error) {
    return String.format("Semantic error at token %s (line %d, column %d): %s",
            error.token, error.token.getLine() + 1, error.token.getColumn(), error.message);
  }

  public static MarcelSemanticException.Error malformedNumber(NumberFormatException e, LexToken token, boolean eof) {
    return new MarcelSemanticException.Error("Malformed number (" + e.getMessage() + ")", eof, token);
  }

}