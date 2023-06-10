package marcel.lang.dynamic;


import lombok.Getter;

@Getter
public class MissingPropertyException extends RuntimeException {

  private final Class type;
  private final String propertyName;

  public MissingPropertyException(Class type, String propertyName) {
    super(generateMessage(type, propertyName));
    this.type = type;
    this.propertyName = propertyName;
  }


  private static String generateMessage(Class type, String propertyName) {
    return String.format("No property '%s' was found for class %s", propertyName, type);
  }
}