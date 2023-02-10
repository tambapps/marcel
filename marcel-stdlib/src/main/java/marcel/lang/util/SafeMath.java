package marcel.lang.util;

public final class SafeMath {
	private SafeMath() {}

	public static char safeIntToChar(final int value) {
		if (value < Character.MIN_VALUE || Character.MAX_VALUE < value) throw new IllegalArgumentException(value + " can't be represented as char");
		return (char) value;
	}

	public static byte safeIntToByte(final int value) {
		if (value < Byte.MIN_VALUE || Byte.MAX_VALUE < value) throw new IllegalArgumentException(value + " can't be represented as byte (out of range)");
		return (byte) value;
	}

	public static short safeIntToShort(final int value) {
		if (value < Short.MIN_VALUE || Short.MAX_VALUE < value) throw new IllegalArgumentException(value + " can't be represented as short (out of range)");
		return (short) value;
	}

	public static char safeLongToChar(final long value) {
		if (value < Character.MIN_VALUE || Character.MAX_VALUE < value ) throw new IllegalArgumentException(value + " can't be represented as int (out of range)");
		return (char) value;
	}

	public static byte safeLongToByte(final long value) {
		if (value < Byte.MIN_VALUE || Byte.MAX_VALUE < value ) throw new IllegalArgumentException(value + " can't be represented as int (out of range)");
		return (byte) value;
	}

	public static short safeLongToShort(final long value) {
		if (value < Short.MIN_VALUE || Short.MAX_VALUE < value ) throw new IllegalArgumentException(value + " can't be represented as int (out of range)");
		return (short) value;
	}

	public static int safeLongToInt(final long value) {
		if (value < Integer.MIN_VALUE || Integer.MAX_VALUE < value ) throw new IllegalArgumentException(value + " can't be represented as int (out of range)");
		return (int) value;
	}

	public static float safeDoubleToFloat(final double value) {
		if (Double.isNaN(value)) return Float.NaN;
		if (Double.isInfinite(value)) return value < 0.0d ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
		if (value < -Float.MAX_VALUE || Float.MAX_VALUE < value) throw new IllegalArgumentException(value + " can't be represented as float (out of range)");
		final float floatValue = (float) value;
		if (floatValue != value) throw new IllegalArgumentException(value + " can't be represented as float (imprecise)");
		return floatValue;
	}
}