package marcel.lang.primitives.collections;

import marcel.lang.primitives.iterators.IntIterator;
import marcel.lang.util.Arrays;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;

public abstract class AbstractIntCollection extends AbstractCollection<Integer> implements IntCollection {
	protected AbstractIntCollection() {}
	@Override
	public abstract IntIterator iterator();
	/** {@inheritDoc}
	 *
	 * This implementation always throws an {@link UnsupportedOperationException}.
	 */
	@Override
	public boolean add(final int k) {
	 throw new UnsupportedOperationException();
	}
	/** {@inheritDoc}
	 *
	 * This implementation iterates over the elements in the collection,
	 * looking for the specified element.
	 */
	@Override
	public boolean contains(final int k) {
	 final IntIterator iterator = iterator();
    while (iterator.hasNext()) {
      if (k == iterator.nextInt()) {
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
	public boolean add(final Integer key) {
	 return IntCollection.super.add(key);
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public boolean contains(final Object key) {
	 return IntCollection.super.contains(key);
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public boolean remove(final Object key) {
	 return IntCollection.super.remove(key);
	}
	@Override
	public int[] toArray(int a[]) {
	 final int size = size();
	 if (a == null) {
	  a = new int[size];
	 } else if (a.length < size) {
	  a = java.util.Arrays.copyOf(a, size);
	 }
	 iterator().unwrap(a);
	 return a;
	}
	@Override
	public int[] toIntArray() {
		final int size = size();
		if (size == 0) {
			return Arrays.EMPTY_INT_ARRAY;
		}
		final int[] a = new int[size];
		iterator().unwrap(a);
		return a;
	}
	/** {@inheritDoc}
	 *
	 * This method exists to make final what should have been final in the interface.
	 */
	@Override
	public void forEach(final IntConsumer action) {
	 IntCollection.super.forEach(action);
	}
	/** {@inheritDoc}
	 *
	 * This method exists to make final what should have been final in the interface.
	 */
	@Override
	public boolean removeIf(final IntPredicate filter) {
	 return IntCollection.super.removeIf(filter);
	}
	@Override
	public boolean addAll(final IntCollection c) {
	 boolean retVal = false;
	 for(final IntIterator i = c.iterator(); i.hasNext();)
     if (add(i.nextInt())) {
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
	public boolean addAll(final Collection<? extends Integer> c) {
	 if (c instanceof IntCollection) {
	  return addAll((IntCollection) c);
	 }
	 return super.addAll(c);
	}
	@Override
	public boolean containsAll(final IntCollection c) {
	 for(final IntIterator i = c.iterator(); i.hasNext();)
     if (!contains(i.nextInt())) {
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
	 if (c instanceof IntCollection) {
	  return containsAll((IntCollection) c);
	 }
	 return super.containsAll(c);
	}
	@Override
	public boolean removeAll(final IntCollection c) {
	 boolean retVal = false;
	 for(final IntIterator i = c.iterator(); i.hasNext();)
     if (removeInt(i.nextInt())) {
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
	 if (c instanceof IntCollection) {
	  return removeAll((IntCollection) c);
	 }
	 return super.removeAll(c);
	}
	@Override
	public boolean retainAll(final IntCollection c) {
	 boolean retVal = false;
	 for(final IntIterator i = iterator(); i.hasNext();)
	  if (! c.contains(i.nextInt())) {
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
	 if (c instanceof IntCollection) {
	  return retainAll((IntCollection) c);
	 }
	 return super.retainAll(c);
	}
	@Override
	public String toString() {
	 final StringBuilder s = new StringBuilder();
	 final IntIterator i = iterator();
	 int n = size();
	 int k;
	 boolean first = true;
	 s.append("{");
	 while(n-- != 0) {
     if (first) {
       first = false;
     } else {
       s.append(", ");
     }
	  k = i.nextInt();
	   s.append(String.valueOf(k));
	 }
	 s.append("}");
	 return s.toString();
	}
}
