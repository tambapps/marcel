package marcel.lang;

import marcel.lang.primitives.iterators.LongIterator;

abstract class AbstractLongRange implements LongRange {

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof LongRange other)) return false;
    if (obj == this) return true;
    LongIterator thisIterator = iterator();
    LongIterator otherIterator = other.iterator();
    while (thisIterator.hasNext() && otherIterator.hasNext()) {
      if (thisIterator.nextLong() != otherIterator.nextLong()) return false;
    }
    return !thisIterator.hasNext() && !otherIterator.hasNext();
  }

  @Override
  public int hashCode() {
    LongIterator i = iterator();
    int h = 1, s = size();
    while (s-- != 0) {
      long l = i.nextLong();
      int k = (int)(l ^ (l >>> 32));
      h = 31 * h + (k);
    }
    return h;
  }

  @Override
  public int size() {
    return (int) Math.abs(getFrom() - getTo()) + 1;
  }
}
