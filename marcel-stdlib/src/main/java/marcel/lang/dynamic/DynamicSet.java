package marcel.lang.dynamic;

import marcel.lang.DynamicObject;

import java.util.HashSet;
import java.util.Set;

public class DynamicSet extends DynamicCollection<Set> {
  public DynamicSet(Set value) {
    super(value);
  }

  @Override
  Set newEmptyInstance() {
    return new HashSet();
  }

  @Override
  public DynamicObject invokeMethod(String name, Object... args) {
    try {
      return super.invokeMethod(name, args);
    } catch (MissingMethodException e) {
      return invokeMethod(Set.class, name, args);
    }
  }
}
