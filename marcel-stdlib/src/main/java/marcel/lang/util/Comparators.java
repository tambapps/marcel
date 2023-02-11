package marcel.lang.util;

import marcel.lang.util.function.CharacterComparator;
import marcel.lang.util.function.DoubleComparator;
import marcel.lang.util.function.FloatComparator;
import marcel.lang.util.function.IntComparator;
import marcel.lang.util.function.LongComparator;

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
  public static LongComparator asLongComparator(final Comparator<? super Long> c) {
    if (c == null || c instanceof LongComparator) return (LongComparator) c;
    return new LongComparator() {
      @Override
      public int compare(long x, long y) { return c.compare(Long.valueOf(x), Long.valueOf(y)); }
      @Override
      public int compare(Long x, Long y) { return c.compare(x, y); }
    };
  }
  public static FloatComparator asFloatComparator(final Comparator<? super Float> c) {
    if (c == null || c instanceof FloatComparator) return (FloatComparator) c;
    return new FloatComparator() {
      @Override
      public int compare(float x, float y) { return c.compare(Float.valueOf(x), Float.valueOf(y)); }
      @Override
      public int compare(Float x, Float y) { return c.compare(x, y); }
    };
  }
  public static CharacterComparator asCharacterComparator(final Comparator<? super Character> c) {
    if (c == null || c instanceof CharacterComparator) return (CharacterComparator) c;
    return new CharacterComparator() {
      @Override
      public int compare(char x, char y) { return c.compare(Character.valueOf(x), Character.valueOf(y)); }
      @Override
      public int compare(Character x, Character y) { return c.compare(x, y); }
    };
  }

  public static DoubleComparator asDoubleComparator(final Comparator<? super Double> c) {
    if (c == null || c instanceof DoubleComparator) return (DoubleComparator) c;
    return new DoubleComparator() {
      @Override
      public int compare(double x, double y) { return c.compare(Double.valueOf(x), Double.valueOf(y)); }
      @Override
      public int compare(Double x, Double y) { return c.compare(x, y); }
    };
  }
}
