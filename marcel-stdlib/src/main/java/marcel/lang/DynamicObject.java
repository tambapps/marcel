package marcel.lang;

import marcel.lang.dynamic.DefaultDynamicObject;
import marcel.lang.dynamic.DynamicByte;
import marcel.lang.dynamic.DynamicCharacter;
import marcel.lang.dynamic.DynamicDouble;
import marcel.lang.dynamic.DynamicFloat;
import marcel.lang.dynamic.DynamicInteger;
import marcel.lang.dynamic.DynamicLong;
import marcel.lang.dynamic.DynamicMap;
import marcel.lang.dynamic.DynamicShort;
import marcel.lang.dynamic.DynamicString;

import java.util.Map;

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

  Object getValue();

  static DynamicObject of(Object o) {
    // TODO do list and other colllections
    // TODO handle dynamic object getProperty in access nodes
    if (o == null) return null;
    else if (o instanceof Byte) return new DynamicByte((Byte) o);
    else if (o instanceof Character) return new DynamicCharacter((Character) o);
    else if (o instanceof Double) return new DynamicDouble((Double) o);
    else if (o instanceof Float) return new DynamicFloat((Float) o);
    else if (o instanceof Integer) return new DynamicInteger((Integer) o);
    else if (o instanceof Long) return new DynamicLong((Long) o);
    else if (o instanceof Short) return new DynamicShort((Short) o);
    else if (o instanceof String) return new DynamicString((String) o);
    else if (o instanceof Map) return new DynamicMap((Map) o);
    else return new DefaultDynamicObject(o);
  }
}
