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
import marcel.util.primitives.collections.sets.DoubleOpenHashSet;
import marcel.util.primitives.collections.sets.DoubleSet;
import marcel.util.primitives.iterable.DoubleIterable;
import marcel.util.primitives.iterators.DoubleIterator;
import marcel.util.primitives.spliterators.DoubleSpliterator;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;

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
public interface DoubleCollection extends Collection<Double>, DoubleIterable {

  boolean add(double key);
  /** Returns {@code true} if this collection contains the specified element.
   * @see Collection#contains(Object)
   */
  boolean contains(double key);
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
  boolean removeDouble(double key);
  /** {@inheritDoc}
   * @deprecated Please use the corresponding type-specific method instead.
   */

  default boolean replace(double element, double replacement) {
    if (removeDouble(element)) {
      return add(replacement);
    }
    throw new NoSuchElementException();
  }

  @Override
  default boolean add(final Double key) {
    return add((key).doubleValue());
  }
  /** {@inheritDoc}
   * @deprecated Please use the corresponding type-specific method instead.
   */
  @Override
  default boolean contains(@Nullable final Object key) {
    if (key == null) return false;
    return contains(((Double)(key)).doubleValue());
  }
  /** {@inheritDoc}
   * @deprecated Please use (and implement) the {@code rem()} method instead.
   */
  @Override
  default boolean remove(@Nullable final Object key) {
    if (key == null) return false;
    return remove(((Double)(key)).doubleValue());
  }
  /** Returns a primitive type array containing the items of this collection.
   * @return a primitive type array containing the items of this collection.
   * @see Collection#toArray()
   */
  double[] toDoubleArray();

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
  double[] toArray(double[] a);
  /** Adds all elements of the given type-specific collection to this collection.
   *
   * @param c a type-specific collection.
   * @see Collection#addAll(Collection)
   * @return {@code true} if this collection changed as a result of the call.
   */
  boolean addAll(DoubleCollection c);
  /** Checks whether this collection contains all elements from the given type-specific collection.
   *
   * @param c a type-specific collection.
   * @see Collection#containsAll(Collection)
   * @return {@code true} if this collection contains all elements of the argument.
   */
  boolean containsAll(DoubleCollection c);
  /** Remove from this collection all elements in the given type-specific collection.
   *
   * @param c a type-specific collection.
   * @see Collection#removeAll(Collection)
   * @return {@code true} if this collection changed as a result of the call.
   */
  boolean removeAll(DoubleCollection c);
  /** {@inheritDoc}
   * @deprecated Please use the corresponding type-specific method instead.
   */
  @Deprecated
  @Override
  default boolean removeIf(final java.util.function.Predicate<? super Double> filter) {
    return removeIf(
        filter instanceof DoublePredicate ?
            ((DoublePredicate) filter) :
            (DoublePredicate) key -> filter.test(Double.valueOf(key)));
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
  default boolean removeIf(final DoublePredicate filter) {
    boolean removed = false;
    final DoubleIterator each = iterator();
    while (each.hasNext()) {
      if (filter.test(each.nextDouble())) {
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
  boolean retainAll(DoubleCollection c);

  DoubleSpliterator spliterator();

  default LongList mapToLong(DoubleFunction<Long> function) {
    LongList list = new LongArrayList(size());
    DoubleIterator iterator = iterator();
    while (iterator.hasNext()) {
      list.add(function.apply(iterator.nextDouble()));
    }
    return list;
  }

  default IntList mapToInt(DoubleFunction<Integer> function) {
    IntList intList = new IntArrayList(size());
    DoubleIterator iterator = iterator();
    while (iterator.hasNext()) {
      intList.add(function.apply(iterator.nextDouble()));
    }
    return intList;
  }

  default FloatList mapToFloat(DoubleFunction<Float> function) {
    FloatList intList = new FloatArrayList(size());
    DoubleIterator iterator = iterator();
    while (iterator.hasNext()) {
      intList.add(function.apply(iterator.nextDouble()));
    }
    return intList;
  }

  default DoubleList mapToDouble(DoubleFunction<Double> function) {
    DoubleList intList = new DoubleArrayList(size());
    DoubleIterator iterator = iterator();
    while (iterator.hasNext()) {
      intList.add(function.apply(iterator.nextDouble()));
    }
    return intList;
  }

  default CharList mapToChar(DoubleFunction<Character> function) {
    CharList intList = new CharArrayList(size());
    DoubleIterator iterator = iterator();
    while (iterator.hasNext()) {
      intList.add(function.apply(iterator.nextDouble()));
    }
    return intList;
  }

  default java.util.stream.DoubleStream doubleStream() {
    return java.util.stream.StreamSupport.doubleStream(spliterator(), false);
  }

  @Nullable
  default Double find(DoublePredicate predicate)  {
    DoubleIterator iterator = iterator();
    double e;
    while (iterator.hasNext()) {
      e = iterator.nextDouble();
      if (predicate.test(e)) return e;
    }
    return null;
  }

  default double findDouble(DoublePredicate predicate)  {
    DoubleIterator iterator = iterator();
    double e;
    while (iterator.hasNext()) {
      e = iterator.nextDouble();
      if (predicate.test(e)) return e;
    }
    throw new NoSuchElementException();
  }

  default DoubleList findAll(DoublePredicate predicate)  {
    DoubleIterator iterator = iterator();
    double e;
    DoubleList l = new DoubleArrayList(size());
    while (iterator.hasNext()) {
      e = iterator.nextDouble();
      if (predicate.test(e)) l.add(e);
    }
    return l;
  }

  /**
   * Add an element to the list
   *
   * @param value the value to add
   * @return whether the value was added or not
   */
  default DoubleCollection leftShift(double value) {
    add(value);
    return this;
  }
  default DoubleCollection leftShift(DoubleCollection value) {
    addAll(value);
    return this;
  }
  default DoubleCollection leftShift(double[] value) {
    for (double c : value) add(c);
    return this;
  }

  default <T> DoubleCollection unique(DoubleFunction<T> keyExtractor) {
    Set<Object> set = new HashSet<>();
    DoubleList list = new DoubleArrayList();
    DoubleIterator iterator = iterator();
    while (iterator.hasNext()) {
      double o = iterator.nextDouble();
      if (set.add(keyExtractor.apply(o))) {
        list.add(o);
      }
    }
    return list;
  }

  default boolean all(DoublePredicate predicate) {
    DoubleIterator iterator = iterator();
    while (iterator.hasNext()) {
      double e = iterator.nextDouble();
      if (!predicate.test(e)) return false;
    }
    return true;
  }

  default boolean any(DoublePredicate predicate) {
    DoubleIterator iterator = iterator();
    while (iterator.hasNext()) {
      double e = iterator.nextDouble();
      if (predicate.test(e)) return true;
    }
    return false;
  }

  default boolean none(DoublePredicate predicate) {
    DoubleIterator iterator = iterator();
    while (iterator.hasNext()) {
      double e = iterator.nextDouble();
      if (predicate.test(e)) return false;
    }
    return true;
  }

  default int count(DoublePredicate predicate) {
    int count = 0;
    for (double e : this) {
      if (predicate.test(e)) count++;
    }
    return count;
  }

  default DoubleList toList() {
    return new DoubleArrayList(this);
  }

  default DoubleSet toSet() {
    return new DoubleOpenHashSet(this);
  }

  /**
   * Calculates the arithmetic mean of all elements in the collection.
   * @return the average of all elements, or 0 if the collection is empty
   */
  default double average() {
    if (isEmpty()) return 0d;
    double sum = 0d;
    int count = 0;
    DoubleIterator iterator = iterator();
    while (iterator.hasNext()) {
      sum += iterator.nextDouble();
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
  default <K> Map<K, DoubleList> groupBy(DoubleFunction<K> keyExtractor) {
    Map<K, DoubleList> map = new HashMap<>();
    DoubleIterator iterator = iterator();
    while (iterator.hasNext()) {
      double element = iterator.nextDouble();
      K key = keyExtractor.apply(element);
      map.computeIfAbsent(key, k -> new DoubleArrayList()).add(element);
    }
    return map;
  }
  
  /**
   * Returns a list containing the first n elements.
   * @param n the number of elements to take
   * @return a list containing at most n elements
   */
  default DoubleList take(int n) {
    if (n <= 0) return new DoubleArrayList(0);
    DoubleList result = new DoubleArrayList(Math.min(n, size()));
    DoubleIterator iterator = iterator();
    int count = 0;
    while (iterator.hasNext() && count < n) {
      result.add(iterator.nextDouble());
      count++;
    }
    return result;
  }
  
  /**
   * Returns a list containing elements from this collection as long as the predicate is true.
   * @param predicate a function that returns true for elements to include
   * @return a list containing elements while predicate returns true
   */
  default DoubleList takeWhile(DoublePredicate predicate) {
    DoubleList result = new DoubleArrayList();
    DoubleIterator iterator = iterator();
    while (iterator.hasNext()) {
      double element = iterator.nextDouble();
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
  default DoubleList drop(int n) {
    if (n <= 0) return new DoubleArrayList(this);
    if (n >= size()) return new DoubleArrayList(0);
    
    DoubleList result = new DoubleArrayList(size() - n);
    DoubleIterator iterator = iterator();
    int count = 0;
    
    // Skip n elements
    while (iterator.hasNext() && count < n) {
      iterator.nextDouble();
      count++;
    }
    
    // Collect the rest
    while (iterator.hasNext()) {
      result.add(iterator.nextDouble());
    }
    
    return result;
  }
  
  /**
   * Returns a list containing all elements except those at the beginning
   * that satisfy the given predicate.
   * @param predicate a function that returns true for elements to skip
   * @return a list containing elements after the predicate returns false
   */
  default DoubleList dropWhile(DoublePredicate predicate) {
    DoubleList result = new DoubleArrayList();
    DoubleIterator iterator = iterator();
    boolean dropping = true;
    
    while (iterator.hasNext()) {
      double element = iterator.nextDouble();
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
  default double fold(double initial, DoubleBinaryOperator operator) {
    double result = initial;
    DoubleIterator iterator = iterator();
    while (iterator.hasNext()) {
      result = operator.applyAsDouble(result, iterator.nextDouble());
    }
    return result;
  }
}
