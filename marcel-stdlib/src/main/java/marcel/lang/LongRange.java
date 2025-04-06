package marcel.lang;

import marcel.util.primitives.collections.lists.LongArrayList;
import marcel.util.primitives.collections.lists.LongList;
import marcel.util.primitives.iterable.LongIterable;
import marcel.util.primitives.iterators.LongIterator;

public interface LongRange extends LongIterable {

  long getFrom();

  long getTo();

  long getToExclusive();

  @Override
  LongIterator iterator();

  boolean isEmpty();
  boolean isReverse();

  default LongList toList() {
    LongIterator iterator = iterator();
    LongList list = new LongArrayList();
    while (iterator.hasNext()) {
      list.add(iterator.nextLong());
    }
    return list;
  }



  default boolean contains(long i) {
    return isReverse() ? i >= getTo() && i <= getFrom() : i >= getFrom() && i <= getTo();
  }

  /**
   * Returns true if all elements in the other ranges also belong to this range
   *
   * @param other the other range
   * @return true if all elements in the other ranges also belong to this range
   */
  default boolean contains(LongRange other) {
    if (other.isEmpty()) return true;
    if (isEmpty()) return false;
    // ranges may not be in ascending order
    long selfMin = Math.min(getFrom(), getTo());
    long selfMax = Math.max(getFrom(), getTo());
    long otherMin = Math.min(other.getFrom(), other.getTo());
    long otherMax = Math.max(other.getFrom(), other.getTo());
    return selfMin <= otherMin && selfMax >= otherMax;
  }

  /**
   * Returns true if the two ranges have at least one element in common
   *
   * @param other the other range
   * @return true if the two ranges have at least one element in common
   */
  default boolean intersects(LongRange other) {
    if (isEmpty() || other.isEmpty()) return false;
    // ranges may not be in ascending order
    long selfMin = Math.min(getFrom(), getTo());
    long selfMax = Math.max(getFrom(), getTo());
    long otherMin = Math.min(other.getFrom(), other.getTo());
    long otherMax = Math.max(other.getFrom(), other.getTo());
    return selfMin <= otherMax && selfMax >= otherMin;
  }

  int size();

}
