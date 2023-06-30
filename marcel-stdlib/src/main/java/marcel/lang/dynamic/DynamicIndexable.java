package marcel.lang.dynamic;

import lombok.RequiredArgsConstructor;
import marcel.lang.DynamicObject;

interface DynamicIndexable extends DynamicObject {

  int size();

  default int getLength() {
    return size();
  }

  default DynamicObject getAtSafe(Object object) {
    if (object instanceof Integer) {
      int i = (Integer) object;
      return i >= 0 && i < size() ? getAt(object) : null;
    }
    throw new MissingMethodException(getValue().getClass(), "getAtSafe", new Object[]{object});
  }

  @Override
  default java.util.Iterator<DynamicObject> iterator() {
    return new Iterator(this);
  }

  @RequiredArgsConstructor
  class Iterator implements java.util.Iterator<DynamicObject> {

    private final DynamicIndexable o;
    private int i = 0;
    @Override
    public boolean hasNext() {
      return i < o.size();
    }

    @Override
    public DynamicObject next() {
      return o.getAt(i++);
    }
  }
}
