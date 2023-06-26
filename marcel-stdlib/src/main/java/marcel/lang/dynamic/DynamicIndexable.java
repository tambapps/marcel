package marcel.lang.dynamic;

import marcel.lang.DynamicObject;

interface DynamicIndexable extends DynamicObject {


  default DynamicObject getAtSafe(Object object) {
    if (object instanceof Integer) {
      int i = (Integer) object;
      return i >= 0 && i < size() ? getAt(object) : null;
    }
    throw new MissingMethodException(getValue().getClass(), "getAtSafe", new Object[]{object});
  }
}
