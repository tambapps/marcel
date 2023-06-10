package marcel.lang;

import marcel.lang.dynamic.DefaultDynamicObject;
import marcel.lang.dynamic.DynamicCharacter;
import marcel.lang.dynamic.DynamicList;
import marcel.lang.dynamic.DynamicMap;
import marcel.lang.dynamic.DynamicNumber;
import marcel.lang.dynamic.DynamicQueue;
import marcel.lang.dynamic.DynamicSet;
import marcel.lang.dynamic.DynamicString;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public interface DynamicObject {

  default DynamicObject getAt(Object key) {
    throw new UnsupportedOperationException();
  }

  default DynamicObject putAt(Object key, Object value) {
    throw new UnsupportedOperationException();
  }

  default DynamicObject getProperty(String name) {
    throw new UnsupportedOperationException();
  }

  default void setProperty(String name, Object value) {
    throw new UnsupportedOperationException();
  }

  // TODO do these operators for all classes
  default DynamicObject plus(DynamicObject object) {
    throw new UnsupportedOperationException();
  }

  default DynamicObject minus(DynamicObject object) {
    throw new UnsupportedOperationException();
  }

  default DynamicObject multiply(DynamicObject object) {
    throw new UnsupportedOperationException();
  }

  default DynamicObject div(DynamicObject object) {
    throw new UnsupportedOperationException();
  }

  Object getValue();

  static DynamicObject of(Object o) {
    // TODO handle dynamic object getProperty in access nodes
    if (o == null) return null;
    else if (o instanceof DynamicObject) return (DynamicObject) o;
    else if (o instanceof Number) return new DynamicNumber((Number) o);
    else if (o instanceof String) return new DynamicString((String) o);
    else if (o instanceof Character) return new DynamicCharacter((Character) o);
    else if (o instanceof List) return new DynamicList((List) o);
    else if (o instanceof Set) return new DynamicSet((Set) o);
    else if (o instanceof Queue) return new DynamicQueue((Queue) o);
    else if (o instanceof Map) return new DynamicMap((Map) o);
    else return new DefaultDynamicObject(o);
  }

}
