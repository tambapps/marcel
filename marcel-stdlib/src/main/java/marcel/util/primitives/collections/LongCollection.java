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
package marcel.util.primitives.collections;

import marcel.util.primitives.collections.lists.CharArrayList;
import marcel.util.primitives.collections.lists.CharList;
import marcel.util.primitives.collections.lists.DoubleArrayList;
import marcel.util.primitives.collections.lists.DoubleList;
import marcel.util.primitives.collections.lists.FloatArrayList;
import marcel.util.primitives.collections.lists.FloatList;
import marcel.util.primitives.collections.lists.IntArrayList;
import marcel.util.primitives.collections.lists.IntList;
import marcel.util.primitives.collections.lists.LongArrayList;
import marcel.util.primitives.collections.lists.LongList;
import marcel.util.primitives.collections.sets.LongOpenHashSet;
import marcel.util.primitives.collections.sets.LongSet;
import marcel.util.primitives.iterable.LongIterable;
import marcel.util.primitives.iterators.LongIterator;
import marcel.util.primitives.spliterators.LongSpliterator;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.LongBinaryOperator;
import java.util.function.LongFunction;
import java.util.function.LongPredicate;

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
@NullMarked
public interface LongCollection extends Collection<Long>, LongIterable {

  boolean add(long key);
  /** Returns {@code true} if this collection contains the specified element.
   * @see Collection#contains(Object)
   */
  boolean contains(long key);
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
  boolean removeLong(long key);

  default boolean replace(long element, long replacement) {
    if (removeLong(element)) {
      return add(replacement);
    }
    throw new NoSuchElementException();
  }

