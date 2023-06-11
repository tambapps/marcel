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

import marcel.lang.primitives.iterable.FloatIterable;
import marcel.lang.primitives.iterators.FloatIterator;
import marcel.lang.primitives.spliterators.FloatSpliterator;
import marcel.lang.util.function.FloatPredicate;

import java.util.Collection;
import java.util.NoSuchElementException;

/** A type-specific {@link Collection}; provides some additional methods
 * that use polymorphism to avoid (un)boxing.
 *
 * <p>Additionally, this class defines strengthens (again) {@link #iterator()}.
 *
 * <p>This interface specifies reference equality semantics (members will be compared equal with
 * {@code ==} instead of {@link Object#equals(Object) equals}), which may result in breaks in contract
 * if attempted to be used with non reference-equality semantics based {@link Collection}s. For example, a
 * {@code aReferenceCollection.equals(aObjectCollection)} may return different a different result then
 * {@code aObjectCollection.equals(aReferenceCollection)}, in violation of {@link Object#equals equals}'s
 * contract requiring it being symmetric.
 *
 * @see Collection
 */
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
  default boolean contains(final Object key) {
    if (key == null) return false;
    return contains(((Float)(key)).floatValue());
  }
  /** {@inheritDoc}
   * @deprecated Please use (and implement) the {@code rem()} method instead.
   */
  @Override
  default boolean remove(final Object key) {
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
   * @apiNote Implementing classes should generally override this method, and take the default
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

  default java.util.stream.DoubleStream doubleStream() {
    return stream().mapToDouble(Float::doubleValue);
  }


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


  /**
   * Add an element to the list
   *
   * @param value the value to add
   * @return whether the value was added or not
   */
  default boolean leftShift(float value) {
    return add(value);
  }
}
