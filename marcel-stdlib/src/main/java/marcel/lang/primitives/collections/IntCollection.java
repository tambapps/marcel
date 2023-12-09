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

import marcel.lang.primitives.collections.lists.CharacterArrayList;
import marcel.lang.primitives.collections.lists.CharacterList;
import marcel.lang.primitives.collections.lists.DoubleArrayList;
import marcel.lang.primitives.collections.lists.DoubleList;
import marcel.lang.primitives.collections.lists.FloatArrayList;
import marcel.lang.primitives.collections.lists.FloatList;
import marcel.lang.primitives.collections.lists.IntArrayList;
import marcel.lang.primitives.collections.lists.IntList;
import marcel.lang.primitives.collections.lists.LongArrayList;
import marcel.lang.primitives.collections.lists.LongList;
import marcel.lang.primitives.collections.sets.IntOpenHashSet;
import marcel.lang.primitives.collections.sets.IntSet;
import marcel.lang.primitives.iterable.IntIterable;
import marcel.lang.primitives.iterators.IntIterator;
import marcel.lang.primitives.spliterators.IntSpliterator;

import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;

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
public interface IntCollection extends Collection<Integer>, IntIterable {

  boolean add(int key);
  /** Returns {@code true} if this collection contains the specified element.
   * @see Collection#contains(Object)
   */
  boolean contains(int key);
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
  boolean removeInt(int key);

  default boolean replace(int element, int replacement) {
    if (removeInt(element)) {
      return add(replacement);
    }
    throw new NoSuchElementException();
  }

