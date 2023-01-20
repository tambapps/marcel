package marcel.lang;


import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

public interface IntRange {

  int getFrom();

  int getTo();

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

}
