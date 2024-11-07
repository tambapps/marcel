package marcel.lang.dynamic;

import lombok.Value;

// TODO handle default value
@Value
public class MethodParameter {
    Class<?> type;
    String name;
}