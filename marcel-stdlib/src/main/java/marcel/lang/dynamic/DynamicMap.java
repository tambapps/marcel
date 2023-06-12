package marcel.lang.dynamic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import marcel.lang.DynamicObject;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class DynamicMap extends AbstractDynamicObject {

  @Getter
  private final Map value;

  @Override
  public DynamicObject getAt(Object key) {
    return DynamicObject.of(value.get(key));
  }

  @Override
  public DynamicObject putAt(Object key, Object value) {
    return DynamicObject.of(this.value.put(key, value));
  }

  @Override
  public DynamicObject getProperty(String name) {
    return getAt(name);
  }

  @Override
  public DynamicObject setProperty(String name, DynamicObject object) {
    return putAt(name, object != null ? object.getValue() : null);
  }

  @Override
  public DynamicObject plus(Object object) {
    Object o = getRealValue(object);
    if (o instanceof Map) {
      Map m = new HashMap(value);
      m.putAll((Map) o);
      return DynamicObject.of(m);
    }
    return super.plus(object);
  }
}
