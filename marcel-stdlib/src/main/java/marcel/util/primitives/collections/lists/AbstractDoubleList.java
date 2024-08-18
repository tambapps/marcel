package marcel.util.primitives.collections.lists;

import marcel.util.primitives.collections.AbstractDoubleCollection;
import marcel.util.primitives.collections.DoubleCollection;
import marcel.util.primitives.iterators.DoubleIterator;
import marcel.util.primitives.iterators.list.DoubleListIterator;
import marcel.util.primitives.spliterators.DoubleSpliterator;
import marcel.util.primitives.Arrays;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

public abstract class AbstractDoubleList extends AbstractDoubleCollection implements DoubleList {


	protected AbstractDoubleList() {}
	/** Ensures that the given index is nonnegative and not greater than the list size.
	 *
	 * @param index an index.
	 * @throws IndexOutOfBoundsException if the given index is negative or greater than the list size.
	 */
	protected void ensureIndex(final int index) {
    if (index < 0) {
      throw new IndexOutOfBoundsException("Index (" + index + ") is negative");
    }
    if (index > size()) {
      throw new IndexOutOfBoundsException(
          "Index (" + index + ") is greater than list size (" + (size()) + ")");
    }
	}
	/** Ensures that the given index is nonnegative and smaller than the list size.
	 *
	 * @param index an index.
	 * @throws IndexOutOfBoundsException if the given index is negative or not smaller than the list size.
	 */
	protected void ensureRestrictedIndex(final int index) {
    if (index < 0) {
      throw new IndexOutOfBoundsException("Index (" + index + ") is negative");
    }
    if (index >= size()) {
      throw new IndexOutOfBoundsException(
          "Index (" + index + ") is greater than or equal to list size (" + (size()) + ")");
    }
	}
	/** {@inheritDoc}
	 *
	 * This implementation always throws an {@link UnsupportedOperationException}.
	 */
	@Override
	public void add(final int index, final double k) {
	 throw new UnsupportedOperationException();
	}
	/** {@inheritDoc}
	 *
	 * This implementation delegates to the type-specific version of {@link List#add(int, Object)}.
	 */
	@Override
	public boolean add(final double k) {
	 add(size(), k);
	 return true;
	}
	/** {@inheritDoc}
	 *
	 */
	@Override
	public boolean removeDouble(final double i) {
	 throw new UnsupportedOperationException();
	}
	/** {@inheritDoc}
	 *
	 * This implementation always throws an {@link UnsupportedOperationException}.
	 */
	@Override
	public double putAt(final int index, final double k) {
	 throw new UnsupportedOperationException();
	}
	/** Adds all of the elements in the specified collection to this list (optional operation). */
	@Override
	public boolean addAll(int index, final Collection<? extends Double> c) {
	 if (c instanceof DoubleCollection) {
	  return addAll(index, (DoubleCollection) c);
	 }
	 ensureIndex(index);
	 final Iterator<? extends Double> i = c.iterator();
	 final boolean retVal = i.hasNext();
    while (i.hasNext()) {
      add(index++, (i.next()).intValue());
    }
	 return retVal;
	}
	/** {@inheritDoc}
	 *
	 * This implementation delegates to the type-specific version of {@link List#addAll(int, Collection)}.
	 */
	@Override
	public boolean addAll(final Collection<? extends Double> c) {
	 return addAll(size(), c);
	}
	/** {@inheritDoc}
	 *
	 * This implementation delegates to {@link #listIterator()}.
	 */
	@Override
	public DoubleListIterator iterator() {
	 return listIterator();
	}

	@Override
	public double removeAt(int index) {
		return remove(index);
	}

	
	/** {@inheritDoc}
	 *
	 * This implementation delegates to {@link #listIterator(int) listIterator(0)}.
	 */
	@Override
	public DoubleListIterator listIterator() {
	 return listIterator(0);
	}
	/** {@inheritDoc}
	 * This implementation is based on the random-access methods. */
	@Override
	public DoubleListIterator listIterator(final int index) {
		throw new UnsupportedOperationException("Not supported yet");
	}

