package marcel.lang.runtime;

import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class PrimitiveKeyMaps {

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
