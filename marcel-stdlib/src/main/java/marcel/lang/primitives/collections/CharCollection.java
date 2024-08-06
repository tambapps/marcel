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
package marcel.lang.primitives.collections;


import marcel.lang.primitives.collections.lists.CharArrayList;
import marcel.lang.primitives.collections.lists.CharList;
import marcel.lang.primitives.collections.lists.DoubleArrayList;
import marcel.lang.primitives.collections.lists.DoubleList;
import marcel.lang.primitives.collections.lists.FloatArrayList;
import marcel.lang.primitives.collections.lists.FloatList;
import marcel.lang.primitives.collections.lists.IntArrayList;
import marcel.lang.primitives.collections.lists.IntList;
import marcel.lang.primitives.collections.lists.LongArrayList;
import marcel.lang.primitives.collections.lists.LongList;
import marcel.lang.primitives.collections.sets.CharOpenHashSet;
import marcel.lang.primitives.collections.sets.CharSet;
import marcel.lang.primitives.iterable.CharIterable;
import marcel.lang.primitives.iterators.CharIterator;
import marcel.lang.primitives.spliterators.CharSpliterator;
import marcel.util.function.CharFunction;
import marcel.util.function.CharPredicate;

import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

/** A type-specific Collection; provides some additional methods
 * that use polymorphism to avoid (un)boxing.
 *
 * <p>Additionally, this class defines strengthens (again) {@link #iterator()}.
 *
 * <p>This interface specifies reference equality semantics (members will be compared equal with
 * {@code ==} instead of {@link Object#equals(Object) equals}), which may result in breaks in contract
 * if attempted to be used with non reference-equality semantics based Collections. For example, a
 * {@code aReferenceCollection.equals(aObjectCollection)} may return different a different result then
 * {@code aObjectCollection.equals(aReferenceCollection)}, in violation of {@link Object#equals equals}'s
 * contract requiring it being symmetric.
 *
 * @see Collection
 */
public interface CharCollection extends Collection<Character>, CharIterable {

  boolean add(char key);
  /** Returns {@code true} if this collection contains the specified element.
   * @param key the key
   * @see Collection#contains(Object)
   * @return whether the key is contained in the collection
   */
  boolean contains(char key);
  /** Removes a single instance of the specified element from this
   * collection, if it is present (optional operation).
   *
   * <p>Note that this method should be called {@link Collection#remove(Object) remove()}, but the clash
   * with the similarly named index-based method in the {@link java.util.List} interface
   * forces us to use a distinguished name. For simplicity, the set interfaces reinstates
   * {@code remove()}.
   *
   * @see Collection#remove(Object)
   */
  boolean removeChar(char key);

  default boolean replace(char element, char replacement) {
    if (removeChar(element)) {
      return add(replacement);
    }
    throw new NoSuchElementException();
  }
  /** {@inheritDoc}
   * @deprecated Please use the corresponding type-specific method instead.
   */
  @Override
  default boolean add(final Character key) {
    return add((key).charValue());
  }
  /** {@inheritDoc}
   * @deprecated Please use the corresponding type-specific method instead.
   */
  @Override
  default boolean contains(final Object key) {
    if (key == null) return false;
    return contains(((Character)(key)).charValue());
  }
  /** {@inheritDoc}
   * @deprecated Please use (and implement) the {@code rem()} method instead.
   */
  @Override
  default boolean remove(final Object key) {
    if (key == null) return false;
    return remove(((Character)(key)).charValue());
  }
  /** Returns a primitive type array containing the items of this collection.
   * @return a primitive type array containing the items of this collection.
   * @see Collection#toArray()
   */
  char[] toCharArray();

  /** Returns an array containing all of the elements in this collection; the runtime type of the returned array is that of the specified array.
   *
   * <p>Note that, contrarily to {@link Collection#toArray(Object[])}, this
   * methods just writes all elements of this collection: no special
   * value will be added after the last one.
   *
   * @param a if this array is big enough, it will be used to store this collection.
   * @return a primitive type array containing the items of this collection.
   * @see Collection#toArray(Object[])
   */
  char[] toArray(char[] a);
  /** Adds all elements of the given type-specific collection to this collection.
   *
   * @param c a type-specific collection.
   * @see Collection#addAll(Collection)
   * @return {@code true} if this collection changed as a result of the call.
   */
  boolean addAll(CharCollection c);

  default boolean addAll(CharSequence c) {
    boolean changed = false;
    for (int i = 0; i < c.length(); i++) {
      if (add(c.charAt(i))) changed = true;
    }
    return changed;
  }

