package marcel.util.function;

import java.util.function.Function;

@FunctionalInterface
public interface Int2ObjectFunction <V> extends Function<Integer, V>, java.util.function.IntFunction <V> {
	/**
	 * {@inheritDoc}
	 *
	 * @since 8.0.0
	 */
	@Override
	V apply(int operand);

	@Override
	default V apply(Integer integer) {
		return apply(integer.intValue());
	}
}