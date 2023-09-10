package marcel.lang.primitives.collections.lists;

import marcel.lang.primitives.collections.FloatCollection;
import marcel.lang.primitives.iterators.FloatIterator;
import marcel.lang.primitives.iterators.list.FloatListIterator;
import marcel.lang.primitives.spliterators.FloatSpliterator;
import marcel.lang.primitives.spliterators.FloatSpliterators;
import marcel.lang.util.Arrays;
import marcel.lang.util.SafeMath;
import marcel.lang.util.function.FloatConsumer;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

public class FloatArrayList extends AbstractFloatList implements RandomAccess, Cloneable, java.io.Serializable {
	private static final long serialVersionUID = -7046029254386353130L;
	/** The initial default capacity of an array list. */
	public static final int DEFAULT_INITIAL_CAPACITY = 10;
	/** The backing array. */
	protected transient float[] a;
	/** The current actual size of the list (never greater than the backing-array length). */
	protected int size;
	/** Ensures that the component type of the given array is the proper type.
	 * This is irrelevant for primitive types, so it will just do a trivial copy.
	 * But for Reference types, you can have a {@code String[]} masquerading as an {@code Object[]},
	 * which is a case we need to prepare for because we let the user give an array to use directly
	 * with {@link #wrap}.
	 */

	private static final float[] copyArraySafe(float[] a, int length) {
    if (length == 0) {
      return Arrays.EMPTY_FLOAT_ARRAY;
    }
	 return java.util.Arrays.copyOf(a, length);
	}
	private static final float[] copyArrayFromSafe(FloatArrayList l) {
	 return copyArraySafe(l.a, l.size);
	}
	/** Creates a new array list using a given array.
	 *
	 * <p>This constructor is only meant to be used by the wrapping methods.
	 *
	 * @param a the array that will be used to back this array list.
	 */
	protected FloatArrayList(final float a[], @SuppressWarnings("unused") boolean wrapped) {
	 this.a = a;
	}

	private void initArrayFromCapacity(final int capacity) {
    if (capacity < 0) {
      throw new IllegalArgumentException("Initial capacity (" + capacity + ") is negative");
    }
    if (capacity == 0) {
      a = Arrays.EMPTY_FLOAT_ARRAY;
    } else {
      a = new float[capacity];
    }
	}
	/** Creates a new array list with given capacity.
	 *
	 * @param capacity the initial capacity of the array list (may be 0).
	 */
	public FloatArrayList(final int capacity) {
	 initArrayFromCapacity(capacity);
	}
	/** Creates a new array list with {@link #DEFAULT_INITIAL_CAPACITY} capacity. */

