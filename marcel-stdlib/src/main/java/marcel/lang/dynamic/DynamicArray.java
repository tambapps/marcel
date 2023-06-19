package marcel.lang.dynamic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import marcel.lang.DynamicObject;

import java.lang.reflect.Array;

@AllArgsConstructor
public class DynamicArray extends AbstractDynamicObject {

  @Getter
  final Object value;

  @Override
  public DynamicObject getAt(Object key) {
    return key instanceof Integer ? DynamicObject.of(Array.get(value, (Integer) key)) : super.getAt(key);
  }

  @Override
  public DynamicObject putAt(Object key, Object value) {
    if (key instanceof Integer) {
      Array.set(value, (Integer) key, value);
      return null;
    } else return super.getAt(key);
  }
}