	/** Returns true if this list contains the specified element.
	 * This implementation delegates to {@code indexOf()}.
	 * @see List#contains(Object)
	 */
	@Override
	public boolean contains(final double k) {
	 return indexOf(k) >= 0;
	}
	@Override
	public int indexOf(final double k) {
	 final DoubleListIterator i = listIterator();
		double e;
	 while(i.hasNext()) {
	  e = i.nextDouble();
     if (((k) == (e))) {
       return i.previousIndex();
     }
	 }
	 return -1;
	}
	@Override
	public int lastIndexOf(final double k) {
	 DoubleListIterator i = listIterator(size());
		double e;
	 while(i.hasPrevious()) {
	  e = i.previousDouble();
     if (((k) == (e))) {
       return i.nextIndex();
     }
	 }
	 return -1;
	}
	@Override
	public void size(final int size) {
	 int i = size();
    if (size > i) {
      while (i++ < size) {
        add((0));
      }
    } else {
      while (i-- != size) {
        removeAt(i);
      }
    }
	}
	@Override
	public DoubleList subList(final int from, final int to) {
	 ensureIndex(from);
	 ensureIndex(to);
    if (from > to) {
      throw new IndexOutOfBoundsException(
          "Start index (" + from + ") is greater than end index (" + to + ")");
    }
	 return this instanceof java.util.RandomAccess ? new DoubleRandomAccessSubList (this, from, to) : new DoubleSubList(this, from, to);
	}