	public FloatArrayList() {
	 a = Arrays.EMPTY_FLOAT_ARRAY; // We delay allocation
	}
	/** Creates a new array list and fills it with a given collection.
	 *
	 * @param c a collection that will be used to fill the array list.
	 */
	public FloatArrayList(final Collection<? extends Float> c) {
	 if (c instanceof FloatArrayList) {
	  a = copyArrayFromSafe((FloatArrayList)c);
	  size = a.length;
	 } else {
	  initArrayFromCapacity(c.size());
	  if (c instanceof FloatList) {
	   ((FloatList )c).getElements(0, a, 0, size = c.size());
	  } else if (c instanceof FloatCollection) {
			size = ((FloatCollection) c).iterator().unwrap(a);
		} else {
			Iterator<? extends Float> iterator = c.iterator();
			int i = 0;
			while (iterator.hasNext()) {
				a[i++] = iterator.next();
			}
			size = c.size();
		 }
	 }
	}
	/** Creates a new array list and fills it with a given type-specific collection.
	 *
	 * @param c a type-specific collection that will be used to fill the array list.
	 */
	public FloatArrayList(final FloatCollection c) {
	 if (c instanceof FloatArrayList) {
	  a = copyArrayFromSafe((FloatArrayList)c);
	  size = a.length;
	 } else {
	  initArrayFromCapacity(c.size());
	  if (c instanceof FloatList) {
	   ((FloatList )c).getElements(0, a, 0, size = c.size());
	  } else {
	   size = c.iterator().unwrap(a);
	  }
	 }
	}
	/** Creates a new array list and fills it with a given type-specific list.
	 *
	 * @param l a type-specific list that will be used to fill the array list.
	 */
	public FloatArrayList(final FloatList l) {
	 if (l instanceof FloatArrayList) {
	  a = copyArrayFromSafe((FloatArrayList)l);
	  size = a.length;
	 } else {
	  initArrayFromCapacity(l.size());
	  l.getElements(0, a, 0, size = l.size());
	 }
	}
	/** Creates a new array list and fills it with the elements of a given array.
	 *
	 * @param a an array whose elements will be used to fill the array list.
	 */
	public FloatArrayList(final int a[]) {
	 this(a, 0, a.length);
	}
	/** Creates a new array list and fills it with the elements of a given array.
	 *
	 * @param a an array whose elements will be used to fill the array list.
	 * @param offset the first element to use.
	 * @param length the number of elements to use.
	 */
	public FloatArrayList(final int a[], final int offset, final int length) {
	 this(length);
	 System.arraycopy(a, offset, this.a, 0, length);
	 size = length;
	}
	/** Creates a new array list and fills it with the elements returned by an iterator..
	 *
	 * @param i an iterator whose returned elements will fill the array list.
	 */
	public FloatArrayList(final Iterator<? extends Float> i) {
	 this();
    while (i.hasNext()) {
      this.add((i.next()).intValue());
    }
	}
	/** Creates a new array list and fills it with the elements returned by a type-specific iterator..
	 *
	 * @param i a type-specific iterator whose returned elements will fill the array list.
	 */
	public FloatArrayList(final FloatIterator i) {
	 this();
    while (i.hasNext()) {
      this.add(i.nextFloat());
    }
	}
	/** Returns the backing array of this list.
	 *
	 * @return the backing array.
	 */
	public float[] elements() {
	 return a;
	}
	/** Wraps a given array into an array list of given size.
	 *
	 * <p>Note it is guaranteed
	 * that the type of the array returned by {@link #elements()} will be the same
	 * (see the comments in the class documentation).
	 *
	 * @param a an array to wrap.
	 * @param length the length of the resulting array list.
	 * @return a new array list of the given size, wrapping the given array.
	 */
	public static FloatArrayList wrap(final float a[], final int length) {
    if (length > a.length) {
      throw new IllegalArgumentException(
          "The specified length (" + length + ") is greater than the array size (" + a.length
              + ")");
    }
	 final FloatArrayList l = new FloatArrayList(a, true);
	 l.size = length;
	 return l;
	}
	/** Wraps a given array into an array list.
	 *
	 * <p>Note it is guaranteed
	 * that the type of the array returned by {@link #elements()} will be the same
	 * (see the comments in the class documentation).
	 *
	 * @param a an array to wrap.
	 * @return a new array list wrapping the given array.
	 */
	public static FloatArrayList wrap(final float a[]) {
	 return wrap(a, a.length);
	}
	/** Creates a new empty array list.
	 *
	 * @return a new empty array list.
	 */
	public static FloatArrayList of() {
	 return new FloatArrayList();
	}
	/** Creates an array list using an array of elements.
	 *
	 * @param init a the array the will become the new backing array of the array list.
	 * @return a new array list backed by the given array.
	 * @see #wrap
	 */

	public static FloatArrayList of(final float... init) {
	 return wrap(init);
	}
	/** Collects the result of a primitive {@code Stream} into a new ArrayList.
	 *
	 * <p>This method performs a terminal operation on the given {@code Stream}
	 *
	 * Taking a primitive stream instead of returning something like a
	 * {@link java.util.stream.Collector Collector} is necessary because there is no
	 * primitive {@code Collector} equivalent in the Java API.
	 */
	 public static FloatArrayList toList(java.util.stream.Stream<Float> stream) {
	  return stream.collect(
	   FloatArrayList::new,
				(f, e) -> f.add(e.floatValue()),
	   FloatArrayList::addAll);
	 }

	public static float[] ensureCapacity(float[] array, int length, int preserve) {
		if (length > array.length) {
			float[] t = new float[length];
			System.arraycopy(array, 0, t, 0, preserve);
			return t;
		} else {
			return array;
		}
	}

