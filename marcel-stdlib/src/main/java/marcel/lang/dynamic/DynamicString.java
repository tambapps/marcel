package marcel.lang.dynamic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import marcel.lang.DynamicObject;

@AllArgsConstructor
public class DynamicString implements DynamicObject {

  @Getter
  private final String value;
}
