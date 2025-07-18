package marcel.util.primitives.collections.sets;

import marcel.util.primitives.collections.FloatCollection;
import marcel.util.primitives.iterators.FloatIterator;
import marcel.util.primitives.spliterators.FloatSpliterator;
import marcel.util.primitives.spliterators.FloatSpliterators;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Set;

@NullMarked
public interface FloatSet extends FloatCollection, Set<Float> {
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
	FloatIterator iterator();
	/**
	 * Returns a type-specific spliterator on the elements of this set.
	 *
	 * <p>Set spliterators must report at least Spliterator#DISTINCT.
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
	 * <p>Additionally, it reports Spliterator#SIZED and Spliterator#DISTINCT.
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
	default FloatSpliterator spliterator() {
	 return FloatSpliterators.asSpliterator(
	   iterator(), size(), FloatSpliterators.SET_SPLITERATOR_CHARACTERISTICS);
	}
	/** Removes an element from this set.
	 *
	 * Note that the corresponding method of a type-specific collection is {@code rem()}.
	 * This unfortunate situation is caused by the clash
	 * with the similarly named index-based method in the {@link java.util.List} interface.
	 *
	 * @see java.util.Collection#remove(Object)
	 */
	boolean remove(float k);

	@Override
	default boolean removeFloat(float key) {
		return remove(key);
	}

	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	default boolean remove(@Nullable final Object o) {
	 return FloatCollection.super.remove(o);
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	default boolean add(final Float o) {
	 return FloatCollection.super.add(o);
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	default boolean contains(@Nullable final Object o) {
	 return FloatCollection.super.contains(o);
	}

	default FloatSet asUnmodifiable() {
		return new UnmodifiableFloatSet(this);
	}

	default FloatSet toUnmodifiable() {
		return new UnmodifiableFloatSet(new FloatOpenHashSet(this));
	}

	@Override
	default FloatSet leftShift(float[] value) {
		return (FloatSet) FloatCollection.super.leftShift(value);
	}

	@Override
	default FloatSet leftShift(float value) {
		return (FloatSet) FloatCollection.super.leftShift(value);
	}

	@Override
	default FloatSet leftShift(FloatCollection value) {
		return (FloatSet) FloatCollection.super.leftShift(value);
	}
}