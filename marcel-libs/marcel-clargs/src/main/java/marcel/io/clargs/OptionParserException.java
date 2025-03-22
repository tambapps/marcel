package marcel.io.clargs;

public class OptionParserException extends IllegalArgumentException {

  public OptionParserException(String message, Throwable cause) {
    super(message, cause);
  }

  public OptionParserException(String s) {
    super(s);
  }
}
