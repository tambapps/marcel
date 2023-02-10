package marcel.lang.util;

import java.util.ArrayList;

public class Arrays {

  /** This is a safe value used by {@link ArrayList} (as of Java 7) to avoid
   *  throwing {@link OutOfMemoryError} on some JVMs. We adopt the same value. */
  public static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
  public static final int[] EMPTY_INT_ARRAY = new int[0];
  public static void ensureOffsetLength(int[] a, int offset, int length) {
    Arrays.ensureOffsetLength(a.length, offset, length);
  }

  public static void ensureOffsetLength(int arrayLength, int offset, int length) {
    if (offset < 0) {
      throw new ArrayIndexOutOfBoundsException("Offset (" + offset + ") is negative");
    } else if (length < 0) {
      throw new IllegalArgumentException("Length (" + length + ") is negative");
    } else if (offset + length > arrayLength) {
      throw new ArrayIndexOutOfBoundsException("Last index (" + (offset + length) + ") is greater than array length (" + arrayLength + ")");
    }
  }
}
