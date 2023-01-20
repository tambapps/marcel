package marcel.lang;

import it.unimi.dsi.fastutil.ints.AbstractIntIterator;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.NoSuchElementException;

public class IntRanges {

  public static IntRange ofDecrExclusive(int from, int to) {
    return from - to <= 1 ? new EmptyRange() : ofDecr(from - 1, to + 1);
  }
  public static IntRange ofDecrFromExclusive(int from, int to) {
    return from == to ? new EmptyRange() : ofDecr(from - 1, to);
  }
  public static IntRange ofDecrToExclusive(int from, int to) {
    return from == to ? new EmptyRange() : ofDecr(from, to + 1);
  }

  public static IntRange ofDecr(int from, int to) {
    if (from < to) {
      throw new IllegalArgumentException("From should be goe to");
    }
    return new IntRangeImpl(from, to, true);
  }

  public static IntRange ofIncrExclusive(int from, int to) {
    return to - from <= 1 ? new EmptyRange() : ofIncr(from + 1, to - 1);
  }
  public static IntRange ofIncrFromExclusive(int from, int to) {
    return from == to ? new EmptyRange() : ofIncr(from + 1, to);
  }
  public static IntRange ofIncrToExclusive(int from, int to) {
    return from == to ? new EmptyRange() : ofIncr(from, to - 1);
  }

  public static IntRange ofIncr(int from, int to) {
    if (from > to) {
      throw new IllegalArgumentException("From should be loe to");
    }
    return new IntRangeImpl(from, to, false);
  }

  @AllArgsConstructor
  private static class IntIncrRangeIterator extends AbstractIntIterator {

    private int current;
    private final int end;

    @Override
    public Integer next() {
      return current++;
    }

    @Override
    public boolean hasNext() {
      return current <= end;
    }
  }

  @AllArgsConstructor
  private static class IntDeccrRangeIterator extends AbstractIntIterator {

    private int current;
    private final int end;

    @Override
    public Integer next() {
      return current--;
    }

    @Override
    public boolean hasNext() {
      return current >= end;
    }
  }

  @Getter
  @AllArgsConstructor
  private static final class IntRangeImpl implements IntRange {

    private final int from;
    private final int to;
    private boolean reverse;

    @Override
    public IntIterator iterator() {
      return reverse ? new IntDeccrRangeIterator(from, to) : new IntIncrRangeIterator(from, to);
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

  }
  private static final class EmptyRange implements IntRange {

    @Override
    public int getFrom() {
      throw new NoSuchElementException();
    }

    @Override
    public int getTo() {
      throw new NoSuchElementException();
    }

    @Override
    public IntIterator iterator() {
      return IntIterators.EMPTY_ITERATOR;
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
    public IntList toList() {
      return IntLists.EMPTY_LIST;
    }
  }
}
