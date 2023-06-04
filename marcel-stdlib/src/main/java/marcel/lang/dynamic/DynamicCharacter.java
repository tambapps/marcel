package marcel.lang.dynamic;

import lombok.AllArgsConstructor;
import marcel.lang.DynamicObject;

@AllArgsConstructor
public class DynamicCharacter implements DynamicObject {

  private final Character value;


  @Override
  public Character getValue() {
    return value;
  }
}