	/** Grows this array list, ensuring that it can contain the given number of entries without resizing,
	 * and in case increasing the current capacity at least by a factor of 50%.
	 *
	 * @param capacity the new minimum capacity for this array list.
	 */

	private void grow(int capacity) {
    if (capacity <= a.length) {
      return;
    }
    if (a != Arrays.EMPTY_FLOAT_ARRAY) {
      capacity = (int) Math.max(
          Math.min((long) a.length + (a.length >> 1), Arrays.MAX_ARRAY_SIZE),
          capacity);
    } else if (capacity < DEFAULT_INITIAL_CAPACITY) {
      capacity = DEFAULT_INITIAL_CAPACITY;
    }
	 a = Arrays.forceCapacity(a, capacity, size);
	}
	@Override
	public void add(final int index, final float k) {
	 ensureIndex(index);
	 grow(size + 1);
    if (index != size) {
      System.arraycopy(a, index, a, index + 1, size - index);
    }
	 a[index] = k;
	 size++;
	}
	@Override
	public boolean add(final float k) {
	 grow(size + 1);
	 a[size++] = k;
	 return true;
	}
	@Override
	public float getAt(final int index) {
    if (index >= size) {
      throw new IndexOutOfBoundsException(
          "Index (" + index + ") is greater than or equal to list size (" + size + ")");
    }
	 return a[index];
	}
	@Override
	public int indexOf(final float k) {
	 for(int i = 0; i < size; i++)
     if (((k) == (a[i]))) {
       return i;
     }
	 return -1;
	}
	@Override
	public int lastIndexOf(final float k) {
	 for(int i = size; i-- != 0;)
     if (((k) == (a[i]))) {
       return i;
     }
	 return -1;
	}
	@Override
	public float removeAt(final int index) {
    if (index >= size) {
      throw new IndexOutOfBoundsException(
          "Index (" + index + ") is greater than or equal to list size (" + size + ")");
    }
	 final float old = a[index];
	 size--;
    if (index != size) {
      System.arraycopy(a, index + 1, a, index, size - index);
    }
	 return old;
	}
	@Override
	public boolean removeFloat(final float k) {
	 int index = indexOf(k);
    if (index == -1) {
      return false;
    }
	 removeAt(index);
	 return true;
	}
	@Override
	public float putAt(final int index, final float k) {
    if (index >= size) {
      throw new IndexOutOfBoundsException(
          "Index (" + index + ") is greater than or equal to list size (" + size + ")");
    }
		float old = a[index];
	 a[index] = k;
	 return old;
	}
	@Override
	public void clear() {
	 size = 0;
	}
	@Override
	public int size() {
	 return size;
	}
	@Override
	public void size(final int size) {
    if (size > a.length) {
      a = Arrays.forceCapacity(a, size, this.size);
    }
    if (size > this.size) {
      java.util.Arrays.fill(a, this.size, size, (0));
    }
	 this.size = size;
	}
	@Override
	public boolean isEmpty() {
	 return size == 0;
	}
	/** Trims this array list so that the capacity is equal to the size.
	 *
	 * @see java.util.ArrayList#trimToSize()
	 */
	public void trim() {
	 trim(0);
	}
	/** Trims the backing array if it is too large.
	 *
	 * If the current array length is smaller than or equal to
	 * {@code n}, this method does nothing. Otherwise, it trims the
	 * array length to the maximum between {@code n} and {@link #size()}.
	 *
	 * <p>This method is useful when reusing lists.  {@linkplain #clear() Clearing a
	 * list} leaves the array length untouched. If you are reusing a list
	 * many times, you can call this method with a typical
	 * size to avoid keeping around a very large array just
	 * because of a few large transient lists.
	 *
	 * @param n the threshold for the trimming.
	 */

