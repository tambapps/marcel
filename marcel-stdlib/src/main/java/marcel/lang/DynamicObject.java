package marcel.lang;

import marcel.lang.dynamic.DefaultDynamicObject;
import marcel.lang.dynamic.DynamicArray;
import marcel.lang.dynamic.DynamicCharacter;
import marcel.lang.dynamic.DynamicList;
import marcel.lang.dynamic.DynamicMap;
import marcel.lang.dynamic.DynamicNumber;
import marcel.lang.dynamic.DynamicQueue;
import marcel.lang.dynamic.DynamicSet;
import marcel.lang.dynamic.DynamicCharSequence;
import marcel.lang.dynamic.MissingMethodException;
import marcel.lang.dynamic.MissingPropertyException;
import marcel.lang.lambda.DynamicObjectLambda1;
import marcel.lang.primitives.collections.lists.CharacterList;
import marcel.lang.primitives.collections.lists.DoubleList;
import marcel.lang.primitives.collections.lists.FloatList;
import marcel.lang.primitives.collections.lists.IntList;
import marcel.lang.primitives.collections.lists.LongList;
import marcel.lang.primitives.collections.sets.CharacterSet;
import marcel.lang.primitives.collections.sets.DoubleSet;
import marcel.lang.primitives.collections.sets.FloatSet;
import marcel.lang.primitives.collections.sets.IntSet;
import marcel.lang.primitives.collections.sets.LongSet;

import java.util.*;

/**
 * Interface providing dynamic features to an object
 */
public interface DynamicObject extends Iterable<DynamicObject>, MarcelTruth {

  default DynamicObject getAt(Object key) {
    throw new MissingMethodException(getValue().getClass(), "getAt", new Object[]{key});
  }

  default DynamicObject getAtSafe(Object object) {
    throw new MissingMethodException(getValue().getClass(), "getAtSafe", new Object[]{object});
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

  default Iterator<DynamicObject> iterator() {
    throw new MissingMethodException(getValue().getClass(), "iterator", new Object[]{});
  }

  default DynamicObject find(DynamicObjectLambda1 lambda1) {
    return MarcelTruth.isTruthy(lambda1.apply(this)) ? this : null;
  }

  default DynamicObject map(DynamicObjectLambda1 lambda1) {
    return lambda1.apply(this);
  }

  @Override
  default boolean isTruthy() {
    return MarcelTruth.isTruthy(getValue());
  }

  Object getValue();

  // TODO rename these below methods asXXX because of incoherence with toString()
  default List asList() {
    if (getValue() instanceof List) return (List) getValue();
    throw new ClassCastException("Value isn't instance of List");
  }

  default IntList asIntList() {
    if (getValue() instanceof IntList) return (IntList) getValue();
    throw new ClassCastException("Value isn't instance of IntList");
  }

  default LongList asLongList() {
    if (getValue() instanceof LongList) return (LongList) getValue();
    throw new ClassCastException("Value isn't instance of LongList");
  }

  default FloatList asFloatList() {
    if (getValue() instanceof FloatList) return (FloatList) getValue();
    throw new ClassCastException("Value isn't instance of List");
  }

  default DoubleList asDoubleList() {
    if (getValue() instanceof DoubleList) return (DoubleList) getValue();
    throw new ClassCastException("Value isn't instance of DoubleList");
  }

  default CharacterList asCharacterList() {
    if (getValue() instanceof CharacterList) return (CharacterList) getValue();
    throw new ClassCastException("Value isn't instance of CharacterList");
  }

  default Set asSet() {
    if (getValue() instanceof Set) return (Set) getValue();
    throw new ClassCastException("Value isn't instance of List");
  }

  default IntSet asIntSet() {
    if (getValue() instanceof IntSet) return (IntSet) getValue();
    throw new ClassCastException("Value isn't instance of IntSet");
  }

  default LongSet asLongSet() {
    if (getValue() instanceof LongSet) return (LongSet) getValue();
    throw new ClassCastException("Value isn't instance of LongSet");
  }

  default FloatSet asFloatSet() {
    if (getValue() instanceof FloatSet) return (FloatSet) getValue();
    throw new ClassCastException("Value isn't instance of Set");
  }

  default DoubleSet asDoubleSet() {
    if (getValue() instanceof DoubleSet) return (DoubleSet) getValue();
    throw new ClassCastException("Value isn't instance of DoubleSet");
  }

  default CharacterSet asCharacterSet() {
    if (getValue() instanceof CharacterSet) return (CharacterSet) getValue();
    throw new ClassCastException("Value isn't instance of CharacterSet");
  }

  default Map asMap() {
    if (getValue() instanceof Map) return (Map) getValue();
    throw new ClassCastException("Value isn't instance of Map");
  }

  default Queue asQueue() {
    if (getValue() instanceof Queue) return (Queue) getValue();
    throw new ClassCastException("Value isn't instance of Queue");
  }

  default Collection asCollection() {
    if (getValue() instanceof Collection) return (Collection) getValue();
    throw new ClassCastException("Value isn't instance of Queue");
  }

  default int asInt() {
    if (getValue() instanceof Integer) return (Integer) getValue();
    throw new ClassCastException("Value isn't instance of Integer");
  }

  default long asLong() {
    if (getValue() instanceof Long) return (Long) getValue();
    throw new ClassCastException("Value isn't instance of Long");
  }

  default float asFloat() {
    if (getValue() instanceof Float) return (Float) getValue();
    throw new ClassCastException("Value isn't instance of Float");
  }

  default double asDouble() {
    if (getValue() instanceof Double) return (Double) getValue();
    throw new ClassCastException("Value isn't instance of Double");
  }

  default char asChar() {
    if (getValue() instanceof Character) return (Character) getValue();
    throw new ClassCastException("Value isn't instance of Character");
  }

  default boolean asBool() {
    if (getValue() instanceof Boolean) return (Boolean) getValue();
    throw new ClassCastException("Value isn't instance of Boolean");
  }

  static DynamicObject of(Object o) {
    if (o == null) return null;
    else if (o instanceof DynamicObject) return (DynamicObject) o;
    else if (o instanceof Number) return new DynamicNumber((Number) o);
    else if (o instanceof String) return new DynamicCharSequence((String) o);
    else if (o instanceof Character) return new DynamicCharacter((Character) o);
    else if (o instanceof List) return new DynamicList((List) o);
    else if (o instanceof Set) return new DynamicSet((Set) o);
    else if (o instanceof Queue) return new DynamicQueue((Queue) o);
    else if (o instanceof Map) return new DynamicMap((Map) o);
    else if (o.getClass().isArray()) return new DynamicArray(o);
    else return new DefaultDynamicObject(o);
  }

}
