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

import static org.ngrinder.common.util.NoOp.noOp;
import static org.ngrinder.common.util.Preconditions.checkArgument;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reflection Utility functions.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public abstract class ReflectionUtil {

	private static final Logger LOG = LoggerFactory.getLogger(ReflectionUtil.class);

	/**
	 * get object field value, bypassing getter method.
	 * 
	 * @param object
	 *            object
	 * @param fieldName
	 *            field Name
	 * @return fileValue
	 */
	public static Object getFieldValue(final Object object, final String fieldName) {
		Field field = getDeclaredField(object, fieldName);
		checkNotNull(field, "Could not find field [%s] on target [%s]", fieldName, object);
		makeAccessible(field);

		try {
			return field.get(object);
		} catch (IllegalAccessException e) {
			LOG.error(e.getMessage(), e);
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

	/**
	 * Invoke private method.
	 * 
	 * @param object
	 *            object
	 * @param methodName
	 *            private method name
	 * @param parameters
	 *            private method parameter
	 * @return return value
	 */
	public static Object invokePrivateMethod(Object object, String methodName, Object[] parameters) {
		checkNotNull(object);
		checkArgument(StringUtils.isNotBlank(methodName));

		Class<?>[] newClassParam = new Class[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			newClassParam[i] = parameters[i].getClass();
		}
		try {
			Method declaredMethod = getDeclaredMethod(object.getClass(), methodName, newClassParam);
			if (declaredMethod == null) {
				LOG.error("No method {} found in {}", methodName, object.getClass());
				return null;
			}
			declaredMethod.setAccessible(true);
			return declaredMethod.invoke(object, parameters);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Get Method object.
	 * 
	 * @param clazz
	 *            clazz to be inspected
	 * @param methodName
	 *            method name
	 * @param parameters
	 *            parameter list
	 * @return {@link Method} instance. otherwise null.
	 */
	private static Method getDeclaredMethod(final Class<?> clazz, final String methodName, 
											final Class<?>[] parameters) {
		checkNotNull(clazz);
		checkArgument(StringUtils.isNotBlank(methodName));

		for (Class<?> superClass = clazz; superClass != Object.class; superClass = superClass.getSuperclass()) {
			try {
				return superClass.getDeclaredMethod(methodName, parameters);
			} catch (Exception e) {
				// Fall through
				noOp();
			}
		}
		return null;
	}

}
