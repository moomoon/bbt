package org.ddrr.bbt.infra;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class TypeUtils {

	public static <K, V> Map<K, V> castMap(Class<K> keyClass,
			Class<V> valueClass, Map<?, ?> src) {
		int count = 0;
		if (null != src) {
			count = src.size();
		}
		Map<K, V> result = new HashMap<K, V>(count);
		if (count > 0) {
			for (Entry<?, ?> entry : src.entrySet()) {
				result.put(keyClass.cast(entry.getKey()),
						valueClass.cast(entry.getValue()));
			}
		}
		return result;
	}
}