	public void trim(final int n) {
    if (n >= a.length || size == a.length) {
      return;
    }
	 final float t[] = new float[Math.max(n, size)];
	 System.arraycopy(a, 0, t, 0, size);
	 a = t;
	}
	private class SubList extends FloatRandomAccessSubList {
	 private static final long serialVersionUID = -3185226345314976296L;
	 protected SubList(int from, int to) {
	  super(FloatArrayList.this, from, to);
	 }
	 // Most of the inherited methods should be fine, but we can override a few of them for performance.
	 // Needed because we can't access the parent class' instance variables directly in a different instance of SubList.
	 private float[] getParentArray() {
	  return a;
	 }
	 @Override
	 public float getAt(int i) {
	  ensureRestrictedIndex(i);
	  return a[i + from];
	 }

	 @Override
	 public FloatListIterator listIterator(int index) {
	  throw new RuntimeException("");
	 }
	 private final class SubListSpliterator extends FloatSpliterators.LateBindingSizeIndexBasedSpliterator {
	  // We are using pos == 0 to be 0 relative to real array 0
	  SubListSpliterator() {
	   super(from);
	  }
	  private SubListSpliterator(int pos, int maxPos) {
	   super(pos, maxPos);
	  }
	  @Override
	  protected final int getMaxPosFromBackingStore() { return to; }
	   @Override
	  protected final float get(int i) { return a[i]; }
	  @Override
	  protected final SubListSpliterator makeForSplit(int pos, int maxPos) {
	   return new SubListSpliterator(pos, maxPos);
	  }
	  @Override
	  public boolean tryAdvance(final FloatConsumer action) {
      if (pos >= getMaxPos()) {
        return false;
      }
	   action.accept(a[pos++]);
	   return true;
	  }
	  @Override
	  public void forEachRemaining(final FloatConsumer action) {
	   final int max = getMaxPos();
	   while(pos < max) {
	    action.accept(a[pos++]);
	   }
	  }
	 }
	 @Override
	 public FloatSpliterator spliterator() {
	  return new SubListSpliterator();
	 }
	 boolean contentsEquals(float[] otherA, int otherAFrom, int otherATo) {
     if (a == otherA && from == otherAFrom && to == otherATo) {
       return true;
     }
	  if (otherATo - otherAFrom != size()) {
	   return false;
	  }
	  int pos = from, otherPos = otherAFrom;
	  // We have already assured that the two ranges are the same size, so we only need to check one bound.
	  // Make sure to split out the reference equality case when you do this.
     while (pos < to) {
       if (a[pos++] != otherA[otherPos++]) {
         return false;
       }
     }
	  return true;
	 }
	 @Override
	 public boolean equals(Object o) {
     if (o == this) {
       return true;
     }
     if (o == null) {
       return false;
     }
     if (!(o instanceof java.util.List)) {
       return false;
     }
	  if (o instanceof FloatArrayList) {
	  
	   FloatArrayList other = (FloatArrayList) o;
	   return contentsEquals(other.a, 0, other.size());
	  }
	  if (o instanceof FloatArrayList.SubList) {
	  
	   FloatArrayList.SubList other = (FloatArrayList.SubList) o;
	   return contentsEquals(other.getParentArray(), other.from, other.to);
	  }
	  return super.equals(o);
	 }
	
	 int contentsCompareTo(float[] otherA, int otherAFrom, int otherATo) {
     if (a == otherA && from == otherAFrom && to == otherATo) {
       return 0;
     }
		 float e1, e2;
	  int r, i, j;
	  for(i = from, j = otherAFrom; i < to && i < otherATo; i++, j++) {
	   e1 = a[i];
	   e2 = otherA[j];
      if ((r = (Float.compare((e1), (e2)))) != 0) {
        return r;
      }
	  }
	  return i < otherATo ? -1 : (i < to ? 1 : 0);
	 }
	
	 @Override
	 public int compareTo(final java.util.List <? extends Float> l) {
	  if (l instanceof FloatArrayList) {
	  
	   FloatArrayList other = (FloatArrayList) l;
	   return contentsCompareTo(other.a, 0, other.size());
	  }
	  if (l instanceof FloatArrayList.SubList) {
	  
	   FloatArrayList.SubList other = (FloatArrayList.SubList) l;
	   return contentsCompareTo(other.getParentArray(), other.from, other.to);
	  }
	  return super.compareTo(l);
	 }

		@Override
		public void sort() {
			java.util.Arrays.sort(a, from, to);
		}

