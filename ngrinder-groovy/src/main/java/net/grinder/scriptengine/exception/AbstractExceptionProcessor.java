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
package net.grinder.scriptengine.exception;

import net.grinder.util.Sleeper;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception filtering processor.
 * 
 * @author JunHo Yoon
 * @since 3.2
 */
public abstract class AbstractExceptionProcessor {

	/**
	 * Filter exception.
	 * 
	 * @param throwable	throwable
	 * @return filtered {@link Throwable}
	 */
	public Throwable filterException(Throwable throwable) {
		return sanitize(getRootCause(throwable));
	}

	/**
	 * Get the root cause of the given {@link Throwable} instance.
	 * 
	 * It stops finding the root cause until it meets the null root cause or
	 * net.grinder.engine.process.ShutdownException.
	 * 
	 * @param throwable	throwable
	 * @return root cause of the given {@link Throwable} instance.
	 */
	public Throwable getRootCause(Throwable throwable) {
		Throwable t = throwable;
		Throwable cause = t.getCause();
		while (cause != null) {
			if (isGenericShutdown(cause)) {
				return cause;
			}
			t = cause;
			cause = t.getCause();
		}
		return t;
	}

	/**
	 * Return true if the given {@link Throwable} is by the generic grinder
	 * shutdown event.
	 * 
	 * @param cause	cause
	 * @return true if generic shutdown
	 */
	public boolean isGenericShutdown(Throwable cause) {
		while (cause != null) {
			if ((cause.getClass().getName().equals("net.grinder.engine.process.ShutdownException"))
					|| (cause instanceof Sleeper.ShutdownException)) {
				return true;
			}
			cause = cause.getCause();
		}
		return false;
	}

	/**
	 * Filter the stack trace elements with only interesting one.
	 * 
	 * @param throwable throwable
	 * @return {@link Throwable} instance with interested stacktrace elements.
	 */
	public Throwable sanitize(Throwable throwable) {
		Throwable t = throwable;
		while (t != null) {
			// Note that this getBoolean access may well be synced...
			StackTraceElement[] trace = t.getStackTrace();
			List<StackTraceElement> newTrace = new ArrayList<StackTraceElement>();
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
		return throwable;
	}

	/**
	 * Check if the given class name is the application class or not.
	 * 
	 * @param className	class name including package name
	 * @return true if application class
	 */
	public boolean isApplicationClass(String className) {
		for (String groovyPackage : getUninterestingPackages()) {
			if (className.startsWith(groovyPackage)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Get interesting packages.
	 * 
	 * @return interesting packages
	 */
	@SuppressWarnings("UnusedDeclaration")
	protected abstract String[] getInterestingPackages();

	/**
	 * Get interesting packages.
	 * 
	 * @return interesting packages
	 */
	protected abstract String[] getUninterestingPackages();

}
