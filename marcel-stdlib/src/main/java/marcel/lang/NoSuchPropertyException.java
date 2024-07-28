package marcel.lang;

import lombok.Getter;

@Getter
public class NoSuchPropertyException extends RuntimeException {

  /// nullable, e.g. for binding
  private final Class<?> type;
  private final String propertyName;

  public NoSuchPropertyException(Class<?> type, String propertyName) {
    super(generateMessage(type, propertyName));
    this.type = type;
    this.propertyName = propertyName;
  }


  private static String generateMessage(Class<?> type, String propertyName) {
    return type == null ? "No such property '" + propertyName + "'"
        : "No such property '" + propertyName + "' for class " + type;
  }
}
