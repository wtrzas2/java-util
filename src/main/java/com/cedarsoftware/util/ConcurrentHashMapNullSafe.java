package com.cedarsoftware.util;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * ConcurrentHashMapNullSafe is a thread-safe implementation of ConcurrentHashMap
 * that allows null keys and null values by using sentinel objects internally.
 *
 * @param <K> The type of keys maintained by this map
 * @param <V> The type of mapped values
 * <br>
 * @author John DeRegnaucourt
 *         <br>
 *         Copyright Cedar Software LLC
 *         <br><br>
 *         Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *         <br><br>
 *         <a href="http://www.apache.org/licenses/LICENSE-2.0">License</a>
 *         <br><br>
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 */
public class ConcurrentHashMapNullSafe<K, V> implements Map<K, V> {
    // Sentinel objects to represent null keys and values
    private enum NullSentinel {
        NULL_KEY, NULL_VALUE
    }

    // Internal ConcurrentHashMap storing Objects
    private final ConcurrentHashMap<Object, Object> internalMap;

    /**
     * Constructs a new, empty map with default initial capacity (16) and load factor (0.75).
     */
    public ConcurrentHashMapNullSafe() {
        this.internalMap = new ConcurrentHashMap<>();
    }

    /**
     * Constructs a new, empty map with the specified initial capacity and default load factor (0.75).
     *
     * @param initialCapacity the initial capacity. The implementation performs internal sizing
     *                        to accommodate this many elements.
     * @throws IllegalArgumentException if the initial capacity is negative.
     */
    public ConcurrentHashMapNullSafe(int initialCapacity) {
        this.internalMap = new ConcurrentHashMap<>(initialCapacity);
    }

    /**
     * Constructs a new, empty map with the specified initial capacity
     * and load factor.
     *
     * @param initialCapacity the initial capacity. The implementation
     * performs internal sizing to accommodate this many elements.
     * @param loadFactor the load factor threshold, used to control resizing.
     * Resizing may be performed when the average number of elements per
     * bin exceeds this threshold.
     * @throws IllegalArgumentException if the initial capacity is
     * negative or the load factor is nonpositive
     */
    public ConcurrentHashMapNullSafe(int initialCapacity, float loadFactor) {
        this.internalMap = new ConcurrentHashMap<>(initialCapacity, loadFactor);
    }

    /**
     * Constructs a new map with the same mappings as the specified map.
     *
     * @param m the map whose mappings are to be placed in this map
     * @throws NullPointerException if the specified map is null
     */
    public ConcurrentHashMapNullSafe(Map<? extends K, ? extends V> m) {
        this.internalMap = new ConcurrentHashMap<>();
        putAll(m);
    }

    // Helper methods to handle nulls
    private Object maskNullKey(K key) {
        return key == null ? NullSentinel.NULL_KEY : key;
    }

    @SuppressWarnings("unchecked")
    private K unmaskNullKey(Object key) {
        return key == NullSentinel.NULL_KEY ? null : (K) key;
    }

    private Object maskNullValue(V value) {
        return value == null ? NullSentinel.NULL_VALUE: value;
    }

    @SuppressWarnings("unchecked")
    private V unmaskNullValue(Object value) {
        return value == NullSentinel.NULL_VALUE ? null : (V) value;
    }

    @Override
    public int size() {
        return internalMap.size();
    }

    @Override
    public boolean isEmpty() {
        return internalMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return internalMap.containsKey(maskNullKey((K) key));
    }

    @Override
    public boolean containsValue(Object value) {
        if (value == null) {
            return internalMap.containsValue(NullSentinel.NULL_VALUE);
        }
        return internalMap.containsValue(value);
    }
    
    @Override
    public V get(Object key) {
        Object val = internalMap.get(maskNullKey((K) key));
        return unmaskNullValue(val);
    }

    @Override
    public V put(K key, V value) {
        Object prev = internalMap.put(maskNullKey(key), maskNullValue(value));
        return unmaskNullValue(prev);
    }

