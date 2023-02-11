package marcel.lang.util.function;

import java.util.function.Consumer;

@FunctionalInterface
public interface FloatConsumer extends Consumer<Float> {
	/**
	 * Performs this operation on the given input.
	 *
	 * @param t the input.
	 */
	void accept(float t);

	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead. */
	@Deprecated
	@Override
	default void accept(final Float t) {
	 this.accept(t.floatValue());
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
	default FloatConsumer andThen(final FloatConsumer after) {
	 return t -> { accept(t); after.accept(t); };
	}

	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead. */
	@Deprecated
	@Override
	default Consumer<Float> andThen(final Consumer<? super Float> after) {
	 return Consumer.super.andThen(after);
	}
}