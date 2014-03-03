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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.ngrinder.common.util.NoOp.noOp;
import static org.ngrinder.common.util.Preconditions.checkArgument;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

/**
 * Reflection Utility functions.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public abstract class ReflectionUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionUtils.class);

	/**
	 * Get object field value, bypassing getter method.
	 * 
	 * @param object	object
	 * @param fieldName	field Name
	 * @return fileValue
	 */
	public static Object getFieldValue(final Object object, final String fieldName) {
		Field field = getDeclaredField(object, fieldName);
		checkNotNull(field, "Could not find field [%s] on target [%s]", fieldName, object);
		makeAccessible(field);

		try {
			return field.get(object);
		} catch (IllegalAccessException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return null;
	}

	private static Field getDeclaredField(final Object object, final String fieldName) {
		checkNotNull(object);
		checkArgument(StringUtils.isNotBlank(fieldName));

		// CHECKSTYLE:OFF
		for (Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass
						.getSuperclass()) {
			try {
				return superClass.getDeclaredField(fieldName);
			} catch (NoSuchFieldException e) {
				// Fall through
				noOp();
			}
		}
		return null;
	}

	private static void makeAccessible(final Field field) {
		if (!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers())) {
			field.setAccessible(true);
		}
	}

}
