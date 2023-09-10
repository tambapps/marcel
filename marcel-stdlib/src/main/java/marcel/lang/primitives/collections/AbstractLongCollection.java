package marcel.lang.primitives.collections;

import marcel.lang.primitives.iterators.LongIterator;
import marcel.lang.util.Arrays;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.function.LongConsumer;
import java.util.function.LongPredicate;

public abstract class AbstractLongCollection extends AbstractCollection<Long> implements LongCollection {
	protected AbstractLongCollection() {}
	@Override
	public abstract LongIterator iterator();
	/** {@inheritDoc}
	 *
	 * This implementation always throws an {@link UnsupportedOperationException}.
	 */
	@Override
	public boolean add(final long k) {
	 throw new UnsupportedOperationException();
	}
	/** {@inheritDoc}
	 *
	 * This implementation iterates over the elements in the collection,
	 * looking for the specified element.
	 */
	@Override
	public boolean contains(final long k) {
	 final LongIterator iterator = iterator();
    while (iterator.hasNext()) {
      if (k == iterator.nextLong()) {
        return true;
      }
    }
	 return false;
	}

	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public boolean add(final Long key) {
	 return LongCollection.super.add(key);
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public boolean contains(final Object key) {
	 return LongCollection.super.contains(key);
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public boolean remove(final Object key) {
	 return LongCollection.super.remove(key);
	}
	@Override
	public long[] toArray(long a[]) {
	 final int size = size();
	 if (a == null) {
	  a = new long[size];
	 } else if (a.length < size) {
	  a = java.util.Arrays.copyOf(a, size);
	 }
	 iterator().unwrap(a);
	 return a;
	}
	@Override
	public long[] toLongArray() {
		final int size = size();
		if (size == 0) {
			return Arrays.EMPTY_LONG_ARRAY;
		}
		final long[] a = new long[size];
		iterator().unwrap(a);
		return a;
	}
	/** {@inheritDoc}
	 *
	 * This method exists to make final what should have been final in the interface.
	 */
	@Override
	public void forEach(final LongConsumer action) {
	 LongCollection.super.forEach(action);
	}
	/** {@inheritDoc}
	 *
	 * This method exists to make final what should have been final in the interface.
	 */
	@Override
	public boolean removeIf(final LongPredicate filter) {
	 return LongCollection.super.removeIf(filter);
	}
	@Override
	public boolean addAll(final LongCollection c) {
	 boolean retVal = false;
	 for(final LongIterator i = c.iterator(); i.hasNext();)
     if (add(i.nextLong())) {
       retVal = true;
     }
	 return retVal;
	}
	/** {@inheritDoc}
	 *
	 * This implementation delegates to the type-specific version if given a type-specific
	 * collection, otherwise is uses the implementation from {@link AbstractCollection}.
	 */
	@Override
	public boolean addAll(final Collection<? extends Long> c) {
	 if (c instanceof LongCollection) {
	  return addAll((LongCollection) c);
	 }
	 return super.addAll(c);
	}
	@Override
	public boolean containsAll(final LongCollection c) {
	 for(final LongIterator i = c.iterator(); i.hasNext();)
     if (!contains(i.nextLong())) {
       return false;
     }
	 return true;
	}
	/** {@inheritDoc}
	 *
	 * This implementation delegates to the type-specific version if given a type-specific
	 * collection, otherwise is uses the implementation from {@link AbstractCollection}.
	 */
	@Override
	public boolean containsAll(final Collection<?> c) {
	 if (c instanceof LongCollection) {
	  return containsAll((LongCollection) c);
	 }
	 return super.containsAll(c);
	}
	@Override
	public boolean removeAll(final LongCollection c) {
	 boolean retVal = false;
	 for(final LongIterator i = c.iterator(); i.hasNext();)
     if (removeLong(i.nextLong())) {
       retVal = true;
     }
	 return retVal;
	}
	/** {@inheritDoc}
	 *
	 * This implementation delegates to the type-specific version if given a type-specific
	 * collection, otherwise is uses the implementation from {@link AbstractCollection}.
	 */
	@Override
	public boolean removeAll(final Collection<?> c) {
	 if (c instanceof LongCollection) {
	  return removeAll((LongCollection) c);
	 }
	 return super.removeAll(c);
	}
	@Override
	public boolean retainAll(final LongCollection c) {
	 boolean retVal = false;
	 for(final LongIterator i = iterator(); i.hasNext();)
	  if (! c.contains(i.nextLong())) {
	   i.remove();
	   retVal = true;
	  }
	 return retVal;
	}
	/** {@inheritDoc}
	 *
	 * This implementation delegates to the type-specific version if given a type-specific
	 * collection, otherwise is uses the implementation from {@link AbstractCollection}.
	 */
	@Override
	public boolean retainAll(final Collection<?> c) {
	 if (c instanceof LongCollection) {
	  return retainAll((LongCollection) c);
	 }
	 return super.retainAll(c);
	}
	@Override
	public String toString() {
	 final StringBuilder s = new StringBuilder();
	 final LongIterator i = iterator();
	 int n = size();
	 long k;
	 boolean first = true;
	 s.append("{");
	 while(n-- != 0) {
     if (first) {
       first = false;
     } else {
       s.append(", ");
     }
	  k = i.nextLong();
	   s.append(String.valueOf(k));
	 }
	 s.append("}");
	 return s.toString();
	}
}
