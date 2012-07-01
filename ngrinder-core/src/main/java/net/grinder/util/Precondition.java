package net.grinder.util;

import java.security.InvalidParameterException;

public class Precondition {
	public static <T> T notNull(T object) {
		if (object != null) {
			return object;
		}
		throw new NullPointerException();
	}

	public static String notEmpty(String object) {
		if (object != null && !object.isEmpty()) {
			return object;
		}
		throw new NullPointerException("passed string should not be empty.");
	}

	public static String notEmpty(String object, String message) {
		if (object != null && !object.isEmpty()) {
			return object;
		}
		throw new NullPointerException(message);
	}

	public static int notZero(int value, String errorMsg) {
		if (value != 0) {
			return value;
		}
		throw new InvalidParameterException(errorMsg);
	}

	public static void isTrue(boolean condition, String errorMsg) {
		if (!condition) {
			throw new InvalidParameterException(errorMsg);
		}
	}
}
