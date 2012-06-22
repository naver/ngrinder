/*
 * Copyright 2012 NHNCorp, Inc.
 *
 * NHN Corp licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.nhncorp.ngrinder.core.util;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenient class for property extraction
 * 
 * @author JunHo Yoon
 */
public class PropertiesWrapper {
	private final Properties properties;
	private final static Logger logger = LoggerFactory.getLogger(PropertiesWrapper.class);

	private static final String DEFAULT_ERROR_MESSGAE = "The %s is not defined in conf file. Use %s instead.";

	public PropertiesWrapper(Properties properties) {
		this.properties = properties;
	}

	public String getProperty(String key, String defaultValue, String errorMsgTemplate) {
		String value = this.properties.getProperty(key);
		if (StringUtils.isBlank(value)) {
			logger.error(errorMsgTemplate, key, defaultValue);
			value = defaultValue;
		}
		return value;
	}

	public String getProperty(String key, String defaultValue) {
		return getProperty(key, defaultValue, DEFAULT_ERROR_MESSGAE);
	}
}
