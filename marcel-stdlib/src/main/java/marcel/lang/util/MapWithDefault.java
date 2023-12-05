/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package marcel.lang.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * A wrapper for Map which allows a default value to be specified using a function.
 * Normally not instantiated directly but used via the DGM <code>withDefault</code> method.
 *
 */
public class MapWithDefault<K, V> implements Map<K, V> {

    protected final Map<K, V> delegate;
    private final Function<Object, V> defaultFunction;

    private MapWithDefault(Map<K, V> m, Function<Object, V> defaultFunction) {
        this.delegate = m;
        this.defaultFunction = defaultFunction;
    }

    public static <K, V> Map<K, V> newInstance(Map<K, V> m, Function<Object, V> defaultFunction, boolean insert) {
        return insert ? new InsertingMapWithDefault<>(m, defaultFunction) : new MapWithDefault<>(m, defaultFunction);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or the default value as specified by the initializing closure
     * if this map contains no mapping for the key.
     *
     * If <code>autoGrow</code> is true and the initializing closure is called,
     * the map is modified to contain the new key and value so that the calculated
     * value is effectively cached if needed again.
     * Otherwise, the map will be unchanged.
     */
    @Override
    public V get(Object key) {
        if (delegate.containsKey(key)) {
            return delegate.get(key);
        }
        return getDefaultValue(key);
    }

    protected V getDefaultValue(Object key) {
        return defaultFunction.apply(key);
    }

    /**
     * Associates the specified value with the specified key in this map.
     *
     * If <code>autoShrink</code> is true, the initializing closure is called
     * and if it evaluates to the value being stored, the value will not be stored
     * and indeed any existing value will be removed. This can be useful when trying
     * to keep the memory requirements small for large key sets where only a spare
     * number of entries differ from the default.
     *
     * @return the previous value associated with {@code key} if any, otherwise {@code null}.
     */
    @Override
    public V put(K key, V value) {
        return delegate.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return delegate.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Set<K> keySet() {
        return delegate.keySet();
    }

    @Override
    public Collection<V> values() {
        return delegate.values();
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return delegate.entrySet();
    }

    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    private static class InsertingMapWithDefault<K, V> extends MapWithDefault<K, V> {

        private InsertingMapWithDefault(Map<K, V> m, Function<Object, V> defaultFunction) {
            super(m, defaultFunction);
        }

        public V get(Object key) {
            if (delegate.containsKey(key)) {
                return delegate.get(key);
            }
            V value = getDefaultValue(key);
            delegate.put((K)key, value);
            return value;
        }
    }
}