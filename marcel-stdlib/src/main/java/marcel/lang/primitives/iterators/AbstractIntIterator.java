package marcel.lang.primitives.iterators;

public abstract class AbstractIntIterator implements IntIterator {
  protected AbstractIntIterator() {
  }

  public int nextInt() {
    return this.next();
  }

  public Integer next() {
    return this.nextInt();
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

  public int skip(int n) {
    int i = n;

    while(i-- != 0 && this.hasNext()) {
      this.nextInt();
    }

    return n - i - 1;
  }
}