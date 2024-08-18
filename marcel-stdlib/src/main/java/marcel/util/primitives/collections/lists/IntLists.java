package marcel.util.primitives.collections.lists;

import marcel.util.primitives.collections.IntCollection;
import marcel.util.primitives.iterators.IntIterators;
import marcel.util.primitives.iterators.list.IntListIterator;
import marcel.util.primitives.spliterators.IntSpliterator;
import marcel.util.primitives.spliterators.IntSpliterators;
import marcel.util.primitives.Arrays;

import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;

public final class IntLists {
	/* Only in the EmptyList and Singleton classes, where performance is critical, do we override
	 * the deprecated, Object based functional methods. For the rest, we just override the
	 * non-deprecated type-specific method, and let the default method from the interface
	 * filter into that. This is an extra method call and lambda creation, but it isn't worth
	 * complexifying the code generation for a case that is already marked as being inefficient.
	 */
	private IntLists() {}

	/** An immutable class representing an empty type-specific list.
	 *
	 * <p>This class may be useful to implement your own in case you subclass
	 * a type-specific list.
	 */
	public static class EmptyList implements IntList , RandomAccess, java.io.Serializable, Cloneable {
	 private static final long serialVersionUID = -7046029254386353129L;
	 protected EmptyList() {}
	 @Override
	 public int getAt(int i) { throw new IndexOutOfBoundsException(); }
	 @Override
	 public boolean removeInt(int i) { throw new UnsupportedOperationException(); }
	 @Override
	 public void add(final int index, final int k) { throw new UnsupportedOperationException(); }
	 @Override
	 public int putAt(final int index, final int k) { throw new UnsupportedOperationException(); }
	 @Override
	 public int indexOf(int k) { return -1; }
	 @Override
	 public int lastIndexOf(int k) { return -1; }
	 @Override
	 public boolean addAll(int i, Collection<? extends Integer> c) { throw new UnsupportedOperationException(); }

		@Override
		public boolean removeAll(Collection<?> c) {
			return false;
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			return false;
		}

		@Deprecated
	 @Override
	 public void replaceAll(final java.util.function.UnaryOperator<Integer> operator) { throw new UnsupportedOperationException(); }
	 @Override
	 public void replaceAll(final java.util.function.IntUnaryOperator operator) { throw new UnsupportedOperationException(); }
	 @Override
	 public boolean addAll(IntList c) { throw new UnsupportedOperationException(); }
	 @Override
	 public boolean addAll(int i, IntCollection c) { throw new UnsupportedOperationException(); }
	 @Override
	 public boolean addAll(int i, IntList c) { throw new UnsupportedOperationException(); }
	 /** {@inheritDoc}
		 * @deprecated Please use the corresponding type-specific method instead. */
	 @SuppressWarnings("deprecation")
	 @Deprecated
	 @Override
	 public void add(final int index, final Integer k) { throw new UnsupportedOperationException(); }
	 /** {@inheritDoc}
		 * @deprecated Please use the corresponding type-specific method instead. */
	 @SuppressWarnings("deprecation")
	 @Deprecated
	 @Override
	 public Integer get(final int index) { throw new UnsupportedOperationException(); }
	 /** {@inheritDoc}
		 * @deprecated Please use the corresponding type-specific method instead. */
	 @SuppressWarnings("deprecation")
	 @Deprecated
	 @Override
	 public boolean add(final Integer k) { throw new UnsupportedOperationException(); }

		@Override
		public int[] toIntArray() {
			return Arrays.EMPTY_INT_ARRAY;
		}

		@Override
		public int[] toArray(int[] a) {
			return Arrays.EMPTY_INT_ARRAY;
		}

		@Override
		public boolean addAll(IntCollection c) {
			return false;
		}

		@Override
		public boolean containsAll(IntCollection c) {
			return false;
		}

		@Override
		public boolean removeAll(IntCollection c) {
			return false;
		}

		@Override
		public boolean retainAll(IntCollection c) {
			return false;
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return false;
		}

		@Override
		public boolean addAll(Collection<? extends Integer> c) {
			return false;
		}

