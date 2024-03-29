package marcel.lang.dynamic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import marcel.lang.DynamicObject;
import marcel.lang.lambda.DynamicObjectLambda1;
import marcel.lang.methods.DefaultMarcelMethods;
import marcel.lang.MarcelTruth;
import marcel.lang.primitives.collections.lists.CharArrayList;
import marcel.lang.primitives.collections.lists.CharList;
import marcel.lang.primitives.collections.lists.DoubleArrayList;
import marcel.lang.primitives.collections.lists.DoubleList;
import marcel.lang.primitives.collections.lists.FloatArrayList;
import marcel.lang.primitives.collections.lists.FloatList;
import marcel.lang.primitives.collections.lists.IntArrayList;
import marcel.lang.primitives.collections.lists.IntList;
import marcel.lang.primitives.collections.lists.LongArrayList;
import marcel.lang.primitives.collections.lists.LongList;
import marcel.lang.primitives.collections.sets.CharOpenHashSet;
import marcel.lang.primitives.collections.sets.CharSet;
import marcel.lang.primitives.collections.sets.DoubleOpenHashSet;
import marcel.lang.primitives.collections.sets.DoubleSet;
import marcel.lang.primitives.collections.sets.FloatOpenHashSet;
import marcel.lang.primitives.collections.sets.FloatSet;
import marcel.lang.primitives.collections.sets.IntOpenHashSet;
import marcel.lang.primitives.collections.sets.IntSet;
import marcel.lang.primitives.collections.sets.LongOpenHashSet;
import marcel.lang.primitives.collections.sets.LongSet;

import java.util.*;

@AllArgsConstructor
abstract class DynamicCollection<T extends Collection> extends AbstractDynamicObject {
  @Getter
  T value;

  @Override
  public DynamicObject plus(Object object) {
    Object o = getRealValue(object);
    if (!(o instanceof Collection)) {
      throw new MissingMethodException(getValue().getClass(), "plus", new Object[]{object});
    }
    T c = copy();
    c.addAll((Collection) o);
    return DynamicObject.of(c);
  }

  @Override
  public DynamicObject leftShift(Object object) {
    return DynamicObject.of(value.add(object));
  }

  abstract T newEmptyInstance();

  private T copy() {
    T collection = newEmptyInstance();
    collection.addAll(value);
    return collection;
  }

  @Override
  public DynamicObject find(DynamicObjectLambda1 lambda1) {
    return DynamicObject.of(
        DefaultMarcelMethods.find(value, (e) -> MarcelTruth.isTruthy(lambda1.apply(DynamicObject.of(e))))
    );
  }

  @Override
  public DynamicObject map(DynamicObjectLambda1 lambda1) {
    List<DynamicObject> list = new ArrayList<>();
    for (Object o : value) {
      list.add(lambda1.apply(DynamicObject.of(o)));
    }
    return DynamicObject.of(list);
  }

  @Override
  public List asList() {
    return new ArrayList(value);
  }

  @Override
  public IntList asIntList() {
    IntList list = new IntArrayList();
    value.forEach(e -> list.add((Integer) e));
    return list;
  }

  @Override
  public LongList asLongList() {
    LongList list = new LongArrayList();
    value.forEach(e -> list.add((Long) e));
    return list;
  }

  @Override
  public FloatList asFloatList() {
    FloatList list = new FloatArrayList();
    value.forEach(e -> list.add((Float) e));
    return list;
  }

  @Override
  public DoubleList asDoubleList() {
    DoubleList list = new DoubleArrayList();
    value.forEach(e -> list.add((Float) e));
    return list;
  }

  @Override
  public CharList asCharList() {
    CharList list = new CharArrayList();
    value.forEach(e -> list.add((Character) e));
    return list;
  }

  @Override
  public Set asSet() {
    return new HashSet(value);
  }

  @Override
  public IntSet asIntSet() {
    IntSet set = new IntOpenHashSet();
    value.forEach(e -> set.add((Integer) e));
    return set;
  }

  @Override
  public LongSet asLongSet() {
    LongSet set = new LongOpenHashSet();
    value.forEach(e -> set.add((Long) e));
    return set;
  }

  @Override
  public FloatSet asFloatSet() {
    FloatSet set = new FloatOpenHashSet();
    value.forEach(e -> set.add((Float) e));
    return set;
  }

  @Override
  public DoubleSet asDoubleSet() {
    DoubleSet set = new DoubleOpenHashSet();
    value.forEach(e -> set.add((Double) e));
    return set;
  }

  @Override
  public CharSet asCharSet() {
    CharSet set = new CharOpenHashSet();
    value.forEach(e -> set.add((Character) e));
    return set;
  }

  @Override
  public DynamicObject invokeMethod(String name, Object... args) {
    return invokeMethod(Collection.class, name, args);
  }

  @Override
  public DynamicObject getProperty(String name) {
    if ("size".equals(name) || "length".equals(name)) {
      return DynamicObject.of(value.size());
    }
    T c = newEmptyInstance();
    for (DynamicObject o : this) {
      c.add(o.getProperty(name));
    }
    return DynamicObject.of(c);
  }

  @Override
  public Queue asQueue() {
    return new LinkedList(value);
  }

  @Override
  public Collection asCollection() {
    return value;
  }
}
