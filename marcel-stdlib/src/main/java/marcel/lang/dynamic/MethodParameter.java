package marcel.lang.dynamic;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Optional;

// TODO handle default value (I've created the getter but I am never using it)
@AllArgsConstructor
@Value
public class MethodParameter {
    Class<?> type;
    String name;
    Optional<Optional> optDefaultValue;

    public MethodParameter(Class<?> type, String name) {
        this(type, name, Optional.empty());
    }

    public boolean hasDefaultValue() {
        return optDefaultValue.isPresent();
    }

    public Object getDefaultValue() {
        return optDefaultValue.orElse(null);
    }
}