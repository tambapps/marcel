package marcel.lang.dynamic;

import lombok.AllArgsConstructor;
import marcel.lang.DynamicObject;

@AllArgsConstructor
public class DynamicDouble implements DynamicObject {

  private final Double value;


  @Override
  public Double getValue() {
    return value;
  }
}
