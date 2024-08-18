package marcel.util.primitives.collections;

import marcel.util.primitives.iterators.DoubleIterator;
import marcel.util.primitives.Arrays;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.function.DoubleConsumer;
import java.util.function.DoublePredicate;

public abstract class AbstractDoubleCollection extends AbstractCollection<Double> implements DoubleCollection {
	protected AbstractDoubleCollection() {}
	@Override
	public abstract DoubleIterator iterator();
	/** {@inheritDoc}
	 *
	 * This implementation always throws an {@link UnsupportedOperationException}.
	 */
	@Override
	public boolean add(final double k) {
	 throw new UnsupportedOperationException();
	}
	/** {@inheritDoc}
	 *
	 * This implementation iterates over the elements in the collection,
	 * looking for the specified element.
	 */
	@Override
	public boolean contains(final double k) {
	 final DoubleIterator iterator = iterator();
    while (iterator.hasNext()) {
      if (k == iterator.nextDouble()) {
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
	public boolean add(final Double key) {
	 return DoubleCollection.super.add(key);
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public boolean contains(final Object key) {
	 return DoubleCollection.super.contains(key);
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public boolean remove(final Object key) {
	 return DoubleCollection.super.remove(key);
	}
	@Override
	public double[] toArray(double a[]) {
	 final int size = size();
	 if (a == null) {
	  a = new double[size];
	 } else if (a.length < size) {
	  a = java.util.Arrays.copyOf(a, size);
	 }
	 iterator().unwrap(a);
	 return a;
	}
	@Override
	public double[] toDoubleArray() {
		final int size = size();
		if (size == 0) {
			return Arrays.EMPTY_DOUBLE_ARRAY;
		}
		final double[] a = new double[size];
		iterator().unwrap(a);
		return a;
	}
	/** {@inheritDoc}
	 *
	 * This method exists to make final what should have been final in the interface.
	 */
	@Override
	public void forEach(final DoubleConsumer action) {
	 DoubleCollection.super.forEach(action);
	}
	/** {@inheritDoc}
	 *
	 * This method exists to make final what should have been final in the interface.
	 */
	@Override
	public boolean removeIf(final DoublePredicate filter) {
	 return DoubleCollection.super.removeIf(filter);
	}
	@Override
	public boolean addAll(final DoubleCollection c) {
	 boolean retVal = false;
	 for(final DoubleIterator i = c.iterator(); i.hasNext();)
     if (add(i.nextDouble())) {
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
	public boolean addAll(final Collection<? extends Double> c) {
	 if (c instanceof DoubleCollection) {
	  return addAll((DoubleCollection) c);
	 }
	 return super.addAll(c);
	}
	@Override
	public boolean containsAll(final DoubleCollection c) {
	 for(final DoubleIterator i = c.iterator(); i.hasNext();)
     if (!contains(i.nextDouble())) {
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
	 if (c instanceof DoubleCollection) {
	  return containsAll((DoubleCollection) c);
	 }
	 return super.containsAll(c);
	}
	@Override
	public boolean removeAll(final DoubleCollection c) {
	 boolean retVal = false;
	 for(final DoubleIterator i = c.iterator(); i.hasNext();)
     if (removeDouble(i.nextDouble())) {
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
	 if (c instanceof DoubleCollection) {
	  return removeAll((DoubleCollection) c);
	 }
	 return super.removeAll(c);
	}
	@Override
	public boolean retainAll(final DoubleCollection c) {
	 boolean retVal = false;
	 for(final DoubleIterator i = iterator(); i.hasNext();)
	  if (! c.contains(i.nextDouble())) {
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
	 if (c instanceof DoubleCollection) {
	  return retainAll((DoubleCollection) c);
	 }
	 return super.retainAll(c);
	}
	@Override
	public String toString() {
	 final StringBuilder s = new StringBuilder();
	 final DoubleIterator i = iterator();
	 int n = size();
	 double k;
	 boolean first = true;
	 s.append("{");
	 while(n-- != 0) {
     if (first) {
       first = false;
     } else {
       s.append(", ");
     }
	  k = i.nextDouble();
	   s.append(String.valueOf(k));
	 }
	 s.append("}");
	 return s.toString();
	}
}
