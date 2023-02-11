package marcel.lang.primitives.iterators;

public abstract class AbstractLongIterator implements LongIterator {
  protected AbstractLongIterator() {
  }

  public long nextLong() {
    return this.next();
  }

  public Long next() {
    return this.nextLong();
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

  public int skip(int n) {
    int i = n;

    while(i-- != 0 && this.hasNext()) {
      this.nextLong();
    }

    return n - i - 1;
  }
}