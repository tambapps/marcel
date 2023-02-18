package marcel.lang;

import marcel.lang.primitives.collections.lists.LongArrayList;
import marcel.lang.primitives.collections.lists.LongList;
import marcel.lang.primitives.iterable.LongIterable;
import marcel.lang.primitives.iterators.LongIterator;

public interface LongRange extends LongIterable {

  long getFrom();

  long getTo();

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

  int size();

  default int getLength() {
    return size();
  }
}
