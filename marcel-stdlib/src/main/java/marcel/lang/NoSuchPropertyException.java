package marcel.lang;

public class NoSuchPropertyException extends RuntimeException {

  public NoSuchPropertyException() {
    super();
  }

  public NoSuchPropertyException(String message) {
    super(message);
  }
}
