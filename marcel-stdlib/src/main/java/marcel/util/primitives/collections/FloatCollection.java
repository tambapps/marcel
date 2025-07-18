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

import marcel.util.function.Float2ObjectFunction;
import marcel.util.function.FloatBinaryOperator;
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
import marcel.util.primitives.collections.sets.FloatOpenHashSet;
import marcel.util.primitives.collections.sets.FloatSet;
import marcel.util.primitives.iterable.FloatIterable;
import marcel.util.primitives.iterators.FloatIterator;
import marcel.util.primitives.spliterators.FloatSpliterator;
import marcel.util.function.FloatFunction;
import marcel.util.function.FloatPredicate;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
@NullMarked
public interface FloatCollection extends Collection<Float>, FloatIterable {

  boolean add(float key);
  /** Returns {@code true} if this collection contains the specified element.
   * @see Collection#contains(Object)
   */
  boolean contains(float key);
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
  boolean removeFloat(float key);

  default boolean replace(float element, float replacement) {
    if (removeFloat(element)) {
      return add(replacement);
    }
    throw new NoSuchElementException();
  }

  /** {@inheritDoc}
   * @deprecated Please use the corresponding type-specific method instead.
   */
  @Override
  default boolean add(final Float key) {
    return add((key).floatValue());
  }
  /** {@inheritDoc}
   * @deprecated Please use the corresponding type-specific method instead.
   */
  @Override
  default boolean contains(@Nullable final Object key) {
    if (key == null) return false;
    return contains(((Float)(key)).floatValue());
  }
  /** {@inheritDoc}
   * @deprecated Please use (and implement) the {@code rem()} method instead.
   */
  @Override
  default boolean remove(@Nullable final Object key) {
    if (key == null) return false;
    return remove(((Float)(key)).floatValue());
  }
  /** Returns a primitive type array containing the items of this collection.
   * @return a primitive type array containing the items of this collection.
   * @see Collection#toArray()
   */
  float[] toFloatArray();

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
  float[] toArray(float[] a);
  /** Adds all elements of the given type-specific collection to this collection.
   *
   * @param c a type-specific collection.
   * @see Collection#addAll(Collection)
   * @return {@code true} if this collection changed as a result of the call.
   */
  boolean addAll(FloatCollection c);
  /** Checks whether this collection contains all elements from the given type-specific collection.
   *
   * @param c a type-specific collection.
   * @see Collection#containsAll(Collection)
   * @return {@code true} if this collection contains all elements of the argument.
   */
  boolean containsAll(FloatCollection c);
  /** Remove from this collection all elements in the given type-specific collection.
   *
   * @param c a type-specific collection.
   * @see Collection#removeAll(Collection)
   * @return {@code true} if this collection changed as a result of the call.
   */
  boolean removeAll(FloatCollection c);
  /** {@inheritDoc}
   * @deprecated Please use the corresponding type-specific method instead.
   */
  @Deprecated
  @Override
  default boolean removeIf(final java.util.function.Predicate<? super Float> filter) {
    return removeIf(
        filter instanceof FloatPredicate ?
            ((FloatPredicate) filter) :
            (FloatPredicate) key -> filter.test(Float.valueOf(key)));
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
  default boolean removeIf(final FloatPredicate filter) {
    boolean removed = false;
    final FloatIterator each = iterator();
    while (each.hasNext()) {
      if (filter.test(each.nextFloat())) {
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
  boolean retainAll(FloatCollection c);

  FloatSpliterator spliterator();

  default LongList mapToLong(FloatFunction<Long> function) {
    LongList list = new LongArrayList(size());
    FloatIterator iterator = iterator();
    while (iterator.hasNext()) {
      list.add(function.apply(iterator.nextFloat()));
    }
    return list;
  }

  default IntList mapToInt(FloatFunction<Integer> function) {
    IntList intList = new IntArrayList(size());
    FloatIterator iterator = iterator();
    while (iterator.hasNext()) {
      intList.add(function.apply(iterator.nextFloat()));
    }
    return intList;
  }

  default FloatList mapToFloat(FloatFunction<Float> function) {
    FloatList intList = new FloatArrayList(size());
    FloatIterator iterator = iterator();
    while (iterator.hasNext()) {
      intList.add(function.apply(iterator.nextFloat()));
    }
    return intList;
  }

  default DoubleList mapToDouble(FloatFunction<Double> function) {
    DoubleList intList = new DoubleArrayList(size());
    FloatIterator iterator = iterator();
    while (iterator.hasNext()) {
      intList.add(function.apply(iterator.nextFloat()));
    }
    return intList;
  }

  default CharList mapToChar(FloatFunction<Character> function) {
    CharList intList = new CharArrayList(size());
    FloatIterator iterator = iterator();
    while (iterator.hasNext()) {
      intList.add(function.apply(iterator.nextFloat()));
    }
    return intList;
  }

  default java.util.stream.DoubleStream doubleStream() {
    return stream().mapToDouble(Float::doubleValue);
  }

  @Nullable
  default Float find(FloatPredicate predicate)  {
    FloatIterator iterator = iterator();
    float e;
    while (iterator.hasNext()) {
      e = iterator.nextFloat();
      if (predicate.test(e)) return e;
    }
    return null;
  }

  default float findFloat(FloatPredicate predicate)  {
    FloatIterator iterator = iterator();
    float e;
    while (iterator.hasNext()) {
      e = iterator.nextFloat();
      if (predicate.test(e)) return e;
    }
    throw new NoSuchElementException();
  }

  default FloatList findAll(FloatPredicate predicate)  {
    FloatIterator iterator = iterator();
    float e;
    FloatList l = new FloatArrayList(size());
    while (iterator.hasNext()) {
      e = iterator.nextFloat();
      if (predicate.test(e)) l.add(e);
    }
    return l;
  }

  default float min() {
    if (isEmpty()) throw new NoSuchElementException();
    FloatIterator iterator = iterator();
    float min = iterator.nextFloat();
    while (iterator.hasNext()) {
      float e = iterator.nextFloat();
      if (e < min) min = e;
    }
    return min;
  }

  default float max() {
    if (isEmpty()) throw new NoSuchElementException();
    FloatIterator iterator = iterator();
    float max = iterator.nextFloat();
    while (iterator.hasNext()) {
      float e = iterator.nextFloat();
      if (e > max) max = e;
    }
    return max;
  }

  default <T> FloatCollection unique(FloatFunction<T> keyExtractor) {
    Set<Object> set = new HashSet<>();
    FloatList list = new FloatArrayList();
    FloatIterator iterator = iterator();
    while (iterator.hasNext()) {
      float o = iterator.nextFloat();
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
  default FloatCollection leftShift(float value) {
    add(value);
    return this;
  }
  default FloatCollection leftShift(FloatCollection value) {
    addAll(value);
    return this;
  }

  default FloatCollection leftShift(float[] value) {
    for (float c : value) add(c);
    return this;
  }

  default boolean all(FloatPredicate predicate) {
    FloatIterator iterator = iterator();
    while (iterator.hasNext()) {
      float e = iterator.nextFloat();
      if (!predicate.test(e)) return false;
    }
    return true;
  }

  default boolean any(FloatPredicate predicate) {
    FloatIterator iterator = iterator();
    while (iterator.hasNext()) {
      float e = iterator.nextFloat();
      if (predicate.test(e)) return true;
    }
    return false;
  }

  default boolean none(FloatPredicate predicate) {
    FloatIterator iterator = iterator();
    while (iterator.hasNext()) {
      float e = iterator.nextFloat();
      if (predicate.test(e)) return false;
    }
    return true;
  }

  default int count(FloatPredicate predicate) {
    int count = 0;
    for (float e : this) {
      if (predicate.test(e)) count++;
    }
    return count;
  }

  default FloatList toList() {
    return new FloatArrayList(this);
  }

  default FloatSet toSet() {
    return new FloatOpenHashSet(this);
  }

  /**
   * Calculates the arithmetic mean of all elements in the collection.
   * @return the average of all elements, or 0 if the collection is empty
   */
  default double average() {
    if (isEmpty()) return 0d;
    double sum = 0d;
    int count = 0;
    FloatIterator iterator = iterator();
    while (iterator.hasNext()) {
      sum += iterator.nextFloat();
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
  default <K> Map<K, FloatList> groupBy(Float2ObjectFunction<K> keyExtractor) {
    Map<K, FloatList> map = new HashMap<>();
    FloatIterator iterator = iterator();
    while (iterator.hasNext()) {
      float element = iterator.nextFloat();
      K key = keyExtractor.apply(element);
      map.computeIfAbsent(key, k -> new FloatArrayList()).add(element);
    }
    return map;
  }
  
  /**
   * Returns a list containing the first n elements.
   * @param n the number of elements to take
   * @return a list containing at most n elements
   */
  default FloatList take(int n) {
    if (n <= 0) return new FloatArrayList(0);
    FloatList result = new FloatArrayList(Math.min(n, size()));
    FloatIterator iterator = iterator();
    int count = 0;
    while (iterator.hasNext() && count < n) {
      result.add(iterator.nextFloat());
      count++;
    }
    return result;
  }
  
  /**
   * Returns a list containing elements from this collection as long as the predicate is true.
   * @param predicate a function that returns true for elements to include
   * @return a list containing elements while predicate returns true
   */
  default FloatList takeWhile(FloatPredicate predicate) {
    FloatList result = new FloatArrayList();
    FloatIterator iterator = iterator();
    while (iterator.hasNext()) {
      float element = iterator.nextFloat();
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
  default FloatList drop(int n) {
    if (n <= 0) return new FloatArrayList(this);
    if (n >= size()) return new FloatArrayList(0);
    
    FloatList result = new FloatArrayList(size() - n);
    FloatIterator iterator = iterator();
    int count = 0;
    
    // Skip n elements
    while (iterator.hasNext() && count < n) {
      iterator.nextFloat();
      count++;
    }
    
    // Collect the rest
    while (iterator.hasNext()) {
      result.add(iterator.nextFloat());
    }
    
    return result;
  }

  /**
   * Returns a list containing all elements except those at the beginning
   * that satisfy the given predicate.
   * @param predicate a function that returns true for elements to skip
   * @return a list containing elements after the predicate returns false
   */
  default FloatList dropWhile(FloatPredicate predicate) {
    FloatList result = new FloatArrayList();
    FloatIterator iterator = iterator();
    boolean dropping = true;
    
    while (iterator.hasNext()) {
      float element = iterator.nextFloat();
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
  default float fold(float initial, FloatBinaryOperator operator) {
    float result = initial;
    FloatIterator iterator = iterator();
    while (iterator.hasNext()) {
      result = operator.applyAsFloat(result, iterator.nextFloat());
    }
    return result;
  }
}
