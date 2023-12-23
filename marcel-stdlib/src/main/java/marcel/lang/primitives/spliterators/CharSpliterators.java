package marcel.lang.primitives.spliterators;

import marcel.lang.primitives.iterators.CharIterator;
import marcel.lang.util.Arrays;
import marcel.lang.util.Comparators;
import marcel.lang.util.SafeMath;
import marcel.lang.util.function.CharComparator;
import marcel.lang.util.function.CharConsumer;
import marcel.lang.util.function.CharPredicate;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

public final class CharSpliterators {
	private CharSpliterators() {}
	static final int BASE_SPLITERATOR_CHARACTERISTICS = Spliterator.NONNULL;
	// Default characteristics for various Collection implementations
	public static final int COLLECTION_SPLITERATOR_CHARACTERISTICS = BASE_SPLITERATOR_CHARACTERISTICS | Spliterator.SIZED;
	public static final int LIST_SPLITERATOR_CHARACTERISTICS = COLLECTION_SPLITERATOR_CHARACTERISTICS | Spliterator.ORDERED | Spliterator.SUBSIZED;
	public static final int SET_SPLITERATOR_CHARACTERISTICS = COLLECTION_SPLITERATOR_CHARACTERISTICS | Spliterator.DISTINCT;
	private static final int SORTED_CHARACTERISTICS = Spliterator.ORDERED | Spliterator.SORTED;
	public static final int SORTED_SET_SPLITERATOR_CHARACTERISTICS = SET_SPLITERATOR_CHARACTERISTICS | SORTED_CHARACTERISTICS;
	/** A class returning no elements and a type-specific spliterator interface.
	 *
	 * <p>This class may be useful to implement your own in case you subclass
	 * a type-specific spliterator.
	 */
	public static class EmptySpliterator implements CharSpliterator, java.io.Serializable, Cloneable {
	 private static final long serialVersionUID = 8379247926738230492L;
	 private static final int CHARACTERISTICS = Spliterator.SIZED | Spliterator.SUBSIZED;
	 protected EmptySpliterator() {}
	 @Override
	 public boolean tryAdvance(final CharConsumer action) { return false; }
	 @Deprecated
	 @Override
	 public boolean tryAdvance(final Consumer<? super Character> action) { return false; }
	 @Override
	 public CharSpliterator trySplit() { return null; }
	 @Override
	 public long estimateSize() { return 0; }
	 @Override
	 public int characteristics() { return CHARACTERISTICS; }
	 @Override
	 public void forEachRemaining(final CharConsumer action) { }
	 @Deprecated
	 @Override
	 public void forEachRemaining(final Consumer<? super Character> action) { }
	 @Override
	 public Object clone() { return EMPTY_SPLITERATOR; }
	 private Object readResolve() { return EMPTY_SPLITERATOR; }
	}
	/** An empty spliterator. It is serializable and cloneable.
	 *
	 * <p>The class of this objects represent an abstract empty spliterator
	 * that can iterate as a type-specific spliterator.
	 */
	public static final EmptySpliterator EMPTY_SPLITERATOR = new EmptySpliterator();
	/** a spliterator returning a single element. */
	private static class SingletonSpliterator implements CharSpliterator {
	 private final char element;
	 private final CharComparator comparator;
	 private boolean consumed = false;
	 private static final int CHARACTERISTICS = BASE_SPLITERATOR_CHARACTERISTICS
	  | Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED
	  | Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.IMMUTABLE;
	 public SingletonSpliterator(final char element) {
	  this(element, null);
	 }
	 public SingletonSpliterator(final char element, final CharComparator comparator) {
	  this.element = element;
	  this.comparator = comparator;
	 }
	 @Override
	 public boolean tryAdvance(CharConsumer action) {
	  Objects.requireNonNull(action);
	  if (consumed) return false;
	  // Existing JVM implementations advance even if the action throw.
	  consumed = true;
	  action.accept(element);
	  return true;
	 }
	 @Override
	 public CharSpliterator trySplit() { return null; }
	 @Override
	 public long estimateSize() { return consumed ? 0 : 1; }
	 @Override
	 public int characteristics() {
	  return CHARACTERISTICS;
	 }
	 @Override
	 public void forEachRemaining(final CharConsumer action) {
	  Objects.requireNonNull(action);
	  if (!consumed) {
	   consumed = true;
	   action.accept(element);
	  }
	 }
	 @Override
	 public CharComparator getComparator() {
	  return comparator;
	 }
	 @Override
	 public long skip(long n) {
	  if (n < 0) throw new IllegalArgumentException("Argument must be nonnegative: " + n);
	  if (n == 0 || consumed) return 0;
	  consumed = true;
	  return 1;
	 }
	}
	/** Returns a spliterator that iterates just over the given element.
	 *
	 * @param element the only element to be returned by a type-specific spliterator.
	 * @return a spliterator that iterates just over {@code element}.
	 */
	public static CharSpliterator singleton(final char element) {
	 return new SingletonSpliterator (element);
	}
	/** Returns a spliterator that iterates just over the given element.
	 *
	 * <p>The {@link Spliterator#getComparator()} method will return the given comparator.
	 * This is within spec because sequences of size 1 are trivially sorted for any
	 * comparison function.
	 *
	 * @param element the only element to be returned by a type-specific spliterator.
	 * @param comparator the comparator to return when {@link Spliterator#getComparator()} is called.
	 * @return a spliterator that iterates just over {@code element}.
	 */
	public static CharSpliterator singleton(final char element, final CharComparator comparator) {
	 return new SingletonSpliterator (element, comparator);
	}
	/** A class to wrap arrays in spiterators. */
	private static class ArraySpliterator implements CharSpliterator {
	 private static final int BASE_CHARACTERISTICS = BASE_SPLITERATOR_CHARACTERISTICS
	  | Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED;
	 final char[] array;
	 private final int offset;
	 private final int length;
		private int curr;
	 final int characteristics;
	 public ArraySpliterator(final char[] array, final int offset, final int length, int additionalCharacteristics) {
	  this.array = array;
	  this.offset = offset;
	  this.length = length;
	  characteristics = BASE_CHARACTERISTICS | additionalCharacteristics;
	 }
	 @Override
	 public boolean tryAdvance(CharConsumer action) {
	  if (curr >= length) return false;
	  Objects.requireNonNull(action);
	  action.accept(array[offset + curr++]);
	  return true;
	 }
	 @Override
	 public long estimateSize() { return length - curr; }
	 @Override
	 public int characteristics() { return characteristics; }
	 protected ArraySpliterator makeForSplit(int newOffset, int newLength) {
	  return new ArraySpliterator (array, newOffset, newLength, characteristics);
	 }
	 @Override
	 public CharSpliterator trySplit() {
	  int retLength = (length - curr) >> 1;
	  if (retLength <= 1) return null;
	  int myNewCurr = curr + retLength;
	  int retOffset = offset + curr;
	  // int myNewLength = length - retLength;
	  this.curr = myNewCurr;
	  // this.length = myNewLength;
	  return makeForSplit(retOffset, retLength);
	 }
	 @Override
	 public void forEachRemaining(final CharConsumer action) {
	  Objects.requireNonNull(action);
	  for (; curr < length; ++curr) {
	   action.accept(array[offset + curr]);
	  }
	 }
	 @Override
	 public long skip(long n) {
	  if (n < 0) throw new IllegalArgumentException("Argument must be nonnegative: " + n);
	  if (curr >= length) return 0;
	  final int remaining = length - curr;
	  if (n < remaining) {
	   curr = SafeMath.safeLongToInt(curr + n);
	   return n;
	  }
	  n = remaining;
	  curr = length;
	  return n;
	 }
	}
	private static class ArraySpliteratorWithComparator extends ArraySpliterator {
	 private final CharComparator comparator;
	 public ArraySpliteratorWithComparator(final char[] array, final int offset, final int length, int additionalCharacteristics, final CharComparator comparator) {
	  super(array, offset, length, additionalCharacteristics | SORTED_CHARACTERISTICS);
	  this.comparator = comparator;
	 }
	 @Override
	 protected ArraySpliteratorWithComparator makeForSplit(int newOffset, int newLength) {
	  return new ArraySpliteratorWithComparator (array, newOffset, newLength, characteristics, comparator);
	 }
	 @Override
	 public CharComparator getComparator() {
	  return comparator;
	 }
	}
	/** Wraps the given part of an array into a type-specific spliterator.
	 *
	 * <p>The type-specific spliterator returned by this method will iterate
	 * {@code length} times, advancing over consecutive elements of the given
	 * array starting from the one with index {@code offset}.
	 *
	 * <p>The returned spliterator will report {@linkplain Spliterator#characteristics() characteristics}
	 * Spliterator#SIZED, {@link Spliterator#SUBSIZED},
	 * {@link Spliterator#ORDERED}, and for primitive arrays, {@link Spliterator#NONNULL}.
	 *
	 * @param array an array to wrap into a type-specific spliterator.
	 * @param offset the first element of the array to be returned.
	 * @param length the number of elements to return.
	 * @return a spliterator that will iterate over {@code length} elements of {@code array} starting at position {@code offset}.
	 */
	public static CharSpliterator wrap(final char[] array, final int offset, final int length) {
	 Arrays.ensureOffsetLength(array, offset, length);
	 return new ArraySpliterator (array, offset, length, 0);
	}
	/** Wraps the given array into a type-specific spliterator.
	 *
	 * <p>The type-specific spliterator returned by this method will advance over
	 * all elements of the given array.
	 *
	 * <p>The returned spliterator will report {@linkplain Spliterator#characteristics() characteristics}
	 * Spliterator#SIZED, {@link Spliterator#SUBSIZED},
	 * {@link Spliterator#ORDERED}, and for primitive arrays, {@link Spliterator#NONNULL}.
	 *
	 * @param array an array to wrap into a type-specific spliterator.
	 * @return a spliterator that will iterate over the elements of {@code array}.
	 */
	public static CharSpliterator wrap(final char[] array) {
	 return new ArraySpliterator (array, 0, array.length, 0);
	}
	/** Wraps the given part of an array into a type-specific spliterator.
	 *
	 * <p>The type-specific spliterator returned by this method will iterate
	 * {@code length} times, advancing over consecutive elements of the given
	 * array starting from the one with index {@code offset}.
	 *
	 * <p>The returned spliterator will report {@linkplain Spliterator#characteristics() characteristics}
	 * Spliterator#SIZED, {@link Spliterator#SUBSIZED},
	 * {@link Spliterator#ORDERED}, and for primitive arrays, {@link Spliterator#NONNULL},
	 * on top of any additional characteristics given in {@code additionalCharacteristics} (for example, if
	 * the caller knows the backing array has distinct elements, they can pass Spliterator#DISTINCT).
	 *
	 * @param array an array to wrap into a type-specific spliterator.
	 * @param offset the first element of the array to be returned.
	 * @param length the number of elements to return.
	 * @param additionalCharacteristics any additional characteristics to report.
	 * @return a spliterator that will iterate over {@code length} elements of {@code array} starting at position {@code offset}.
	 */
	public static CharSpliterator wrap(final char[] array, final int offset, final int length, final int additionalCharacteristics) {
	 Arrays.ensureOffsetLength(array, offset, length);
	 return new ArraySpliterator (array, offset, length, additionalCharacteristics);
	}
	/** Wraps the given part of a sorted array into a type-specific spliterator.
	 *
	 * <p>It is the caller's responsibility to ensure the array is actually sorted using
	 * the comparator given.
	 *
	 * <p>The type-specific spliterator returned by this method will iterate
	 * {@code length} times, advancing over consecutive elements of the given
	 * array starting from the one with index {@code offset}.
	 *
	 * <p>The returned spliterator will report {@linkplain Spliterator#characteristics() characteristics}
	 * Spliterator#SIZED, {@link Spliterator#SUBSIZED},
	 * {@link Spliterator#ORDERED}, {@link Spliterator#SORTED}, and for primitive arrays,
	 * {@link Spliterator#NONNULL},
	 * on top of any additional characteristics given in {@code additionalCharacteristics} (for example, if
	 * the caller knows the backing array has distinct elements, they can pass Spliterator#DISTINCT).
	 *
	 * @param array an array to wrap into a type-specific spliterator.
	 * @param offset the first element of the array to be returned.
	 * @param length the number of elements to return.
	 * @param additionalCharacteristics any additional characteristics to report.
	 * @param comparator the comparator the array was sorted with (or {@code null} for natural ordering)
	 * @return a spliterator that will iterate over {@code length} elements of {@code array} starting at position {@code offset}.
	 */
	public static CharSpliterator wrapPreSorted(
	  final char[] array, final int offset, final int length, final int additionalCharacteristics, CharComparator comparator) {
	 Arrays.ensureOffsetLength(array, offset, length);
	 return new ArraySpliteratorWithComparator (array, offset, length, additionalCharacteristics, comparator);
	}
	/** Wraps the given part of a sorted array into a type-specific spliterator.
	 *
	 * <p>It is the caller's responsibility to ensure the array is actually sorted using
	 * the comparator given.
	 *
	 * <p>The type-specific spliterator returned by this method will iterate
	 * {@code length} times, advancing over consecutive elements of the given
	 * array starting from the one with index {@code offset}.
	 *
	 * <p>The returned spliterator will report {@linkplain Spliterator#characteristics() characteristics}
	 * Spliterator#SIZED, {@link Spliterator#SUBSIZED},
	 * {@link Spliterator#ORDERED}, {@link Spliterator#SORTED}, and for primitive arrays,
	 * {@link Spliterator#NONNULL}.
	 *
	 * @param array an array to wrap into a type-specific spliterator.
	 * @param offset the first element of the array to be returned.
	 * @param length the number of elements to return.
	 * @param comparator the comparator the array was sorted with (or {@code null} for natural ordering)
	 * @return a spliterator that will iterate over {@code length} elements of {@code array} starting at position {@code offset}.
	 */
	public static CharSpliterator wrapPreSorted(
	  final char[] array, final int offset, final int length, CharComparator comparator) {
	 return wrapPreSorted(array, offset, length, 0, comparator);
	}
	/** Wraps the given sorted array into a type-specific spliterator.
	 *
	 * <p>It is the caller's responsibility to ensure the array is actually sorted using
	 * the comparator given.
	 *
	 * <p>The type-specific spliterator returned by this method will advance over
	 * all elements of the given array.
	 *
	 * <p>The returned spliterator will report {@linkplain Spliterator#characteristics() characteristics}
	 * Spliterator#SIZED, {@link Spliterator#SUBSIZED},
	 * {@link Spliterator#ORDERED}, {@link Spliterator#SORTED}, and for primitive arrays,
	 * {@link Spliterator#NONNULL}.
	 *
	 * @param array an array to wrap into a type-specific spliterator.
	 * @param comparator the comparator the array was sorted with (or {@code null} for natural ordering)
	 * @return a spliterator that will iterate over {@code length} elements of {@code array} starting at position {@code offset}.
	 */
	public static CharSpliterator wrapPreSorted(
	  final char[] array, CharComparator comparator) {
	 return wrapPreSorted(array, 0, array.length, comparator);
	}
	// There is no non-comparator version of wrapPreSorted; because Spliterator has to return the Comparator
	// it is ordered with respect to, the caller should think about the Spliterator they use.
	// wrap, unwrap, and pour are not provided because if you are using Spliterators, you typically
	// are going to be using streams. That and Spliterator's API isn't well suited for these
	// types of tasks.
	private static class SpliteratorWrapper implements CharSpliterator {
	 final Spliterator<Character> i;
	 public SpliteratorWrapper(final Spliterator<Character> i) {
	  this.i = i;
	 }
	 // This is pretty much the only time overriding this overload is correct; we want to
	 // delegate as an Object consumer, not wrap it as a primitive one.

