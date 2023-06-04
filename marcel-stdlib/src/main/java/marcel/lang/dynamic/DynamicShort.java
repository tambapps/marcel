package marcel.lang.dynamic;

import lombok.AllArgsConstructor;
import marcel.lang.DynamicObject;

@AllArgsConstructor
public class DynamicShort implements DynamicObject {

  private final Short value;


  @Override
  public Short getValue() {
    return value;
  }
}