	/** {@inheritDoc}
	 *
	 * <p>This is a trivial iterator-based based implementation. It is expected that
	 * implementations will override this method with a more optimized version.
	 */
	@Override
	public void removeElements(final int from, final int to) {
	 ensureIndex(to);
	 // Always use the iterator based implementation even for RandomAccess so we don't have to worry about shifting indexes.
	 DoubleListIterator i = listIterator(from);
	 int n = to - from;
    if (n < 0) {
      throw new IllegalArgumentException(
          "Start index (" + from + ") is greater than end index (" + to + ")");
    }
	 while(n-- != 0) {
	  i.nextDouble();
	  i.remove();
	 }
	}
	/** {@inheritDoc}
	 *
	 * <p>This is a trivial iterator-based implementation. It is expected that
	 * implementations will override this method with a more optimized version.
	 */
	@Override
	public void addElements(int index, final double a[], int offset, int length) {
	 ensureIndex(index);
	 Arrays.ensureOffsetLength(a.length, offset, length);
	 if (this instanceof java.util.RandomAccess) {
     while (length-- != 0) {
       add(index++, a[offset++]);
     }
	 } else {
	  DoubleListIterator iter = listIterator(index);
     while (length-- != 0) {
       iter.add(a[offset++]);
     }
	 }
	}
	/** {@inheritDoc}
	 *
	 * This implementation delegates to the analogous method for array fragments.
	 */
	@Override
	public void addElements(final int index, final double a[]) {
	 addElements(index, a, 0, a.length);
	}
	/** {@inheritDoc}
	 *
	 * <p>This is a trivial iterator-based implementation. It is expected that
	 * implementations will override this method with a more optimized version.
	 */
	@Override
	public void getElements(final int from, final double a[], int offset, int length) {
	 ensureIndex(from);
		Arrays.ensureOffsetLength(a.length, offset, length);
    if (from + length > size()) {
      throw new IndexOutOfBoundsException(
          "End index (" + (from + length) + ") is greater than list size (" + size() + ")");
    }
	 if (this instanceof java.util.RandomAccess) {
	  int current = from;
     while (length-- != 0) {
       a[offset++] = getAt(current++);
     }
	 } else {
	  DoubleListIterator i = listIterator(from);
     while (length-- != 0) {
       a[offset++] = i.nextDouble();
     }
	 }
	}
	@Override
	public void setElements(int index, double a[], int offset, int length) {
	 ensureIndex(index);
		Arrays.ensureOffsetLength(a.length, offset, length);
    if (index + length > size()) {
      throw new IndexOutOfBoundsException(
          "End index (" + (index + length) + ") is greater than list size (" + size() + ")");
    }
	 if (this instanceof java.util.RandomAccess) {
	  for (int i = 0; i < length; ++i) {
	   putAt(i + index, a[i + offset]);
	  }
	 } else {
	  DoubleListIterator iter = listIterator(index);
	  int i = 0;
	  while (i < length) {
	   iter.nextDouble();
	   iter.set(a[offset + i++]);
	  }
	 }
	}
	/** {@inheritDoc}
	 * This implementation delegates to {@link #removeElements(int, int)}.*/
	@Override
	public void clear() {
	 removeElements(0, size());
	}
	/** Returns the hash code for this list, which is identical to {@link List#hashCode()}.
	 *
	 * @return the hash code for this list.
	 */
	@Override
	public int hashCode() {
	 DoubleIterator i = iterator();
	 int h = 1, s = size();
	 while (s-- != 0) {
		 long l = Double.doubleToRawLongBits(i.nextDouble());
	  int k = (int)(l ^ (l >>> 32));
	  h = 31 * h + (k);
	 }
	 return h;
	}
	@Override
	public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof List)) {
      return false;
    }
	 final List<?> l = (List<?>)o;
	 int s = size();
    if (s != l.size()) {
      return false;
    }
	 if (l instanceof DoubleList) {
	  final DoubleListIterator i1 = listIterator(), i2 = ((DoubleList )l).listIterator();
     while (s-- != 0) {
       if (i1.nextDouble() != i2.nextDouble()) {
         return false;
       }
     }
	  return true;
	 }
	 final ListIterator<?> i1 = listIterator(), i2 = l.listIterator();
    while (s-- != 0) {
      if (!Objects.equals(i1.next(), i2.next())) {
        return false;
      }
    }
	 return true;
	}
	/** Compares this list to another object. If the
	 * argument is a {@link List}, this method performs a lexicographical comparison; otherwise,
	 * it throws a {@code ClassCastException}.
	 *
	 * @param l a list.
	 * @return if the argument is a {@link List}, a negative integer,
	 * zero, or a positive integer as this list is lexicographically less than, equal
	 * to, or greater than the argument.
	 * @throws ClassCastException if the argument is not a list.
	 */

	@Override
	public int compareTo(final List<? extends Double> l) {
    if (l == this) {
      return 0;
    }
	 if (l instanceof DoubleList) {
	  final DoubleListIterator i1 = listIterator(), i2 = ((DoubleList )l).listIterator();
	  int r;
	  double e1, e2;
	  while(i1.hasNext() && i2.hasNext()) {
	   e1 = i1.nextDouble();
	   e2 = i2.nextDouble();
      if ((r = (Double.compare((e1), (e2)))) != 0) {
        return r;
      }
	  }
	  return i2.hasNext() ? -1 : (i1.hasNext() ? 1 : 0);
	 }
	 ListIterator<? extends Double> i1 = listIterator(), i2 = l.listIterator();
	 int r;
	 while(i1.hasNext() && i2.hasNext()) {
     if ((r = ((Comparable<? super Double>) i1.next()).compareTo(i2.next())) != 0) {
       return r;
     }
	 }
	 return i2.hasNext() ? -1 : (i1.hasNext() ? 1 : 0);
	}

	/** Removes a single instance of the specified element from this collection, if it is present (optional operation).
	 * This implementation delegates to {@code indexOf()}.
	 * @see List#remove(Object)
	 */
	@Override
	public Double remove(int k) {
	 int index = indexOf(k);
    if (index == -1) {
      return null;
    }
	 return removeAt(index);
	}
	@Override
	public double[] toDoubleArray() {
	 final int size = size();
    if (size == 0) {
      return Arrays.EMPTY_DOUBLE_ARRAY;
    }
		double[] ret = new double[size];
	 getElements(0, ret, 0, size);
	 return ret;
	}
	@Override
	public double[] toArray(double a[]) {
	 final int size = size();
	 if (a.length < size) {
	  a = java.util.Arrays.copyOf(a, size);
	 }
	 getElements(0, a, 0, size);
	 return a;
	}
	@Override
	public boolean addAll(int index, final DoubleCollection c) {
	 ensureIndex(index);
	 final DoubleIterator i = c.iterator();
	 final boolean retVal = i.hasNext();
    while (i.hasNext()) {
      add(index++, i.nextDouble());
    }
	 return retVal;
	}
	/** {@inheritDoc}
	 *
	 * This implementation delegates to the type-specific version of {@link List#addAll(int, Collection)}.
	 */
	@Override
	public boolean addAll(final DoubleCollection c) {
	 return addAll(size(), c);
	}
	/** {@inheritDoc} 
	 * This method just delegates to the interface default method,
	 * as the default method, but it is final, so it cannot be overridden.
	 */
	@Override
	public final void replaceAll(final DoubleUnaryOperator operator) {
		Objects.requireNonNull(operator);
		final DoubleListIterator li = this.listIterator();
		while (li.hasNext()) {
			li.set(operator.applyAsDouble(li.next()));
		}
	}

	@Override
	public String toString() {
	 final StringBuilder s = new StringBuilder();
	 final DoubleIterator i = iterator();
	 int n = size();
	 double k;
	 boolean first = true;
	 s.append("[");
	 while(n-- != 0) {
     if (first) {
       first = false;
     } else {
       s.append(", ");
     }
	  k = i.nextDouble();
	   s.append(String.valueOf(k));
	 }
	 s.append("]");
	 return s.toString();
	}

	/** A class implementing a sublist view. */
	public static class DoubleSubList extends AbstractDoubleList implements java.io.Serializable {
	 private static final double serialVersionUID = -7046029254386353129L;
	 /** The list this sublist restricts. */
	 protected final DoubleList l;
	 /** Initial (inclusive) index of this sublist. */
	 protected final int from;
	 /** Final (exclusive) index of this sublist. */
	 protected int to;
	 public DoubleSubList(final DoubleList l, final int from, final int to) {
	  this.l = l;
	  this.from = from;
	  this.to = to;
	 }

	 @Override
	 public boolean add(final double k) {
	  l.add(to, k);
	  to++;
	  return true;
	 }
	 @Override
	 public void add(final int index, final double k) {
	  ensureIndex(index);
	  l.add(from + index, k);
	  to++;
	 }
	 @Override
	 public boolean addAll(final int index, final Collection<? extends Double> c) {
	  ensureIndex(index);
	  to += c.size();
	  return l.addAll(from + index, c);
	 }
	 @Override
	 public double getAt(final int index) {
	  ensureRestrictedIndex(index);
	  return l.getAt(from + index);
	 }
	 @Override
	 public double removeAt(final int index) {
	  ensureRestrictedIndex(index);
	  to--;
	  return l.removeAt(from + index);
	 }
	 @Override
	 public double putAt(final int index, final double k) {
	  ensureRestrictedIndex(index);
	  return l.putAt(from + index, k);
	 }
	 @Override
	 public int size() {
	  return to - from;
	 }
	 @Override
	 public void getElements(final int from, final double[] a, final int offset, final int length) {
	  ensureIndex(from);
     if (from + length > size()) {
       throw new IndexOutOfBoundsException(
           "End index (" + from + length + ") is greater than list size (" + size() + ")");
     }
	  l.getElements(this.from + from, a, offset, length);
	 }
	 @Override
	 public void removeElements(final int from, final int to) {
	  ensureIndex(from);
	  ensureIndex(to);
	  l.removeElements(this.from + from, this.from + to);
	  this.to -= (to - from);
	 }
	 @Override
	 public void addElements(int index, final double a[], int offset, int length) {
	  ensureIndex(index);
	  l.addElements(this.from + index, a, offset, length);
	  this.to += length;
	 }
	 @Override
	 public void setElements(int index, final double a[], int offset, int length) {
	  ensureIndex(index);
	  l.setElements(this.from + index, a, offset, length);
	 }


	 @Override
	 public DoubleListIterator listIterator(final int index) {
	  ensureIndex(index);
		throw new UnsupportedOperationException("Not implemented yet");
	 }
	 @Override
	 public DoubleSpliterator spliterator() {
		 throw new UnsupportedOperationException("Not implemented yet");
	 }
	 @Override
	 public DoubleList subList(final int from, final int to) {
	  ensureIndex(from);
	  ensureIndex(to);
     if (from > to) {
       throw new IllegalArgumentException(
           "Start index (" + from + ") is greater than end index (" + to + ")");
     }
	  // Sadly we have to rewrap this, because if there is a sublist of a sublist, and the
	  // subsublist adds, both sublists need to update their "to" value.
	  return new DoubleSubList (this, from, to);
	 }

	 @Override
	 public boolean addAll(final int index, final DoubleCollection c) {
	  ensureIndex(index);
	  return super.addAll(index, c);
	 }
	 @Override
	 public boolean addAll(final int index, final DoubleList l) {
	  ensureIndex(index);
	  return super.addAll(index, l);
	 }

		@Override
		public void sort() {
			throw new UnsupportedOperationException("Not implemented yet");
		}

		@Override
		public void sortReverse() {
			throw new UnsupportedOperationException("Not implemented yet");
		}
	}
	public static class DoubleRandomAccessSubList extends DoubleSubList implements java.util.RandomAccess {
	 private static final double serialVersionUID = -107070782945191929L;
	 public DoubleRandomAccessSubList(final DoubleList l, final int from, final int to) {
	  super(l, from, to);
	 }
	 @Override
	 public DoubleList subList(final int from, final int to) {
	  ensureIndex(from);
	  ensureIndex(to);
     if (from > to) {
       throw new IllegalArgumentException(
           "Start index (" + from + ") is greater than end index (" + to + ")");
     }
	  // Sadly we have to rewrap this, because if there is a sublist of a sublist, and the
	  // subsublist adds, both sublists need to update their "to" value.
	  return new DoubleRandomAccessSubList (this, from, to);
	 }

	}
}