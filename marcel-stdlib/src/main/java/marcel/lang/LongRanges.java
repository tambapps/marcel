package marcel.lang;

import lombok.AllArgsConstructor;
import lombok.Getter;
import marcel.util.primitives.collections.lists.LongArrayList;
import marcel.util.primitives.collections.lists.LongList;
import marcel.util.primitives.iterators.AbstractLongIterator;
import marcel.util.primitives.iterators.LongIterator;
import marcel.util.primitives.iterators.LongIterators;

import java.util.NoSuchElementException;

public class LongRanges {

  public static LongRange ofExclusive(long from, long to) {
    return from <= to ? ofIncrExclusive(from, to) : ofDecrExclusive(from, to);
  }

  public static LongRange ofFromExclusive(long from, long to) {
    return from <= to ? ofIncrFromExclusive(from, to) : ofDecrFromExclusive(from, to);
  }

  public static LongRange ofToExclusive(long from, long to) {
    return from <= to ? ofIncrToExclusive(from, to) : ofDecrToExclusive(from, to);
  }

  public static LongRange of(long from, long to) {
    return from <= to ? ofIncr(from, to) : ofDecr(from, to);
  }

  public static LongRange ofDecrExclusive(long from, long to) {
    return from - to <= 1 ? new EmptyRange() : ofDecr(from - 1, to + 1);
  }
  public static LongRange ofDecrFromExclusive(long from, long to) {
    return from == to ? new EmptyRange() : ofDecr(from - 1, to);
  }
  public static LongRange ofDecrToExclusive(long from, long to) {
    return from == to ? new EmptyRange() : ofDecr(from, to + 1);
  }

  public static LongRange ofDecr(long from, long to) {
    if (from < to) {
      throw new IllegalArgumentException("From should be goe to");
    }
    return new LongRangeImpl(from, to, true);
  }

  public static LongRange ofIncrExclusive(long from, long to) {
    return to - from <= 1 ? new EmptyRange() : ofIncr(from + 1, to - 1);
  }
  public static LongRange ofIncrFromExclusive(long from, long to) {
    return from == to ? new EmptyRange() : ofIncr(from + 1, to);
  }
  public static LongRange ofIncrToExclusive(long from, long to) {
    return from == to ? new EmptyRange() : ofIncr(from, to - 1);
  }

  public static LongRange ofIncr(long from, long to) {
    if (from > to) {
      throw new IllegalArgumentException("From should be loe to");
    }
    return new LongRangeImpl(from, to, false);
  }

  @AllArgsConstructor
  private static class LongIncrRangeIterator extends AbstractLongIterator {

    private long current;
    private final long end;

    @Override
    public Long next() {
      return current++;
    }

    @Override
    public boolean hasNext() {
      return current <= end;
    }
  }

  @AllArgsConstructor
  private static class LongDecrRangeIterator extends AbstractLongIterator {

    private long current;
    private final long end;

    @Override
    public Long next() {
      return current--;
    }

    @Override
    public boolean hasNext() {
      return current >= end;
    }
  }

  @Getter
  @AllArgsConstructor
  private static final class LongRangeImpl extends AbstractLongRange {

    private final long from;
    private final long to;
    private boolean reverse;

    @Override
    public long getToExclusive() {
      return to + 1;
    }

    @Override
    public LongIterator iterator() {
      return reverse ? new LongDecrRangeIterator(from, to) : new LongIncrRangeIterator(from, to);
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public String toString() {
      return from + ".." + to;
    }
  }

  // TODO change empty range behaviour. it should behave like kotlin empty range:
  //  - it should return the entered start and from
  //  - is empty should return true

  private static final class EmptyRange extends AbstractLongRange {

    @Override
    public long getFrom() {
      throw new NoSuchElementException();
    }

    @Override
    public long getTo() {
      throw new NoSuchElementException();
    }

    @Override
    public long getToExclusive() {
      throw new NoSuchElementException();
    }

    @Override
    public LongIterator iterator() {
      return LongIterators.EMPTY_ITERATOR;
    }

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public boolean isReverse() {
      return false;
    }

    @Override
    public LongList toList() {
      return new LongArrayList();
    }

    @Override
    public String toString() {
      return "..";
    }

    @Override
    public int size() {
      return 0;
    }
  }
}
