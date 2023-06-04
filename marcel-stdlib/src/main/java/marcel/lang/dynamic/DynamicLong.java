package marcel.lang.dynamic;

import lombok.AllArgsConstructor;
import marcel.lang.DynamicObject;

@AllArgsConstructor
public class DynamicLong implements DynamicObject {

  private final Long value;


  @Override
  public Long getValue() {
    return value;
  }
}
