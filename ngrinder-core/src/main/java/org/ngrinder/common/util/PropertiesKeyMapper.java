package org.ngrinder.common.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.util.*;

import static org.ngrinder.common.util.Preconditions.checkNotNull;

public class PropertiesKeyMapper {
	private final Map<String, List<String>> keyMaps = new HashMap<>();
	private final Map<String, String> defaultValues = new HashMap<>();

	private PropertiesKeyMapper() {
	}

	public PropertiesKeyMapper init(String propertyMapName) {
		InputStream is = checkNotNull(PropertiesKeyMapper.class.getClassLoader().getResourceAsStream(propertyMapName));
		Scanner scanner = new Scanner(is);
		while (scanner.hasNextLine()) {
			String line = StringUtils.trimToEmpty(scanner.nextLine());
			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}
			String[] split = line.split(",");
			String key = null;
			List<String> values = new ArrayList<>();
			for (int i = 0; i < split.length; i++) {
				if (i == 0) {
					key = split[0];
					keyMaps.put(key, values);
				} else if (i == 1) {
					defaultValues.put(key, StringUtils.trimToNull(split[1]));
				} else {
					values.add(split[i]);
				}
			}
		}
		IOUtils.closeQuietly(is);
		return this;
	}

	public static PropertiesKeyMapper create(String mapName) {
		return new PropertiesKeyMapper().init(mapName);
	}

	public List<String> getKeys(String key) {
		return keyMaps.get(key);
	}

	public String getDefaultValue(String key) {
		return defaultValues.get(key);
	}

	public Set<String> getAllKeys() {
		return keyMaps.keySet();
	}
}
