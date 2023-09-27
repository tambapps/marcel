package marcel.lang.dynamic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import marcel.lang.DynamicObject;
import marcel.lang.lambda.DynamicObjectLambda1;
import marcel.lang.methods.MarcelTruth;

import java.lang.reflect.Array;

@AllArgsConstructor
public class DynamicArray extends AbstractDynamicObject implements DynamicIndexable {

  @Getter
  final Object value;

  @Override
  public int size() {
    return Array.getLength(value);
  }

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

  @Override
  public DynamicObject map(DynamicObjectLambda1 lambda1) {
    Object[] array = new Object[size()];
    for (int i = 0; i < size(); i++) {
      array[i] = lambda1.apply(DynamicObject.of(Array.get(value, i)));
    }
    return DynamicObject.of(array);
  }

  @Override
  public DynamicObject getProperty(String name) {
    Object[] array = new Object[size()];
    for (int i = 0; i < size(); i++) {
      array[i] = getAt(i);
    }
    return DynamicObject.of(array);
  }

  @Override
  public DynamicObject find(DynamicObjectLambda1 lambda1) {
    for (int i = 0; i < size(); i++) {
      DynamicObject e = DynamicObject.of(Array.get(value, i));
      if (MarcelTruth.isTruthy(lambda1.apply(e))) {
        return DynamicObject.of(e);
      }
    }
    return null;
  }


}
