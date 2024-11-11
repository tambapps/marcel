package marcel.lang.dynamic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import marcel.lang.DynamicObject;
import marcel.lang.methods.DefaultMarcelMethods;

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
  public void registerField(String name, Object value) {
    putAt(name, value);
  }

  @Override
  public DynamicObject setProperty(String name, DynamicObject object) {
    return putAt(name, object != null ? object.getValue() : null);
  }

  @Override
  public Map asMap() {
    return new HashMap(value);
  }

  @Override
  public DynamicObject invokeMethod(String name, Map<String, Object> namedArgs, Object... positionalArgs) {
    return invokeMethod(Map.class, name, namedArgs, positionalArgs);
  }

  @Override
  public DynamicObject plus(Object object) {
    Object o = getRealValue(object);
    if (o instanceof Map) {
      return DynamicObject.of(DefaultMarcelMethods.plus(value, (Map) o));
    }
    return super.plus(object);
  }
}
