package marcel.lang.runtime;

import it.unimi.dsi.fastutil.booleans.BooleanOpenHashSet;
import it.unimi.dsi.fastutil.booleans.BooleanSet;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.doubles.DoubleOpenHashSet;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.floats.FloatOpenHashSet;
import it.unimi.dsi.fastutil.floats.FloatSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import marcel.lang.primitives.collections.sets.IntOpenHashSet;
import marcel.lang.primitives.collections.sets.IntSet;
import marcel.lang.primitives.collections.sets.LongOpenHashSet;
import marcel.lang.primitives.collections.sets.LongSet;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
  public static BooleanSet createSet(boolean[] array) {
    return new BooleanOpenHashSet(array);
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
  public static Float2ObjectMap<?> newFloat2ObjectMap() {
    return new Float2ObjectOpenHashMap<>();
  }
  public static Double2ObjectMap<?> newDouble2ObjectMap() {
    return new Double2ObjectOpenHashMap<>();
  }
  public static Map<?, ?> newObject2ObjectMap() {
    return new HashMap<>();
  }

  public static boolean objectsEqual(Object left, Object right) {
    if (left == right) return true;
    if (left == null) return right == null;
    if (right == null) return left == null;

    return left.equals(right);

    // TODO handle arrays, handle list with arrays comparison
  }
}
