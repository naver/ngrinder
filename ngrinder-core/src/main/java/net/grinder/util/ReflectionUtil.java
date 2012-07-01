package net.grinder.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reflection Utility functions.
 * 
 */
public final class ReflectionUtil {

	private static final Logger LOG = LoggerFactory
			.getLogger(ReflectionUtil.class);

	private ReflectionUtil() {
	}

	/**
	 * get object field value, bypassing getter method.
	 */
	public static Object getFieldValue(final Object object,
			final String fieldName) {
		Field field = getDeclaredField(object, fieldName);

		if (field == null) {
			throw new IllegalArgumentException("Could not find field ["
					+ fieldName + "] on target [" + object + "]");
		}

		makeAccessible(field);

		Object result = null;
		try {
			result = field.get(object);
		} catch (IllegalAccessException e) {
			LOG.error(e.getMessage(), e);
		}
		return result;
	}

	private static Field getDeclaredField(final Object object,
			final String fieldName) {
		if (object == null) {
			return null;
		}
		if (StringUtils.isBlank(fieldName)) {
			return null;
		}
		for (Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass
				.getSuperclass()) {
			try {
				return superClass.getDeclaredField(fieldName);
			} catch (NoSuchFieldException e) {
				// Field is not defined in current class, go on get superClass
			}
		}
		return null;
	}

	private static void makeAccessible(final Field field) {
		if (!Modifier.isPublic(field.getModifiers())
				|| !Modifier.isPublic(field.getDeclaringClass().getModifiers())) {
			field.setAccessible(true);
		}
	}

}
