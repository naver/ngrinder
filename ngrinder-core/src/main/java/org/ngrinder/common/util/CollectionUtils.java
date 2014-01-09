package org.ngrinder.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Collection utilities for object creation.
 *
 * @author JunHo Yoon
 * @since 3.1
 */
public abstract class CollectionUtils {
	/**
	 * Create new {@link HashMap}.
	 *
	 * @param <K> key
	 * @param <V> value
	 * @return {@link HashMap}
	 */
	public static <K, V> Map<K, V> newHashMap() {
		return new HashMap<K, V>();
	}

	/**
	 * Create new {@link LinkedHashMap}.
	 *
	 * @param <K> key
	 * @param <V> value
	 * @return {@link LinkedHashMap}
	 */
	public static <K, V> Map<K, V> newLinkedHashMap() {
		return new LinkedHashMap<K, V>();
	}

	/**
	 * Create new {@link HashSet}.
	 *
	 * @param <K> key
	 * @return {@link HashSet}
	 */
	public static <K> Set<K> newHashSet() {
		return new HashSet<K>();
	}

	/**
	 * Create new {@link LinkedHashSet}.
	 *
	 * @param <K> key
	 * @return {@link LinkedHashSet}
	 */
	public static <K> Set<K> newLinkedHashSet() {
		return new LinkedHashSet<K>();
	}

	/**
	 * Create new {@link ArrayList}.
	 *
	 * @param <L>  type
	 * @param size size
	 * @return {@link ArrayList}
	 */
	public static <L> List<L> newArrayList(int size) {
		return new ArrayList<L>(size);
	}

	/**
	 * Create new {@link ArrayList}.
	 *
	 * @param <L> type
	 * @return {@link ArrayList}
	 */
	public static <L> List<L> newArrayList() {
		return new ArrayList<L>();
	}

	/**
	 * Build Map with 1 pair.
	 *
	 * @param <K>    key type
	 * @param <V>    value type
	 * @param key1   key
	 * @param value1 value
	 * @return created map
	 */
	public static <K, V> Map<K, V> buildMap(K key1, V value1) {
		Map<K, V> map = new HashMap<K, V>(1);
		map.put(key1, value1);
		return map;
	}

	/**
	 * Build Map with 2 pairs.
	 *
	 * @param <K>    key type
	 * @param <V>    value type
	 * @param key1   key
	 * @param value1 value
	 * @param key2   key
	 * @param value2 value
	 * @return create map
	 */
	public static <K, V> Map<K, V> buildMap(K key1, V value1, K key2, V value2) {
		Map<K, V> map = new HashMap<K, V>(2);
		map.put(key1, value1);
		map.put(key2, value2);
		return map;
	}

	/**
	 * Build Map with 3 pairs.
	 *
	 * @param <K>    key type
	 * @param <V>    value type
	 * @param key1   key
	 * @param value1 value
	 * @param key2   key
	 * @param value2 value
	 * @param key3   key
	 * @param value3 value
	 * @return create map
	 */
	public static <K, V> Map<K, V> buildMap(K key1, V value1, K key2, V value2, K key3, V value3) {
		Map<K, V> map = new HashMap<K, V>(3);
		map.put(key1, value1);
		map.put(key2, value2);
		map.put(key3, value3);
		return map;
	}

	/**
	 * Select the given number of elements from the given set.
	 *
	 * @param <T>   encapsulated type
	 * @param set   set
	 * @param count number of elements to retrieve
	 * @return set
	 */
	public static <T> Set<T> selectSome(Set<T> set, int count) {
		Set<T> newSet = new HashSet<T>();
		int i = 0;
		for (T each : set) {
			if (++i > count) {
				break;
			}
			newSet.add(each);
		}
		return newSet;
	}

	/**
	 * Create new {@link HashMap}.
	 *
	 * @param size size of {@link HashMap}
	 * @param <K>  keyType
	 * @param <V>  valueType
	 * @return created {@link HashMap} instance
	 */
	public static <K, V> HashMap<K, V> newHashMap(int size) {
		return new HashMap<K, V>(size);
	}

	/**
	 * Create new {@link HashMap} having same size of given base collection.
	 *
	 * @param base collection which size will be referred
	 * @param <K>  keyType
	 * @param <V>  valueType
	 * @return created {@link HashMap} instance
	 */
	public static <K, V> HashMap<K, V> newHashMap(Collection<?> base) {
		return new HashMap<K, V>(base.size());
	}

	/**
	 * Get the value from the map for the given key. It the value does not exist, return the given
	 * defaultValue.
	 *
	 * @param <K>          key type
	 * @param <V>          value type
	 * @param map          map
	 * @param key          key
	 * @param defaultValue default value if the value is null.
	 * @return value
	 */
	public static <K, V> V getValue(Map<K, V> map, K key, V defaultValue) {
		V v = map.get(key);
		if (v == null) {
			return defaultValue;
		}
		return v;
	}

}
