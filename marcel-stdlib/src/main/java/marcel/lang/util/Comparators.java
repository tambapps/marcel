package marcel.lang.util;

import it.unimi.dsi.fastutil.ints.IntComparator;

import java.util.Comparator;

public class Comparators {

  public static IntComparator asIntComparator(final Comparator<? super Integer> c) {
    if (c == null || c instanceof IntComparator) return (IntComparator) c;
    return new IntComparator() {
      @Override
      public int compare(int x, int y) { return c.compare(Integer.valueOf(x), Integer.valueOf(y)); }
      @Override
      public int compare(Integer x, Integer y) { return c.compare(x, y); }
    };
  }
}