	 @SuppressWarnings("unchecked")
	 @Override
	 public boolean tryAdvance(final CharConsumer action) {
	  Objects.requireNonNull(action);
	  return i.tryAdvance(action);
	 }
	 @Deprecated
	 @Override
	 public boolean tryAdvance(final Consumer<? super Character> action) {
	  return i.tryAdvance(action);
	 }
	 // This is pretty much the only time overriding this overload is correct; we want to
	 // delegate as an Object consumer, not wrap it as a primitive one.
	 @SuppressWarnings("unchecked")
	 @Override
	 public void forEachRemaining(final CharConsumer action) {
	  Objects.requireNonNull(action);
	  i.forEachRemaining(action);
	 }
	 @Deprecated
	 @Override
	 public void forEachRemaining(final Consumer<? super Character> action) {
	  i.forEachRemaining(action);
	 }
	 @Override
	 public long estimateSize() { return i.estimateSize(); }
	 @Override
	 public int characteristics() { return i.characteristics(); }
	 @Override
	 public CharComparator getComparator() {
	  return Comparators.asCharComparator(i.getComparator());
	 }
	 @Override
	 public CharSpliterator trySplit() {
	  Spliterator<Character> innerSplit = i.trySplit();
	  if (innerSplit == null) return null;
	  return new SpliteratorWrapper (innerSplit);
	 }
	}
	private static class SpliteratorWrapperWithComparator extends SpliteratorWrapper {
	 final CharComparator comparator;
	 public SpliteratorWrapperWithComparator(final Spliterator<Character> i, final CharComparator comparator) {
	  super(i);
	  this.comparator = comparator;
	 }
	 @Override
	 public CharComparator getComparator() {
	  return comparator;
	 }
	 @Override
	 public CharSpliterator trySplit() {
	  Spliterator<Character> innerSplit = i.trySplit();
	  if (innerSplit == null) return null;
	  return new SpliteratorWrapperWithComparator (innerSplit, comparator);
	 }
	}
	private static class PrimitiveSpliteratorWrapper implements CharSpliterator {
	 final OfPrimitive<Character, CharConsumer, CharSpliterator> i;
	 public PrimitiveSpliteratorWrapper(final OfPrimitive<Character, CharConsumer, CharSpliterator> i) {
	  this.i = i;
	 }
	 @Override
	 public boolean tryAdvance(final CharConsumer action) {
	  return i.tryAdvance(action);
	 }
	 @Override
	 public void forEachRemaining(final CharConsumer action) {
	  i.forEachRemaining(action);
	 }
	 @Override
	 public long estimateSize() { return i.estimateSize(); }
	 @Override
	 public int characteristics() { return i.characteristics(); }
	 @Override
	 public CharComparator getComparator() {
	   return Comparators.asCharComparator(i.getComparator());
	 }
	 @Override
	 public CharSpliterator trySplit() {
		 OfPrimitive<Character, CharConsumer, CharSpliterator> innerSplit = i.trySplit();
	  if (innerSplit == null) return null;
	  return new PrimitiveSpliteratorWrapper(innerSplit);
	 }
	}
	private static class PrimitiveSpliteratorWrapperWithComparator extends PrimitiveSpliteratorWrapper {
	 final CharComparator comparator;
	 public PrimitiveSpliteratorWrapperWithComparator(final OfPrimitive<Character, CharConsumer, CharSpliterator> i, final CharComparator comparator) {
	  super(i);
	  this.comparator = comparator;
	 }
	 @Override
	 public CharComparator getComparator() {
	  return comparator;
	 }
	 @Override
	 public CharSpliterator trySplit() {
		 OfPrimitive<Character, CharConsumer, CharSpliterator> innerSplit = i.trySplit();
	  if (innerSplit == null) return null;
	  return new PrimitiveSpliteratorWrapperWithComparator(innerSplit, comparator);
	 }
	}
	/** Wraps a standard spliterator into a type-specific spliterator.
	 *
	 * <p>This method wraps a standard spliterator into a type-specific one which will handle the
	 * type conversions for you. Of course, any attempt to wrap a spliterator returning the
	 * instances of the wrong class will generate a {@link ClassCastException}. The
	 * returned spliterator is backed by {@code i}: changes to one of the spliterators
	 * will affect the other, too.
	 *
	 * <p>If {@code i} is already type-specific, it will returned and no new object
	 * will be generated.
	 *
	 * @param i a spliterator.
	 * @return a type-specific spliterator backed by {@code i}.
	 */
	@SuppressWarnings({"unchecked","rawtypes"})
	public static CharSpliterator asCharacterSpliterator(final Spliterator i) {
	 if (i instanceof CharSpliterator) return (CharSpliterator)i;
	 if (i instanceof Spliterator.OfPrimitive) return new PrimitiveSpliteratorWrapper ((Spliterator.OfPrimitive<Character, CharConsumer, CharSpliterator>)i);
	 return new SpliteratorWrapper (i);
	}
	/** Wraps a standard spliterator into a type-specific spliterator.
	 *
	 * <p>This method wraps a standard spliterator into a type-specific one which will handle the
	 * type conversions for you. Of course, any attempt to wrap a spliterator returning the
	 * instances of the wrong class will generate a {@link ClassCastException}. The
	 * returned spliterator is backed by {@code i}: changes to one of the spliterators
	 * will affect the other, too.
	 *
	 * <p>This method will cause the returned spliterator's {@link Spliterator#getComparator()} method
	 * to always return {@code comparatorOverride}, regardless of what the wrapped spliterator's
	 * {@code getComparator()} method returns.
	 *
	 * <p><b>NOTE:</b>This is mostly intended for supporting default
	 * implementations in interfaces that wrap JDK spliterators, and not a general purpose method.
	 *
	 * <p>If {@code i} is already type-specific, this method will throw, as such spliterators already
	 * have a {@code getComparator()} that returns a properly typed comparator.
	 *
	 * @param i a spliterator.
	 * @param comparatorOverride the comparator to return when {@link Spliterator#getComparator()}
	 * @return a type-specific spliterator backed by {@code i}.
	 */
	@SuppressWarnings({"unchecked","rawtypes"})
	public static CharSpliterator asCharacterSpliterator(final Spliterator i, final CharComparator comparatorOverride) {
	 if (i instanceof CharSpliterator) throw new IllegalArgumentException("Cannot override comparator on instance that is already a " + CharSpliterator.class.getSimpleName());
	 if (i instanceof Spliterator.OfPrimitive) return new PrimitiveSpliteratorWrapperWithComparator ((Spliterator.OfPrimitive<Character, CharConsumer, CharSpliterator>)i, comparatorOverride);
	 return new SpliteratorWrapperWithComparator (i, comparatorOverride);
	}
	/**
	 * Perform the given {@code action} on each element that matches the given {@code predicate}.
	 *
	 * <p>This is equivalent to {@code java.util.stream.StreamSupport.stream(spliterator).filter(predicate).forEach(action)}
	 * (substitute the proper primitive stream as needed), except it may perform better (but no potential for parallelism).
	 */
	public static void onEachMatching(final CharSpliterator spliterator, final CharPredicate predicate, final CharConsumer action) {
	 Objects.requireNonNull(predicate);
	 Objects.requireNonNull(action);
	 spliterator.forEachRemaining((char value) -> {
	  if (predicate.test(value)) {
	   action.accept(value);
	  }
	 });
	}
	/**
	 * A skeletal implementation for a spliterator backed by an index based data store. High performance
	 * concrete implementations (like the main Spliterator of ArrayList) generally should avoid using this
	 * and just implement the interface directly, but should be decent for less
	 * performance critical implementations.
	 *
	 * <p>This class is only appropriate for sequences that are at most {@link Character#MAX_VALUE} long.
	 * If your backing data store can be bigger then this, consider the equivalently named class in
	 * the type specific {@code BigSpliterators} class.
	 *
	 * <p>As the abstract methods in this class are used in inner loops, it is generally a
	 * good idea to override the class as {@code final} as to encourage the JVM to inline
	 * them (or alternatively, override the abstract methods as final).
	 */
	public static abstract class AbstractIndexBasedSpliterator implements CharSpliterator {
	 /** The current position index, the index of the item to be given after the next call to {@link #tryAdvance}.
		 *
		 * <p>This value will be between {@code minPos} and {@link #getMaxPos()} (exclusive) (on a best effort, so concurrent
		 * structural modifications may cause this to be violated, but that usually invalidates spliterators anyways).
		 * Thus {@code pos} being {@code minPos + 2} would mean {@link #tryAdvance}
		 * was called twice and the next call will give the third element of this spliterator.
		 */
	 protected int pos;
	 protected AbstractIndexBasedSpliterator(int initialPos) {
	  this.pos = initialPos;
	 }
	 // When you implement these, you should probably declare them final to encourage the JVM to inline them.
	 /** Get the item corresponding to the given index location.
		 *
		 * <p>Do <em>not</em> advance {@link #pos} in this method; the default {@link #tryAdvance} and
		 * {@link #forEachRemaining} methods takes care of this.
		 *
		 * <p>The {@code location} given will be between {@code minPos} and {@link #getMaxPos()} (exclusive).
		 * Thus, a {@code location} of {@code minPos + 2} would mean {@link #tryAdvance} was called twice
		 * and this method should return what the next call to tryAdvance() should give.
		 */
	 protected abstract char get(int location);
	 /** The maximum pos can be, and is the logical end (exclusive) of the "range".
		 *
		 * <p>If pos is equal to the return of this method, this means the last element has been returned and the next call to {@link #tryAdvance} will return {@code false}.
		 *
		 * <p>Usually set return the parent {@linkplain java.util.Collection#size() collection's size}, but does not have to be
		 * (for example, sublists and subranges).
		 *
		 * <p>This method allows the implementation to decide how it binds on the size (late or early).
		 * However, {@link EarlyBindingSizeIndexBasedSpliterator} and {@link LateBindingSizeIndexBasedSpliterator} give
		 * an implementation of this method for the two most common strategies.
		 */
	 protected abstract int getMaxPos();
	 /** Make a new spliterator to {@link #trySplit()} starting with the given {@code pos}
		 * and ending at the given {@code maxPos}.
		 *
		 * <p>An implementation is free to look at the range given, and if it deems it too small
		 * to split further, return {@code null}. In which case, {@link #trySplit()} will not
		 * modify the state of this spliterator.
		 *
		 * <p>Do <em>not</em> modify {@link #pos} in this method; the default {@link #trySplit()}
		 * method takes care of this.
		 *
		 * <p>To comply with the spec of {@link Spliterator#ORDERED}, this will
		 * only be called to create prefixes of the current sequence this spliterator is over,
		 * and this instance will start at the end of the returned sequence and have the same
		 * end point.
		 * As such, this method should also not change what {@link #getMaxPos()} returns.
		 */
	 protected abstract CharSpliterator makeForSplit(int pos, int maxPos);
	 /** Compute where to split on the next {@link #trySplit()}, given the current pos and
		 * {@link #getMaxPos()} (or any other metric the implementation wishes to use).
		 *
		 * <p>If a value {@code == pos} or {@code == getMaxPos()} is returned, the
		 * {@link #trySplit()} method will assume a split of size 0 was computed,
		 * and thus won't split or change state. If a value outside that range is
		 * returned, then {@link #trySplit()} will throw {@link IndexOutOfBoundsException}.
		 * In particular, this means that no handling of overflow or underflow
		 * is performed.
		 *
		 * The reasoning behind the throwing if out of range behavior is that, even
		 * though it can significantly slow the process of splitting, it is much better then
		 * risking a buggy implementation causing splits to stop happening much earlier then
		 * intended. Also, splitting is not usually in the "inner loop" of stream operations,
		 * so this slowness isn't in the bottleneck. That and we have already warned that
		 * high performance spliterators should prefer implementing all the methods themselves
		 * instead of through this interface.
		 *
		 * This default implementation is a simple split-by-2 strategy, dividing
		 * in the middle of pos and {@link #getMaxPos()}. It is unspecified whether
		 * the first range or the second range will be larger in the case of an odd length range.
		 */
	 protected int computeSplitPoint() {
	  // Overflow safe midpoint computation.
	  return pos + ((getMaxPos() - pos) / 2);
	 }
	 private void splitPointCheck(final int splitPoint, final int observedMax) {
	  if (splitPoint < pos || splitPoint > observedMax) {
	   throw new IndexOutOfBoundsException("splitPoint " + splitPoint + " outside of range of current position " + pos + " and range end " + observedMax);
	  }
	 }
	 // Since this is an index based spliterator, list characteristics make sense.
	 @Override
	 public int characteristics() { return CharSpliterators.LIST_SPLITERATOR_CHARACTERISTICS; }
	 @Override
	 public long estimateSize() { return (long)getMaxPos() - pos; }
	 @Override
	 public boolean tryAdvance(final CharConsumer action) {
	  if (pos >= getMaxPos()) return false;
	  action.accept(get(pos++));
	  return true;
	 }
	 @Override
	 public void forEachRemaining(final CharConsumer action) {
	  for (final int max = getMaxPos(); pos < max; ++pos) {
	   action.accept(get(pos));
	  }
	 }
	 @Override
	 public long skip(long n) {
	  if (n < 0) throw new IllegalArgumentException("Argument must be nonnegative: " + n);
	  final int max = getMaxPos();
	  if (pos >= max) return 0;
	  final int remaining = max - pos;
	  if (n < remaining) {
	   pos = SafeMath.safeLongToInt(pos + n);
	   return n;
	  }
	  n = remaining;
	  pos = max;
	  return n;
	 }
	 /** {@inheritDoc}
		 *
		 * This implementation always returns a prefix of the elements, in order to comply with
		 * the {@link Spliterator#ORDERED} property. This means this current iterator does not need to
		 * to update what {@link #getMaxPos()} returns in response to this method (but it may do
		 * "book-keeping" on it based on binding strategy).
		 *
		 * <p>The split point is computed by {@link #computeSplitPoint()}; see that method for details.
		 *
		 * @throws IndexOutOfBoundsException if the return of {@link #computeSplitPoint()} was
		 *  {@code < pos} or {@code > {@link #getMaxPos()}}.
		 */
	 @Override
	 public CharSpliterator trySplit() {
	  final int max = getMaxPos();
	  final int splitPoint = computeSplitPoint();
	  if (splitPoint == pos || splitPoint == max) return null;
	  splitPointCheck(splitPoint, max);
	  int oldPos = pos;
	  CharSpliterator maybeSplit = makeForSplit(oldPos, splitPoint);
	  if (maybeSplit != null) this.pos = splitPoint;
	  return maybeSplit;
	 }
	}
	/**
	 * A skeletal implementation for a spliterator backed by an index based data store. High performance
	 * concrete implementations (like the main Spliterator of ArrayList) generally should avoid using this
	 * and just implement the interface directly, but should be decent for less
	 * performance critical implementations.
	 *
	 * <p>This class implements an early binding strategy for {@link #getMaxPos()}. The last index
	 * this spliterator covers is fixed at construction time and does not vary on changes to the
	 * backing data store. This should usually be the {@linkplain java.util.Collection#size() size} of the
	 * backing data store (until a split at least), hence the class' name, but this is not required.
	 *
	 * <p>As the abstract methods in this class are used in inner loops, it is generally a
	 * good idea to override the class as {@code final} as to encourage the JVM to inline
	 * them (or alternatively, override the abstract methods as final).
	 */
	 public static abstract class EarlyBindingSizeIndexBasedSpliterator extends AbstractIndexBasedSpliterator {
	  /** The maximum {@link #pos} can be */
	  protected final int maxPos;
	 protected EarlyBindingSizeIndexBasedSpliterator(int initialPos, int maxPos) {
	  super(initialPos);
	  this.maxPos = maxPos;
	 }
	 @Override
	 protected final int getMaxPos() { return maxPos; }
	}
	/**
	 * A skeletal implementation for a spliterator backed by an index based data store. High performance
	 * concrete implementations (like the main Spliterator of ArrayList) generally should avoid using this
	 * and just implement the interface directly, but should be decent for less
	 * performance critical implementations.
	 *
	 * <p>This class implements a late binding strategy. On a new, non-split instance, the
	 * {@link #getMaxPos() max pos} will track the given data store (usually it's
	 * {@linkplain java.util.Collection#size() size}, hence the class' name). On the first
	 * {@linkplain #trySplit() split}, the last index will be read from the backing data store one
	 * last time and then be fixed for the remaining duration of this instance.<br>
	 * The returned split <em>should</em> should also be have a constant {@code maxPos}.
	 *
	 * <p>As the abstract methods in this class are used in inner loops, it is generally a
	 * good idea to override the class as {@code final} as to encourage the JVM to inline
	 * them (or alternatively, override the abstract methods as final).
	 */
	 public static abstract class LateBindingSizeIndexBasedSpliterator extends AbstractIndexBasedSpliterator {
	  /** The maximum {@link #pos} can be, or -1 if it hasn't been fixed yet. */
	  protected int maxPos = -1;
	  private boolean maxPosFixed;
	 protected LateBindingSizeIndexBasedSpliterator(int initialPos) {
	  super(initialPos);
	  this.maxPosFixed = false;
	 }
	 protected LateBindingSizeIndexBasedSpliterator(int initialPos, int fixedMaxPos) {
	  super(initialPos);
	  this.maxPos = fixedMaxPos;
	  this.maxPosFixed = true;
	 }
	 /** Return the maximum pos can be dynamically tracking the backing data store.
		 *
		 * <p>This method will be the return value of {@link #getMaxPos()} until this spliterator
		 * is {@linkplain #trySplit()} split, in which case its final return value will be saved
		 * and remain constant for the rest of the duration of this instance.
		 */
	 protected abstract int getMaxPosFromBackingStore();
	 @Override
	 protected final int getMaxPos() { return maxPosFixed ? maxPos : getMaxPosFromBackingStore(); }
	 @Override
	 public CharSpliterator trySplit() {
	  CharSpliterator maybeSplit = super.trySplit();
	  if (!maxPosFixed && maybeSplit != null) {
	   maxPos = getMaxPosFromBackingStore();
	   maxPosFixed = true;
	  }
	  return maybeSplit;
	 }
	}
	private static class IntervalSpliterator implements CharSpliterator {
	 private static final int DONT_SPLIT_THRESHOLD = 2;
	 private static final int CHARACTERISTICS = BASE_SPLITERATOR_CHARACTERISTICS
	  | Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED
	  | Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.IMMUTABLE;
	 private char curr;
		private final char to;
	 public IntervalSpliterator(final char from, final char to) {
	  this.curr = from;
	  this.to = to;
	 }
	 @Override
	 public boolean tryAdvance(final CharConsumer action) {
	  if (curr >= to) return false;
	  action.accept(curr++);
	  return true;
	 }
	 @Override
	 public void forEachRemaining(final CharConsumer action) {
	  Objects.requireNonNull(action);
	  for (; curr < to; ++curr) {
	   action.accept(curr);
	  }
	 }
	 @Override
	 public long estimateSize() {
	  return (long)to - curr;
	 }
	 @Override
	 public int characteristics() {
	  return CHARACTERISTICS;
	 }
	 @Override
	 public CharComparator getComparator() {
	  // Return null to indicate natural ordering.
	  return null;
	 }
	 @Override
	 public CharSpliterator trySplit() {
	  long remaining = to - curr;
	  char mid = (char)(curr + (remaining >> 1));
	  if (remaining >= 0 && remaining <= DONT_SPLIT_THRESHOLD) return null;
	  char old_curr = curr;
	  curr = mid;
	  return new IntervalSpliterator(old_curr, mid);
	 }
	 @Override
	 public long skip(long n) {
	  if (n < 0) throw new IllegalArgumentException("Argument must be nonnegative: " + n);
	  if (curr >= to) return 0;
	  // Can't do the traditional int remaining = to - curr
	  // because that could overflow, due to signed from and to (and thus curr).
	  long newCurr = curr + n;
	  // Can be less then "curr" if overflow
	  if (newCurr <= to && newCurr >= curr) {
	   curr = (char) SafeMath.safeLongToInt(newCurr);
	   return n;
	  }
	  n = to - curr;
	  curr = to;
	  return n;
	 }
	}
	/** Creates a type-specific spliterator over an interval.
	 *
	 * <p>The type-specific spliterator returned by this method will return the
	 * elements {@code from}, {@code from+1},&hellip;, {@code to-1}.
	 *
	 * @param from the starting element (inclusive).
	 * @param to the ending element (exclusive).
	 * @return a type-specific spliterator enumerating the elements from {@code from} to {@code to}.
	 */
	public static CharSpliterator fromTo(final char from, final char to) {
	 return new IntervalSpliterator(from, to);
	}
	private static class SpliteratorConcatenator implements CharSpliterator {
	 private static final int EMPTY_CHARACTERISTICS = Spliterator.SIZED | Spliterator.SUBSIZED;
	 // Neither SORTED nor DISTINCT "combine". Two combined spliterators with these characteristics may not have it.
	 // Example, {1, 2} and {1, 3}, both SORTED and DISTINCT, concat to {1, 2, 1, 3}, which isn't.
	 private static final int CHARACTERISTICS_NOT_SUPPORTED_WHILE_MULTIPLE = Spliterator.SORTED | Spliterator.DISTINCT;
	 final CharSpliterator[] a;
	 // Unlike the other classes in this file, length represents remaining, NOT the high mark for offset.
	 int offset, length;
	 /** The sum of estimatedRemaining <em>except</em> current offset */
	 long remainingEstimatedExceptCurrent = Long.MAX_VALUE;
	 int characteristics = 0;
	 public SpliteratorConcatenator(final CharSpliterator[] a, int offset, int length) {
	  this.a = a;
	  this.offset = offset;
	  this.length = length;
	  this.remainingEstimatedExceptCurrent = recomputeRemaining();
	  this.characteristics = computeCharacteristics();
	 }
	 private long recomputeRemaining() {
	  int curLength = length - 1;
	  int curOffset = offset + 1;
	  long result = 0;
	  while (curLength > 0) {
	   long cur = a[curOffset++].estimateSize();
	   --curLength;
	   if (cur == Long.MAX_VALUE) return Long.MAX_VALUE;
	   result += cur;
	   // Hit max or overflow
	   if (result == Long.MAX_VALUE || result < 0) {
	    return Long.MAX_VALUE;
	   }
	  }
	  return result;
	 }
	 /** Compute the intersection of all contained spliterators' characteristics. */
	 private int computeCharacteristics() {
	  if (length <= 0) {
	   return EMPTY_CHARACTERISTICS;
	  }
	  int current = ~0;
	  int curLength = length;
	  int curOffset = offset;
	  if (curLength > 1) {
	   current &= ~CHARACTERISTICS_NOT_SUPPORTED_WHILE_MULTIPLE;
	  }
	  while (curLength > 0) {
	   current &= a[curOffset++].characteristics();
	   --curLength;
	  }
	  return current;
	 }
	 private void advanceNextSpliterator() {
	  if (length <= 0) {
	   throw new AssertionError("advanceNextSpliterator() called with none remaining");
	  }
	  ++offset;
	  --length;
	  this.remainingEstimatedExceptCurrent = recomputeRemaining();
	  // We do NOT recompute the union of all characteristics here.
	  // Per spec, the only time characteristics() can change its
	  // return value on an instance is after a call to trySplt().
	 }
	 @Override
	 public boolean tryAdvance(final CharConsumer action) {
	  boolean any = false;
	  while(length > 0) {
	   if (a[offset].tryAdvance(action)) {
	    any = true;
	    break;
	   }
	   advanceNextSpliterator();
	  }
	  return any;
	 }
	 @Override
	 public void forEachRemaining(final CharConsumer action) {
	  while (length > 0) {
	   a[offset].forEachRemaining(action);
	   advanceNextSpliterator();
	  }
	 }
	 @Deprecated
	 @Override
	 public void forEachRemaining(final Consumer<? super Character> action) {
	  while (length > 0) {
	   a[offset].forEachRemaining(action);
	   advanceNextSpliterator();
	  }
	 }
	 @Override
	 public long estimateSize() {
	  if (length <= 0) return 0;
	  long est = a[offset].estimateSize() + remainingEstimatedExceptCurrent;
	  if (est < 0) {
	   // Overflow
	   return Long.MAX_VALUE;
	  }
	  return est;
	 }
	 @Override
	 public int characteristics() {
	  return characteristics;
	 }
	 @Override
	 public CharComparator getComparator() {
	  if (length == 1 && ((characteristics & Spliterator.SORTED) != 0) ) {
	   return (CharComparator) a[offset].getComparator();
	  }
	  throw new IllegalStateException();
	 }
	 @Override
	 public CharSpliterator trySplit() {
	  /* First we split on the spliterators array, with new concating spliterators for those array slices.
			 * Then if we can't split anymore due to only 1 spliterator we are "concating", return the splits
			 * of that single spliterator.
			 */
	  switch(length) {
	   case 0: return null;
	   case 1: {
	    // We are on the last spliterator. So now we ask it to split.
	    CharSpliterator split = a[offset].trySplit();
	    // It is possible for a Spliterator to change characteristics after a split.
	    // e.g. a SIZED but not SUBSIZED spliterator may split into non-SIZED spliterators.
	    this.characteristics = a[offset].characteristics();
	    return split;
	   }
	   case 2: {
	    // Per spec, this instance becomes suffix, and we return prefix.
	    // Fetch first to return
	    CharSpliterator split = a[offset++];
	    --length;
	    // assert length == 1;
	    // We become the second
	    this.characteristics = a[offset].characteristics();
	    this.remainingEstimatedExceptCurrent = 0;
	    return split;
	   }
	   default: // Fallthrough to below
	  }
	  int mid = length >> 1;
	  int ret_offset = offset;
	  int new_offset = offset + mid;
	  int ret_length = mid;
	  int new_length = length - mid;
	  this.offset = new_offset;
	  this.length = new_length;
	  this.remainingEstimatedExceptCurrent = recomputeRemaining();
	  this.characteristics = computeCharacteristics();
	  return new SpliteratorConcatenator (a, ret_offset, ret_length);
	 }
	 @Override
	 public long skip(long n) {
	  long skipped = 0;
	  if (length <= 0) return 0;
	  while(skipped < n && length >= 0) {
	   long curSkipped = a[offset].skip(n - skipped);
	   skipped += curSkipped;
	   // This relies on the sub spliterators implementing skip() correctly
	   // and always skipping as much as possible first call, so the next
	   // call to skip() will always return 0.
	   // If this assumption does not hold, change the condition to curSkipped == 0.
	   // That will make it work correctly in the face of non-conforming implementations,
	   // at the cost of doing at least 2 passes through this loop for each spliterator.
	   if (skipped < n) advanceNextSpliterator();
	  }
	  return skipped;
	 }
	}
	/** Concatenates all spliterators contained in an array.
	 *
	 * <p>This method returns a spliterator that will enumerate in order the elements returned
	 * by all spliterators contained in the given array.
	 *
	 * <p> Note: Due to there being no way to ensure theComparator is consistent
	 * between each inner spliterator, the returned spliterator's {@link Spliterator#getComparator()}
	 * will always throw {@link IllegalStateException}, even when if the current or even all
	 * the inner spliterators are {@linkplain Spliterator#SORTED sorted}.
	 *
	 * @param a an array of spliterators.
	 * @return a spliterator obtained by concatenation.
	 */
	public static CharSpliterator concat(final CharSpliterator... a) {
	 return concat(a, 0, a.length);
	}
	/** Concatenates a sequence of spliterators contained in an array.
	 *
	 * <p>This method returns a spliterator that will enumerate in order the elements returned
	 * by {@code a[offset]}, then those returned
	 * by {@code a[offset + 1]}, and so on up to
	 * {@code a[offset + length - 1]}.
	 *
	 * <p> Note: Due to there being no way to ensure the Comparator is consistent
	 * between each inner spliterator, the returned spliterator's {@link Spliterator#getComparator()}
	 * will always throw {@link IllegalStateException}, even when if the current or even all
	 * the inner spliterators are {@linkplain Spliterator#SORTED sorted}.
	 *
	 * @param a an array of spliterators.
	 * @param offset the index of the first spliterator to concatenate.
	 * @param length the number of spliterators to concatenate.
	 * @return a spliterator obtained by concatenation of {@code length} elements of {@code a} starting at {@code offset}.
	 */
	public static CharSpliterator concat(final CharSpliterator[] a, final int offset, final int length) {
	 return new SpliteratorConcatenator (a, offset, length);
	}
	private static class SpliteratorFromIterator implements CharSpliterator {
	 private static final int BATCH_INCREMENT_SIZE = 1024;
	 private static final int BATCH_MAX_SIZE = 1 << 25;
	 private final CharIterator iter;
	 final int characteristics;
	 private final boolean knownSize;
	 /** If {@code knownSize}, then has the remaining size left.
		 * Otherwise the value of this variable has no meaning.
		 */
	 private long size = Long.MAX_VALUE;
	 private int nextBatchSize = BATCH_INCREMENT_SIZE;
	 /** Used to "finish off" elements once we hit the end while splitting. */
	 private CharSpliterator delegate = null;
	 SpliteratorFromIterator(final CharIterator iter, int characteristics) {
	  this.iter = iter;
	  this.characteristics = BASE_SPLITERATOR_CHARACTERISTICS | characteristics;
	  knownSize = false;
	 }
	 SpliteratorFromIterator(final CharIterator iter, long size, int additionalCharacteristics) {
	  this.iter = iter;
	  knownSize = true;
	  this.size = size;
	  if ((additionalCharacteristics & Spliterator.CONCURRENT) != 0) {
	   this.characteristics = BASE_SPLITERATOR_CHARACTERISTICS | additionalCharacteristics;
	  } else {
	   this.characteristics = Spliterator.SIZED | Spliterator.SUBSIZED | BASE_SPLITERATOR_CHARACTERISTICS | additionalCharacteristics;
	  }
	 }
	 @Override
	 public boolean tryAdvance(final CharConsumer action) {
	  if (delegate != null){
	   boolean hadRemaining = delegate.tryAdvance(action);
	   if (!hadRemaining) delegate = null;
	   return hadRemaining;
	  }
	  if (!iter.hasNext()) return false;
	  --size;
	  action.accept(iter.nextChar());
	  return true;
	 }
	 @Override
	 public void forEachRemaining(final CharConsumer action) {
	  if (delegate != null) {
	   delegate.forEachRemaining(action);
	   delegate = null;
	  }
	  iter.forEachRemaining(action);
	  size = 0;
	 }
	 @Override
	 public long estimateSize() {
	  if (delegate != null) return delegate.estimateSize();
	  if (!iter.hasNext()) return 0;
	  // Size can be less then or equal to zero yet still have next if the iterator
	  // was concurrently modified, in which case we don't know anymore.
	  return knownSize && size >= 0 ? size : Long.MAX_VALUE;
	 }
	 @Override
	 public int characteristics() {
	  return characteristics;
	 }
	 protected CharSpliterator makeForSplit(char[] batch, int len) {
	  return wrap(batch, 0, len, characteristics);
	 }
	 @Override
	 public CharSpliterator trySplit() {
	  if (!iter.hasNext()) return null;
	  int batchSizeEst = knownSize && size > 0 ? (int)Math.min(nextBatchSize, size) : nextBatchSize;
	  char[] batch = new char[batchSizeEst];
	  int actualSeen = 0;
	  while (actualSeen < batchSizeEst && iter.hasNext()) {
	   batch[actualSeen++] = iter.nextChar();
	   --size;
	  }
	  // Check if the local size variable fell behind the backing source, and if so, fill up remaining of batch
	  if (batchSizeEst < nextBatchSize && iter.hasNext()) {
	   batch = java.util.Arrays.copyOf(batch, nextBatchSize);
	   while (iter.hasNext() && actualSeen < nextBatchSize) {
	    batch[actualSeen++] = iter.nextChar();
	    --size;
	   }
	  }
	  nextBatchSize = Math.min(BATCH_MAX_SIZE, nextBatchSize + BATCH_INCREMENT_SIZE);
	  // If we have none remaining, then set our delegate to "finish off" the batch we just made.
	  CharSpliterator split = makeForSplit(batch, actualSeen);
	  if (!iter.hasNext()) {
	   delegate = split;
	   return split.trySplit();
	  } else {
	   return split;
	  }
	 }
	 @Override
	 public long skip(long n) {
	  if (n < 0) throw new IllegalArgumentException("Argument must be nonnegative: " + n);
		 long skippedSoFar = 0;
		 while (skippedSoFar < n && iter.hasNext()) {
			 int skipped = iter.skip(SafeMath.safeLongToInt(Math.min(n, Integer.MAX_VALUE)));
			 size -= skipped;
			 skippedSoFar += skipped;
		 }
		 return skippedSoFar;
	 }
	}
	private static class SpliteratorFromIteratorWithComparator extends SpliteratorFromIterator {
	 private final CharComparator comparator;
	 SpliteratorFromIteratorWithComparator(final CharIterator iter, int additionalCharacteristics, final CharComparator comparator) {
	  super(iter, additionalCharacteristics | SORTED_CHARACTERISTICS);
	  this.comparator = comparator;
	 }
	 SpliteratorFromIteratorWithComparator(final CharIterator iter, long size, int additionalCharacteristics, final CharComparator comparator) {
	  super(iter, size, additionalCharacteristics | SORTED_CHARACTERISTICS);
	  this.comparator = comparator;
	 }
	 @Override
	 public CharComparator getComparator() {
	  return comparator;
	 }
	 @Override
	 protected CharSpliterator makeForSplit(char[] array, int len) {
	  return wrapPreSorted(array, 0, len, characteristics, comparator);
	 }
	}
	/** Wrap a type-specific {@link java.util.Iterator} of a known size as a type-specific Spliterator
	 *
	 * <p>The returned spliterator will report
	 * {@link Spliterator#characteristics() characteristics} {@code additionalCharacterisitcs},
	 * and for primitive types, {@link Spliterator#NONNULL}.
	 * It will also report Spliterator#SIZED, {@link Spliterator#SUBSIZED},
	 * unless {@link Spliterator#CONCURRENT} is to be reported, in which case these two
	 * are not implicitly reported.
	 *
	 * <p>Because {@link java.util.Iterator} is an inherently linear API, the returned spliterator will
	 * yield limited performance gains when run in parallel contexts, as the returned spliterator's
	 * {@link Spliterator#trySplit()} will have linear runtime.
	 *
	 * @param iter the type-specific {@code Iterator} to wrap
	 * @param size the number of elements the iterator will return
	 * @param additionalCharacterisitcs any additional characteristics to report
	 * @return a type-specific {@code Spliterator} that will give the same elements the iterator will return.
	 * @see java.util.Spliterators#spliterator(java.util.Iterator, long, int)
	 */
	public static CharSpliterator asSpliterator(final CharIterator iter, final long size, final int additionalCharacterisitcs) {
	 return new SpliteratorFromIterator (iter, size, additionalCharacterisitcs);
	}
	/** Wrap a type-specific, sorted {@link java.util.Iterator} of a known size as a type-specific Spliterator
	 *
	 * <p>It is the caller's responsibility to ensure the iterator's order
	 * is actually sorted according to the comparator given.
	 *
	 * <p>The returned spliterator will report
	 * {@link Spliterator#characteristics() characteristics} {@code additionalCharacterisitcs},
	 * {@link Spliterator#ORDERED}, {@link Spliterator#SORTED}, and for primitive types,
	 * {@link Spliterator#NONNULL}.
	 * It will also report Spliterator#SIZED, {@link Spliterator#SUBSIZED},
	 * unless {@link Spliterator#CONCURRENT} is to be reported, in which case these two
	 * are not implicitly reported.
	 *
	 * <p>Because {@link java.util.Iterator} is an inherently linear API, the returned spliterator will
	 * yield limited performance gains when run in parallel contexts, as the returned spliterator's
	 * {@link Spliterator#trySplit()} will have linear runtime.
	 *
	 * @param iter the type-specific {@code Iterator} to wrap
	 * @param size the number of elements the iterator will return
	 * @param additionalCharacterisitcs any additional characteristics to report
	 * @param comparator the comparator the iterator is ordered on (or {@code null} for natural ordering)
	 * @return a type-specific {@code Spliterator} that will give the same elements the iterator will return.
	 */
	public static CharSpliterator asSpliteratorFromSorted(
			final CharIterator iter, final long size, final int additionalCharacterisitcs, final CharComparator comparator) {
	 return new SpliteratorFromIteratorWithComparator (iter, size, additionalCharacterisitcs, comparator);
	}
	/** Wrap a type-specific {@link java.util.Iterator} of an unknown size as a type-specific Spliterator
	 *
	 * <p>The returned spliterator will report {@code additionalCharacterisitcs},
	 * and for primitive types, {@link Spliterator#NONNULL}.
	 *
	 * <p>Because {@link java.util.Iterator} is an inherently linear API, the returned spliterator will
	 * yield limited performance gains when run in parallel contexts, as the returned spliterator's
	 * {@link Spliterator#trySplit()} will have linear runtime.
	 *
	 * @param iter the type-specific {@code Iterator} to wrap
	 * @param characterisitcs the characteristics to report
	 * @return a type-specific {@code Spliterator} that will give the same elements the iterator will return.
	 * @see java.util.Spliterators#spliteratorUnknownSize(java.util.Iterator, int)
	 */
	public static CharSpliterator asSpliteratorUnknownSize(final CharIterator iter, final int characterisitcs) {
	 return new SpliteratorFromIterator (iter, characterisitcs);
	}
	/** Wrap a type-specific, sorted {@link java.util.Iterator} of an unknown size as a type-specific Spliterator
	 *
	 * <p>It is the caller's responsibility to ensure the iterator's order
	 * is actually sorted according to the comparator given.
	 *
	 * <p>The returned spliterator will report
	 * {@link Spliterator#characteristics() characteristics} {@code additionalCharacterisitcs},
	 * {@link Spliterator#ORDERED}, {@link Spliterator#SORTED}, and for primitive types,
	 * {@link Spliterator#NONNULL}.
	 *
	 * <p>Because {@link java.util.Iterator} is an inherently linear API, the returned spliterator will
	 * yield limited performance gains when run in parallel contexts, as the returned spliterator's
	 * {@link Spliterator#trySplit()} will have linear runtime.
	 *
	 * @param iter the type-specific {@code Iterator} to wrap
	 * @param additionalCharacterisitcs the characteristics to report
	 * @param comparator the comparator the iterator is ordered on (or {@code null} for natural ordering)
	 * @return a type-specific {@code Spliterator} that will give the same elements the iterator will return.
	 */
	public static CharSpliterator asSpliteratorFromSortedUnknownSize(final CharIterator iter, final int additionalCharacterisitcs, final CharComparator comparator) {
	 return new SpliteratorFromIteratorWithComparator (iter, additionalCharacterisitcs, comparator);
	}
	private static final class IteratorFromSpliterator implements CharIterator, CharConsumer {
	 private final CharSpliterator spliterator;
	 private char holder = (0);
	 /** Whether we have an element "peeked" from a hasNext that we have yet to return */
	 private boolean hasPeeked = false;
	 IteratorFromSpliterator(final CharSpliterator spliterator) {
	  this.spliterator = spliterator;
	 }
	 @Override
	 public void accept(final char item) {
	  holder = item;
	 }
	 @Override
	 public boolean hasNext() {
	  if (hasPeeked) return true;
	  boolean hadElement = spliterator.tryAdvance(this);
	  if (!hadElement) return false;
	  hasPeeked = true;
	  return true;
	 }
	 @Override
	 public char nextChar() {
	  if (hasPeeked) {
	   hasPeeked = false;
	   return holder;
	  }
	  boolean hadElement = spliterator.tryAdvance(this);
	  if (!hadElement) throw new java.util.NoSuchElementException();
	  return holder;
	 }
	 @Override
	 public void forEachRemaining(final CharConsumer action) {
	  if (hasPeeked) {
	   hasPeeked = false;
	   action.accept(holder);
	  }
	  spliterator.forEachRemaining(action);
	 }
	 @Override
	 public int skip(int n) {
	  if (n < 0) throw new IllegalArgumentException("Argument must be nonnegative: " + n);
	  int skipped = 0;
	  if (hasPeeked) {
	   hasPeeked = false;
	   spliterator.skip(1);
	   ++skipped;
	   --n;
	  }
	  if (n > 0) {
	   skipped += SafeMath.safeLongToInt(spliterator.skip(n));
	  }
	  return skipped;
	 }
	}
	/** Wrap a type-specific Spliterator as a type-specific {@link java.util.Iterator}
	 *
	 * @param spliterator the type-specific {@code Spliterator} to wrap
	 * @return a type-specific {@code Iterator} that will return the same elements the spliterator will give.
	 * @see java.util.Spliterators#iterator(Spliterator)
	 */
	public static CharIterator asIterator(final CharSpliterator spliterator) {
	 return new IteratorFromSpliterator (spliterator);
	}

}