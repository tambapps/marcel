package marcel.lang.primitives.collections.maps;

import marcel.lang.primitives.collections.sets.CharacterSet;
import marcel.lang.util.function.Character2ObjectFunction;
import marcel.lang.util.function.CharacterFunction;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public interface Character2ObjectMap<V> extends Map<Character,V> {

	/** Adds a pair to the map (optional operation).
	 *
	 * @param key the key.
	 * @param value the value.
	 * @return the old value, or the {@linkplain #defaultReturnValue() default return value} if no value was present for the given key.
	 * @see Function#put(Object,Object)
	 */
	default V put(final char key, final V value) {
		throw new UnsupportedOperationException();
	}
	/** Returns the value to which the given key is mapped.
	 *
	 * @param key the key.
	 * @return the corresponding value, or the {@linkplain #defaultReturnValue() default return value} if no value was present for the given key.
	 * @see Function#get(Object)
	 */
	V get(char key);

	@Deprecated
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	default Set<Map.Entry<Character, V>> entrySet() {
		return (Set)char2ObjectEntrySet();
	}

	/** Removes the mapping with the given key (optional operation).
	 * @param key the key.
	 * @return the old value, or the {@linkplain #defaultReturnValue() default return value} if no value was present for the given key.
	 * @see Function#remove(Object)
	 */
	default V remove(final char key) {
		throw new UnsupportedOperationException();
	}

	/** An entry set providing fast iteration.
	 *
	 * <p>In some cases (e.g., hash-based classes) iteration over an entry set requires the creation
	 * of a large number of {@link Map.Entry} objects. Some {@code fastutil}
	 * maps might return {@linkplain Map#entrySet() entry set} objects of type {@code FastEntrySet}: in this case, {@link #iterator() fastIterator()}
	 * will return an iterator that is guaranteed not to create a large number of objects, <em>possibly
	 * by returning always the same entry</em> (of course, mutated), and {@link #fastForEach(Consumer)} will apply
	 * the provided consumer to all elements of the entry set, <em>which might be represented
	 * always by the same entry</em> (of course, mutated).
	 */
	/**
	 * Returns the number of key/value mappings in this map.  If the
	 * map contains more than {@link Character#MAX_VALUE} elements, returns {@link Character#MAX_VALUE}.
	 *
	 * @return the number of key-value mappings in this map.
	 */
	@Override
	int size();


	Set<Entry <V> > char2ObjectEntrySet();

	/** Removes all of the mappings from this map (optional operation).
	 * The map will be empty after this call returns.
	 *
	 * @throws UnsupportedOperationException if the {@link #clear()} operation is not supported by this map
	 */
	@Override
	default void clear() { throw new UnsupportedOperationException(); }

	interface FastEntrySet <V> extends Set<Entry <V> > {
		/** Returns a fast iterator over this entry set; the iterator might return always the same entry instance, suitably mutated.
		 *
		 * @return a fast iterator over this entry set; the iterator might return always the same {@link Map.Entry} instance, suitably mutated.
		 */
		Iterator<Entry <V> > fastIterator();
		/** Iterates quickly over this entry set; the iteration might happen always on the same entry instance, suitably mutated.
		 *
		 * <p>This default implementation just delegates to {@link #forEach(Consumer)}.
		 *
		 * @param consumer a consumer that will by applied to the entries of this set; the entries might be represented
		 * by the same entry instance, suitably mutated.
		 * @since 8.1.0
		 */
		default void fastForEach(final Consumer<? super Entry <V> > consumer) {
			forEach(consumer);
		}
	}

	default Iterator<Entry <V> > iterator() {
		return char2ObjectEntrySet().iterator();
	}


	/** {@inheritDoc}
	 * @apiNote Note that this specification strengthens the one given in {@link Map#keySet()}.
	 * @return a set view of the keys contained in this map.
	 * @see Map#keySet()
	 */
	@Override
	CharacterSet keySet();

	/** Returns true if this function contains a mapping for the specified key.
	 *
	 * @param key the key.
	 * @return true if this function associates a value to {@code key}.
	 * @see Map#containsKey(Object)
	 */
	boolean containsKey(char key);
	// Defaultable methods
	@Override
	default void forEach(final java.util.function.BiConsumer<? super Character, ? super V> consumer) {
	 final Iterator<Entry<V>> entrySet = iterator();
	 final Consumer<Character2ObjectMap.Entry <V> > wrappingConsumer = (entry) -> consumer.accept(Character.valueOf(entry.getCharacterKey()), (entry.getValue()));
	 if (entrySet instanceof FastEntrySet) {
	  ((FastEntrySet <V>)entrySet).fastForEach(wrappingConsumer);
	 } else {
	  entrySet.forEachRemaining(wrappingConsumer);
	 }
	}
	/** Returns the value to which the specified key is mapped, or the {@code defaultValue} if this
	 * map contains no mapping for the key.
	 *
	 * @param key the key.
	 * @param defaultValue the default mapping of the key.
	 *
	 * @return the value to which the specified key is mapped, or the {@code defaultValue} if this map contains no mapping for the key.
	 *
	 * @see Map#getOrDefault(Object, Object)
	 * @since 8.0.0
	 */
	default V getOrDefault(final char key, final V defaultValue) {
	 return containsKey(key) ? get(key) : defaultValue;
	}

	/** If the specified key is not already associated with a value, associates it with the given
	 * value and returns null, else returns
	 * the current value.
	 *
	 * @param key key with which the specified value is to be associated.
	 * @param value value to be associated with the specified key.
	 *
	 * @return the previous value associated with the specified key, or null if there was no mapping for the key.
	 *
	 * @see Map#putIfAbsent(Object, Object)
	 * @since 8.0.0
	 */
	default V putIfAbsent(final char key, final V value) {
	 final V v = get(key);
	 if (containsKey(key)) return v;
	 put(key, value);
	 return null;
	}
	/** Removes the entry for the specified key only if it is currently mapped to the specified value.
	 *
	 * @param key key with which the specified value is associated.
	 * @param value value expected to be associated with the specified key.
	 *
	 * @return {@code true} if the value was removed.
	 *
	 * @see Map#remove(Object, Object)
	 * @since 8.0.0
	 */
	default boolean remove(final char key, final Object value) {
	 final V curValue = get(key);
	 if (!java.util.Objects.equals(curValue, value) || !containsKey(key)) return false;
	 remove(key);
	 return true;
	}
	/** Replaces the entry for the specified key only if currently mapped to the specified value.
	 *
	 * @param key key with which the specified value is associated.
	 * @param oldValue value expected to be associated with the specified key.
	 * @param newValue value to be associated with the specified key.
	 *
	 * @return {@code true} if the value was replaced.
	 *
	 * @see Map#replace(Object, Object, Object)
	 * @since 8.0.0
	 */
	default boolean replace(final char key, final V oldValue, final V newValue) {
	 final V curValue = get(key);
	 if (!java.util.Objects.equals(curValue, oldValue) || !containsKey(key)) return false;
	 put(key, newValue);
	 return true;
	}
	/** Replaces the entry for the specified key only if it is currently mapped to some value.
	 *
	 * @param key key with which the specified value is associated.
	 * @param value value to be associated with the specified key.
	 *
	 * @return the previous value associated with the specified key
	 *
	 * @see Map#replace(Object, Object)
	 * @since 8.0.0
	 */
	default V replace(final char key, final V value) { return containsKey(key) ? put(key, value) : null; }
	/** If the specified key is not already associated with a value, attempts to compute its value
	 * using the given mapping function and enters it into this map.
	 *
	 * <p>Note that contrarily to the default {@linkplain Map#computeIfAbsent(Object, java.util.function.Function) computeIfAbsent()},
	 * it is not possible to not add a value for a given key, since the {@code mappingFunction} cannot
	 * return {@code null}. If such a behavior is needed, please use the corresponding <em>nullable</em> version.
	 *
	 * @apiNote all {@code computeIfAbsent()} methods have a different logic based on the argument;
	 * no delegation is performed, contrarily to other superficially similar 
	 * methods such as {@link Iterator#forEachRemaining} or {@link java.util.List#replaceAll}.
	 *
	 * @param key key with which the specified value is to be associated.
	 * @param mappingFunction the function to compute a value.
	 *
	 * @return the current (existing or computed) value associated with the specified key.
	 *
	 * @see Map#computeIfAbsent(Object, java.util.function.Function)
	 * @since 8.0.0
	 */
	default V computeIfAbsent(final char key, final CharacterFunction<? extends V> mappingFunction) {
	 java.util.Objects.requireNonNull(mappingFunction);
	 final V v = get(key);
	 if (containsKey(key)) return v;
	 V newValue = mappingFunction.apply(key);
	 put(key, newValue);
	 return newValue;
	}
	/** If the specified key is not already associated with a value, attempts to compute its value
	 * using the given mapping function and enters it into this map, unless the key is not present
	 * in the given mapping function.
	 *
	 * <p>This version of {@linkplain Map#computeIfAbsent(Object, java.util.function.Function) computeIfAbsent()}
	 * uses a type-specific version of {@code fastutil}'s {@link it.unimi.dsi.fastutil.Function Function}.
	 * Since {@link it.unimi.dsi.fastutil.Function Function} has a {@link it.unimi.dsi.fastutil.Function#containsKey(Object) containsKey()}
	 * method, it is possible to avoid adding a key by having {@code containsKey()} return {@code false} for that key.
	 *
	 * @apiNote all {@code computeIfAbsent()} methods have a different logic based on the argument;
	 * no delegation is performed, contrarily to other superficially similar 
	 * methods such as {@link Iterator#forEachRemaining} or {@link java.util.List#replaceAll}.
	 *
	 * @param key key with which the specified value is to be associated.
	 * @param mappingFunction the function to compute a value.
	 *
	 * @return the current (existing or computed) value associated with the specified key.
	 *
	 * @see Map#computeIfAbsent(Object, java.util.function.Function)
	 * @since 8.0.0
	 */
	default V computeIfAbsent(final char key, final Character2ObjectFunction<? extends V> mappingFunction) {
	 java.util.Objects.requireNonNull(mappingFunction);
	 final V v = get(key);
	 if (containsKey(key)) return v;
	 V newValue = mappingFunction.apply(key);
	 put(key, newValue);
	 return newValue;
	}
	/**
	 * @deprecated Please use {@code computeIfAbsent()} instead.
	 */
	@Deprecated
	default V computeIfAbsentPartial(final char key, final Character2ObjectFunction <? extends V> mappingFunction) {
	 return computeIfAbsent(key, mappingFunction);
	}
	/** If the value for the specified key is present, attempts to compute a new mapping given the key and its current mapped value.
	 *
	 * @apiNote The JDK specification for this method equates not being associated with a value with being associated with {code null}.
	 * This is not the case for this method.
	 *
	 * @param key key with which the specified value is to be associated.
	 * @param remappingFunction the function to compute a value.
	 *
	 * @return the new value associated with the specified key, or null if none.
	 *
	 * @see Map#computeIfPresent(Object, java.util.function.BiFunction)
	 * @since 8.0.0
	 */
	default V computeIfPresent(final char key, final java.util.function.BiFunction<? super Character, ? super V, ? extends V> remappingFunction) {
	 java.util.Objects.requireNonNull(remappingFunction);
	 final V oldValue = get(key);
	 if (!containsKey(key)) return null;
	 final V newValue = remappingFunction.apply(Character.valueOf(key), (oldValue));
	 if (newValue == null) { remove(key); return null; }
	 put(key, newValue);
	 return newValue;
	}
	/** Attempts to compute a mapping for the specified key and its current mapped value (or {@code null} if there is no current mapping).
	 *
	 * <p>If the function returns {@code null}, the mapping is removed (or remains absent if initially absent).
	 * If the function itself throws an (unchecked) exception, the exception is rethrown, and the current mapping is left unchanged.
	 *
	 * @param key key with which the specified value is to be associated.
	 * @param remappingFunction the function to compute a value.
	 *
	 * @return the new value associated with the specified key, or the null if none.
	 *
	 * @see Map#compute(Object, java.util.function.BiFunction)
	 * @since 8.0.0
	 */
	default V compute(final char key, final java.util.function.BiFunction<? super Character, ? super V, ? extends V> remappingFunction) {
	 java.util.Objects.requireNonNull(remappingFunction);
	 final V oldValue = get(key);
	 final boolean contained = containsKey(key);
	 final V newValue = remappingFunction.apply(Character.valueOf(key), contained ? (oldValue) : null);
	 if (newValue == null) {
	  if (contained) remove(key);
	  return null;
	 }
	 put(key, newValue);
	 return newValue;
	}

	@Deprecated
	@Override
	default V put(final Character key, final V value) {
		final char k = (key).charValue();
		final boolean containsKey = containsKey(k);
		final V v = put(k, (value));
		return containsKey ? (v) : null;
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead. */
	@Deprecated
	@Override
	default V get(final Object key) {
		if (key == null) return null;
		final char k = ((Character)(key)).charValue();
		return get(k);
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead. */
	@Deprecated
	@Override
	default V getOrDefault(final Object key, V defaultValue) {
		if (key == null) return defaultValue;
		final char k = ((Character)(key)).charValue();
		final V v = get(k);
		return (v != null) ? (v) : defaultValue;
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead. */
	@Deprecated
	@Override
	default V remove(final Object key) {
		if (key == null) return null;
		final char k = ((Character)(key)).charValue();
		return containsKey(k) ? (remove(k)) : null;
	}

	@Override
	default boolean containsKey(final Object key) {
		return key == null ? false : containsKey(((Character)(key)).charValue());
	}

	/**
	 * If the specified key is not already associated with a value, associates it with the given {@code value}.
	 * Otherwise, replaces the associated value with the results of the given remapping function, or removes if the result is {@code null}.
	 *
	 * @apiNote The JDK specification for this method equates not being associated with a value with being associated with {code null}.
	 * This is not the case for this method.
	 *
	 * @param key key with which the resulting value is to be associated.
	 * @param value the value to be merged with the existing value associated with the key or, if no existing value is associated with the key, to be associated with the key.
	 * @param remappingFunction the function to recompute a value if present.
	 *
	 * @return the new value associated with the specified key if no value is associated with the key.
	 *
	 * @see Map#merge(Object, Object, java.util.function.BiFunction)
	 * @since 8.0.0
	 */
	default V merge(final char key, final V value, final java.util.function.BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
	 java.util.Objects.requireNonNull(remappingFunction);
	 java.util.Objects.requireNonNull(value);
	 final V oldValue = get(key);
	 final V newValue;
	 if (containsKey(key)) {
	  final V mergedValue = remappingFunction.apply((oldValue), (value));
	  if (mergedValue == null) { remove(key); return null; }
	  newValue = (mergedValue);
	 } else {
	  newValue = value;
	 }
	 put(key, newValue);
	 return newValue;
	}
	/** A type-specific {@link Map.Entry}; provides some additional methods
	 *  that use polymorphism to avoid (un)boxing.
	 *
	 * @see Map.Entry
	 */
	interface Entry <V> extends Map.Entry <Character,V> {
	 /** Returns the key corresponding to this entry.
		 * @see Map.Entry#getKey()
		 */
	 char getCharacterKey();
	 /** {@inheritDoc}
		 * @deprecated Please use the corresponding type-specific method instead. */
	 @Deprecated
	 @Override
	 default Character getKey() {
	  return Character.valueOf(getCharacterKey());
	 }
	}
}