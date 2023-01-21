package com.tambapps.marcel.parser;

public class MarcelParsingException extends RuntimeException {
  public MarcelParsingException(String message) {
    super(message);
  }

  public MarcelParsingException(Exception e) {
    super(e.getMessage(), e.getCause());
  }
}
