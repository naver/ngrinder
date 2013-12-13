package org.ngrinder.common.util;

import org.ngrinder.common.exception.NGrinderRuntimeException;

import java.util.List;

/**
 * Exception processing utility.
 * 
 * @author junoyoon
 * @since 3.2.3
 */
@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
public abstract class ExceptionUtils {
	/**
	 * Check if the exception is {@link NGrinderRuntimeException}. If so, throw it. If
	 * not, wrap the given exception and throw it.
	 * 
	 * @param t 	Throwable
	 * @return exception
	 */
	public static NGrinderRuntimeException processException(Throwable t) {
		if (t instanceof NGrinderRuntimeException) {
			throw (NGrinderRuntimeException) sanitize(t);
		} else {
			throw new NGrinderRuntimeException(sanitize(t), true);
		}
	}

	/**
	 * Check if the exception {@link NGrinderRuntimeException}. If so, throw. If
	 * not, wrap the given exception.
	 * 
	 * @param message	message
	 * @return exception
	 */
	public static NGrinderRuntimeException processException(String message) {
		throw processException(new NGrinderRuntimeException(message));
	}

	/**
	 * Check if the exception is {@link NGrinderRuntimeException}. If so, throw.
	 * If not, wrap the given exception.
	 * 
	 * @param message	message
	 * @param t 		Throwable
	 * @return exception
	 */
	public static NGrinderRuntimeException processException(String message, Throwable t) {
		if (t instanceof NGrinderRuntimeException) {
			throw (NGrinderRuntimeException) sanitize(t);
		} else {
			throw new NGrinderRuntimeException(message, sanitize(t), true);
		}
	}

	/**
	 * Filter the stacktrace elements with only interesting one.
	 * 
	 * @param throwable	throwable
	 * @return {@link Throwable} instance with interested stacktrace elements.
	 */
	public static Throwable sanitize(Throwable throwable) {
		if (throwable instanceof NGrinderRuntimeException) {
			if (((NGrinderRuntimeException) throwable).isSanitized()) {
				return throwable;
			}
		}
		Throwable t = throwable;
		while (t != null) {
			// Note that this getBoolean access may well be synced...
			StackTraceElement[] trace = t.getStackTrace();
			List<StackTraceElement> newTrace = CollectionUtils.newArrayList();
			for (StackTraceElement stackTraceElement : trace) {
				if (isApplicationClass(stackTraceElement.getClassName())) {
					newTrace.add(stackTraceElement);
				}
			}
			StackTraceElement[] clean = new StackTraceElement[newTrace.size()];
			newTrace.toArray(clean);
			t.setStackTrace(clean);
			t = t.getCause();
		}
		if (throwable instanceof NGrinderRuntimeException) {
			((NGrinderRuntimeException) throwable).setSanitized(true);
		}
		return throwable;
	}

	/**
	 * Check if the given class name is the application class or not.
	 * 
	 * @param className	class name including package name
	 * @return true if application class
	 */
	private static boolean isApplicationClass(String className) {
		for (String each : getUninterestingPackages()) {
			if (className.startsWith(each)) {
				return false;
			}
		}
		return true;
	}

	private static final String[] NON_NGRINDER_PACKAGE = ("org.springframework.," + "javax.," + "org.apache.catalina.,"
			+ "sun.," + "net.sf.," + "java.," + "org.ngrinder.common.exception.NGrinderRuntimeException,"
			+ "com.springsource.," + "org.apache.coyote.," + "org.apache.tomact.,"
			+ "org.ngrinder.common.util.ExceptionUtils.").split("(\\s|,)+");

	/**
	 * Get interesting packages.
	 * 
	 * @return interesting packages
	 */
	protected static String[] getUninterestingPackages() {
		return NON_NGRINDER_PACKAGE;
	}

}
