package marcel.lang.runtime;

import marcel.lang.primitives.collections.sets.CharacterOpenHashSet;
import marcel.lang.primitives.collections.sets.CharacterSet;
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

// methods are used by the compiler
public final class BytecodeHelper {

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
  public static CharacterSet createSet(char[] array) {
    return new CharacterOpenHashSet(array);
  }

  public static Set<?> createSet(Object array) {
    int length = Array.getLength(array);
    Set<Object> set = new HashSet<>(length);
    for (int i = 0; i < length; i++) {
      set.add(Array.get(array, i));
    }
    return set;
  }

  // TODO remove this. just smart compare in semantic analysis
  public static boolean objectsEqual(Object left, Object right) {
    if (left == right) return true;
    if (left == null) return false;
    if (right == null) return false;
    if (left.getClass().isArray() && right.getClass().isArray()) {
      int length = Array.getLength(left);
      if (length != Array.getLength(right)) return false;
      for (int i = 0; i < length; i++) {
        if (!objectsEqual(Array.get(left, i), Array.get(right, i))) return false;
      }
      return true;
    }
    return left.equals(right);
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
