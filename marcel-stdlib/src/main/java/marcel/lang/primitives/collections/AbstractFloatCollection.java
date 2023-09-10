package marcel.lang.primitives.collections;

import marcel.lang.primitives.iterators.FloatIterator;
import marcel.lang.util.Arrays;
import marcel.lang.util.function.FloatConsumer;
import marcel.lang.util.function.FloatPredicate;

import java.util.AbstractCollection;
import java.util.Collection;

public abstract class AbstractFloatCollection extends AbstractCollection<Float> implements FloatCollection {
	protected AbstractFloatCollection() {}
	@Override
	public abstract FloatIterator iterator();
	/** {@inheritDoc}
	 *
	 * This implementation always throws an {@link UnsupportedOperationException}.
	 */
	@Override
	public boolean add(final float k) {
	 throw new UnsupportedOperationException();
	}
	/** {@inheritDoc}
	 *
	 * This implementation iterates over the elements in the collection,
	 * looking for the specified element.
	 */
	@Override
	public boolean contains(final float k) {
	 final FloatIterator iterator = iterator();
    while (iterator.hasNext()) {
      if (k == iterator.nextFloat()) {
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
	public boolean add(final Float key) {
	 return FloatCollection.super.add(key);
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public boolean contains(final Object key) {
	 return FloatCollection.super.contains(key);
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public boolean remove(final Object key) {
	 return FloatCollection.super.remove(key);
	}
	@Override
	public float[] toArray(float a[]) {
	 final int size = size();
	 if (a == null) {
	  a = new float[size];
	 } else if (a.length < size) {
	  a = java.util.Arrays.copyOf(a, size);
	 }
	 iterator().unwrap(a);
	 return a;
	}
	@Override
	public float[] toFloatArray() {
		final int size = size();
		if (size == 0) {
			return Arrays.EMPTY_FLOAT_ARRAY;
		}
		final float[] a = new float[size];
		iterator().unwrap(a);
		return a;
	}
	/** {@inheritDoc}
	 *
	 * This method exists to make final what should have been final in the interface.
	 */
	@Override
	public void forEach(final FloatConsumer action) {
	 FloatCollection.super.forEach(action);
	}
	/** {@inheritDoc}
	 *
	 * This method exists to make final what should have been final in the interface.
	 */
	@Override
	public boolean removeIf(final FloatPredicate filter) {
	 return FloatCollection.super.removeIf(filter);
	}
	@Override
	public boolean addAll(final FloatCollection c) {
	 boolean retVal = false;
	 for(final FloatIterator i = c.iterator(); i.hasNext();)
     if (add(i.nextFloat())) {
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
	public boolean addAll(final Collection<? extends Float> c) {
	 if (c instanceof FloatCollection) {
	  return addAll((FloatCollection) c);
	 }
	 return super.addAll(c);
	}
	@Override
	public boolean containsAll(final FloatCollection c) {
	 for(final FloatIterator i = c.iterator(); i.hasNext();)
     if (!contains(i.nextFloat())) {
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
	 if (c instanceof FloatCollection) {
	  return containsAll((FloatCollection) c);
	 }
	 return super.containsAll(c);
	}
	@Override
	public boolean removeAll(final FloatCollection c) {
	 boolean retVal = false;
	 for(final FloatIterator i = c.iterator(); i.hasNext();)
     if (removeFloat(i.nextFloat())) {
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
	 if (c instanceof FloatCollection) {
	  return removeAll((FloatCollection) c);
	 }
	 return super.removeAll(c);
	}
	@Override
	public boolean retainAll(final FloatCollection c) {
	 boolean retVal = false;
	 for(final FloatIterator i = iterator(); i.hasNext();)
	  if (! c.contains(i.nextFloat())) {
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
	 if (c instanceof FloatCollection) {
	  return retainAll((FloatCollection) c);
	 }
	 return super.retainAll(c);
	}
	@Override
	public String toString() {
	 final StringBuilder s = new StringBuilder();
	 final FloatIterator i = iterator();
	 int n = size();
	 float k;
	 boolean first = true;
	 s.append("{");
	 while(n-- != 0) {
     if (first) {
       first = false;
     } else {
       s.append(", ");
     }
	  k = i.nextFloat();
	   s.append(String.valueOf(k));
	 }
	 s.append("}");
	 return s.toString();
	}
}
