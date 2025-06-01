package marcel.csv;

import marcel.lang.DynamicObject;
import marcel.lang.dynamic.DynamicList;
import marcel.lang.dynamic.MissingMethodException;
import marcel.lang.dynamic.MissingPropertyException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DynamicCsvLine extends DynamicList {

  // nullable
  private final List<String> headers;

  DynamicCsvLine(List<String> headers, List values) {
    super(values);
    this.headers = headers;
  }

  public DynamicCsvLine(List values) {
    this(null, values);
  }

  @Override
  public DynamicObject getAt(Object key) {
    if (key instanceof Integer i) {
      return super.getAt(i);
    } else if (key instanceof String s) {
      if (headers != null) {
        int index = headers.indexOf(s);
        if (index >= 0 && index < size()) {
          return super.getAt(index);
        }
      }
    }
    throw new MissingMethodException(getValue().getClass(), "getAt", new Object[]{key});
  }

  @Override
  public DynamicObject getProperty(String name) {
    try {
      return getAt(name);
    } catch (MissingMethodException e) {
      throw new MissingPropertyException(getValue().getClass(), name);
    }
  }

  @Override
  public Map asMap() {
    Map<Object, Object> map = new HashMap<>();
    if (headers != null) {
      for (int i = 0; i < headers.size() && i < size(); i++) {
        map.put(headers.get(i), getValue().get(i));
      }
    } else {
      for (int i = 0; i < getValue().size(); i++) {
        map.put(i, getValue().get(i));
      }
    }
    return map;
  }
}
