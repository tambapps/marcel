package marcel.lang.primitives.collections.sets;

import marcel.lang.primitives.collections.IntCollection;
import marcel.lang.primitives.collections.lists.IntArrayList;
import marcel.lang.primitives.collections.lists.IntList;
import marcel.lang.primitives.iterators.IntIterator;
import marcel.lang.primitives.spliterators.IntSpliterator;
import marcel.lang.primitives.spliterators.IntSpliterators;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.IntPredicate;

public interface IntSet extends IntCollection, Set<Integer> {
	/** Returns a type-specific iterator on the elements of this set.
	 *
	 * @apiNote This specification strengthens the one given in {@link java.lang.Iterable#iterator()},
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
	 * <p>Set spliterators must report at least {@link Spliterator#DISTINCT}.
	 *
	 * <p>See {@link java.util.Set#spliterator()} for more documentation on the requirements
	 * of the returned spliterator.
	 *
	 * @apiNote This specification strengthens the one given in
	 * {@link java.util.Collection#spliterator()}, which was already
	 * strengthened in the corresponding type-specific class,
	 * but was weakened by the fact that this interface extends {@link Set}.
	 * <p>Also, this is generally the only {@code spliterator} method subclasses should override.
	 *
	 * @implSpec The default implementation returns a late-binding spliterator (see
	 * {@link Spliterator} for documentation on what binding policies mean)
	 * that wraps this instance's type specific {@link #iterator}.
	 * <p>Additionally, it reports {@link Spliterator#SIZED} and {@link Spliterator#DISTINCT}.
	 *
	 * @implNote As this default implementation wraps the iterator, and {@link java.util.Iterator}
	 * is an inherently linear API, the returned spliterator will yield limited performance gains
	 * when run in parallel contexts, as the returned spliterator's
	 * {@link Spliterator#trySplit() trySplit()} will have linear runtime.
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
	 * @apiNote Note that the corresponding method of a type-specific collection is {@code rem()}.
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
}