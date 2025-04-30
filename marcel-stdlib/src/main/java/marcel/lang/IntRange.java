package marcel.lang;

import marcel.util.function.ToCharFunction;
import marcel.util.function.ToFloatFunction;
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
import marcel.util.primitives.iterable.IntIterable;
import marcel.util.primitives.iterators.IntIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

public interface IntRange extends IntIterable {

  int getFrom();

  int getTo();

  int getToExclusive();

  @Override
  IntIterator iterator();

  boolean isEmpty();
  boolean isReverse();

  default IntList toList() {
    IntIterator iterator = iterator();
    IntList list = new IntArrayList();
    while (iterator.hasNext()) {
      list.add(iterator.nextInt());
    }
    return list;
  }

  default <T> List<T> map(IntFunction<T> mapper) {
    List<T> list = new ArrayList<>();
    IntIterator iterator = iterator();
    while (iterator.hasNext()) {
      list.add(mapper.apply(iterator.nextInt()));
    }
    return list;
  }

  default IntList mapToInt(IntFunction<Integer> mapper) {
    IntList list = new IntArrayList();
    IntIterator iterator = iterator();
    while (iterator.hasNext()) {
      list.add(mapper.apply(iterator.nextInt()));
    }
    return list;
  }

  default LongList mapToLong(IntFunction<Long> mapper) {
    LongList list = new LongArrayList();
    IntIterator iterator = iterator();
    while (iterator.hasNext()) {
      list.add(mapper.apply(iterator.nextInt()));
    }
    return list;
  }

  default FloatList mapToFloat(IntFunction<Float> mapper) {
    FloatList list = new FloatArrayList();
    IntIterator iterator = iterator();
    while (iterator.hasNext()) {
      list.add(mapper.apply(iterator.nextInt()));
    }
    return list;
  }

  default DoubleList mapToDouble(IntFunction<Double> mapper) {
    DoubleList list = new DoubleArrayList();
    IntIterator iterator = iterator();
    while (iterator.hasNext()) {
      list.add(mapper.apply(iterator.nextInt()));
    }
    return list;
  }

  default CharList mapToChar(IntFunction<Character> mapper) {
    CharList list = new CharArrayList();
    IntIterator iterator = iterator();
    while (iterator.hasNext()) {
      list.add(mapper.apply(iterator.nextInt()));
    }
    return list;
  }

  default boolean contains(int i) {
    return isReverse() ? i >= getTo() && i <= getFrom() : i >= getFrom() && i <= getTo();
  }

  /**
   * Returns true if all elements in the other ranges also belong to this range
   *
   * @param other the other range
   * @return true if all elements in the other ranges also belong to this range
   */
  default boolean contains(IntRange other) {
    if (other.isEmpty()) return true;
    if (isEmpty()) return false;
    // ranges may not be in ascending order
    int selfMin = Math.min(getFrom(), getTo());
    int selfMax = Math.max(getFrom(), getTo());
    int otherMin = Math.min(other.getFrom(), other.getTo());
    int otherMax = Math.max(other.getFrom(), other.getTo());
    return selfMin <= otherMin && selfMax >= otherMax;
  }

  /**
   * Returns true if the two ranges have at least one element in common
   *
   * @param other the other range
   * @return true if the two ranges have at least one element in common
   */
  default boolean intersects(IntRange other) {
    if (isEmpty() || other.isEmpty()) return false;
    // ranges may not be in ascending order
    int selfMin = Math.min(getFrom(), getTo());
    int selfMax = Math.max(getFrom(), getTo());
    int otherMin = Math.min(other.getFrom(), other.getTo());
    int otherMax = Math.max(other.getFrom(), other.getTo());
    return selfMin <= otherMax && selfMax >= otherMin;
  }

  int size();

}