  /** {@inheritDoc}
   * @deprecated Please use the corresponding type-specific method instead.
   */
  @Override
  default boolean add(final Long key) {
    return add((key).longValue());
  }
  /** {@inheritDoc}
   * @deprecated Please use the corresponding type-specific method instead.
   */
  @Override
  default boolean contains(@Nullable final Object key) {
    if (key == null) return false;
    return contains(((Long)(key)).longValue());
  }
  /** {@inheritDoc}
   * @deprecated Please use (and implement) the {@code rem()} method instead.
   */
  @Override
  default boolean remove(@Nullable final Object key) {
    if (key == null) return false;
    return remove(((Long)(key)).longValue());
  }
  /** Returns a primitive type array containing the items of this collection.
   * @return a primitive type array containing the items of this collection.
   * @see Collection#toArray()
   */
  long[] toLongArray();

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
  long[] toArray(long[] a);
  /** Adds all elements of the given type-specific collection to this collection.
   *
   * @param c a type-specific collection.
   * @see Collection#addAll(Collection)
   * @return {@code true} if this collection changed as a result of the call.
   */
  boolean addAll(LongCollection c);
  /** Checks whether this collection contains all elements from the given type-specific collection.
   *
   * @param c a type-specific collection.
   * @see Collection#containsAll(Collection)
   * @return {@code true} if this collection contains all elements of the argument.
   */
  boolean containsAll(LongCollection c);
  /** Remove from this collection all elements in the given type-specific collection.
   *
   * @param c a type-specific collection.
   * @see Collection#removeAll(Collection)
   * @return {@code true} if this collection changed as a result of the call.
   */
  boolean removeAll(LongCollection c);
  /** {@inheritDoc}
   * @deprecated Please use the corresponding type-specific method instead.
   */
  @Deprecated
  @Override
  default boolean removeIf(final java.util.function.Predicate<? super Long> filter) {
    return removeIf(
        filter instanceof LongPredicate ?
            ((LongPredicate) filter) :
            (LongPredicate) key -> filter.test(Long.valueOf(key)));
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
  default boolean removeIf(final LongPredicate filter) {
    boolean removed = false;
    final LongIterator each = iterator();
    while (each.hasNext()) {
      if (filter.test(each.nextLong())) {
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
  boolean retainAll(LongCollection c);

  LongSpliterator spliterator();

  default java.util.stream.LongStream longStream() {
    return java.util.stream.StreamSupport.longStream(spliterator(), false);
  }

  default LongList mapToLong(LongFunction<Long> function) {
    LongList list = new LongArrayList(size());
    LongIterator iterator = iterator();
    while (iterator.hasNext()) {
      list.add(function.apply(iterator.nextLong()));
    }
    return list;
  }

  default IntList mapToInt(LongFunction<Integer> function) {
    IntList intList = new IntArrayList(size());
    LongIterator iterator = iterator();
    while (iterator.hasNext()) {
      intList.add(function.apply(iterator.nextLong()));
    }
    return intList;
  }

  default FloatList mapToFloat(LongFunction<Float> function) {
    FloatList intList = new FloatArrayList(size());
    LongIterator iterator = iterator();
    while (iterator.hasNext()) {
      intList.add(function.apply(iterator.nextLong()));
    }
    return intList;
  }

  default DoubleList mapToDouble(LongFunction<Double> function) {
    DoubleList intList = new DoubleArrayList(size());
    LongIterator iterator = iterator();
    while (iterator.hasNext()) {
      intList.add(function.apply(iterator.nextLong()));
    }
    return intList;
  }

  default CharList mapToChar(LongFunction<Character> function) {
    CharList intList = new CharArrayList(size());
    LongIterator iterator = iterator();
    while (iterator.hasNext()) {
      intList.add(function.apply(iterator.nextLong()));
    }
    return intList;
  }

  @Nullable
  default Long find(LongPredicate predicate)  {
    LongIterator iterator = iterator();
    long e;
    while (iterator.hasNext()) {
      e = iterator.nextLong();
      if (predicate.test(e)) return e;
    }
    return null;
  }

  default long findLong(LongPredicate predicate)  {
    LongIterator iterator = iterator();
    long e;
    while (iterator.hasNext()) {
      e = iterator.nextLong();
      if (predicate.test(e)) return e;
    }
    throw new NoSuchElementException();
  }

  default LongList findAll(LongPredicate predicate)  {
    LongIterator iterator = iterator();
    long e;
    LongList l = new LongArrayList(size());
    while (iterator.hasNext()) {
      e = iterator.nextLong();
      if (predicate.test(e)) l.add(e);
    }
    return l;
  }

  default long min() {
    if (isEmpty()) throw new NoSuchElementException();
    LongIterator iterator = iterator();
    long min = iterator.nextLong();
    while (iterator.hasNext()) {
      long e = iterator.nextLong();
      if (e < min) min = e;
    }
    return min;
  }

  default long max() {
    if (isEmpty()) throw new NoSuchElementException();
    LongIterator iterator = iterator();
    long max = iterator.nextLong();
    while (iterator.hasNext()) {
      long e = iterator.nextLong();
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
  default LongCollection leftShift(long value) {
    add(value);
    return this;
  }
  default LongCollection leftShift(LongCollection value) {
    addAll(value);
    return this;
  }
  default LongCollection leftShift(long[] value) {
    for (long c : value) add(c);
    return this;
  }

  default <T> LongCollection unique(LongFunction<T> keyExtractor) {
    Set<Object> set = new HashSet<>();
    LongList list = new LongArrayList();
    LongIterator iterator = iterator();
    while (iterator.hasNext()) {
      long o = iterator.nextLong();
      if (set.add(keyExtractor.apply(o))) {
        list.add(o);
      }
    }
    return list;
  }

  default boolean all(LongPredicate predicate) {
    LongIterator iterator = iterator();
    while (iterator.hasNext()) {
      long e = iterator.nextLong();
      if (!predicate.test(e)) return false;
    }
    return true;
  }

  default boolean any(LongPredicate predicate) {
    LongIterator iterator = iterator();
    while (iterator.hasNext()) {
      long e = iterator.nextLong();
      if (predicate.test(e)) return true;
    }
    return false;
  }

  default boolean none(LongPredicate predicate) {
    LongIterator iterator = iterator();
    while (iterator.hasNext()) {
      long e = iterator.nextLong();
      if (predicate.test(e)) return false;
    }
    return true;
  }

  default int count(LongPredicate predicate) {
    int count = 0;
    for (long e : this) {
      if (predicate.test(e)) count++;
    }
    return count;
  }

  default LongList toList() {
    return new LongArrayList(this);
  }

  default LongSet toSet() {
    return new LongOpenHashSet(this);
  }
  
  /**
   * Calculates the arithmetic mean of all elements in the collection.
   * @return the average of all elements, or 0 if the collection is empty
   */
  default double average() {
    if (isEmpty()) return 0d;
    double sum = 0d;
    int count = 0;
    LongIterator iterator = iterator();
    while (iterator.hasNext()) {
      sum += iterator.nextLong();
      count++;
    }
    return sum / count;
  }
  
  /**
   * Groups the elements in the collection by a key produced by the provided function.
   * @param keyExtractor function to extract the key to group by
   * @param <K> the type of key
   * @return a map containing the grouped elements
   */
  default <K> Map<K, LongList> groupBy(LongFunction<K> keyExtractor) {
    Map<K, LongList> map = new HashMap<>();
    LongIterator iterator = iterator();
    while (iterator.hasNext()) {
      long element = iterator.nextLong();
      K key = keyExtractor.apply(element);
      map.computeIfAbsent(key, k -> new LongArrayList()).add(element);
    }
    return map;
  }
  
  /**
   * Returns a list containing the first n elements.
   * @param n the number of elements to take
   * @return a list containing at most n elements
   */
  default LongList take(int n) {
    if (n <= 0) return new LongArrayList(0);
    LongList result = new LongArrayList(Math.min(n, size()));
    LongIterator iterator = iterator();
    int count = 0;
    while (iterator.hasNext() && count < n) {
      result.add(iterator.nextLong());
      count++;
    }
    return result;
  }
  
  /**
   * Returns a list containing elements from this collection as long as the predicate is true.
   * @param predicate a function that returns true for elements to include
   * @return a list containing elements while predicate returns true
   */
  default LongList takeWhile(LongPredicate predicate) {
    LongList result = new LongArrayList();
    LongIterator iterator = iterator();
    while (iterator.hasNext()) {
      long element = iterator.nextLong();
      if (!predicate.test(element)) break;
      result.add(element);
    }
    return result;
  }
  
  /**
   * Returns a list containing all elements except the first n elements.
   * @param n the number of elements to drop
   * @return a list containing elements after skipping n elements
   */
  default LongList drop(int n) {
    if (n <= 0) return new LongArrayList(this);
    if (n >= size()) return new LongArrayList(0);
    
    LongList result = new LongArrayList(size() - n);
    LongIterator iterator = iterator();
    int count = 0;
    
    // Skip n elements
    while (iterator.hasNext() && count < n) {
      iterator.nextLong();
      count++;
    }
    
    // Collect the rest
    while (iterator.hasNext()) {
      result.add(iterator.nextLong());
    }
    
    return result;
  }
  
  /**
   * Returns a list containing all elements except those at the beginning
   * that satisfy the given predicate.
   * @param predicate a function that returns true for elements to skip
   * @return a list containing elements after the predicate returns false
   */
  default LongList dropWhile(LongPredicate predicate) {
    LongList result = new LongArrayList();
    LongIterator iterator = iterator();
    boolean dropping = true;
    
    while (iterator.hasNext()) {
      long element = iterator.nextLong();
      if (dropping && !predicate.test(element)) {
        dropping = false;
      }
      
      if (!dropping) {
        result.add(element);
      }
    }
    
    return result;
  }
  
  /**
   * Folds the collection to a single value by applying the operation to
   * the accumulated value and each element.
   * @param initial the initial value
   * @param operator the binary operation to apply
   * @return the final accumulated value
   */
  default long fold(long initial, LongBinaryOperator operator) {
    long result = initial;
    LongIterator iterator = iterator();
    while (iterator.hasNext()) {
      result = operator.applyAsLong(result, iterator.nextLong());
    }
    return result;
  }
}
