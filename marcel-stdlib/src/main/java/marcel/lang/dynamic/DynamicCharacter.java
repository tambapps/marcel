package marcel.lang.dynamic;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DynamicCharacter extends AbstractDynamicObject {

  private final Character value;

  @Override
  public Character getValue() {
    return value;
  }

}
