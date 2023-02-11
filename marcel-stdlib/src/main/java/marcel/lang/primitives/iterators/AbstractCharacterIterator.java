package marcel.lang.primitives.iterators;

public abstract class AbstractCharacterIterator implements CharacterIterator {
  protected AbstractCharacterIterator() {
  }

  public char nextCharacter() {
    return this.next();
  }

  public Character next() {
    return this.nextCharacter();
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

  public int skip(int n) {
    int i = n;

    while(i-- != 0 && this.hasNext()) {
      this.nextCharacter();
    }

    return n - i - 1;
  }
}