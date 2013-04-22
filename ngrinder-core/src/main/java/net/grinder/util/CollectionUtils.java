package net.grinder.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class CollectionUtils {
	public static <K, V> Map<K, V> newHashMap() {
		return new HashMap<K, V>();
	}

	public static <K, V> Map<K, V> newLinkedHashMap() {
		return new LinkedHashMap<K, V>();
	}

	public static <K> Set<K> newHashSet() {
		return new HashSet<K>();
	}

	public static <K> Set<K> newLinkedHashSet() {
		return new LinkedHashSet<K>();
	}

	public static <L> List<L> newArrayList() {
		return new ArrayList<L>();
	}
}
