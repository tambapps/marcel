package marcel.lang.primitives.floats;

import java.util.function.Predicate;

@FunctionalInterface
public interface FloatPredicate extends Predicate<Float> {
	/**
	 * Evaluates this predicate on the given input.
	 *
	 * @param t the input.
	 * @return {@code true} if the input matches the predicate,
	 * otherwise {@code false}
	 */
	boolean test(float t);

	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead. */
	@Deprecated
	@Override
	default boolean test(final Float t) {
	 return test(t.floatValue());
	}
	   /**
	 * Returns a composed type-specific predicate that represents a short-circuiting logical
	 * AND of this type-specific predicate and another.
	 * @param other a predicate that will be logically-ANDed with this predicate.
	    * @return a composed predicate that represents the short-circuiting logical
	    * AND of this predicate and the {@code other} predicate.
	 * @see Predicate#and
	 * @apiNote Implementing classes should generally override this method and 
	 * keep the default implementation of the other overloads, which will 
	 * delegate to this method (after proper conversions).
	 */
	default FloatPredicate and(final FloatPredicate other) {
	 return t -> test(t) && other.test(t);
	}

	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead. */
	@Deprecated
	@Override
	default Predicate<Float> and(final Predicate<? super Float> other) {
	 return Predicate.super.and(other);
	}
	@Override
	/** {@inheritDoc} */
	default FloatPredicate negate() {
	 return t -> ! test(t);
	}
	   /**
	 * Returns a composed type-specific predicate that represents a short-circuiting logical
	 * OR of this type-specific predicate and another. 
	 * @param other a predicate that will be logically-ORed with this predicate.
	 * @return a composed predicate that represents the short-circuiting logical
	 * OR of this predicate and the {@code other} predicate.
	 * @see Predicate#or
	 * @apiNote Implementing classes should generally override this method and 
	 * keep the default implementation of the other overloads, which will 
	 * delegate to this method (after proper conversions).
	 */
	default FloatPredicate or(final FloatPredicate other) {
	 return t -> test(t) || other.test(t);
	}

	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead. */
	@Deprecated
	@Override
	default Predicate<Float> or(final Predicate<? super Float> other) {
	 return Predicate.super.or(other);
	}
}