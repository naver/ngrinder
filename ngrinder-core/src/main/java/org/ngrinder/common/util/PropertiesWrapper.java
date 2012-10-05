/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
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
	 * Get property.
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
		}
		return value;
	}

	/**
	 * Get property.
	 * 
	 * @param key
	 *            property key
	 * @param defaultValue
	 *            default value when data is not available
	 * @return property value
	 */
	public String getProperty(String key, String defaultValue) {
		return getProperty(key, defaultValue, DEFAULT_ERROR_MESSGAE);
	}

	/**
	 * Add property.
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
	 * Get property as boolean.
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
}
