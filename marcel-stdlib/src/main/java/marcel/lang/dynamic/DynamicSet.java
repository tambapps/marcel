package marcel.lang.dynamic;

import marcel.lang.DynamicObject;

import java.util.HashSet;
import java.util.Map;
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
  public DynamicObject invokeMethod(String name, Map<String, Object> namedArgs, Object... args) {
    try {
      return super.invokeMethod(name, namedArgs, args);
    } catch (MissingMethodException e) {
      return invokeMethod(Set.class, name, namedArgs, args);
    }
  }
}
