package marcel.lang.primitives.iterators.list;


import marcel.lang.primitives.iterators.FloatIterator;

import java.util.ListIterator;

public interface FloatListIterator extends FloatIterator, ListIterator<Float> {

	default Float next() {
		return nextFloat();
	}

	/**
	 * Replaces the last element returned by {@link #next} or
	 * {@link #previous} with the specified element (optional operation).
	 * @param k the element used to replace the last element returned.
	 *
	 * <p>This default implementation just throws an {@link UnsupportedOperationException}.
	 * @see ListIterator#set(Object)
	 */
	default void set(final float k) { throw new UnsupportedOperationException(); }
	/**
	 * Inserts the specified element into the list (optional operation).
	 *
	 * <p>This default implementation just throws an {@link UnsupportedOperationException}.
	 * @param k the element to insert.
	 * @see ListIterator#add(Object)
	 */
	default void add(final float k) { throw new UnsupportedOperationException(); }
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
	default void set(final Float k) { set(k.floatValue()); }
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead. */
	@Deprecated
	@Override
	default void add(final Float k) { add(k.floatValue()); }

	float previousFloat();

	default Float previous() {
		return previousFloat();
	}
}