package marcel.lang.android;

public class PermissionDeniedException extends RuntimeException {
  public PermissionDeniedException(String message) {
    super(message);
  }
}
