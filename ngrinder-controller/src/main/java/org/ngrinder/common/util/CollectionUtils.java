/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.ngrinder.common.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Collection utilities.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public abstract class CollectionUtils {
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
