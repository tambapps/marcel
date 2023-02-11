package marcel.lang.util.function;

import java.util.function.Function;

@FunctionalInterface
public interface Float2ObjectFunction<V> extends Function<Float, V>, FloatFunction <V> {
	/**
	 * {@inheritDoc}
	 *
	 * @since 8.0.0
	 */
	@Override
	V apply(float operand);

	@Override
	default V apply(Float f) {
		return apply(f.floatValue());
	}
}