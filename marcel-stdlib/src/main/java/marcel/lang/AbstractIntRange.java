package marcel.lang;

import marcel.util.primitives.iterators.IntIterator;

abstract class AbstractIntRange implements IntRange {

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof IntRange other)) return false;
    if (obj == this) return true;
    IntIterator thisIterator = iterator();
    IntIterator otherIterator = other.iterator();
    while (thisIterator.hasNext() && otherIterator.hasNext()) {
      if (thisIterator.nextInt() != otherIterator.nextInt()) return false;
    }
    return !thisIterator.hasNext() && !otherIterator.hasNext();
  }

  @Override
  public int hashCode() {
    IntIterator i = iterator();
    int h = 1;
    while (i.hasNext()) {
      int k = i.nextInt();
      h = 31 * h + (k);
    }
    return h;
  }

  @Override
  public int size() {
    return Math.abs(getFrom() - getTo()) + 1;
  }
}
