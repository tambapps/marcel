package marcel.lang;

import marcel.lang.dynamic.DefaultDynamicObject;
import marcel.lang.dynamic.DynamicArray;
import marcel.lang.dynamic.DynamicCharacter;
import marcel.lang.dynamic.DynamicList;
import marcel.lang.dynamic.DynamicMap;
import marcel.lang.dynamic.DynamicNumber;
import marcel.lang.dynamic.DynamicQueue;
import marcel.lang.dynamic.DynamicSet;
import marcel.lang.dynamic.DynamicString;
import marcel.lang.dynamic.MissingMethodException;
import marcel.lang.dynamic.MissingPropertyException;
import marcel.lang.lambda.DynamicObjectLambda1;
import marcel.lang.methods.MarcelTruth;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Interface providing dynamic features to an object
 */
public interface DynamicObject {

  default DynamicObject getAt(Object key) {
    throw new MissingMethodException(getValue().getClass(), "getAt", new Object[]{key});
  }

  default DynamicObject getAtSafe(Object object) {
    throw new MissingMethodException(getValue().getClass(), "getAtSafe", new Object[]{object});
  }

  default int size() {
    throw new MissingMethodException(getValue().getClass(), "size", new Object[]{});
  }

  // TODO transform to getSize(). Its shorter then length
  default int getLength() {
    return size();
  }

  default DynamicObject putAt(Object key, Object value) {
    throw new MissingMethodException(getValue().getClass(), "putAt", new Object[]{key, value});
  }

  default DynamicObject getProperty(String name) {
    throw new MissingPropertyException(getValue().getClass(), name);
  }

  default DynamicObject setProperty(String name, DynamicObject value) {
    throw new MissingPropertyException(getValue().getClass(), name);
  }

  default DynamicObject invokeMethod(String name, Object... args) {
    throw new MissingMethodException(getValue().getClass(), name, args);
  }

  default DynamicObject plus(Object object) {
    throw new MissingMethodException(getValue().getClass(), "plus", new Object[]{object});
  }

  default DynamicObject minus(Object object) {
    throw new MissingMethodException(getValue().getClass(), "minus", new Object[]{object});
  }

  default DynamicObject multiply(Object object) {
    throw new MissingMethodException(getValue().getClass(), "multiply", new Object[]{object});
  }

  default DynamicObject div(Object object) {
    throw new MissingMethodException(getValue().getClass(), "div", new Object[]{object});
  }

  default DynamicObject leftShift(Object object) {
    throw new MissingMethodException(getValue().getClass(), "leftShift", new Object[]{object});
  }

  default DynamicObject rightShift(Object object) {
    throw new MissingMethodException(getValue().getClass(), "rightShift", new Object[]{object});
  }

  default DynamicObject find(DynamicObjectLambda1 lambda1) {
    return MarcelTruth.truthy(lambda1.apply(this)) ? this : null;
  }

  default DynamicObject map(DynamicObjectLambda1 lambda1) {
    return lambda1.apply(this);
  }

  default boolean isTruthy() {
    return MarcelTruth.truthy(getValue());
  }

  Object getValue();

  static DynamicObject of(Object o) {
    if (o == null) return null;
    else if (o instanceof DynamicObject) return (DynamicObject) o;
    else if (o instanceof Number) return new DynamicNumber((Number) o);
    else if (o instanceof String) return new DynamicString((String) o);
    else if (o instanceof Character) return new DynamicCharacter((Character) o);
    else if (o instanceof List) return new DynamicList((List) o);
    else if (o instanceof Set) return new DynamicSet((Set) o);
    else if (o instanceof Queue) return new DynamicQueue((Queue) o);
    else if (o instanceof Map) return new DynamicMap((Map) o);
    else if (o.getClass().isArray()) return new DynamicArray(o);
    else return new DefaultDynamicObject(o);
  }
}
