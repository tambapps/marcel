package marcel.lang.dynamic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import marcel.lang.DynamicObject;

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
  public DynamicObject setProperty(String name, Object value) {
    return putAt(name, value);
  }
}
