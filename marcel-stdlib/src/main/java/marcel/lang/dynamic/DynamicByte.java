package marcel.lang.dynamic;

import lombok.AllArgsConstructor;
import marcel.lang.DynamicObject;

@AllArgsConstructor
public class DynamicByte implements DynamicObject {

  private final Byte value;


  @Override
  public Byte getValue() {
    return value;
  }
}
