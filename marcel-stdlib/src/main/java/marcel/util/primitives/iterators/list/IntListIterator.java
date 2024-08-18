package marcel.util.primitives.iterators.list;

import marcel.util.primitives.iterators.IntIterator;

import java.util.ListIterator;

public interface IntListIterator extends IntIterator, ListIterator<Integer> {

	default Integer next() {
		return nextInt();
	}

	/**
	 * Replaces the last element returned by {@link #next} or
	 * {@link #previous} with the specified element (optional operation).
	 * @param k the element used to replace the last element returned.
	 *
	 * <p>This default implementation just throws an {@link UnsupportedOperationException}.
	 * @see ListIterator#set(Object)
	 */
	default void set(final int k) { throw new UnsupportedOperationException(); }
	/**
	 * Inserts the specified element into the list (optional operation).
	 *
	 * <p>This default implementation just throws an {@link UnsupportedOperationException}.
	 * @param k the element to insert.
	 * @see ListIterator#add(Object)
	 */
	default void add(final int k) { throw new UnsupportedOperationException(); }
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
	default void set(final Integer k) { set(k.intValue()); }
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead. */
	@Deprecated
	@Override
	default void add(final Integer k) { add(k.intValue()); }

	int previousInt();

	default Integer previous() {
		return previousInt();
	}
}