  /** {@inheritDoc}
   * @deprecated Please use the corresponding type-specific method instead.
   */
  @Override
  default boolean add(final Integer key) {
    return add((key).intValue());
  }
  /** {@inheritDoc}
   * @deprecated Please use the corresponding type-specific method instead.
   */
  @Override
  default boolean contains(final Object key) {
    if (key == null) return false;
    return contains(((Integer)(key)).intValue());
  }
  /** {@inheritDoc}
   * @deprecated Please use (and implement) the {@code rem()} method instead.
   */
  @Override
  default boolean remove(final Object key) {
    if (key == null) return false;
    return remove(((Integer)(key)).intValue());
  }
  /** Returns a primitive type array containing the items of this collection.
   * @return a primitive type array containing the items of this collection.
   * @see Collection#toArray()
   */
  int[] toIntArray();

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
  int[] toArray(int[] a);
  /** Adds all elements of the given type-specific collection to this collection.
   *
   * @param c a type-specific collection.
   * @see Collection#addAll(Collection)
   * @return {@code true} if this collection changed as a result of the call.
   */
  boolean addAll(IntCollection c);
  /** Checks whether this collection contains all elements from the given type-specific collection.
   *
   * @param c a type-specific collection.
   * @see Collection#containsAll(Collection)
   * @return {@code true} if this collection contains all elements of the argument.
   */
  boolean containsAll(IntCollection c);
  /** Remove from this collection all elements in the given type-specific collection.
   *
   * @param c a type-specific collection.
   * @see Collection#removeAll(Collection)
   * @return {@code true} if this collection changed as a result of the call.
   */
  boolean removeAll(IntCollection c);
  /** {@inheritDoc}
   * @deprecated Please use the corresponding type-specific method instead.
   */
  @Deprecated
  @Override
  default boolean removeIf(final java.util.function.Predicate<? super Integer> filter) {
    return removeIf(
        filter instanceof java.util.function.IntPredicate ?
            ((java.util.function.IntPredicate) filter) :
            (java.util.function.IntPredicate) key -> filter.test(Integer.valueOf(key)));
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
  default boolean removeIf(final IntPredicate filter) {
    boolean removed = false;
    final IntIterator each = iterator();
    while (each.hasNext()) {
      if (filter.test(each.nextInt())) {
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
  boolean retainAll(IntCollection c);

  IntSpliterator spliterator();

  default java.util.stream.IntStream intStream() {
    return java.util.stream.StreamSupport.intStream(spliterator(), false);
  }

  default IntList mapToInt(IntFunction<Integer> function) {
    IntList intList = new IntArrayList(size());
    IntIterator iterator = iterator();
    while (iterator.hasNext()) {
      intList.add(function.apply(iterator.nextInt()));
    }
    return intList;
  }

  default LongList mapToLong(IntFunction<Long> function) {
    LongList intList = new LongArrayList(size());
    IntIterator iterator = iterator();
    while (iterator.hasNext()) {
      intList.add(function.apply(iterator.nextInt()));
    }
    return intList;
  }

  default FloatList mapToFloat(IntFunction<Float> function) {
    FloatList intList = new FloatArrayList(size());
    IntIterator iterator = iterator();
    while (iterator.hasNext()) {
      intList.add(function.apply(iterator.nextInt()));
    }
    return intList;
  }

  default DoubleList mapToDouble(IntFunction<Double> function) {
    DoubleList intList = new DoubleArrayList(size());
    IntIterator iterator = iterator();
    while (iterator.hasNext()) {
      intList.add(function.apply(iterator.nextInt()));
    }
    return intList;
  }

  default CharacterList mapToCharacter(IntFunction<Character> function) {
    CharacterList intList = new CharacterArrayList(size());
    IntIterator iterator = iterator();
    while (iterator.hasNext()) {
      intList.add(function.apply(iterator.nextInt()));
    }
    return intList;
  }

  default Integer find(IntPredicate predicate)  {
    IntIterator iterator = iterator();
    int e;
    while (iterator.hasNext()) {
      e = iterator.nextInt();
      if (predicate.test(e)) return e;
    }
    return null;
  }

  default int findInt(IntPredicate predicate)  {
    IntIterator iterator = iterator();
    int e;
    while (iterator.hasNext()) {
      e = iterator.nextInt();
      if (predicate.test(e)) return e;
    }
    throw new NoSuchElementException();
  }

  default IntList findAll(IntPredicate predicate)  {
    IntIterator iterator = iterator();
    int e;
    IntList intList = new IntArrayList(size());
    while (iterator.hasNext()) {
      e = iterator.nextInt();
      if (predicate.test(e)) intList.add(e);
    }
    return intList;
  }

  default int min() {
    if (isEmpty()) throw new NoSuchElementException();
    IntIterator iterator = iterator();
    int min = iterator.nextInt();
    while (iterator.hasNext()) {
      int e = iterator.nextInt();
      if (e < min) min = e;
    }
    return min;
  }

  default int max() {
    if (isEmpty()) throw new NoSuchElementException();
    IntIterator iterator = iterator();
    int max = iterator.nextInt();
    while (iterator.hasNext()) {
      int e = iterator.nextInt();
      if (e > max) max = e;
    }
    return max;
  }


  /**
   * Add an element to the list
   *
   * @param value the value to add
   * @return whether the value was added or not
   */
  default boolean leftShift(int value) {
    return add(value);
  }

  default <T> IntCollection unique(IntFunction<T> keyExtractor) {
    Set<Object> set = new HashSet<>();
    IntList list = new IntArrayList();
    IntIterator iterator = iterator();
    while (iterator.hasNext()) {
      int o = iterator.nextInt();
      if (set.add(keyExtractor.apply(o))) {
        list.add(o);
      }
    }
    return list;
  }

  default boolean all(IntPredicate predicate) {
    IntIterator iterator = iterator();
    while (iterator.hasNext()) {
      int e = iterator.nextInt();
      if (!predicate.test(e)) return false;
    }
    return true;
  }

  default boolean any(IntPredicate predicate) {
    IntIterator iterator = iterator();
    while (iterator.hasNext()) {
      int e = iterator.nextInt();
      if (predicate.test(e)) return true;
    }
    return false;
  }

  default boolean none(IntPredicate predicate) {
    IntIterator iterator = iterator();
    while (iterator.hasNext()) {
      int e = iterator.nextInt();
      if (predicate.test(e)) return false;
    }
    return true;
  }

  default int count(IntPredicate predicate) {
    int count = 0;
    for (int e : this) {
      if (predicate.test(e)) count++;
    }
    return count;
  }

  default IntList toList() {
    return new IntArrayList(this);
  }

  default IntSet toSet() {
    return new IntOpenHashSet(this);
  }

}
