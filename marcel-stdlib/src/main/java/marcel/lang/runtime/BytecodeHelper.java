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
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

public final class BytecodeHelper {

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
}
