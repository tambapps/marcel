package com.tambapps.marcel.android.compiler;

public class DexException extends RuntimeException {
  public DexException(String message) {
    super(message);
  }
  public DexException(String message, Throwable cause) {
    super(message, cause);
  }
}
