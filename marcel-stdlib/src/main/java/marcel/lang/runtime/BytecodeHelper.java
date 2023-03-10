package marcel.lang.runtime;

import marcel.lang.primitives.collections.maps.Character2ObjectMap;
import marcel.lang.primitives.collections.maps.Character2ObjectOpenHashMap;
import marcel.lang.primitives.collections.maps.Int2ObjectMap;
import marcel.lang.primitives.collections.maps.Int2ObjectOpenHashMap;
import marcel.lang.primitives.collections.maps.Long2ObjectMap;
import marcel.lang.primitives.collections.maps.Long2ObjectOpenHashMap;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

  // maps
  public static Int2ObjectMap<?> newInt2ObjectMap() {
    return new Int2ObjectOpenHashMap<>();
  }
  public static Long2ObjectMap<?> newLong2ObjectMap() {
    return new Long2ObjectOpenHashMap<>();
  }
  public static Character2ObjectMap<?> newChar2ObjectMap() {
    return new Character2ObjectOpenHashMap<>();
  }
  public static Map<?, ?> newObject2ObjectMap() {
    return new HashMap<>();
  }

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
}
