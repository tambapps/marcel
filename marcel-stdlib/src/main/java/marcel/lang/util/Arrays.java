package marcel.lang.util;

import java.util.ArrayList;

public class Arrays {

  /** This is a safe value used by {@link ArrayList} (as of Java 7) to avoid
   *  throwing {@link OutOfMemoryError} on some JVMs. We adopt the same value. */
  public static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
  public static final int[] EMPTY_INT_ARRAY = new int[0];
  public static final long[] EMPTY_LONG_ARRAY = new long[0];
  public static final float[] EMPTY_FLOAT_ARRAY = new float[0];
  public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
    public static final char[] EMPTY_CHARACTER_ARRAY = new char[0];

  public static void ensureOffsetLength(char[] a, int offset, int length) {
    Arrays.ensureOffsetLength(a.length, offset, length);
  }

  public static void ensureOffsetLength(int[] a, int offset, int length) {
    Arrays.ensureOffsetLength(a.length, offset, length);
  }

  public static void ensureOffsetLength(long[] a, int offset, int length) {
    Arrays.ensureOffsetLength(a.length, offset, length);
  }

  public static void ensureOffsetLength(float[] a, int offset, int length) {
    Arrays.ensureOffsetLength(a.length, offset, length);
  }

  public static void ensureOffsetLength(double[] a, int offset, int length) {
    Arrays.ensureOffsetLength(a.length, offset, length);
  }
  /** Forces an array to contain the given number of entries, preserving just a part of the array.
   *
   * @param array an array.
   * @param length the new minimum length for this array.
   * @param preserve the number of elements of the array that must be preserved in case a new allocation is necessary.
   * @return an array with {@code length} entries whose first {@code preserve}
   * entries are the same as those of {@code array}.
   */
  public static int[] forceCapacity(final int[] array, final int length, final int preserve) {
    final int t[] =
        new int[length];
    System.arraycopy(array, 0, t, 0, preserve);
    return t;
  }

  public static long[] forceCapacity(final long[] array, final int length, final int preserve) {
    final long t[] =
        new long[length];
    System.arraycopy(array, 0, t, 0, preserve);
    return t;
  }

  public static float[] forceCapacity(final float[] array, final int length, final int preserve) {
    final float t[] =
        new float[length];
    System.arraycopy(array, 0, t, 0, preserve);
    return t;
  }

  public static double[] forceCapacity(final double[] array, final int length, final int preserve) {
    final double t[] =
        new double[length];
    System.arraycopy(array, 0, t, 0, preserve);
    return t;
  }

  public static char[] forceCapacity(final char[] array, final int length, final int preserve) {
    final char t[] = new char[length];
    System.arraycopy(array, 0, t, 0, preserve);
    return t;
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

  /**
   * Ensures that a range given by its first (inclusive) and last (exclusive) elements fits an array
   * of given length.
   *
   * <p>
   * This method may be used whenever an array range check is needed.
   *
   * <p>
   * In Java 9 and up, this method should be considered deprecated in favor of the
   * {@link java.util.Objects#checkFromToIndex(int, int, int)} method, which may be intrinsified in
   * recent JVMs.
   *
   * @param arrayLength an array length (must be nonnegative).
   * @param from a start index (inclusive).
   * @param to an end index (inclusive).
   * @throws IllegalArgumentException if {@code from} is greater than {@code to}.
   * @throws ArrayIndexOutOfBoundsException if {@code from} or {@code to} are greater than
   *             {@code arrayLength} or negative.
   *
   * An {@code assert} checks whether {@code arrayLength} is nonnegative.
   */
  public static void ensureFromTo(final int arrayLength, final int from, final int to) {
    assert arrayLength >= 0;
    // When Java 9 becomes the minimum, use Objects#checkFromToIndex​​, as that can be an intrinsic
    if (from < 0) throw new ArrayIndexOutOfBoundsException("Start index (" + from + ") is negative");
    if (from > to) throw new IllegalArgumentException("Start index (" + from + ") is greater than end index (" + to + ")");
    if (to > arrayLength) throw new ArrayIndexOutOfBoundsException("End index (" + to + ") is greater than array length (" + arrayLength + ")");
  }
}
