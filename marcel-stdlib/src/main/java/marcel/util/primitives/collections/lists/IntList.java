/*
	* Copyright (C) 2002-2022 Sebastiano Vigna
	*
	* Licensed under the Apache License, Version 2.0 (the "License");
	* you may not use this file except in compliance with the License.
	* You may obtain a copy of the License at
	*
	*     http://www.apache.org/licenses/LICENSE-2.0
	*
	* Unless required by applicable law or agreed to in writing, software
	* distributed under the License is distributed on an "AS IS" BASIS,
	* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	* See the License for the specific language governing permissions and
	* limitations under the License.
	*/
package marcel.util.primitives.collections.lists;


import marcel.lang.IntRange;
import marcel.util.primitives.collections.IntCollection;
import marcel.util.primitives.collections.sets.IntOpenHashSet;
import marcel.util.primitives.collections.sets.IntSet;
import marcel.util.primitives.iterators.IntIterator;
import marcel.util.primitives.iterators.list.IntListIterator;
import marcel.util.primitives.spliterators.IntSpliterator;
import marcel.util.primitives.Arrays;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Spliterator;
import java.util.function.IntPredicate;

/** A type-specific {@link List}; provides some additional methods that use polymorphism to avoid (un)boxing.
	*
	* <p>Note that this type-specific interface extends {@link Comparable}: it is expected that implementing
	* classes perform a lexicographical comparison using the standard operator "less then" for primitive types,
	* and the usual {@link Comparable#compareTo(Object) compareTo()} method for objects.
	*
	* <p>Additionally, this interface strengthens {@link #iterator()}, {@link #listIterator()},
	* {@link #listIterator(int)} and {@link #subList(int,int)}. The former had been already
	* strengthened upstream, but unfortunately {@link List} re-specifies it.
	*
	* <p>Besides polymorphic methods, this interfaces specifies methods to copy into an array or remove contiguous
	* sublists. Although the abstract implementation of this interface provides simple, one-by-one implementations
	* of these methods, it is expected that concrete implementation override them with optimized versions.
	*
	* @see List
	*/
