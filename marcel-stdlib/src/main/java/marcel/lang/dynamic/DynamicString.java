package marcel.lang.dynamic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import marcel.lang.DynamicObject;

@AllArgsConstructor
public class DynamicString implements DynamicObject {

  @Getter
  private final String value;

  @Override
  public DynamicObject plus(DynamicObject object) {
    if (object.getValue() instanceof String) return DynamicObject.of(value + object.getValue().toString());
    throw new IllegalArgumentException("Cannot add a String with " + object.getValue().getClass().getSimpleName());
  }
}