		@Override
		public void sortReverse() {
			sort();
			for (int i = from; i < to / 2; i++) {
				float temp = a[i];
				a[i] = a[size - 1 - i];
				a[size - 1 - i] = temp;
			}
		}
	}
	@Override
	public FloatList subList(int from, int to) {
    if (from == 0 && to == size()) {
      return this;
    }
	 ensureIndex(from);
	 ensureIndex(to);
    if (from > to) {
      throw new IndexOutOfBoundsException(
          "Start index (" + from + ") is greater than end index (" + to + ")");
    }
	 return new SubList(from, to);
	}
	/** Copies element of this type-specific list into the given array using optimized system calls.
	 *
	 * @param from the start index (inclusive).
	 * @param a the destination array.
	 * @param offset the offset into the destination array where to store the first element copied.
	 * @param length the number of elements to be copied.
	 */
	@Override
	public void getElements(final int from, final float[] a, final int offset, final int length) {
	 Arrays.ensureOffsetLength(a, offset, length);
	 System.arraycopy(this.a, from, a, offset, length);
	}
	/** Removes elements of this type-specific list using optimized system calls.
	 *
	 * @param from the start index (inclusive).
	 * @param to the end index (exclusive).
	 */
	@Override
	public void removeElements(final int from, final int to) {
		Arrays.ensureFromTo(size, from, to);
	 System.arraycopy(a, to, a, from, size - to);
	 size -= (to - from);
	}
	/** Adds elements to this type-specific list using optimized system calls.
	 *
	 * @param index the index at which to add elements.
	 * @param a the array containing the elements.
	 * @param offset the offset of the first element to add.
	 * @param length the number of elements to add.
	 */
	@Override
	public void addElements(final int index, final float a[], final int offset, final int length) {
	 ensureIndex(index);
		Arrays.ensureOffsetLength(a, offset, length);
	 grow(size + length);
	 System.arraycopy(this.a, index, this.a, index + length, size - index);
	 System.arraycopy(a, offset, this.a, index, length);
	 size += length;
	}
	/** Sets elements to this type-specific list using optimized system calls.
	 *
	 * @param index the index at which to start setting elements.
	 * @param a the array containing the elements.
	 * @param offset the offset of the first element to add.
	 * @param length the number of elements to add.
	 */
	@Override
	public void setElements(final int index, final float a[], final int offset, final int length) {
	 ensureIndex(index);
		Arrays.ensureOffsetLength(a, offset, length);
    if (index + length > size) {
      throw new IndexOutOfBoundsException(
          "End index (" + (index + length) + ") is greater than list size (" + size + ")");
    }
	 System.arraycopy(a, offset, this.a, index, length);
	}
	@Override
	public void forEach(final FloatConsumer action) {
	 for (int i = 0; i < size; ++i) {
	  action.accept(a[i]);
	 }
	}
	@Override
	public boolean addAll(int index, final FloatCollection c) {
	 if (c instanceof FloatList) {
	  return addAll(index, (FloatList )c);
	 }
	 ensureIndex(index);
	 int n = c.size();
    if (n == 0) {
      return false;
    }
	 grow(size + n);
	 System.arraycopy(a, index, a, index + n, size - index);
	 final FloatIterator i = c.iterator();
	 size += n;
    while (n-- != 0) {
      a[index++] = i.nextFloat();
    }
	 return true;
	}
	@Override
	public boolean addAll(final int index, final FloatList l) {
	 ensureIndex(index);
	 final int n = l.size();
    if (n == 0) {
      return false;
    }
	 grow(size + n);
	 System.arraycopy(a, index, a, index + n, size - index);
	 l.getElements(0, a, index, n);
	 size += n;
	 return true;
	}
	@Override
	public boolean removeAll(final FloatCollection c) {
	 final float[] a = this.a;
	 int j = 0;
	 for(int i = 0; i < size; i++)
     if (!c.contains(a[i])) {
       a[j++] = a[i];
     }
	 final boolean modified = size != j;
	 size = j;
	 return modified;
	}
	@Override
	public float[] toArray(float a[]) {
    if (a == null || a.length < size) {
      a = java.util.Arrays.copyOf(a, size);
    }
	 System.arraycopy(this.a, 0, a, 0, size);
	 return a;
	}
	@Override
	public FloatListIterator listIterator(final int index) {
	 ensureIndex(index);
	 return new FloatListIterator () {
	   int pos = index, last = -1;
	   @Override
	   public boolean hasNext() { return pos < size; }
	   @Override
	   public boolean hasPrevious() { return pos > 0; }

		 @Override
	   public float nextFloat() {
       if (!hasNext()) {
         throw new NoSuchElementException();
       }
       return a[last = pos++]; }
	   @Override
	   public float previousFloat() {
       if (!hasPrevious()) {
         throw new NoSuchElementException();
       }
       return a[last = --pos]; }
	   @Override
	   public int nextIndex() { return pos; }
	   @Override
	   public int previousIndex() { return pos - 1; }
	   @Override
	   public void add(float k) {
	    FloatArrayList.this.add(pos++, k);
	    last = -1;
	   }
	   @Override
	   public void set(float k) {
       if (last == -1) {
         throw new IllegalStateException();
       }
	    FloatArrayList.this.putAt(last, k);
	   }
	   @Override
	   public void remove() {
       if (last == -1) {
         throw new IllegalStateException();
       }
	    FloatArrayList.this.removeAt(last);
	    /* If the last operation was a next(), we are removing an element *before* us, and we must decrease pos correspondingly. */
       if (last < pos) {
         pos--;
       }
	    last = -1;
	   }
	   @Override
	   public void forEachRemaining(final FloatConsumer action) {
	    while (pos < size) {
	     action.accept(a[last = pos++]);
	    }
	   }

	   @Override
	   public int skip(int n) {
       if (n < 0) {
         throw new IllegalArgumentException("Argument must be nonnegative: " + n);
       }
	    final int remaining = size - pos;
	    if (n < remaining) {
	     pos += n;
	    } else {
	     n = remaining;
	     pos = size;
	    }
	    last = pos - 1;
	    return n;
	   }
	  };
	}
	// If you update this, you will probably want to update ArraySet as well
	private final class Spliterator implements FloatSpliterator {
	 // Until we split, we will track the size of the list.
	 // Once we split, then we stop updating on structural modifications.
	 // Aka, size is late-binding.
	 boolean hasSplit = false;
	 int pos, max;
	 public Spliterator() {
	  this(0, FloatArrayList.this.size, false);
	 }
	 private Spliterator(int pos, int max, boolean hasSplit) {
	  this.pos = pos;
	  this.max = max;
	  this.hasSplit = hasSplit;
	 }
	 private int getWorkingMax() {
	  return hasSplit ? max : FloatArrayList.this.size;
	 }
	 @Override
	 public int characteristics() { return FloatSpliterators.LIST_SPLITERATOR_CHARACTERISTICS; }
	 @Override
	 public long estimateSize() { return getWorkingMax() - pos; }
	 @Override
	 public boolean tryAdvance(final FloatConsumer action) {
     if (pos >= getWorkingMax()) {
       return false;
     }
	  action.accept(a[pos++]);
	  return true;
	 }
	 @Override
	 public void forEachRemaining(final FloatConsumer action) {
	  for (final int max = getWorkingMax(); pos < max; ++pos) {
	   action.accept(a[pos]);
	  }
	 }
	 @Override
	 public long skip(long n) {
     if (n < 0) {
       throw new IllegalArgumentException("Argument must be nonnegative: " + n);
     }
	  final int max = getWorkingMax();
     if (pos >= max) {
       return 0;
     }
	  final int remaining = max - pos;
	  if (n < remaining) {
	   pos = SafeMath.safeLongToInt(pos + n);
	   return n;
	  }
	  n = remaining;
	  pos = max;
	  return n;
	 }
	 @Override
	 public FloatSpliterator trySplit() {
	  final int max = getWorkingMax();
	  int retLen = (max - pos) >> 1;
     if (retLen <= 1) {
       return null;
     }
	  // Update instance max with the last seen list size (if needed) before continuing
	  this.max = max;
	  int myNewPos = pos + retLen;
	  int retMax = myNewPos;
	  int oldPos = pos;
	  this.pos = myNewPos;
	  this.hasSplit = true;
	  return new Spliterator(oldPos, retMax, true);
	 }
	}
	/** {@inheritDoc}
	 *
	 * <p>The returned spliterator is late-binding; it will track structural changes
	 * after the current index, up until the first {@link java.util.Spliterator#trySplit() trySplit()},
	 * at which point the maximum index will be fixed.
	 * <br>Structural changes before the current index or after the first
	 * {@link java.util.Spliterator#trySplit() trySplit()} will result in unspecified behavior.
	 */
	@Override
	public FloatSpliterator spliterator() {
	 // If it wasn't for the possibility of the list being expanded or shrunk,
	 // we could return SPLITERATORS.wrap(a, 0, size).
	 return new Spliterator();
	}

