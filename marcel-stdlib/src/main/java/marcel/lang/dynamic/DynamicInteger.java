package marcel.lang.dynamic;

import lombok.AllArgsConstructor;
import marcel.lang.DynamicObject;

@AllArgsConstructor
public class DynamicInteger implements DynamicObject {

  private final Integer value;


  @Override
  public Integer getValue() {
    return value;
  }
}
