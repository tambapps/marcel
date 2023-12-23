package marcel.lang.runtime;

import marcel.lang.MarcelTruth;
import marcel.lang.primitives.collections.sets.CharOpenHashSet;
import marcel.lang.primitives.collections.sets.CharSet;
import marcel.lang.primitives.collections.sets.DoubleOpenHashSet;
import marcel.lang.primitives.collections.sets.DoubleSet;
import marcel.lang.primitives.collections.sets.FloatOpenHashSet;
import marcel.lang.primitives.collections.sets.FloatSet;
import marcel.lang.primitives.collections.sets.IntOpenHashSet;
import marcel.lang.primitives.collections.sets.IntSet;
import marcel.lang.primitives.collections.sets.LongOpenHashSet;
import marcel.lang.primitives.collections.sets.LongSet;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

/**
 * Helpful methods used by the compiler
 */
public final class BytecodeHelper {

  // Object parameter to handle all kinds of arrays. Object[], char[][], etc...
  // list
  public static List<?> createList(Object array) {
    int length = Array.getLength(array);
    List<Object> list = new ArrayList<>(length);
    for (int i = 0; i < length; i++) {
      list.add(Array.get(array, i));
    }
    return list;
  }

  // sets
  public static IntSet createSet(int[] array) {
    return new IntOpenHashSet(array);
  }
  public static LongSet createSet(long[] array) {
    return new LongOpenHashSet(array);
  }
  public static FloatSet createSet(float[] array) {
    return new FloatOpenHashSet(array);
  }
  public static DoubleSet createSet(double[] array) {
    return new DoubleOpenHashSet(array);
  }
  public static CharSet createSet(char[] array) {
    return new CharOpenHashSet(array);
  }

  // Object parameter to handle all kinds of arrays. Object[], char[][], etc...
  public static Set<?> createSet(Object array) {
    int length = Array.getLength(array);
    Set<Object> set = new HashSet<>(length);
    for (int i = 0; i < length; i++) {
      set.add(Array.get(array, i));
    }
    return set;
  }

  public static <T> T elvisThrow(T o, Throwable throwable) throws Throwable {
    if (!MarcelTruth.isTruthy(o)) throw throwable;
    return o;
  }

  public static Integer orElseNull(OptionalInt opt) {
    return opt.isPresent() ? opt.getAsInt() : null;
  }

  public static Long orElseNull(OptionalLong opt) {
    return opt.isPresent() ? opt.getAsLong() : null;
  }

  public static Double orElseNull(OptionalDouble opt) {
    return opt.isPresent() ? opt.getAsDouble() : null;
  }
}
