package marcel.lang.util.function;

import java.util.function.Consumer;

@FunctionalInterface
public interface CharacterConsumer extends Consumer<Character> {
	/**
	 * Performs this operation on the given input.
	 *
	 * @param t the input.
	 */
	void accept(char t);

	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead. */
	@Deprecated
	@Override
	default void accept(final Character t) {
	 this.accept(t.charValue());
	}
	/**
	 * Returns a composed type-specific consumer that performs, in sequence, this
	 * operation followed by the {@code after} operation.
	 * @param after the operation to perform after this operation.
	 * @return a composed {@code Consumer} that performs in sequence this
	 * operation followed by the {@code after} operation.
	 * @see Consumer#andThen
	 * @apiNote Implementing classes should generally override this method and 
	 * keep the default implementation of the other overloads, which will 
	 * delegate to this method (after proper conversions).
	 */
	default CharacterConsumer andThen(final CharacterConsumer after) {
	 return t -> { accept(t); after.accept(t); };
	}

	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead. */
	@Deprecated
	@Override
	default Consumer<Character> andThen(final Consumer<? super Character> after) {
	 return Consumer.super.andThen(after);
	}
}