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
import marcel.util.primitives.collections.CharCollection;
import marcel.util.primitives.collections.sets.CharOpenHashSet;
import marcel.util.primitives.collections.sets.CharSet;
import marcel.util.primitives.iterators.CharIterator;
import marcel.util.primitives.iterators.IntIterator;
import marcel.util.primitives.iterators.list.CharListIterator;
import marcel.util.primitives.spliterators.CharSpliterator;
import marcel.util.primitives.Arrays;
import marcel.util.function.CharPredicate;
import marcel.util.function.CharUnaryOperator;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Spliterator;

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
public interface CharList extends List<Character>, Comparable<List<? extends Character>>, CharCollection, CharSequence {

	@Override
	default int length() {
		return size();
	}

	@Override
	default char charAt(int index) {
		return getAt(index);
	}




	// don't mind IntelIJ warning
	boolean isEmpty();

	@Override
	default CharSequence subSequence(int start, int end) {
		return subList(start, end);
	}

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
	CharListIterator iterator();
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
	CharSpliterator spliterator();
	/** Returns a type-specific list iterator on the list.
	 *
	 * @see List#listIterator()
	 */
	@Override
	CharListIterator listIterator();
	/** Returns a type-specific list iterator on the list starting at a given index.
	 *
	 * @see List#listIterator(int)
	 */
	@Override
	CharListIterator listIterator(int index);
	/** Returns a type-specific view of the portion of this list from the index {@code from}, inclusive, to the index {@code to}, exclusive.
	 *
	 * This specification strengthens the one given in {@link List#subList(int,int)}.
	 *
	 * @see List#subList(int,int)
	 */
	@Override
	CharList subList(int from, int to);
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
	void getElements(int from, char a[], int offset, int length);
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
	void addElements(int index, char a[]);
	/** Add (hopefully quickly) elements to this type-specific list.
	 *
	 * @param index the index at which to add elements.
	 * @param a the array containing the elements.
	 * @param offset the offset of the first element to add.
	 * @param length the number of elements to add.
	 */
	void addElements(int index, char a[], int offset, int length);
	/** Set (hopefully quickly) elements to match the array given.
	 * @param a the array containing the elements.
	 * @since 8.3.0
	 */
	default void setElements(char a[]) {
	 setElements(0, a);
	}
	/** Set (hopefully quickly) elements to match the array given.
	 * @param index the index at which to start setting elements.
	 * @param a the array containing the elements.
	 * @since 8.3.0
	 */
	default void setElements(int index, char a[]) {
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
	default void setElements(int index, char a[], int offset, int length) {
	 // We can't use AbstractList#ensureIndex, sadly.
	 if (index < 0) throw new IndexOutOfBoundsException("Index (" + index + ") is negative");
	 if (index > size()) throw new IndexOutOfBoundsException("Index (" + index + ") is greater than list size (" + (size()) + ")");
	 Arrays.ensureOffsetLength(a, offset, length);
	 if (index + length > size()) throw new IndexOutOfBoundsException("End index (" + (index + length) + ") is greater than list size (" + size() + ")");
	 CharListIterator iter = listIterator(index);
	 int i = 0;
	 while (i < length) {
	  iter.nextChar();
	  iter.set(a[offset + i++]);
	 }
	}
	/** Appends the specified element to the end of this list (optional operation).
	 * @see List#add(Object)
	 */
	@Override
	boolean add(char key);
	/** Inserts the specified element at the specified position in this list (optional operation).
	 * @see List#add(int,Object)
	 */
	void add(int index, char key);
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead. */
	@Deprecated
	@Override
	default void add(int index, Character key) {
	 add(index, (key).charValue());
	}
	/** Inserts all of the elements in the specified type-specific collection into this type-specific list at the specified position (optional operation).
	 * @see List#addAll(int,java.util.Collection)
	 */
	boolean addAll(int index, CharCollection c);
	/** Replaces the element at the specified position in this list with the specified element (optional operation).
	 * @see List#set(int,Object)
	 */
	char putAt(int index, char k);
	/**
	 * Replaces each element of this list with the result of applying the
	 * operator to that element. 
	 * @param operator the operator to apply to each element.
	 * @see List#replaceAll
	 */
	default void replaceAll(final CharUnaryOperator operator) {
	 final CharListIterator iter = listIterator();
	 while(iter.hasNext()) {
	  iter.set(operator.applyAsChar(iter.nextChar()));
	 }
	}

	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	@SuppressWarnings("boxing")
	default void replaceAll(final java.util.function.UnaryOperator<Character> operator) {
	 java.util.Objects.requireNonNull(operator);
	 // The instanceof and cast is required for performance. Without it, calls routed through this
	 // overload using a primitive consumer would go through the slow lambda.
	 replaceAll(operator instanceof CharUnaryOperator ? (CharUnaryOperator) operator : (CharUnaryOperator) operator::apply);
	}
	/** Returns the element at the specified position in this list.
	 * @see List#get(int)
	 */
	char getAt(int index);

	default Character getAtSafe(int index) {
		return index >= 0 && index < size() ? getAt(index) : null;
	}

	default void putAtSafe(int index, char value) {
		if (index >= 0 && index < size()) {
			putAt(index, value);
		}
	}

	/** Returns the index of the first occurrence of the specified element in this list, or -1 if this list does not contain the element.
	 * @see List#indexOf(Object)
	 */
	int indexOf(char k);
	/** Returns the index of the last occurrence of the specified element in this list, or -1 if this list does not contain the element.
	 * @see List#lastIndexOf(Object)
	 */
	int lastIndexOf(char k);
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default boolean contains(@Nullable final Object key) {
	 return CharCollection.super.contains(key);
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead. */
	@Deprecated
	@Override
	default Character get(int index) {
	 return Character.valueOf(getAt(index));
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead. */
	@Deprecated
	@Override
	default int indexOf(Object o) {
	 return indexOf(((Character)(o)).charValue());
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead. */
	@Deprecated
	@Override
	default int lastIndexOf(Object o) {
	 return lastIndexOf(((Character)(o)).charValue());
	}
	/** {@inheritDoc}
	 * <p>This method specification is a workaround for
	 * <a href="http://bugs.java.com/bugdatabase/view_bug.do?bug_id=JDK-8177440">bug 8177440</a>.
	 * @deprecated Please use the corresponding type-specific method instead. */
	@Deprecated
	@Override
	default boolean add(Character k) {
	 return add((k).charValue());
	}
	/** Removes the element at the specified position in this list (optional operation).
	 * @see List#remove(int)
	 */
	char removeAt(int index);
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default boolean remove(@Nullable final Object key) {
	 return CharCollection.super.remove(key);
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead. */
	@Deprecated
	@Override
	default Character remove(int index) {
	 return Character.valueOf(removeAt(index));
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead. */
	@Deprecated
	@Override
	default Character set(int index, Character k) {
	 return Character.valueOf(putAt(index, (k).charValue()));
	}
	/** Inserts all of the elements in the specified type-specific list into this type-specific list at the specified position (optional operation).
	 * This method exists only for the sake of efficiency: override are expected to use {@link #getElements}/{@link #addElements}.
	 * This method delegates to the one accepting a collection, but it might be implemented more efficiently.
	 * @see List#addAll(int,Collection)
	 */
	default boolean addAll(int index, CharList l) {
	 return addAll(index, (CharCollection) l);
	}
	/** Appends all of the elements in the specified type-specific list to the end of this type-specific list (optional operation).
	 * This method delegates to the index-based version, passing {@link #size()} as first argument.
	 */
	default boolean addAll(CharList l) {
	 return addAll(size(), l);
	}

	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	default void sort(final Comparator<? super Character> comparator) {
	 throw new UnsupportedOperationException("Not Implemented");
	}
	/** Sort a list using a type-specific comparator.
	 *
	 * <p>Pass {@code null} to sort using natural ordering.
	 * @see List#sort(Comparator)
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
			final char t = getAt(i);
			putAt(i, getAt(p));
			putAt(p, t);
		}
	}

	default CharSet toSet() {
		CharIterator iterator = iterator();
		CharSet set = new CharOpenHashSet(size());
		while (iterator.hasNext()) {
			set.add(iterator.nextChar());
		}
		return set;
	}

	/**
	 * Get the last element of the list. This method will throw an exception if the list is empty
	 *
	 * @return the last element of the list
	 */
	default Character getLast() {
		return getAt(size() - 1);
	}

	/**
	 * Get the first element of the list. This method will throw an exception if the list is empty
	 *
	 * @return the last element of the list
	 */
	// object in order not to class with Java 21's getFirst
	default Character getFirst() {
		return getAt(0);
	}

	default void setFirst(Character value) {
		putAt(0, value);
	}
	/**
	 * Sets the last element of the list. This method wil throw an exception if the list is empty
	 *
	 * @param value the value to set
	 */
	default void setLast(Character value) {
		putAt(size() - 1, value);
	}


	/**
	 * Get the elements at the specified indexes from the range. The order of elements returned respects the order of the range
	 *
	 * @param range the range
	 * @return the elements at the specified indexes from the ranges
	 */
	default CharList getAt(IntRange range) {
		CharList subList = new CharArrayList();
		IntIterator iterator = range.iterator();
		while (iterator.hasNext()) subList.add(get(iterator.nextInt()));
		return subList;
	}

	default Character findLast(CharPredicate predicate)  {
		char e;
		for (int i = size() - 1; i >= 0; i--) {
			e = getAt(i);
			if (predicate.test(e)) return e;
		}
		return null;
	}

	default char findLastChar(CharPredicate predicate)  {
		char e;
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
	default CharList plus(char[] b) {
		CharList sum = new CharArrayList(size() + b.length);
		sum.addAll(this);
		for (char l : b) sum.add(l);
		return sum;
	}

	default CharList asUnmodifiable() {
		return new UnmodifiableCharList(this);
	}

	default CharList toImmutable() {
		return new UnmodifiableCharList(new CharArrayList(this));
	}

	@Override
	default CharList leftShift(char value) {
		return (CharList) CharCollection.super.leftShift(value);
	}

	@Override
	default CharList leftShift(char[] array) {
		return (CharList) CharCollection.super.leftShift(array);
	}

	@Override
	default CharList leftShift(String string) {
		return (CharList) CharCollection.super.leftShift(string);
	}

	@Override
	default CharList leftShift(CharCollection collection) {
		return (CharList) CharCollection.super.leftShift(collection);
	}

	default void sortedAdd(char element) {
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
	default CharList takeLast(int n) {
		if (n <= 0) return new CharArrayList(0);
		if (n >= size()) return new CharArrayList(this);
		
		CharList result = new CharArrayList(n);
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
	default CharList dropLast(int n) {
		if (n <= 0) return new CharArrayList(this);
		if (n >= size()) return new CharArrayList(0);
		
		int newSize = size() - n;
		CharList result = new CharArrayList(newSize);
		for (int i = 0; i < newSize; i++) {
			result.add(getAt(i));
		}
		return result;
	}
}