		@Override
		public int removeAt(int index) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 * @deprecated Please use the corresponding type-specific method instead. */
	 @SuppressWarnings("deprecation")
	 @Deprecated
	 @Override
	 public Integer set(final int index, final Integer k) { throw new UnsupportedOperationException(); }
	 /** {@inheritDoc}
		 * @deprecated Please use the corresponding type-specific method instead. */
	 @SuppressWarnings("deprecation")
	 @Deprecated
	 @Override
	 public Integer remove(int k) { throw new UnsupportedOperationException(); }
	 /** {@inheritDoc}
		 * @deprecated Please use the corresponding type-specific method instead. */
	 @SuppressWarnings("deprecation")
	 @Deprecated
	 @Override
	 public int indexOf(Object k) { return -1; }
	 /** {@inheritDoc}
		 * @deprecated Please use the corresponding type-specific method instead. */
	 @SuppressWarnings("deprecation")
	 @Deprecated
	 @Override
	 public int lastIndexOf(Object k) { return -1; }
	 // Empty lists are trivially always sorted

		@Override
		public void sortReverse() {
		}

		@Override
		public void sort() {
		}

		@Deprecated
	 @Override
	 public void sort(final java.util.Comparator<? super Integer> comparator) { }

		@Override
		public void clear() {

		}

		@Override
		public int size() {
			return 0;
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public IntListIterator iterator() {
			return IntIterators.EMPTY_ITERATOR;
		}

		@Override
		public Object[] toArray() {
			return new Object[0];
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return null;
		}

		@Override
		public IntSpliterator spliterator() {
			return IntSpliterators.wrap(Arrays.EMPTY_INT_ARRAY);
		}

		@Override
	 public IntListIterator listIterator() { return IntIterators.EMPTY_ITERATOR; }
	 @Override
	 public IntListIterator listIterator(int i) { if (i == 0) return IntIterators.EMPTY_ITERATOR; throw new IndexOutOfBoundsException(String.valueOf(i)); }
	 @Override
	 public IntList subList(int from, int to) { if (from == 0 && to == 0) return this; throw new IndexOutOfBoundsException(); }
	 @Override
	 public void getElements(int from, int[] a, int offset, int length) { if (from == 0 && length == 0 && offset >= 0 && offset <= a.length) return; throw new IndexOutOfBoundsException(); }
	 @Override
	 public void removeElements(int from, int to) { throw new UnsupportedOperationException(); }
	 @Override
	 public void addElements(int index, final int a[], int offset, int length) { throw new UnsupportedOperationException(); }
	 @Override
	 public void addElements(int index, final int a[]) { throw new UnsupportedOperationException(); }
	 @Override
	 public void setElements(final int a[]) { throw new UnsupportedOperationException(); }
	 @Override
	 public void setElements(int index, final int a[]) { throw new UnsupportedOperationException(); }
	 @Override
	 public void setElements(int index, final int a[], int offset, int length) { throw new UnsupportedOperationException(); }

		@Override
		public boolean add(int key) {
			return false;
		}

		@Override
		public boolean contains(int key) {
			return false;
		}

		@Override
	 public void size(int s) { throw new UnsupportedOperationException(); }
	 @Override
	 public int compareTo(final List<? extends Integer> o) {
	  if (o == this) return 0;
	  return ((List<?>)o).isEmpty() ? 0 : -1;
	 }
	 @Override
	 public Object clone() { return EMPTY_LIST; }
	 @Override
	 public int hashCode() { return 1; }
	 @Override
	 @SuppressWarnings("rawtypes")
	 public boolean equals(Object o) { return o instanceof List && ((List)o).isEmpty(); }
	 @Override
	 public String toString() { return "[]"; }
	 private Object readResolve() { return EMPTY_LIST; }
	}
	/** An empty list (immutable). It is serializable and cloneable.
	 */
	public static final EmptyList EMPTY_LIST = new EmptyList();
	/** Returns an empty list (immutable). It is serializable and cloneable.
	 *
	 * <p>This method provides a typesafe access to {@link #EMPTY_LIST}.
	 * @return an empty list (immutable).
	 */
	@SuppressWarnings("unchecked")
	public static IntList emptyList() {
	 return EMPTY_LIST;
	}

}