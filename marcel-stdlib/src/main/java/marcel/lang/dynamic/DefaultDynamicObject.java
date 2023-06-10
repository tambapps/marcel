package marcel.lang.dynamic;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DefaultDynamicObject extends AbstractDynamicObject {

  private final Object value;


  @Override
  public Object getValue() {
    return value;
  }
}
