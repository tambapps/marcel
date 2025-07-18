package marcel.util.primitives.collections.sets;

import marcel.util.primitives.collections.CharCollection;
import marcel.util.primitives.iterators.CharIterator;
import marcel.util.primitives.spliterators.CharSpliterator;
import marcel.util.primitives.spliterators.CharSpliterators;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Set;

@NullMarked
public interface CharSet extends CharCollection, Set<Character> {
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
	CharIterator iterator();
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
	default CharSpliterator spliterator() {
	 return CharSpliterators.asSpliterator(
	   iterator(), size(), CharSpliterators.SET_SPLITERATOR_CHARACTERISTICS);
	}
	/** Removes an element from this set.
	 *
	 * Note that the corresponding method of a type-specific collection is {@code rem()}.
	 * This unfortunate situation is caused by the clash
	 * with the similarly named index-based method in the {@link java.util.List} interface.
	 *
	 * @see java.util.Collection#remove(Object)
	 */
	boolean remove(char k);

	@Override
	default boolean removeChar(char key) {
		return remove(key);
	}

	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	default boolean remove(@Nullable final Object o) {
	 return CharCollection.super.remove(o);
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	default boolean add(final Character o) {
	 return CharCollection.super.add(o);
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	default boolean contains(@Nullable final Object o) {
	 return CharCollection.super.contains(o);
	}

	default CharSet asUnmodifiable() {
		return new UnmodifiableCharSet(this);
	}

	default CharSet toImmutable() {
		return new UnmodifiableCharSet(new CharOpenHashSet(this));
	}

	@Override
	default CharSet leftShift(char value) {
		return (CharSet) CharCollection.super.leftShift(value);
	}

	@Override
	default CharSet leftShift(char[] array) {
		return (CharSet) CharCollection.super.leftShift(array);
	}

	@Override
	default CharSet leftShift(String string) {
		return (CharSet) CharCollection.super.leftShift(string);
	}

	@Override
	default CharSet leftShift(CharCollection collection) {
		return (CharSet) CharCollection.super.leftShift(collection);
	}
}