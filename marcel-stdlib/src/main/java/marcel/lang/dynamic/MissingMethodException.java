package marcel.lang.dynamic;


import lombok.Getter;

import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
public class MissingMethodException extends RuntimeException {

  private final Class type;
  private final String methodName;
  private final Object[] arguments;

  public MissingMethodException(Class type, String methodName, Object[] arguments) {
    super(generateMessage(type, methodName, arguments));
    this.type = type;
    this.methodName = methodName;
    this.arguments = arguments;
  }


  private static String generateMessage(Class type, String methodName, Object[] arguments) {
    return String.format("No method is applicable for the signature %s.%s(%s)", type.getName(), methodName,
        Arrays.stream(arguments)
            .map(a -> a != null ? a.getClass().getSimpleName() : "null")
            .collect(Collectors.joining(", ")));
  }
}