@NullMarked
public interface IntList extends List<Integer>, Comparable<List<? extends Integer>>, IntCollection {
	/** Returns a type-specific iterator on the elements of this list.
	 *
	 * This specification strengthens the one given in {@link List#iterator()}.
	 * It would not be normally necessary, but {@link Iterable#iterator()} is bizarrily re-specified
	 * in {@link List}.
	 * <p>Also, this is generally the only {@code iterator} method subclasses should override.
	 *
	 * @return an iterator on the elements of this list.
	 */
	@Override
	IntListIterator iterator();
	/** Returns a type-specific spliterator on the elements of this list.
	 *
	 * <p>List spliterators must report at least Spliterator#SIZED and {@link Spliterator#ORDERED}.
	 *
	 * <p>See {@link List#spliterator()} for more documentation on the requirements
	 * of the returned spliterator.
	 *
	 * This specification strengthens the one given in
	 * {@link java.util.Collection#spliterator()}, which was already
	 * strengthened in the corresponding type-specific class,
	 * but was weakened by the fact that this interface extends {@link List}.
	 * <p>Also, this is generally the only {@code spliterator} method subclasses should override.
	 *
	 * The default implementation returns a late-binding spliterator (see
	 * Spliterator for documentation on what binding policies mean).
	 * <ul>
	 * <li>For {@link java.util.RandomAccess RandomAccess} lists, this will return a spliterator
	 * that calls the type-specific {@link #get(int)} method on the appropriate indexes.</li>
	 * <li>Otherwise, the spliterator returned will wrap this instance's type specific {@link #iterator}.</li>
	 * </ul>
	 * <p>In either case, the spliterator reports Spliterator#SIZED,
	 * {@link Spliterator#SUBSIZED}, and {@link Spliterator#ORDERED}.
	 *
	 * As the non-{@linkplain java.util.RandomAccess RandomAccess} case is based on the
	 * iterator, and {@link java.util.Iterator} is an inherently linear API, the returned
	 * spliterator will yield limited performance gains when run in parallel contexts, as the
	 * returned spliterator's Spliterator#trySplit() will have linear runtime.
	 * <p>For {@link java.util.RandomAccess RandomAccess} lists, the parallel performance should
	 * be reasonable assuming {@link #get(int)} is truly constant time like {@link java.util.RandomAccess
	 * RandomAccess} suggests. 
	 *
	 * @return {@inheritDoc}
	 * @since 8.5.0
	 */
	@Override
	IntSpliterator spliterator();
	/** Returns a type-specific list iterator on the list.
	 *
	 * @see List#listIterator()
	 */
	@Override
	IntListIterator listIterator();
	/** Returns a type-specific list iterator on the list starting at a given index.
	 *
	 * @see List#listIterator(int)
	 */
	@Override
	IntListIterator listIterator(int index);
	/** Returns a type-specific view of the portion of this list from the index {@code from}, inclusive, to the index {@code to}, exclusive.
	 *
	 * This specification strengthens the one given in {@link List#subList(int,int)}.
	 *
	 * @see List#subList(int,int)
	 */
	@Override
	IntList subList(int from, int to);
	/** Sets the size of this list.
	 *
	 * <p>If the specified size is smaller than the current size, the last elements are
	 * discarded. Otherwise, they are filled with 0/{@code null}/{@code false}.
	 *
	 * @param size the new size.
	 */
	void size(int size);
	/** Copies (hopefully quickly) elements of this type-specific list into the given array.
	 *
	 * @param from the start index (inclusive).
	 * @param a the destination array.
	 * @param offset the offset into the destination array where to store the first element copied.
	 * @param length the number of elements to be copied.
	 */
	void getElements(int from, int a[], int offset, int length);
	/** Removes (hopefully quickly) elements of this type-specific list.
	 *
	 * @param from the start index (inclusive).
	 * @param to the end index (exclusive).
	 */
	void removeElements(int from, int to);
	/** Add (hopefully quickly) elements to this type-specific list.
	 *
	 * @param index the index at which to add elements.
	 * @param a the array containing the elements.
	 */
	void addElements(int index, int a[]);
	/** Add (hopefully quickly) elements to this type-specific list.
	 *
	 * @param index the index at which to add elements.
	 * @param a the array containing the elements.
	 * @param offset the offset of the first element to add.
	 * @param length the number of elements to add.
	 */
	void addElements(int index, int a[], int offset, int length);
	/** Set (hopefully quickly) elements to match the array given.
	 * @param a the array containing the elements.
	 * @since 8.3.0
	 */
	default void setElements(int a[]) {
	 setElements(0, a);
	}
	/** Set (hopefully quickly) elements to match the array given.
	 * @param index the index at which to start setting elements.
	 * @param a the array containing the elements.
	 * @since 8.3.0
	 */
	default void setElements(int index, int a[]) {
	 setElements(index, a, 0, a.length);
	}
	/** Set (hopefully quickly) elements to match the array given.
	 *
	 * Sets each in this list to the corresponding elements in the array, as if by
	 * <pre>
	 * ListIterator iter = listIterator(index);
	 * int i = 0;
	 * while (i &lt; length) {
	 *   iter.next();
	 *   iter.set(a[offset + i++]);
	 * }
	 * </pre>
	 * However, the exact implementation may be more efficient, taking into account
	 * whether random access is faster or not, or at the discretion of subclasses,
	 * abuse internals.
	 *
	 * @param index the index at which to start setting elements.
	 * @param a the array containing the elements
	 * @param offset the offset of the first element to add.
	 * @param length the number of elements to add.
	 * @since 8.3.0
	 */
	default void setElements(int index, int a[], int offset, int length) {
	 // We can't use AbstractList#ensureIndex, sadly.
	 if (index < 0) throw new IndexOutOfBoundsException("Index (" + index + ") is negative");
	 if (index > size()) throw new IndexOutOfBoundsException("Index (" + index + ") is greater than list size (" + (size()) + ")");
	 Arrays.ensureOffsetLength(a, offset, length);
	 if (index + length > size()) throw new IndexOutOfBoundsException("End index (" + (index + length) + ") is greater than list size (" + size() + ")");
	 IntListIterator iter = listIterator(index);
	 int i = 0;
	 while (i < length) {
	  iter.nextInt();
	  iter.set(a[offset + i++]);
	 }
	}
	/** Appends the specified element to the end of this list (optional operation).
	 * @see List#add(Object)
	 */
	@Override
	boolean add(int key);
	/** Inserts the specified element at the specified position in this list (optional operation).
	 * @see List#add(int,Object)
	 */
	void add(int index, int key);
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead. */
	@Deprecated
	@Override
	default void add(int index, Integer key) {
	 add(index, (key).intValue());
	}
	/** Inserts all of the elements in the specified type-specific collection into this type-specific list at the specified position (optional operation).
	 * @see List#addAll(int,java.util.Collection)
	 */
	boolean addAll(int index, IntCollection c);
	/** Replaces the element at the specified position in this list with the specified element (optional operation).
	 * @see List#set(int,Object)
	 */
	int putAt(int index, int k);
	/**
	 * Replaces each element of this list with the result of applying the
	 * operator to that element. 
	 * @param operator the operator to apply to each element.
	 * @see List#replaceAll
	 */
	default void replaceAll(final java.util.function.IntUnaryOperator operator) {
	 final IntListIterator iter = listIterator();
	 while(iter.hasNext()) {
	  iter.set(operator.applyAsInt(iter.nextInt()));
	 }
	}

	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	@SuppressWarnings("boxing")
	default void replaceAll(final java.util.function.UnaryOperator<Integer> operator) {
	 java.util.Objects.requireNonNull(operator);
	 // The instanceof and cast is required for performance. Without it, calls routed through this
	 // overload using a primitive consumer would go through the slow lambda.
	 replaceAll(operator instanceof java.util.function.IntUnaryOperator ? (java.util.function.IntUnaryOperator) operator : (java.util.function.IntUnaryOperator) operator::apply);
	}
	/** Returns the element at the specified position in this list.
	 * @see List#get(int)
	 */
	int getAt(int index);

