package marcel.lang.dynamic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import marcel.lang.DynamicObject;
import marcel.lang.lambda.DynamicObjectLambda1;
import marcel.lang.MarcelTruth;
import marcel.util.primitives.collections.lists.CharArrayList;
import marcel.util.primitives.collections.lists.CharList;
import marcel.util.primitives.collections.lists.DoubleArrayList;
import marcel.util.primitives.collections.lists.DoubleList;
import marcel.util.primitives.collections.lists.FloatArrayList;
import marcel.util.primitives.collections.lists.FloatList;
import marcel.util.primitives.collections.lists.IntArrayList;
import marcel.util.primitives.collections.lists.IntList;
import marcel.util.primitives.collections.lists.LongArrayList;
import marcel.util.primitives.collections.lists.LongList;
import marcel.util.primitives.collections.sets.CharOpenHashSet;
import marcel.util.primitives.collections.sets.CharSet;
import marcel.util.primitives.collections.sets.DoubleOpenHashSet;
import marcel.util.primitives.collections.sets.DoubleSet;
import marcel.util.primitives.collections.sets.FloatOpenHashSet;
import marcel.util.primitives.collections.sets.FloatSet;
import marcel.util.primitives.collections.sets.IntOpenHashSet;
import marcel.util.primitives.collections.sets.IntSet;
import marcel.util.primitives.collections.sets.LongOpenHashSet;
import marcel.util.primitives.collections.sets.LongSet;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

  @Override
  public DynamicObject findAll(DynamicObjectLambda1 lambda1) {
    List<Object> list = new ArrayList<>();
    for (int i = 0; i < size(); i++) {
      DynamicObject e = DynamicObject.of(Array.get(value, i));
      if (MarcelTruth.isTruthy(lambda1.apply(e))) {
        list.add(e);
      }
    }
    return DynamicObject.of(list);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("[");
    for (int i = 0; i < size(); i++) {
      if (i > 0) builder.append(", ");
      builder.append(getAt(i));
    }
    builder.append("]");
    return builder.toString();
  }

  @Override
  public List asList() {
    List<Object> list = new ArrayList<>();
    for (Object o: this) {
      list.add(o);
    }
    return list;
  }

  @Override
  public IntList asIntList() {
    IntList list = new IntArrayList();
    for (Object o: this) list.add((Integer) o);
    return list;
  }

  @Override
  public LongList asLongList() {
    LongList list = new LongArrayList();
    for (Object o: this) list.add((Long) o);
    return list;
  }

  @Override
  public FloatList asFloatList() {
    FloatList list = new FloatArrayList();
    for (Object o: this) list.add((Float) o);
    return list;
  }

  @Override
  public DoubleList asDoubleList() {
    DoubleList list = new DoubleArrayList();
    for (Object o: this) list.add((Double) o);
    return list;
  }

  @Override
  public CharList asCharList() {
    CharList list = new CharArrayList();
    for (Object o: this) list.add((Character) o);
    return list;
  }

  @Override
  public Set asSet() {
    Set<Object> set = new HashSet<>();
    for (Object o: this) {
      set.add(o);
    }
    return set;
  }

  @Override
  public IntSet asIntSet() {
    IntSet set = new IntOpenHashSet();
    for (Object o: this) set.add((Integer) o);
    return set;
  }

  @Override
  public LongSet asLongSet() {
    LongSet set = new LongOpenHashSet();
    for (Object o: this) set.add((Long) o);
    return set;
  }

  @Override
  public FloatSet asFloatSet() {
    FloatSet set = new FloatOpenHashSet();
    for (Object o: this) set.add((Float) o);
    return set;
  }

  @Override
  public DoubleSet asDoubleSet() {
    DoubleSet set = new DoubleOpenHashSet();
    for (Object o: this) set.add((Double) o);
    return set;
  }

  @Override
  public CharSet asCharSet() {
    CharSet set = new CharOpenHashSet();
    for (Object o: this) set.add((Character) o);
    return set;
  }
}
