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

// TODO do some inline or function aliases for methods like leftShift (may need to do JavaType refacto first)
// TODO add visitor for inline functions?
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

  // getAt
  public static <T> T getAt(List<T> self, int index) {
    return self.get(index);
  }

  public static int getAt(IntList self, int index) {
    return self.getInt(index);
  }

  public static long getAt(LongList self, int index) {
    return self.getLong(index);
  }

  public static float getAt(FloatList self, int index) {
    return self.getFloat(index);
  }

  public static double getAt(DoubleList self, int index) {
    return self.getDouble(index);
  }

  public static boolean getAt(BooleanList self, int index) {
    return self.getBoolean(index);
  }

  // putAt
  public static <T> void putAt(List<T> self, int index, T value) {
    self.set(index, value);
  }

  public static void getAt(IntList self, int index, int value) {
    self.set(index, value);
  }

  public static void getAt(LongList self, int index, long value) {
    self.set(index, value);
  }

  public static void getAt(FloatList self, int index, float value) {
    self.set(index, value);
  }

  public static void getAt(DoubleList self, int index, double value) {
    self.set(index, value);
  }

  public static void getAt(BooleanList self, int index, boolean value) {
    self.set(index, value);
  }
}
