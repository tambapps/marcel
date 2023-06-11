package marcel.lang.dynamic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import marcel.lang.DynamicObject;

@AllArgsConstructor
public class DynamicString extends AbstractDynamicObject {

  @Getter
  private final String value;

  @Override
  public DynamicObject plus(Object object) {
    Object o = getRealValue(object);
    if (o instanceof CharSequence) return DynamicObject.of(value + o);
    throw new MissingMethodException(getValue().getClass(), "plus", new Object[]{object});
  }
}