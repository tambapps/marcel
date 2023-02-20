package marcel.lang;


import marcel.lang.primitives.collections.lists.IntArrayList;
import marcel.lang.primitives.collections.lists.IntList;
import marcel.lang.primitives.iterable.IntIterable;
import marcel.lang.primitives.iterators.IntIterator;

public interface IntRange extends IntIterable {

  int getFrom();

  int getTo();

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
  default int getLength() {
    return size();
  }
}
