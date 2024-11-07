package marcel.lang.dynamic;

import marcel.lang.DynamicObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DynamicList extends DynamicCollection<List> implements DynamicIndexable {
  public DynamicList(List value) {
    super(value);
  }

  @Override
  public DynamicObject getAt(Object key) {
    return key instanceof Integer ? DynamicObject.of(getValue().get((Integer) key)) : super.getAt(key);
  }

  @Override
  public DynamicObject putAt(Object key, Object value) {
    return key instanceof Integer ? DynamicObject.of(getValue().set((Integer) key, value)) : super.getAt(key);
  }

  @Override
  List newEmptyInstance() {
    return new ArrayList();
  }

  @Override
  public int size() {
    return value.size();
  }

  @Override
  public DynamicObject invokeMethod(String name, Map<String, Object> namedArgs, Object... args) {
    try {
      return super.invokeMethod(name, namedArgs, args);
    } catch (MissingMethodException e) {
      return invokeMethod(List.class, name, namedArgs, args);
    }
  }
}
