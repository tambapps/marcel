package marcel.lang.dynamic;

import lombok.AllArgsConstructor;
import marcel.lang.DynamicObject;

@AllArgsConstructor
public class DynamicFloat implements DynamicObject {

  private final Float value;


  @Override
  public Float getValue() {
    return value;
  }
}
