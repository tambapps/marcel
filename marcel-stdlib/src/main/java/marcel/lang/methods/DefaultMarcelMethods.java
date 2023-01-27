package marcel.lang.methods;

import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.booleans.BooleanSet;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.floats.FloatSet;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class DefaultMarcelMethods {

  public static void reverse(List<?> list) {
    Collections.reverse(list);
  }

  public static <T> boolean leftShift(Collection<T> self, T value) {
    return self.add(value);
  }

  public static boolean leftShift(IntList self, int value) {
    return self.add(value);
  }

  public static boolean leftShift(LongList self, long value) {
    return self.add(value);
  }

  public static boolean leftShift(FloatList self, float value) {
    return self.add(value);
  }

  public static boolean leftShift(DoubleList self, double value) {
    return self.add(value);
  }

  public static boolean leftShift(BooleanList self, boolean value) {
    return self.add(value);
  }

  public static boolean leftShift(IntSet self, int value) {
    return self.add(value);
  }

  public static boolean leftShift(LongSet self, long value) {
    return self.add(value);
  }

  public static boolean leftShift(FloatSet self, float value) {
    return self.add(value);
  }

  public static boolean leftShift(DoubleSet self, double value) {
    return self.add(value);
  }

  public static boolean leftShift(BooleanSet self, boolean value) {
    return self.add(value);
  }
}