	default Integer getAtSafe(int index) {
		return index >= 0 && index < size() ? getAt(index) : null;
	}

	default void putAtSafe(int index, int value) {
		if (index >= 0 && index < size()) {
			putAt(index, value);
		}
	}

	/** Returns the index of the first occurrence of the specified element in this list, or -1 if this list does not contain the element.
	 * @see List#indexOf(Object)
	 */
	int indexOf(int k);
	/** Returns the index of the last occurrence of the specified element in this list, or -1 if this list does not contain the element.
	 * @see List#lastIndexOf(Object)
	 */
	int lastIndexOf(int k);
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default boolean contains(@Nullable final Object key) {
	 return IntCollection.super.contains(key);
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead. */
	@Deprecated
	@Override
	default Integer get(int index) {
	 return Integer.valueOf(getAt(index));
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead. */
	@Deprecated
	@Override
	default int indexOf(Object o) {
	 return indexOf(((Integer)(o)).intValue());
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead. */
	@Deprecated
	@Override
	default int lastIndexOf(Object o) {
	 return lastIndexOf(((Integer)(o)).intValue());
	}
	/** {@inheritDoc}
	 * <p>This method specification is a workaround for
	 * <a href="http://bugs.java.com/bugdatabase/view_bug.do?bug_id=JDK-8177440">bug 8177440</a>.
	 * @deprecated Please use the corresponding type-specific method instead. */
	@Deprecated
	@Override
	default boolean add(Integer k) {
	 return add((k).intValue());
	}
	/** Removes the element at the specified position in this list (optional operation).
	 * @see List#remove(int)
	 */
	int removeAt(int index);
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default boolean remove(@Nullable final Object key) {
	 return IntCollection.super.remove(key);
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead. */
	@Deprecated
	@Override
	default Integer remove(int index) {
	 return Integer.valueOf(removeAt(index));
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead. */
	@Deprecated
	@Override
	default Integer set(int index, Integer k) {
	 return Integer.valueOf(putAt(index, (k).intValue()));
	}
	/** Inserts all of the elements in the specified type-specific list into this type-specific list at the specified position (optional operation).
	 * This method exists only for the sake of efficiency: override are expected to use {@link #getElements}/{@link #addElements}.
	 * This method delegates to the one accepting a collection, but it might be implemented more efficiently.
	 * @see List#addAll(int,Collection)
	 */
	default boolean addAll(int index, IntList l) {
	 return addAll(index, (IntCollection ) l);
	}
	/** Appends all of the elements in the specified type-specific list to the end of this type-specific list (optional operation).
	 * This method delegates to the index-based version, passing {@link #size()} as first argument.
	 * @see List#addAll(Collection)
	 */
	default boolean addAll(IntList l) {
	 return addAll(size(), l);
	}

	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default void sort(final java.util.Comparator<? super Integer> comparator) {
	 throw new UnsupportedOperationException("Not Implemented");
	}
	/** Sort a list using a type-specific comparator.
	 *
	 * <p>Pass {@code null} to sort using natural ordering.
	 * @see List#sort(java.util.Comparator)
	 *
	 * The default implementation dumps the elements into an array using
	 * {@link #toArray()}, sorts the array, then replaces all elements using the
	 * {@link #setElements} function.
	 *
	 *
	 * @since 8.3.0
	 */
	void sort();
	void sortReverse();

	/** Shuffles the specified list using the specified pseudorandom number generator.
	 *
	 * @param random a pseudorandom number generator.
	 */
	default void shuffle(final Random random) {
		for(int i = size(); i-- != 0;) {
			final int p = random.nextInt(i + 1);
			final int t = getAt(i);
			putAt(i, getAt(p));
			putAt(p, t);
		}
	}

	default int sum() {
		int sum = 0;
		for (int i = 0; i < size(); i++) {
			sum += getAt(i);
		}
		return sum;
	}

	default IntSet toSet() {
		IntSet set = new IntOpenHashSet(size());
		IntIterator iterator = iterator();
		while (iterator.hasNext()) {
			set.add(iterator.nextInt());
		}
		return set;
	}

	default IntList filter(IntPredicate predicate) {
		IntList list = new IntArrayList(size());
		for (int i = 0; i < size(); i++) {
			int e = getAt(i);
			if (predicate.test(e)) list.add(e);
		}
		return list;
	}

	/**
	 * Get the last element of the list. This method will throw an exception if the list is empty
	 *
	 * @return the last element of the list
	 */
	// object in order not to class with Java 21's getFirst
	default Integer getLast() {
		return getAt(size() - 1);
	}

	/**
	 * Get the first element of the list. This method will throw an exception if the list is empty
	 *
	 * @return the first element of the list
	 */
	// object in order not to class with Java 21's getFirst
	default Integer getFirst() {
		return getAt(0);
	}
	default void setFirst(Integer value) {
		putAt(0, value);
	}

	/**
	 * Sets the last element of the list. This method wil throw an exception if the list is empty
	 *
	 * @param value the value to set
	 */
	default void setLast(Integer value) {
		putAt(size() - 1, value);
	}


	/**
	 * Get the elements at the specified indexes from the range. The order of elements returned respects the order of the range
	 *
	 * @param range the range
	 * @return the elements at the specified indexes from the ranges
	 */
	default IntList getAt(IntRange range) {
		IntList subList = new IntArrayList(range.size());
		IntIterator iterator = range.iterator();
		while (iterator.hasNext()) subList.add(get(iterator.nextInt()));
		return subList;
	}

	default Integer findLast(IntPredicate predicate)  {
		int e;
		for (int i = size() - 1; i >= 0; i--) {
			e = getAt(i);
			if (predicate.test(e)) return e;
		}
		return null;
	}

	default int findLastInt(IntPredicate predicate)  {
		int e;
		for (int i = size() - 1; i >= 0; i--) {
			e = getAt(i);
			if (predicate.test(e)) return e;
		}
		throw new NoSuchElementException();
	}

	/**
	 * Returns a new collection containing the content of the first one then the content of the second
	 *
	 * @param b the second collection
	 * @return a new array containing the content of the first one then the content of the second
	 */
	default IntList plus(int[] b) {
		IntList sum = new IntArrayList(size() + b.length);
		sum.addAll(this);
		for (int l : b) sum.add(l);
		return sum;
	}

	default IntList asUnmodifiable() {
		return new UnmodifiableIntList(this);
	}

	default IntList toImmutable() {
		return new UnmodifiableIntList(new IntArrayList(this));
	}

	@Override
	default IntList leftShift(int value) {
		return (IntList) IntCollection.super.leftShift(value);
	}

	@Override
	default IntList leftShift(int[] array) {
		return (IntList)  IntCollection.super.leftShift(array);
	}

	@Override
	default IntList leftShift(IntCollection value) {
		return (IntList)  IntCollection.super.leftShift(value);
	}

	default void sortedAdd(int element) {
		int i = 0;
		while (i < size() && get(i).compareTo(element) < 0) {
			i++;
		}
		add(i, element);
	}
	
	/**
	 * Returns a list containing the last n elements.
	 * @param n the number of elements to take from the end
	 * @return a list containing at most the last n elements
	 */
	default IntList takeLast(int n) {
		if (n <= 0) return new IntArrayList(0);
		if (n >= size()) return new IntArrayList(this);
		
		IntList result = new IntArrayList(n);
		int startIndex = size() - n;
		for (int i = startIndex; i < size(); i++) {
			result.add(getAt(i));
		}
		return result;
	}
	
	/**
	 * Returns a list containing all elements except the last n elements.
	 * @param n the number of elements to drop from the end
	 * @return a list containing elements after dropping the last n elements
	 */
	default IntList dropLast(int n) {
		if (n <= 0) return new IntArrayList(this);
		if (n >= size()) return new IntArrayList(0);
		
		int newSize = size() - n;
		IntList result = new IntArrayList(newSize);
		for (int i = 0; i < newSize; i++) {
			result.add(getAt(i));
		}
		return result;
	}
}