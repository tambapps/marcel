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

  int size();
  default int getLength() {
    return size();
  }
}
