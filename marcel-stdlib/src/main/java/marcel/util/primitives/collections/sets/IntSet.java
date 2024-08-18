package marcel.util.primitives.collections.sets;

import marcel.util.primitives.collections.IntCollection;
import marcel.util.primitives.iterators.IntIterator;
import marcel.util.primitives.spliterators.IntSpliterator;
import marcel.util.primitives.spliterators.IntSpliterators;

import java.util.Set;

public interface IntSet extends IntCollection, Set<Integer> {
	/** Returns a type-specific iterator on the elements of this set.
	 *
	 * This specification strengthens the one given in {@link java.lang.Iterable#iterator()},
	 * which was already strengthened in the corresponding type-specific class,
	 * but was weakened by the fact that this interface extends {@link Set}.
	 * <p>Also, this is generally the only {@code iterator} method subclasses should override.
	 *
	 * @return a type-specific iterator on the elements of this set.
	 */
	@Override
	IntIterator iterator();
	/**
	 * Returns a type-specific spliterator on the elements of this set.
	 *
	 *
	 * <p>See {@link java.util.Set#spliterator()} for more documentation on the requirements
	 * of the returned spliterator.
	 *
	 * This specification strengthens the one given in
	 * {@link java.util.Collection#spliterator()}, which was already
	 * strengthened in the corresponding type-specific class,
	 * but was weakened by the fact that this interface extends {@link Set}.
	 * <p>Also, this is generally the only {@code spliterator} method subclasses should override.
	 *
	 * The default implementation returns a late-binding spliterator (see
	 * that wraps this instance's type specific {@link #iterator}.
	 *
	 * As this default implementation wraps the iterator, and {@link java.util.Iterator}
	 * is an inherently linear API, the returned spliterator will yield limited performance gains
	 * when run in parallel contexts, as the returned spliterator's
	 * Spliterator#trySplit() will have linear runtime.
	 *
	 * @return {@inheritDoc}
	 * @since 8.5.0
	 */
	@Override
	default IntSpliterator spliterator() {
	 return IntSpliterators.asSpliterator(
	   iterator(), size(), IntSpliterators.SET_SPLITERATOR_CHARACTERISTICS);
	}
	/** Removes an element from this set.
	 *
	 * Note that the corresponding method of a type-specific collection is {@code rem()}.
	 * This unfortunate situation is caused by the clash
	 * with the similarly named index-based method in the {@link java.util.List} interface.
	 *
	 * @see java.util.Collection#remove(Object)
	 */
	boolean remove(int k);

	@Override
	default boolean removeInt(int key) {
		return remove(key);
	}

	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	default boolean remove(final Object o) {
	 return IntCollection.super.remove(o);
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	default boolean add(final Integer o) {
	 return IntCollection.super.add(o);
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	default boolean contains(final Object o) {
	 return IntCollection.super.contains(o);
	}


	default IntSet asUnmodifiable() {
		return new UnmodifiableIntSet(this);
	}

	default IntSet toImmutable() {
		return new UnmodifiableIntSet(new IntOpenHashSet(this));
	}

	@Override
	default IntSet leftShift(int value) {
		return (IntSet) IntCollection.super.leftShift(value);
	}

	@Override
	default IntSet leftShift(int[] array) {
		return (IntSet)  IntCollection.super.leftShift(array);
	}

	@Override
	default IntSet leftShift(IntCollection value) {
		return (IntSet)  IntCollection.super.leftShift(value);
	}
}