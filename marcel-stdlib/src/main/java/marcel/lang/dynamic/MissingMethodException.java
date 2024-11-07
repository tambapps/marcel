package marcel.lang.dynamic;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class MissingMethodException extends RuntimeException {

  private final Class type;
  private final String methodName;
  private final Map<String, Object> namedArgs;
  private final Object[] arguments;

  public MissingMethodException(Class<?> type, String methodName, Object[] arguments) {
    this(type, methodName, Map.of(), arguments);
  }

  public MissingMethodException(Class<?> type, String methodName, Map<String, Object> namedArgs, Object[] arguments) {
    super(generateMessage(type, methodName, namedArgs, arguments));
    this.type = type;
    this.methodName = methodName;
    this.namedArgs = namedArgs;
    this.arguments = arguments;
  }


  private static String generateMessage(Class<?> type, String methodName, Map<String, Object> namedArgs, Object[] arguments) {
    return String.format("No method is applicable for the signature %s.%s(%s)", type.getName(), methodName,
        Stream.concat(
            Arrays.stream(arguments)
                .map(a -> a != null ? a.getClass().getSimpleName() : "null"),
            namedArgs.entrySet().stream()
                .map(e -> "%s: %s".formatted(e.getKey(), e.getValue()))
        ).collect(Collectors.joining(", "))
    );
  }
}