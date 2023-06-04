package marcel.lang.dynamic;

import lombok.AllArgsConstructor;
import marcel.lang.DynamicObject;

@AllArgsConstructor
public class DefaultDynamicObject implements DynamicObject {

  private final Object value;


  @Override
  public Object getValue() {
    return value;
  }
}
