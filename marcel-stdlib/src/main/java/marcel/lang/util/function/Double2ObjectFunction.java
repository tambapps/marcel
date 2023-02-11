package marcel.lang.util.function;

import java.util.function.Function;

@FunctionalInterface
public interface Double2ObjectFunction<V> extends Function<Double, V>, java.util.function.DoubleFunction <V> {
	/**
	 * {@inheritDoc}
	 *
	 * @since 8.0.0
	 */
	@Override
	V apply(double operand);

	@Override
	default V apply(Double d) {
		return apply(d.doubleValue());
	}
}