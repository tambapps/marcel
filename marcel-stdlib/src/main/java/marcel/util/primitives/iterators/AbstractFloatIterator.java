package marcel.util.primitives.iterators;

public abstract class AbstractFloatIterator implements FloatIterator {
  protected AbstractFloatIterator() {
  }

  public float nextFloat() {
    return this.next();
  }

  public Float next() {
    return this.nextFloat();
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

  public int skip(int n) {
    int i = n;

    while(i-- != 0 && this.hasNext()) {
      this.nextFloat();
    }

    return n - i - 1;
  }
}