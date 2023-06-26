package marcel.lang.dynamic;

import marcel.lang.DynamicObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
  List copy() {
    return new ArrayList(value);
  }
}
