package marcel.lang.methods;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.booleans.BooleanSet;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.floats.FloatSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongSet;
import marcel.lang.IntRange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class DefaultMarcelMethods {

  public static String join(List<?> self, String separator) {
    return self.stream()
        .map(String::valueOf)
        .collect(Collectors.joining(separator));
  }

  public static <T, U extends Comparable> void sort(List<T> self, Function<T, U> keyExtractor) {
    self.sort(Comparator.comparing(keyExtractor));
  }

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

  // last
  public static <T> T getLast(List<T> self) {
    return self.get(self.size() - 1);
  }

  public static int getLast(IntList self) {
    return self.getInt(self.size() - 1);
  }

  public static long getLast(LongList self) {
    return self.getLong(self.size() - 1);
  }

  public static float getLast(FloatList self) {
    return self.getFloat(self.size() - 1);
  }

  public static double getLast(DoubleList self) {
    return self.getDouble(self.size() - 1);
  }

  public static boolean getLast(BooleanList self) {
    return self.getBoolean(self.size() - 1);
  }

  // lastOrNull
  public static <T> T getLastOrNull(List<T> self) {
    if (self.isEmpty()) return null;
    return self.get(self.size() - 1);
  }

  // setLast
  public static void setLast(IntList self, int value) {
    self.set(self.size() - 1, value);
  }

  public static void setLast(LongList self, long value) {
    self.set(self.size() - 1, value);
  }

  public static void setLast(FloatList self, float value) {
    self.set(self.size() - 1, value);
  }

  public static void setLast(DoubleList self, double value) {
    self.set(self.size() - 1, value);
  }

  public static void setLast(BooleanList self, boolean value) {
    self.set(self.size() - 1, value);
  }
  public static <T> void setLast(List<T> self, T value) {
    self.set(self.size() - 1, value);
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

  // getAt range
  public static <T> List<T> getAt(List<T> self, IntRange range) {
    List<T> subList = new ArrayList<>();
    for (Integer integer : range) subList.add(self.get(integer));
    return subList;
  }

  public static IntList getAt(IntList self, IntRange range) {
    IntList subList = new IntArrayList();
    for (Integer integer : range) subList.add(self.getInt(integer));
    return subList;
  }

  public static LongList getAt(LongList self, IntRange range) {
    LongList subList = new LongArrayList();
    for (Integer integer : range) subList.add(self.getLong(integer));
    return subList;
  }

  public static FloatList getAt(FloatList self, IntRange range) {
    FloatList subList = new FloatArrayList();
    for (Integer integer : range) subList.add(self.getFloat(integer));
    return subList;
  }

  public static DoubleList getAt(DoubleList self, IntRange range) {
    DoubleList subList = new DoubleArrayList();
    for (Integer integer : range) subList.add(self.getDouble(integer));
    return subList;
  }

  public static BooleanList getAt(BooleanList self, IntRange range) {
    BooleanList subList = new BooleanArrayList();
    for (Integer integer : range) subList.add(self.getBoolean(integer));
    return subList;
  }

  // putAt
  public static <T> void putAt(List<T> self, int index, T value) {
    self.set(index, value);
  }

  public static void putAt(IntList self, int index, int value) {
    self.set(index, value);
  }

  public static void putAt(LongList self, int index, long value) {
    self.set(index, value);
  }

  public static void putAt(FloatList self, int index, float value) {
    self.set(index, value);
  }

  public static void putAt(DoubleList self, int index, double value) {
    self.set(index, value);
  }

  public static void putAt(BooleanList self, int index, boolean value) {
    self.set(index, value);
  }
}