    @Override
    public V remove(Object key) {
        Object prev = internalMap.remove(maskNullKey((K) key));
        return unmaskNullValue(prev);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            internalMap.put(maskNullKey(entry.getKey()), maskNullValue(entry.getValue()));
        }
    }

    @Override
    public void clear() {
        internalMap.clear();
    }

    @Override
    public Set<K> keySet() {
        Set<Object> internalKeys = internalMap.keySet();
        return new AbstractSet<K>() {
            @Override
            public Iterator<K> iterator() {
                Iterator<Object> it = internalKeys.iterator();
                return new Iterator<K>() {
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public K next() {
                        return unmaskNullKey(it.next());
                    }

                    @Override
                    public void remove() {
                        it.remove();
                    }
                };
            }

            @Override
            public int size() {
                return internalKeys.size();
            }

            @Override
            public boolean contains(Object o) {
                return internalMap.containsKey(maskNullKey((K) o));
            }

            @Override
            public boolean remove(Object o) {
                return internalMap.remove(maskNullKey((K) o)) != null;
            }

            @Override
            public void clear() {
                internalMap.clear();
            }
        };
    }

    @Override
    public Collection<V> values() {
        Collection<Object> internalValues = internalMap.values();
        return new AbstractCollection<V>() {
            @Override
            public Iterator<V> iterator() {
                Iterator<Object> it = internalValues.iterator();
                return new Iterator<V>() {
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public V next() {
                        return unmaskNullValue(it.next());
                    }

                    @Override
                    public void remove() {
                        it.remove();
                    }
                };
            }

            @Override
            public int size() {
                return internalValues.size();
            }

            @Override
            public boolean contains(Object o) {
                return internalMap.containsValue(maskNullValue((V) o));
            }

            @Override
            public void clear() {
                internalMap.clear();
            }
        };
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<Object, Object>> internalEntries = internalMap.entrySet();
        return new AbstractSet<Entry<K, V>>() {
            @Override
            public Iterator<Entry<K, V>> iterator() {
                Iterator<Entry<Object, Object>> it = internalEntries.iterator();
                return new Iterator<Entry<K, V>>() {
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public Entry<K, V> next() {
                        Entry<Object, Object> internalEntry = it.next();
                        return new Entry<K, V>() {
                            @Override
                            public K getKey() {
                                return unmaskNullKey(internalEntry.getKey());
                            }

                            @Override
                            public V getValue() {
                                return unmaskNullValue(internalEntry.getValue());
                            }

                            @Override
                            public V setValue(V value) {
                                Object oldValue = internalEntry.setValue(maskNullValue(value));
                                return unmaskNullValue(oldValue);
                            }
                        };
                    }

                    @Override
                    public void remove() {
                        it.remove();
                    }
                };
            }

            @Override
            public int size() {
                return internalEntries.size();
            }

            @Override
            public boolean contains(Object o) {
                if (!(o instanceof Entry)) return false;
                Entry<?, ?> e = (Entry<?, ?>) o;
                Object val = internalMap.get(maskNullKey((K) e.getKey()));
                return maskNullValue((V) e.getValue()).equals(val);
            }

            @Override
            public boolean remove(Object o) {
                if (!(o instanceof Entry)) return false;
                Entry<?, ?> e = (Entry<?, ?>) o;
                return internalMap.remove(maskNullKey((K) e.getKey()), maskNullValue((V) e.getValue()));
            }

            @Override
            public void clear() {
                internalMap.clear();
            }
        };
    }

    // Implement other default methods as needed, ensuring they handle nulls appropriately.

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        Object val = internalMap.get(maskNullKey((K) key));
        return (val != null) ? unmaskNullValue(val) : defaultValue;
    }

    @Override
    public V putIfAbsent(K key, V value) {
        Object prev = internalMap.putIfAbsent(maskNullKey(key), maskNullValue(value));
        return unmaskNullValue(prev);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return internalMap.remove(maskNullKey((K) key), maskNullValue((V) value));
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return internalMap.replace(maskNullKey(key), maskNullValue(oldValue), maskNullValue(newValue));
    }

    @Override
    public V replace(K key, V value) {
        Object prev = internalMap.replace(maskNullKey(key), maskNullValue(value));
        return unmaskNullValue(prev);
    }

    @Override
    public V computeIfAbsent(K key, java.util.function.Function<? super K, ? extends V> mappingFunction) {
        Object maskedKey = maskNullKey(key);
        Object currentValue = internalMap.get(maskedKey);

        if (currentValue != null && currentValue != NullSentinel.NULL_VALUE) {
            // The key exists with a non-null value, so we don't compute
            return unmaskNullValue(currentValue);
        }

        // The key doesn't exist or is mapped to null, so we should compute
        V newValue = mappingFunction.apply(unmaskNullKey(maskedKey));
        if (newValue != null) {
            Object result = internalMap.compute(maskedKey, (k, v) -> {
                if (v != null && v != NullSentinel.NULL_VALUE) {
                    return v;  // Another thread set a non-null value, so we keep it
                }
                return maskNullValue(newValue);
            });
            return unmaskNullValue(result);
        } else {
            // If the new computed value is null, ensure no mapping exists
            internalMap.remove(maskedKey);
            return null;
        }
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Object maskedKey = maskNullKey(key);
        Object result = internalMap.compute(maskedKey, (k, v) -> {
            V oldValue = unmaskNullValue(v);
            V newValue = remappingFunction.apply(unmaskNullKey(k), oldValue);
            return (newValue == null) ? null : maskNullValue(newValue);
        });

        return unmaskNullValue(result);
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(value);
        Object maskedKey = maskNullKey(key);
        Object result = internalMap.merge(maskedKey, maskNullValue(value), (v1, v2) -> {
            V unmaskV1 = unmaskNullValue(v1);
            V unmaskV2 = unmaskNullValue(v2);
            V newValue = remappingFunction.apply(unmaskV1, unmaskV2);
            return (newValue == null) ? null : maskNullValue(newValue);
        });

        return unmaskNullValue(result);
    }
    /**
     * Overrides the equals method to ensure proper comparison between two maps.
     * Two maps are considered equal if they contain the same key-value mappings.
     *
     * @param o the object to be compared for equality with this map
     * @return true if the specified object is equal to this map
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Map)) return false;
        Map<?, ?> other = (Map<?, ?>) o;
        if (this.size() != other.size()) return false;
        for (Entry<K, V> entry : this.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();
            if (!other.containsKey(key)) return false;
            Object otherValue = other.get(key);
            if (value == null) {
                if (otherValue != null) return false;
            } else {
                if (!value.equals(otherValue)) return false;
            }
        }
        return true;
    }

    /**
     * Overrides the hashCode method to ensure consistency with equals.
     * The hash code of a map is defined to be the sum of the hash codes of each entry in the map.
     *
     * @return the hash code value for this map
     */
    @Override
    public int hashCode() {
        int h = 0;
        for (Entry<K, V> entry : this.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();
            int keyHash = (key == null) ? 0 : key.hashCode();
            int valueHash = (value == null) ? 0 : value.hashCode();
            h += keyHash ^ valueHash;
        }
        return h;
    }

    /**
     * Overrides the toString method to provide a string representation of the map.
     * The string representation consists of a list of key-value mappings in the order returned by the map's entrySet view's iterator,
     * enclosed in braces ("{}"). Adjacent mappings are separated by the characters ", " (comma and space).
     *
     * @return a string representation of this map
     */
    @Override
    public String toString() {
        Iterator<Entry<K, V>> it = this.entrySet().iterator();
        if (!it.hasNext())
            return "{}";

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (;;) {
            Entry<K, V> e = it.next();
            K key = e.getKey();
            V value = e.getValue();
            sb.append(key == this ? "(this Map)" : key);
            sb.append('=');
            sb.append(value == this ? "(this Map)" : value);
            if (!it.hasNext())
                return sb.append('}').toString();
            sb.append(',').append(' ');
        }
    }
}
