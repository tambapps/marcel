package marcel.io.clargs;

public class OptionParserException extends RuntimeException {

  public OptionParserException(Throwable cause) {
    super(cause);
  }

  public OptionParserException(String message, Throwable cause) {
    super(message, cause);
  }

  public OptionParserException(String s) {
    super(s);
  }
}
