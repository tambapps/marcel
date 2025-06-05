package marcel.lang.dynamic;

import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor
@Value
public class MethodParameter {
    Class<?> type;
    String name;
}