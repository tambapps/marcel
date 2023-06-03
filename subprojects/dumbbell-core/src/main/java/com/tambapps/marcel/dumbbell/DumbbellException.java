package com.tambapps.marcel.dumbbell;

public class DumbbellException extends RuntimeException {

  public DumbbellException(String message) {
    super(message);
  }

  public DumbbellException(String message, Throwable cause) {
    super(message, cause);
  }

  public DumbbellException(Throwable cause) {
    super(cause != null ? cause.getMessage() : null, cause);
  }
}
