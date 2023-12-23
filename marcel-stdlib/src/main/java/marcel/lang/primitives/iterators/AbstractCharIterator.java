package marcel.lang.primitives.iterators;

public abstract class AbstractCharIterator implements CharIterator {
  protected AbstractCharIterator() {
  }

  public char nextChar() {
    return this.next();
  }

  public Character next() {
    return this.nextChar();
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

  public int skip(int n) {
    int i = n;

    while(i-- != 0 && this.hasNext()) {
      this.nextChar();
    }

    return n - i - 1;
  }
}