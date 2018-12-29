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

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

import static org.ngrinder.common.util.Preconditions.checkNotNull;

/**
 * Convenient class for property extraction.
 *
 * @author JunHo Yoon
 */
public class PropertiesWrapper {
	private final Properties properties;
	@SuppressWarnings("UnusedDeclaration")
	private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesWrapper.class);
	private PropertiesKeyMapper propertiesKeyMapper;

	/**
	 * Constructor.
	 *
	 * @param properties {@link Properties} which will be used for data retrieval.
	 */
	public PropertiesWrapper(Properties properties, PropertiesKeyMapper propertiesKeyMapper) {
		this.properties = properties;
		this.propertiesKeyMapper = propertiesKeyMapper;
	}

	public boolean exist(String key) {
		String value = this.properties.getProperty(key);
		if (value != null) {
			return true;
		}

		List<String> keys = propertiesKeyMapper.getKeys(key);
		if (keys != null && !keys.isEmpty()) {
			for (String each : keys) {
				value = this.properties.getProperty(each);
				if (value != null) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get the property.
	 *
	 * @param key property key
	 * @return property value
	 */
	public String getProperty(String key) {
		String value = this.properties.getProperty(key);
		if (StringUtils.isNotBlank(value)) {
			return value.trim();
		}

		List<String> keys = propertiesKeyMapper.getKeys(key);
		for (String each : checkNotNull(keys, key + " should be exists")) {
			value = this.properties.getProperty(each);
			if (StringUtils.isNotBlank(value)) {
				return value.trim();
			}
		}
		return propertiesKeyMapper.getDefaultValue(key);
	}

	public String getProperty(String key, String defaultValue) {
		try {
			String property = getProperty(key);
			return StringUtils.isEmpty(property) ? defaultValue : property;
		} catch (IllegalArgumentException e) {
			return defaultValue;
		}
	}

	public int getPropertyInt(String key, int defaultValue) {
		try {
			return getPropertyInt(key);
		} catch (IllegalArgumentException e) {
			return defaultValue;
		}
	}

	public boolean getPropertyBoolean(String key, boolean defaultValue) {
		try {
			return getPropertyBoolean(key);
		} catch (IllegalArgumentException e) {
			return defaultValue;
		}
	}

	/**
	 * Add the property.
	 *
	 * @param key   property key
	 * @param value property value
	 */
	public void addProperty(String key, String value) {
		this.properties.put(key, value);
	}

	/**
	 * Get property as integer.
	 *
	 * @param key property key
	 * @return property integer value
	 */
	public int getPropertyInt(String key) {
		return NumberUtils.toInt(getProperty(key));
	}

	/**
	 * Get property as long.
	 *
	 * @param key property key
	 * @return property long value
	 */
	public long getPropertyLong(String key) {
		return NumberUtils.toLong(getProperty(key));
	}

	/**
	 * Get the property as boolean.
	 *
	 * @param key property key
	 * @return property boolean value
	 */
	public boolean getPropertyBoolean(String key) {
		return BooleanUtils.toBoolean(getProperty(key));
	}

}
