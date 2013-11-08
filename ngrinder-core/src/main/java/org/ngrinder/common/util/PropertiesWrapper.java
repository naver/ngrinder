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

import java.util.Properties;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenient class for property extraction.
 * 
 * @author JunHo Yoon
 */
public class PropertiesWrapper {
	private final Properties properties;
	private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesWrapper.class);

	private static final String DEFAULT_ERROR_MESSGAE = "The {} is not defined in conf file. Use {} instead.";

	/**
	 * Constructor.
	 * 
	 * @param properties
	 *            {@link Properties} which will be used for data retrieval.
	 */
	public PropertiesWrapper(Properties properties) {
		this.properties = properties;
	}

	/**
	 * Get the property.
	 * 
	 * @param key
	 *            property key
	 * @param defaultValue
	 *            default value when data is not available
	 * @param errorMsgTemplate
	 *            error msg
	 * @return property value
	 */
	public String getProperty(String key, String defaultValue, String errorMsgTemplate) {
		String value = this.properties.getProperty(key);
		if (StringUtils.isBlank(value)) {
			LOGGER.trace(errorMsgTemplate, key, defaultValue);
			value = defaultValue;
		} else {
			value = value.trim();
		}
		return value;
	}

	/**
	 * Get the property for the given property key.
	 * 
	 * @param key
	 *            property key
	 * @param defaultValue
	 *            default value when data is not available
	 * @return property value
	 */
	public String getProperty(String key, String defaultValue) {
		return StringUtils.trim(getProperty(key, defaultValue, DEFAULT_ERROR_MESSGAE));
	}

	/**
	 * Get the property for the given property key considering with backward
	 * compatibility.
	 * 
	 * @param key
	 *            property key
	 * @param oldKey
	 *            old property key.
	 * @param defaultValue
	 *            default value when data is not available
	 * @return property value
	 */
	public String getPropertyWithBackwardCompatibility(String key, String oldKey, String defaultValue) {
		String property = getProperty(key, "", DEFAULT_ERROR_MESSGAE);
		if (StringUtils.isEmpty(property)) {
			property = getProperty(oldKey, defaultValue, DEFAULT_ERROR_MESSGAE);
		}
		return StringUtils.trim(property);
	}

	/**
	 * Add the property.
	 * 
	 * @param key
	 *            property key
	 * @param value
	 *            property value
	 */
	public void addProperty(String key, String value) {
		this.properties.put(key, value);
	}

	/**
	 * Get property as integer.
	 * 
	 * @param key
	 *            property key
	 * @param defaultValue
	 *            default value when data is not available
	 * @return property integer value
	 */
	public int getPropertyInt(String key, int defaultValue) {
		String property = getProperty(key, String.valueOf(defaultValue), DEFAULT_ERROR_MESSGAE);
		return NumberUtils.toInt(property, defaultValue);
	}

	/**
	 * Get the property as boolean.
	 * 
	 * @param key
	 *            property key
	 * @param defaultValue
	 *            default value when data is not available
	 * @return property boolean value
	 */
	public boolean getPropertyBoolean(String key, boolean defaultValue) {
		String property = getProperty(key, String.valueOf(defaultValue), DEFAULT_ERROR_MESSGAE);
		return BooleanUtils.toBoolean(property);
	}

	/**
	 * Set the property.
	 * 
	 * @param key
	 *            key
	 * @param value
	 *            value to be stored.
	 */
	public void setProperty(String key, String value) {
		this.properties.setProperty(key, value);
	}
}