	@Override
	public void sort() {
		java.util.Arrays.sort(a, 0, size);
	}

	@Override
	public void sortReverse() {
		sort();
		for (int i = 0; i < size / 2; i++) {
			float temp = a[i];
			a[i] = a[size - 1 - i];
			a[size - 1 - i] = temp;
		}
	}

	/** Compares this type-specific array list to another one.
	 *
	 * This method exists only for sake of efficiency. The implementation
	 * inherited from the abstract implementation would already work.
	 *
	 * @param l a type-specific array list.
	 * @return true if the argument contains the same elements of this type-specific array list.
	 */
	public boolean equals(final FloatArrayList l) {
    if (l == this) {
      return true;
    }
	 int s = size();
    if (s != l.size()) {
      return false;
    }
	 final float[] a1 = a;
	 final float[] a2 = l.a;
    if (a1 == a2 && s == l.size()) {
      return true;
    }
    while (s-- != 0) {
      if (a1[s] != a2[s]) {
        return false;
      }
    }
	 return true;
	}
	@SuppressWarnings("unlikely-arg-type")
	@Override
	public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (o == null) {
      return false;
    }
    if (!(o instanceof java.util.List)) {
      return false;
    }
	 if (o instanceof FloatArrayList) {
	  // Safe cast because we are only going to take elements from other list, never give them
	  return equals((FloatArrayList) o);
	 }
	 if (o instanceof FloatArrayList.SubList) {
	  // Safe cast because we are only going to take elements from other list, never give them
	  // Sublist has an optimized sub-array based comparison, reuse that.
	  return ((FloatArrayList.SubList)o).equals(this);
	 }
	 return super.equals(o);
	}
	/** Compares this array list to another array list.
	 *
	 * This method exists only for sake of efficiency. The implementation
	 * inherited from the abstract implementation would already work.
	 *
	 * @param l an array list.
	 * @return a negative integer,
	 * zero, or a positive integer as this list is lexicographically less than, equal
	 * to, or greater than the argument.
	 */

	public int compareTo(final FloatArrayList l) {
	 final int s1 = size(), s2 = l.size();
	 final float a1[] = a, a2[] = l.a;
    if (a1 == a2 && s1 == s2) {
      return 0;
    }
		float e1, e2;
	 int r, i;
	 for(i = 0; i < s1 && i < s2; i++) {
	  e1 = a1[i];
	  e2 = a2[i];
     if ((r = (Float.compare((e1), (e2)))) != 0) {
       return r;
     }
	 }
	 return i < s2 ? -1 : (i < s1 ? 1 : 0);
	}

	@Override
	public int compareTo(final java.util.List <? extends Float> l) {
	 if (l instanceof FloatArrayList) {
	  return compareTo((FloatArrayList)l);
	 }
	 if (l instanceof FloatArrayList.SubList) {
	  // Must negate because we are inverting the order of the comparison.
	  return -((FloatArrayList.SubList) l).compareTo(this);
	 }
	 return super.compareTo(l);
	}
	private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
	 s.defaultWriteObject();
	 for(int i = 0; i < size; i++) s.writeFloat(a[i]);
	}

	private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
	 s.defaultReadObject();
	 a = new float[size];
	 for(int i = 0; i < size; i++) a[i] = s.readFloat();
	}
}