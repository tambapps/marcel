package marcel.util.function;

import java.util.function.Function;

@FunctionalInterface
public interface Char2ObjectFunction<V> extends Function<Character, V>, CharFunction<V> {
	/**
	 * {@inheritDoc}
	 *
	 * @since 8.0.0
	 */
	@Override
	V apply(char operand);

	@Override
	default V apply(Character f) {
		return apply(f.charValue());
	}
}