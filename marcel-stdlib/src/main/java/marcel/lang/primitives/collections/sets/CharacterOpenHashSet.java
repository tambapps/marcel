package marcel.lang.primitives.collections.sets;

import marcel.lang.primitives.Hash;
import marcel.lang.primitives.collections.CharacterCollection;
import marcel.lang.primitives.collections.lists.CharacterArrayList;
import marcel.lang.primitives.iterators.CharacterIterator;
import marcel.lang.primitives.iterators.CharacterIterators;
import marcel.lang.primitives.spliterators.CharacterSpliterator;
import marcel.lang.primitives.spliterators.CharacterSpliterators;
import marcel.lang.util.function.CharacterConsumer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class CharacterOpenHashSet extends AbstractCharacterSet implements java.io.Serializable, Cloneable, Hash {
	private static final long serialVersionUID = 0L;
	private static final boolean ASSERTS = false;
	/** The array of keys. */
	protected transient char[] key;
	/** The mask for wrapping a position counter. */
	protected transient int mask;
	/** Whether this set contains the null key. */
	protected transient boolean containsNull;
	/** The current table size. Note that an additional element is allocated for storing the null key. */
	protected transient int n;
	/** Threshold after which we rehash. It must be the table size times {@link #f}. */
	protected transient int maxFill;
	/** We never resize below this threshold, which is the construction-time {#n}. */
	protected final transient int minN;
	/** Number of entries in the set (including the null key, if present). */
	protected int size;
	/** The acceptable load factor. */
	protected final float f;
	/** Creates a new hash set.
	 *
	 * <p>The actual table size will be the least power of two greater than {@code expected}/{@code f}.
	 *
	 * @param expected the expected number of elements in the hash set.
	 * @param f the load factor.
	 */
	public CharacterOpenHashSet(final int expected, final float f) {
		if (f <= 0 || f >= 1) throw new IllegalArgumentException("Load factor must be greater than 0 and smaller than 1");
		if (expected < 0) throw new IllegalArgumentException("The expected number of elements must be nonnegative");
		this.f = f;
		minN = n = Hash.arraySize(expected, f);
		mask = n - 1;
		maxFill = Hash.maxFill(n, f);
		key = new char[n + 1];
	}
	/** Creates a new hash set with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor.
	 *
	 * @param expected the expected number of elements in the hash set.
	 */
	public CharacterOpenHashSet(final int expected) {
		this(expected, DEFAULT_LOAD_FACTOR);
	}
	/** Creates a new hash set with initial expected {@link Hash#DEFAULT_INITIAL_SIZE} elements
	 * and {@link Hash#DEFAULT_LOAD_FACTOR} as load factor.
	 */
	public CharacterOpenHashSet() {
		this(DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR);
	}
	/** Creates a new hash set copying a given collection.
	 *
	 * @param c a Collection to be copied into the new hash set.
	 * @param f the load factor.
	 */
	public CharacterOpenHashSet(final Collection<? extends Character> c, final float f) {
		this(c.size(), f);
		addAll(c);
	}
	/** Creates a new hash set  with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor
	 * copying a given collection.
	 *
	 * @param c a Collection to be copied into the new hash set.
	 */
	public CharacterOpenHashSet(final Collection<? extends Character> c) {
		this(c, DEFAULT_LOAD_FACTOR);
	}
	/** Creates a new hash set copying a given type-specific collection.
	 *
	 * @param c a type-specific collection to be copied into the new hash set.
	 * @param f the load factor.
	 */
	public CharacterOpenHashSet(final CharacterCollection c, final float f) {
		this(c.size(), f);
		addAll(c);
	}
	/** Creates a new hash set  with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor
	 * copying a given type-specific collection.
	 *
	 * @param c a type-specific collection to be copied into the new hash set.
	 */
	public CharacterOpenHashSet(final CharacterCollection c) {
		this(c, DEFAULT_LOAD_FACTOR);
	}
	/** Creates a new hash set using elements provided by a type-specific iterator.
	 *
	 * @param i a type-specific iterator whose elements will fill the set.
	 * @param f the load factor.
	 */
	public CharacterOpenHashSet(final CharacterIterator i, final float f) {
		this(DEFAULT_INITIAL_SIZE, f);
		while(i.hasNext()) add(i.nextCharacter());
	}
	/** Creates a new hash set with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor using elements provided by a type-specific iterator.
	 *
	 * @param i a type-specific iterator whose elements will fill the set.
	 */
	public CharacterOpenHashSet(final CharacterIterator i) {
		this(i, DEFAULT_LOAD_FACTOR);
	}
	/** Creates a new hash set using elements provided by an iterator.
	 *
	 * @param i an iterator whose elements will fill the set.
	 * @param f the load factor.
	 */
	public CharacterOpenHashSet(final Iterator<?> i, final float f) {
		this(CharacterIterators.asCharacterIterator(i), f);
	}
	/** Creates a new hash set with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor using elements provided by an iterator.
	 *
	 * @param i an iterator whose elements will fill the set.
	 */
	public CharacterOpenHashSet(final Iterator<?> i) {
		this(CharacterIterators.asCharacterIterator(i));
	}
	/** Creates a new hash set and fills it with the elements of a given array.
	 *
	 * @param a an array whose elements will be used to fill the set.
	 * @param offset the first element to use.
	 * @param length the number of elements to use.
	 * @param f the load factor.
	 */
	public CharacterOpenHashSet(final char[] a, final int offset, final int length, final float f) {
		this(length < 0 ? 0 : length, f);
		marcel.lang.util.Arrays.ensureOffsetLength(a, offset, length);
		for(int i = 0; i < length; i++) add(a[offset + i]);
	}
	/** Creates a new hash set with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor and fills it with the elements of a given array.
	 *
	 * @param a an array whose elements will be used to fill the set.
	 * @param offset the first element to use.
	 * @param length the number of elements to use.
	 */
	public CharacterOpenHashSet(final char[] a, final int offset, final int length) {
		this(a, offset, length, DEFAULT_LOAD_FACTOR);
	}
	/** Creates a new hash set copying the elements of an array.
	 *
	 * @param a an array to be copied into the new hash set.
	 * @param f the load factor.
	 */
	public CharacterOpenHashSet(final char[] a, final float f) {
		this(a, 0, a.length, f);
	}

	public CharacterOpenHashSet(String s) {
		this(s.toCharArray());
	}

	/** Creates a new hash set with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor
	 * copying the elements of an array.
	 *
	 * @param a an array to be copied into the new hash set.
	 */
	public CharacterOpenHashSet(final char[] a) {
		this(a, DEFAULT_LOAD_FACTOR);
	}
	/** Creates a new empty hash set.
	 *
	 * @return a new empty hash set.
	 */
	public static CharacterOpenHashSet of() {
		return new CharacterOpenHashSet();
	}
	/** Creates a new hash set with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor
	 * using the given element.
	 *
	 * @param e the element that the returned set will contain.
	 * @return a new hash set with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor containing {@code e}.
	 */
	public static CharacterOpenHashSet of(final char e) {
		CharacterOpenHashSet result = new CharacterOpenHashSet(1, DEFAULT_LOAD_FACTOR);
		result.add(e);
		return result;
	}
	/** Creates a new hash set with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor
	 * using the elements given.
	 *
	 * @param e0 the first element.
	 * @param e1 the second element.
	 * @return a new hash set with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor containing {@code e0} and {@code e1}.
	 * @throws IllegalArgumentException if there were duplicate entries.
	 */
	public static CharacterOpenHashSet of(final char e0, final char e1) {
		CharacterOpenHashSet result = new CharacterOpenHashSet(2, DEFAULT_LOAD_FACTOR);
		result.add(e0);
		if (!result.add(e1)) {
			throw new IllegalArgumentException("Duplicate element: " + e1);
		}
		return result;
	}
	/** Creates a new hash set with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor
	 * using the elements given.
	 *
	 * @param e0 the first element.
	 * @param e1 the second element.
	 * @param e2 the third element.
	 * @return a new hash set with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor containing {@code e0}, {@code e1}, and {@code e2}.
	 * @throws IllegalArgumentException if there were duplicate entries.
	 */
	public static CharacterOpenHashSet of(final char e0, final char e1, final char e2) {
		CharacterOpenHashSet result = new CharacterOpenHashSet(3, DEFAULT_LOAD_FACTOR);
		result.add(e0);
		if (!result.add(e1)) {
			throw new IllegalArgumentException("Duplicate element: " + e1);
		}
		if (!result.add(e2)) {
			throw new IllegalArgumentException("Duplicate element: " + e2);
		}
		return result;
	}
	/** Creates a new hash set with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor
	 * using a list of elements.
	 *
	 * @param a a list of elements that will be used to initialize the new hash set.
	 * @return a new hash set with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor containing the elements of {@code a}.
	 * @throws IllegalArgumentException if a duplicate entry was encountered.
	 */
	public static CharacterOpenHashSet of(final char... a) {
		CharacterOpenHashSet result = new CharacterOpenHashSet(a.length, DEFAULT_LOAD_FACTOR);
		for (char element : a) {
			if (!result.add(element)) {
				throw new IllegalArgumentException("Duplicate element " + element);
			}
		}
		return result;
	}
	/** Collects the result of a primitive {@code Stream} into a new hash set.
	 *
	 * <p>This method performs a terminal operation on the given {@code Stream}
	 *
	 * Taking a primitive stream instead of returning something like a
	 * {@link java.util.stream.Collector Collector} is necessary because there is no
	 * primitive {@code Collector} equivalent in the Java API.
	 */
	public static CharacterOpenHashSet toSet(java.util.stream.IntStream stream) {
		return stream.collect(
				CharacterOpenHashSet::new,
				(a,b) -> a.add((char) b),
				CharacterOpenHashSet::addAll);
	}

	private int realSize() {
		return containsNull ? size - 1 : size;
	}
	/** Ensures that this set can hold a certain number of elements without rehashing.
	 *
	 * @param capacity a number of elements; there will be no rehashing unless
	 * the set {@linkplain #size() size} exceeds this number.
	 */
	public void ensureCapacity(final int capacity) {
		final int needed = Hash.arraySize(capacity, f);
		if (needed > n) rehash(needed);
	}
	private void tryCapacity(final int capacity) {
		final int needed = (int)Math.min(1 << 30, Math.max(2, Hash.nextPowerOfTwo((char)Math.ceil(capacity / f))));
		if (needed > n) rehash(needed);
	}
	@Override
	public boolean addAll(CharacterCollection c) {
		if (f <= .5) ensureCapacity(c.size()); // The resulting collection will be sized for c.size() elements
		else tryCapacity(size() + c.size()); // The resulting collection will be tentatively sized for size() + c.size() elements
		return super.addAll(c);
	}
	@Override
	public boolean addAll(Collection<? extends Character> c) {
		// The resulting collection will be at least c.size() big
		if (f <= .5) ensureCapacity(c.size()); // The resulting collection will be sized for c.size() elements
		else tryCapacity(size() + c.size()); // The resulting collection will be tentatively sized for size() + c.size() elements
		return super.addAll(c);
	}
	@Override
	public boolean add(final char k) {
		int pos;
		if (( (k) == (0) )) {
			if (containsNull) return false;
			containsNull = true;
		}
		else {
			char curr;
			final char[] key = this.key;
			// The starting point.
			if (! ( (curr = key[pos = (int)Hash.mix( (k) ) & mask]) == (0) )) {
				if (( (curr) == (k) )) return false;
				while(! ( (curr = key[pos = (pos + 1) & mask]) == (0) ))
					if (( (curr) == (k) )) return false;
			}
			key[pos] = k;
		}
		if (size++ >= maxFill) rehash(Hash.arraySize(size + 1, f));
		return true;
	}
	/** Shifts left entries with the specified hash code, starting at the specified position,
	 * and empties the resulting free entry.
	 *
	 * @param pos a starting position.
	 */
	protected final void shiftKeys(int pos) {
		// Shift entries with the same hash.
		int last, slot;
		char curr;
		final char[] key = this.key;
		for(;;) {
			pos = ((last = pos) + 1) & mask;
			for(;;) {
				if (( (curr = key[pos]) == (0) )) {
					key[last] = (0);
					return;
				}
				slot = (int)Hash.mix( (curr) ) & mask;
				if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) break;
				pos = (pos + 1) & mask;
			}
			key[last] = curr;
		}
	}
	private boolean removeEntry(final int pos) {
		size--;
		shiftKeys(pos);
		if (n > minN && size < maxFill / 4 && n > DEFAULT_INITIAL_SIZE) rehash(n / 2);
		return true;
	}
	private boolean removeNullEntry() {
		containsNull = false;
		key[n] = (0);
		size--;
		if (n > minN && size < maxFill / 4 && n > DEFAULT_INITIAL_SIZE) rehash(n / 2);
		return true;
	}
	@Override
	public boolean remove(final char k) {
		if (( (k) == (0) )) {
			if (containsNull) return removeNullEntry();
			return false;
		}
		char curr;
		final char[] key = this.key;
		int pos;
		// The starting point.
		if (( (curr = key[pos = (int)Hash.mix( (k) ) & mask]) == (0) )) return false;
		if (( (k) == (curr) )) return removeEntry(pos);
		while(true) {
			if (( (curr = key[pos = (pos + 1) & mask]) == (0) )) return false;
			if (( (k) == (curr) )) return removeEntry(pos);
		}
	}
	@Override
	public boolean contains(final char k) {
		if (( (k) == (0) )) return containsNull;
		char curr;
		final char[] key = this.key;
		int pos;
		// The starting point.
		if (( (curr = key[pos = (int)Hash.mix( (k) ) & mask]) == (0) )) return false;
		if (( (k) == (curr) )) return true;
		while(true) {
			if (( (curr = key[pos = (pos + 1) & mask]) == (0) )) return false;
			if (( (k) == (curr) )) return true;
		}
	}
	/* Removes all elements from this set.
	 *
	 * <p>To increase object reuse, this method does not change the table size.
	 * If you want to reduce the table size, you must use {@link #trim()}.
	 *
	 */
	@Override
	public void clear() {
		if (size == 0) return;
		size = 0;
		containsNull = false;
		Arrays.fill(key, (char) (0));
	}
	@Override
	public int size() {
		return size;
	}
	@Override
	public boolean isEmpty() {
		return size == 0;
	}
	/** An iterator over a hash set. */
	private final class SetIterator implements CharacterIterator {
		/** The index of the last entry returned, if positive or zero; initially, {@link #n}. If negative, the last
		 element returned was that of index {@code - pos - 1} from the {@link #wrapped} list. */
		int pos = n;
		/** The index of the last entry that has been returned (more precisely, the value of {@link #pos} if {@link #pos} is positive,
		 or {@link Integer#MIN_VALUE} if {@link #pos} is negative). It is -1 if either
		 we did not return an entry yet, or the last returned entry has been removed. */
		int last = -1;
		/** A downward counter measuring how many entries must still be returned. */
		int c = size;
		/** A boolean telling us whether we should return the null key. */
		boolean mustReturnNull = CharacterOpenHashSet.this.containsNull;
		/** A lazily allocated list containing elements that have wrapped around the table because of removals. */
		CharacterArrayList wrapped;
		@Override
		public boolean hasNext() {
			return c != 0;
		}
		@Override
		public char nextCharacter() {
			if (! hasNext()) throw new NoSuchElementException();
			c--;
			if (mustReturnNull) {
				mustReturnNull = false;
				last = n;
				return key[n];
			}
			final char key[] = CharacterOpenHashSet.this.key;
			for(;;) {
				if (--pos < 0) {
					// We are just enumerating elements from the wrapped list.
					last = Integer.MIN_VALUE;
					return wrapped.getAt(- pos - 1);
				}
				if (! ( (key[pos]) == (0) )) return key[last = pos];
			}
		}
		/** Shifts left entries with the specified hash code, starting at the specified position,
		 * and empties the resulting free entry.
		 *
		 * @param pos a starting position.
		 */
		private final void shiftKeys(int pos) {
			// Shift entries with the same hash.
			int last, slot;
			char curr;
			final char[] key = CharacterOpenHashSet.this.key;
			for(;;) {
				pos = ((last = pos) + 1) & mask;
				for(;;) {
					if (( (curr = key[pos]) == (0) )) {
						key[last] = (0);
						return;
					}
					slot = (int)Hash.mix( (curr) ) & mask;
					if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) break;
					pos = (pos + 1) & mask;
				}
				if (pos < last) { // Wrapped entry.
					if (wrapped == null) wrapped = new CharacterArrayList(2);
					wrapped.add(key[pos]);
				}
				key[last] = curr;
			}
		}
		@Override
		public void remove() {
			if (last == -1) throw new IllegalStateException();
			if (last == n) {
				CharacterOpenHashSet.this.containsNull = false;
				CharacterOpenHashSet.this.key[n] = (0);
			}
			else if (pos >= 0) shiftKeys(last);
			else {
				// We're removing wrapped entries.
				CharacterOpenHashSet.this.remove(wrapped.getAt(- pos - 1));
				last = -1; // Note that we must not decrement size
				return;
			}
			size--;
			last = -1; // You can no charer remove this entry.
		}
		@Override
		public void forEachRemaining(final CharacterConsumer action) {
			final char key[] = CharacterOpenHashSet.this.key;
			if (mustReturnNull) {
				mustReturnNull = false;
				last = n;
				action.accept(key[n]);
				c--;
			}
			while(c != 0) {
				if (--pos < 0) {
					// We are just enumerating elements from the wrapped list.
					last = Integer.MIN_VALUE;
					action.accept(wrapped.getAt(- pos - 1));
					c--;
				} else if (! ( (key[pos]) == (0) )) {
					action.accept(key[last = pos]);
					c--;
				}
			}
		}
	}
	@Override
	public CharacterIterator iterator() {
		return new CharacterOpenHashSet.SetIterator();
	}
	private final class SetSpliterator implements CharacterSpliterator {
		private static final int POST_SPLIT_CHARACTERISTICS = CharacterSpliterators.SET_SPLITERATOR_CHARACTERISTICS & ~java.util.Spliterator.SIZED;
		/** The index (which bucket) of the next item to give to the action.
		 * Unlike {@link CharacterOpenHashSet.SetIterator}, this counts up instead of down.
		 */
		int pos = 0;
		/** The maximum bucket (exclusive) to iterate to */
		int max = n;
		/** An upwards counter counting how many we have given */
		int c = 0;
		/** A boolean telling us whether we should return the null key. */
		boolean mustReturnNull = CharacterOpenHashSet.this.containsNull;
		boolean hasSplit = false;
		SetSpliterator() {}
		SetSpliterator(int pos, int max, boolean mustReturnNull, boolean hasSplit) {
			this.pos = pos;
			this.max = max;
			this.mustReturnNull = mustReturnNull;
			this.hasSplit = hasSplit;
		}
		@Override
		public boolean tryAdvance(final CharacterConsumer action) {
			if (mustReturnNull) {
				mustReturnNull = false;
				++c;
				action.accept(key[n]);
				return true;
			}
			final char key[] = CharacterOpenHashSet.this.key;
			while (pos < max) {
				if (! ( (key[pos]) == (0) )) {
					++c;
					action.accept(key[pos++]);
					return true;
				} else {
					++pos;
				}
			}
			return false;
		}
		@Override
		public void forEachRemaining(final CharacterConsumer action) {
			final char key[] = CharacterOpenHashSet.this.key;
			if (mustReturnNull) {
				mustReturnNull = false;
				action.accept(key[n]);
				++c;
			}
			while (pos < max) {
				if (! ( (key[pos]) == (0) )) {
					action.accept(key[pos]);
					++c;
				}
				++pos;
			}
		}
		@Override
		public int characteristics() {
			return hasSplit ? POST_SPLIT_CHARACTERISTICS : CharacterSpliterators.SET_SPLITERATOR_CHARACTERISTICS;
		}
		@Override
		public long estimateSize() {
			if (!hasSplit) {
				// Root spliterator; we know how many are remaining.
				return size - c;
			} else {
				// After we split, we can no charer know exactly how many we have (or at least not efficiently).
				// (size / n) * (max - pos) aka currentTableDensity * numberOfBucketsLeft seems like a good estimate.
				return Math.min(size - c, (char)(((double)realSize() / n) * (max - pos)) + (mustReturnNull ? 1 : 0));
			}
		}
		@Override
		public SetSpliterator trySplit() {
			if (pos >= max - 1) return null;
			int retLen = (max - pos) >> 1;
			if (retLen <= 1) return null;
			int myNewPos = pos + retLen;
			int retPos = pos;
			int retMax = myNewPos;
			// Since null is returned first, and the convention is that the returned split is the prefix of elements,
			// the split will take care of returning null (if needed), and we won't return it anymore.
			SetSpliterator split = new SetSpliterator(retPos, retMax, mustReturnNull, true);
			this.pos = myNewPos;
			this.mustReturnNull = false;
			this.hasSplit = true;
			return split;
		}
		@Override
		public long skip(long n) {
			if (n < 0) throw new IllegalArgumentException("Argument must be nonnegative: " + n);
			if (n == 0) return 0;
			char skipped = 0;
			if (mustReturnNull) {
				mustReturnNull = false;
				++skipped;
				--n;
			}
			final char key[] = CharacterOpenHashSet.this.key;
			while (pos < max && n > 0) {
				if (! ( (key[pos++]) == (0) )) {
					++skipped;
					--n;
				}
			}
			return skipped;
		}
	}
	@Override
	public CharacterSpliterator spliterator() {
		return new SetSpliterator();
	}
	@Override
	public void forEach(final CharacterConsumer action) {
		if (containsNull) action.accept(key[n]);
		final char key[] = this.key;
		for(int pos = n; pos-- != 0; ) if (! ( (key[pos]) == (0) )) action.accept(key[pos]);
	}
	/** Rehashes this set, making the table as small as possible.
	 *
	 * <p>This method rehashes the table to the smallest size satisfying the
	 * load factor. It can be used when the set will not be changed anymore, so
	 * to optimize access speed and size.
	 *
	 * <p>If the table size is already the minimum possible, this method
	 * does nothing.
	 *
	 * @return true if there was enough memory to trim the set.
	 * @see #trim(int)
	 */
	public boolean trim() {
		return trim(size);
	}
	/** Rehashes this set if the table is too large.
	 *
	 * <p>Let <var>N</var> be the smallest table size that can hold
	 * <code>max(n,{@link #size()})</code> entries, still satisfying the load factor. If the current
	 * table size is smaller than or equal to <var>N</var>, this method does
	 * nothing. Otherwise, it rehashes this set in a table of size
	 * <var>N</var>.
	 *
	 * <p>This method is useful when reusing sets.  {@linkplain #clear() Clearing a
	 * set} leaves the table size untouched. If you are reusing a set
	 * many times, you can call this method with a typical
	 * size to avoid keeping around a very large table just
	 * because of a few large transient sets.
	 *
	 * @param n the threshold for the trimming.
	 * @return true if there was enough memory to trim the set.
	 * @see #trim()
	 */
	public boolean trim(final int n) {
		final int l = Hash.nextPowerOfTwo((int)Math.ceil(n / f));
		if (l >= this.n || size > Hash.maxFill(l, f)) return true;
		try {
			rehash(l);
		}
		catch(OutOfMemoryError cantDoIt) { return false; }
		return true;
	}
	/** Rehashes the set.
	 *
	 * <p>This method implements the basic rehashing strategy, and may be
	 * overriden by subclasses implementing different rehashing strategies (e.g.,
	 * disk-based rehashing). However, you should not override this method
	 * unless you understand the internal workings of this class.
	 *
	 * @param newN the new size
	 */
	protected void rehash(final int newN) {
		final char key[] = this.key;
		final int mask = newN - 1; // Note that this is used by the hashing macro
		final char newKey[] = new char[newN + 1];
		int i = n, pos;
		for(int j = realSize(); j-- != 0;) {
			while(( (key[--i]) == (0) ));
			if (! ( (newKey[pos = (int)Hash.mix( (key[i]) ) & mask]) == (0) ))
				while (! ( (newKey[pos = (pos + 1) & mask]) == (0) ));
			newKey[pos] = key[i];
		}
		n = newN;
		this.mask = mask;
		maxFill = Hash.maxFill(n, f);
		this.key = newKey;
	}
	/** Returns a deep copy of this set.
	 *
	 * <p>This method performs a deep copy of this hash set; the data stored in the
	 * set, however, is not cloned. Note that this makes a difference only for object keys.
	 *
	 *  @return a deep copy of this set.
	 */
	@Override
	public CharacterOpenHashSet clone() {
		CharacterOpenHashSet c;
		try {
			c = (CharacterOpenHashSet)super.clone();
		}
		catch(CloneNotSupportedException cantHappen) {
			throw new InternalError();
		}
		c.key = key.clone();
		c.containsNull = containsNull;
		return c;
	}
	/** Returns a hash code for this set.
	 *
	 * This method overrides the generic method provided by the superclass.
	 * Since {@code equals()} is not overriden, it is important
	 * that the value returned by this method is the same value as
	 * the one returned by the overriden method.
	 *
	 * @return a hash code for this set.
	 */
	@Override
	public int hashCode() {
		int h = 0;
		for(int j = realSize(), i = 0; j-- != 0;) {
			while(( (key[i]) == (0) )) i++;
			h += (int) key[i];
			i++;
		}
		// Zero / null have hash zero.
		return h;
	}

}