  /** Checks whether this collection contains all elements from the given type-specific collection.
   *
   * @param c a type-specific collection.
   * @see Collection#containsAll(Collection)
   * @return {@code true} if this collection contains all elements of the argument.
   */
  boolean containsAll(CharCollection c);
  /** Remove from this collection all elements in the given type-specific collection.
   *
   * @param c a type-specific collection.
   * @see Collection#removeAll(Collection)
   * @return {@code true} if this collection changed as a result of the call.
   */
  boolean removeAll(CharCollection c);
  /** {@inheritDoc}
   * @deprecated Please use the corresponding type-specific method instead.
   */
  @Deprecated
  @Override
  default boolean removeIf(final java.util.function.Predicate<? super Character> filter) {
    return removeIf(
        filter instanceof CharPredicate ?
            ((CharPredicate) filter) :
            (CharPredicate) key -> filter.test(Character.valueOf(key)));
  }
  /** Remove from this collection all elements which satisfy the given predicate.
   *
   * @param filter a predicate which returns {@code true} for elements to be
   *        removed.
   * @see Collection#removeIf(java.util.function.Predicate)
   * @return {@code true} if any elements were removed.
   * Implementing classes should generally override this method, and take the default
   *   implementation of the other overloads which will delegate to this method (after proper
   *   conversions).
   */
  default boolean removeIf(final CharPredicate filter) {
    boolean removed = false;
    final CharIterator each = iterator();
    while (each.hasNext()) {
      if (filter.test(each.nextChar())) {
        each.remove();
        removed = true;
      }
    }
    return removed;
  }

  /** Retains in this collection only elements from the given type-specific collection.
   *
   * @param c a type-specific collection.
   * @see Collection#retainAll(Collection)
   * @return {@code true} if this collection changed as a result of the call.
   */
  boolean retainAll(CharCollection c);

  CharSpliterator spliterator();

  default LongList mapToLong(CharFunction<Long> function) {
    LongList list = new LongArrayList(size());
    CharIterator iterator = iterator();
    while (iterator.hasNext()) {
      list.add(function.apply(iterator.nextChar()));
    }
    return list;
  }

  default IntList mapToInt(CharFunction<Integer> function) {
    IntList intList = new IntArrayList(size());
    CharIterator iterator = iterator();
    while (iterator.hasNext()) {
      intList.add(function.apply(iterator.nextChar()));
    }
    return intList;
  }

  default FloatList mapToFloat(CharFunction<Float> function) {
    FloatList intList = new FloatArrayList(size());
    CharIterator iterator = iterator();
    while (iterator.hasNext()) {
      intList.add(function.apply(iterator.nextChar()));
    }
    return intList;
  }

  default DoubleList mapToDouble(CharFunction<Double> function) {
    DoubleList intList = new DoubleArrayList(size());
    CharIterator iterator = iterator();
    while (iterator.hasNext()) {
      intList.add(function.apply(iterator.nextChar()));
    }
    return intList;
  }

  default CharList mapToChar(CharFunction<Character> function) {
    CharList intList = new CharArrayList(size());
    CharIterator iterator = iterator();
    while (iterator.hasNext()) {
      intList.add(function.apply(iterator.nextChar()));
    }
    return intList;
  }

  default java.util.stream.IntStream charStream() {
    return stream().mapToInt(Character::charValue);
  }

  default Character find(CharPredicate predicate)  {
    CharIterator iterator = iterator();
    char c;
    while (iterator.hasNext()) {
      c = iterator.nextChar();
      if (predicate.test(c)) return c;
    }
    return null;
  }

  default char findChar(CharPredicate predicate)  {
    CharIterator iterator = iterator();
    char c;
    while (iterator.hasNext()) {
      c = iterator.nextChar();
      if (predicate.test(c)) return c;
    }
    throw new NoSuchElementException();
  }

  default CharList findAll(CharPredicate predicate)  {
    CharIterator iterator = iterator();
    char e;
    CharList l = new CharArrayList(size());
    while (iterator.hasNext()) {
      e = iterator.nextChar();
      if (predicate.test(e)) l.add(e);
    }
    return l;
  }

  default <T> CharCollection unique(CharFunction<T> keyExtractor) {
    Set<Object> set = new HashSet<>();
    CharList list = new CharArrayList();
    CharIterator iterator = iterator();
    while (iterator.hasNext()) {
      char o = iterator.nextChar();
      if (set.add(keyExtractor.apply(o))) {
        list.add(o);
      }
    }
    return list;
  }

  /**
   * Add an element to the list
   *
   * @param value the value to add
   * @return whether the value was added or not
   */
  default CharCollection leftShift(char value) {
    add(value);
    return this;
  }

  default CharCollection leftShift(CharCollection collection) {
    addAll(collection);
    return this;
  }
  default CharCollection leftShift(char[] array) {
    for (char c : array) add(c);
    return this;
  }

  default CharCollection leftShift(String string) {
    addAll(string);
    return this;
  }

  default boolean all(CharPredicate predicate) {
    CharIterator iterator = iterator();
    while (iterator.hasNext()) {
      char e = iterator.nextChar();
      if (!predicate.test(e)) return false;
    }
    return true;
  }

  default boolean any(CharPredicate predicate) {
    CharIterator iterator = iterator();
    while (iterator.hasNext()) {
      char e = iterator.nextChar();
      if (predicate.test(e)) return true;
    }
    return false;
  }

  default boolean none(CharPredicate predicate) {
    CharIterator iterator = iterator();
    while (iterator.hasNext()) {
      char e = iterator.nextChar();
      if (predicate.test(e)) return false;
    }
    return true;
  }

  default int count(CharPredicate predicate) {
    int count = 0;
    for (char e : this) {
      if (predicate.test(e)) count++;
    }
    return count;
  }

  default CharList toList() {
    return new CharArrayList(this);
  }

  default CharSet toSet() {
    return new CharOpenHashSet(this);
  }
}
