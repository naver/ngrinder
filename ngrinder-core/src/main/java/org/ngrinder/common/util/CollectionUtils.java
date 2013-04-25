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
	 * @param <K>
	 *            key
	 * @param <V>
	 *            value
	 * @return {@link HashMap}
	 */
	public static <K, V> Map<K, V> newHashMap() {
		return new HashMap<K, V>();
	}

	/**
	 * Create new {@link LinkedHashMap}.
	 * 
	 * @param <K>
	 *            key
	 * @param <V>
	 *            value
	 * @return {@link LinkedHashMap}
	 */
	public static <K, V> Map<K, V> newLinkedHashMap() {
		return new LinkedHashMap<K, V>();
	}

	/**
	 * Create new {@link HashSet}.
	 * 
	 * @param <K>
	 *            key
	 * @return {@link HashSet}
	 */
	public static <K> Set<K> newHashSet() {
		return new HashSet<K>();
	}

	/**
	 * Create new {@link LinkedHashSet}.
	 * 
	 * @param <K>
	 *            key
	 * @return {@link LinkedHashSet}
	 */
	public static <K> Set<K> newLinkedHashSet() {
		return new LinkedHashSet<K>();
	}

	/**
	 * Create new {@link ArrayList}.
	 * 
	 * @param <L>
	 *            type
	 * @return {@link ArrayList}
	 */
	public static <L> List<L> newArrayList() {
		return new ArrayList<L>();
	}

	/**
	 * @param key1
	 * @param value1
	 * @return
	 */
	public static Map<String, Object> buildMap(String key1, Object value1) {
		Map<String, Object> map = new HashMap<String, Object>(1);
		map.put(key1, value1);
		return map;
	}

	public static Map<String, Object> buildMap(String key1, Object value1, String key2, Object value2) {
		Map<String, Object> map = new HashMap<String, Object>(2);
		map.put(key1, value1);
		map.put(key2, value2);
		return map;
	}

	public static Map<String, Object> buildMap(String key1, Object value1, String key2, Object value2, String key3,
					Object value3) {
		Map<String, Object> map = new HashMap<String, Object>(2);
		map.put(key1, value1);
		map.put(key2, value2);
		map.put(key3, value3);
		return map;
	}

	/**
	 * Select the given number of elements from the given set.
	 * 
	 * @param <T>
	 *            encapsulated type
	 * @param set
	 *            set
	 * @param count
	 *            number of elements to retrieve
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
	 * @param size
	 *            size of {@link HashMap}
	 * @param <K>
	 *            keyType
	 * @param <V>
	 *            valueType
	 * @return created {@link HashMap} instance
	 */
	public static <K, V> HashMap<K, V> newHashMap(int size) {
		return new HashMap<K, V>(size);
	}

	/**
	 * Create new {@link HashMap} having same size of given base collection.
	 * 
	 * @param base
	 *            collection which size will be referred
	 * @param <K>
	 *            keyType
	 * @param <V>
	 *            valueType
	 * @return created {@link HashMap} instance
	 */
	public static <K, V> HashMap<K, V> newHashMap(Collection<?> base) {
		return new HashMap<K, V>(base.size());
	}

}
