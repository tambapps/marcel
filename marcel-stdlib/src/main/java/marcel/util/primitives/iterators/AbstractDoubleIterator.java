package marcel.util.primitives.iterators;

public abstract class AbstractDoubleIterator implements DoubleIterator {
  protected AbstractDoubleIterator() {
  }

  public double nextDouble() {
    return this.next();
  }

  public Double next() {
    return this.nextDouble();
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

  public int skip(int n) {
    int i = n;

    while(i-- != 0 && this.hasNext()) {
      this.nextDouble();
    }

    return n - i - 1;
  }
}