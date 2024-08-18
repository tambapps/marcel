package marcel.util.primitives.iterators.list;


import marcel.util.primitives.iterators.CharIterator;

import java.util.ListIterator;

public interface CharListIterator extends CharIterator, ListIterator<Character> {

	default Character next() {
		return nextChar();
	}

	/**
	 * Replaces the last element returned by {@link #next} or
	 * {@link #previous} with the specified element (optional operation).
	 * @param k the element used to replace the last element returned.
	 *
	 * <p>This default implementation just throws an {@link UnsupportedOperationException}.
	 * @see ListIterator#set(Object)
	 */
	default void set(final char k) { throw new UnsupportedOperationException(); }
	/**
	 * Inserts the specified element into the list (optional operation).
	 *
	 * <p>This default implementation just throws an {@link UnsupportedOperationException}.
	 * @param k the element to insert.
	 * @see ListIterator#add(Object)
	 */
	default void add(final char k) { throw new UnsupportedOperationException(); }
	/**
	 * Removes from the underlying collection the last element returned
	 * by this iterator (optional operation).
	 *
	 * <p>This default implementation just throws an {@link UnsupportedOperationException}.
	 * @see ListIterator#remove()
	 */
	@Override
	default void remove() { throw new UnsupportedOperationException(); }
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead. */
	@Deprecated
	@Override
	default void set(final Character k) { set(k.charValue()); }
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead. */
	@Deprecated
	@Override
	default void add(final Character k) { add(k.charValue()); }

	char previousChar();

	default Character previous() {
		return previousChar();
	}
}