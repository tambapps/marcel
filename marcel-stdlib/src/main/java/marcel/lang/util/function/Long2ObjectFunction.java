package marcel.lang.util.function;

import java.util.function.Function;

@FunctionalInterface
public interface Long2ObjectFunction<V> extends Function<Long, V>, java.util.function.LongFunction <V> {
	/**
	 * {@inheritDoc}
	 *
	 * @since 8.0.0
	 */
	@Override
	V apply(long operand);

	@Override
	default V apply(Long l) {
		return apply(l.longValue());
	}
}