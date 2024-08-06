package marcel.lang.primitives.collections.sets;

import marcel.lang.primitives.collections.DoubleCollection;
import marcel.lang.primitives.collections.lists.DoubleList;
import marcel.lang.primitives.iterators.DoubleIterator;
import marcel.lang.primitives.spliterators.DoubleSpliterator;
import marcel.lang.primitives.spliterators.DoubleSpliterators;

import java.util.Set;

public interface DoubleSet extends DoubleCollection, Set<Double> {
	/** Returns a type-specific iterator on the elements of this set.
	 *
	 * This specification strengthens the one given in {@link Iterable#iterator()},
	 * which was already strengthened in the corresponding type-specific class,
	 * but was weakened by the fact that this interface extends {@link Set}.
	 * <p>Also, this is generally the only {@code iterator} method subclasses should override.
	 *
	 * @return a type-specific iterator on the elements of this set.
	 */
	@Override
	DoubleIterator iterator();
	/**
	 * Returns a type-specific spliterator on the elements of this set.
	 *
	 *
	 * <p>See {@link Set#spliterator()} for more documentation on the requirements
	 * of the returned spliterator.
	 *
	 * This specification strengthens the one given in
	 * {@link java.util.Collection#spliterator()}, which was already
	 * strengthened in the corresponding type-specific class,
	 * but was weakened by the fact that this interface extends {@link Set}.
	 * <p>Also, this is generally the only {@code spliterator} method subclasses should override.
	 *
	 * The default implementation returns a late-binding spliterator (see
	 * Spliterator for documentation on what binding policies mean)
	 * that wraps this instance's type specific {@link #iterator}.
	 *
	 * As this default implementation wraps the iterator, and {@link java.util.Iterator}
	 * is an inherently linear API, the returned spliterator will yield limited performance gains
	 * when run in parallel contexts, as the returned spliterator's
	 *
	 * @return {@inheritDoc}
	 * @since 8.5.0
	 */
	@Override
	default DoubleSpliterator spliterator() {
	 return DoubleSpliterators.asSpliterator(
	   iterator(), size(), DoubleSpliterators.SET_SPLITERATOR_CHARACTERISTICS);
	}
	/** Removes an element from this set.
	 *
	 * Note that the corresponding method of a type-specific collection is {@code rem()}.
	 * This unfortunate situation is caused by the clash
	 * with the similarly named index-based method in the {@link java.util.List} interface.
	 *
	 * @see java.util.Collection#remove(Object)
	 */
	boolean remove(double k);

	@Override
	default boolean removeDouble(double key) {
		return remove(key);
	}

	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	default boolean remove(final Object o) {
	 return DoubleCollection.super.remove(o);
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	default boolean add(final Double o) {
	 return DoubleCollection.super.add(o);
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	default boolean contains(final Object o) {
	 return DoubleCollection.super.contains(o);
	}

	default DoubleSet asUnmodifiable() {
		return new UnmodifiableDoubleSet(this);
	}

	default DoubleSet toImmutable() {
		return new UnmodifiableDoubleSet(new DoubleOpenHashSet(this));
	}

	@Override
	default DoubleSet leftShift(double value) {
		return (DoubleSet) DoubleCollection.super.leftShift(value);
	}

	@Override
	default DoubleSet leftShift(double[] value) {
		return (DoubleSet) DoubleCollection.super.leftShift(value);
	}

	@Override
	default DoubleSet leftShift(DoubleCollection value) {
		return (DoubleSet) DoubleCollection.super.leftShift(value);
	}
}