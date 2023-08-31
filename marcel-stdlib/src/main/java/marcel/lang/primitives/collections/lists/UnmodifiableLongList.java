package marcel.lang.primitives.collections.lists;

import lombok.AllArgsConstructor;
import marcel.lang.primitives.iterators.list.LongListIterator;
import marcel.lang.primitives.spliterators.LongSpliterator;
import marcel.lang.primitives.spliterators.LongSpliterators;
import marcel.lang.util.SafeMath;

import java.util.NoSuchElementException;

@AllArgsConstructor
class UnmodifiableLongList extends AbstractLongList {

  private final LongList base;

  @Override
  public int size() {
    return base.size();
  }

  @Override
  public LongListIterator listIterator(int index) {
    return new ImmutableListIterator(index);
  }

  @Override
  public long getAt(int index) {
    return base.getAt(index);
  }

  @Override
  public void sort() {
    unsupportedOperation();
  }

  @Override
  public void sortReverse() {
    unsupportedOperation();
  }

  private void unsupportedOperation() {
    throw new UnsupportedOperationException();
  }

  @Override
  public LongSpliterator spliterator() {
    return new ImmutableSpliterator();
  }

  private final class ImmutableSpliterator implements LongSpliterator {
    // Until we split, we will track the size of the list.
    // Once we split, then we stop updating on structural modifications.
    // Aka, size is late-binding.
    boolean hasSplit = false;
    int pos, max;
    public ImmutableSpliterator() {
      this(0, UnmodifiableLongList.this.size(), false);
    }
    private ImmutableSpliterator(int pos, int max, boolean hasSplit) {
      this.pos = pos;
      this.max = max;
      this.hasSplit = hasSplit;
    }
    private int getWorkingMax() {
      return hasSplit ? max : UnmodifiableLongList.this.size();
    }
    @Override
    public int characteristics() { return LongSpliterators.LIST_SPLITERATOR_CHARACTERISTICS; }
    @Override
    public long estimateSize() { return getWorkingMax() - pos; }
    @Override
    public boolean tryAdvance(final java.util.function.LongConsumer action) {
      if (pos >= getWorkingMax()) {
        return false;
      }
      action.accept(getAt(pos++));
      return true;
    }
    @Override
    public void forEachRemaining(final java.util.function.LongConsumer action) {
      for (final int max = getWorkingMax(); pos < max; ++pos) {
        action.accept(getAt(pos));
      }
    }
    @Override
    public long skip(long n) {
      if (n < 0) {
        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
      }
      final int max = getWorkingMax();
      if (pos >= max) {
        return 0;
      }
      final int remaining = max - pos;
      if (n < remaining) {
        pos = SafeMath.safeLongToInt(pos + n);
        return n;
      }
      n = remaining;
      pos = max;
      return n;
    }
    @Override
    public LongSpliterator trySplit() {
      final int max = getWorkingMax();
      int retLen = (max - pos) >> 1;
      if (retLen <= 1) {
        return null;
      }
      // Update instance max with the last seen list size (if needed) before continuing
      this.max = max;
      int myNewPos = pos + retLen;
      int retMax = myNewPos;
      int oldPos = pos;
      this.pos = myNewPos;
      this.hasSplit = true;
      return new ImmutableSpliterator(oldPos, retMax, true);
    }
  }
  private class ImmutableListIterator implements LongListIterator {
    int pos, last = -1;

    ImmutableListIterator(int index) {
      pos = index;
    }
    @Override
    public boolean hasNext() { return pos < size(); }
    @Override
    public boolean hasPrevious() { return pos > 0; }

    @Override
    public long nextLong() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      return getAt(last = pos++); }
    @Override
    public long previousLong() {
      if (!hasPrevious()) {
        throw new NoSuchElementException();
      }
      return getAt(last = --pos); }
    @Override
    public int nextIndex() { return pos; }
    @Override
    public int previousIndex() { return pos - 1; }
    @Override
    public void add(long k) {
      unsupportedOperation();
    }
    @Override
    public void set(long k) {
      unsupportedOperation();
    }
    @Override
    public void remove() {
      unsupportedOperation();
    }
    @Override
    public void forEachRemaining(final java.util.function.LongConsumer action) {
      while (pos < size()) {
        action.accept(getAt(last = pos++));
      }
    }

    @Override
    public int skip(int n) {
      if (n < 0) {
        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
      }
      final int remaining = size() - pos;
      if (n < remaining) {
        pos += n;
      } else {
        n = remaining;
        pos = size();
      }
      last = pos - 1;
      return n;
    }
  }
}
