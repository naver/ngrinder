package net.grinder.scriptengine.groovy;

import java.util.ArrayList;
import java.util.List;

import net.grinder.scriptengine.groovy.GroovyScriptEngine.GroovyScriptExecutionException;
import net.grinder.util.Sleeper;

import org.codehaus.groovy.runtime.StackTraceUtils;

public class GroovyExceptionUtils {

	public static GroovyScriptExecutionException filterException(Throwable e) {
		Throwable t = getRootCause(e);
		if (isGenericShutdown(t)) {
			return new GroovyScriptExecutionException("ShutDown", t);
		}
		GroovyScriptExecutionException groovyScriptExecutionException = new GroovyScriptExecutionException(
						t.getMessage());
		groovyScriptExecutionException.setStackTrace(sanitize(t).getStackTrace());
		return groovyScriptExecutionException;
	}

	public static Throwable filterException1(Throwable e) {
		Throwable t = getRootCause(e);
		t.setStackTrace(sanitize(t).getStackTrace());
		return t;
	}

	public static Throwable getRootCause(Throwable throwable) {
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

	public static boolean isGenericShutdown(Throwable cause) {
		return (cause.getClass().getName().equals("net.grinder.engine.process.ShutdownException"))
						|| (cause instanceof Sleeper.ShutdownException);
	}

	public static Throwable sanitize(Throwable t) {
		t = StackTraceUtils.sanitize(t);
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
		return t;
	}

	private static final String[] NGRINDER_GROOVY_PACKAGE = System.getProperty(
					"groovy.sanitized.stacktraces",
					"groovy.," + "org.codehaus.groovy.," + "java.," + "javax.," + "sun.," + "gjdk.groovy.,"
									+ "org.junit.,").split("(\\s|,)+");

	public static boolean isApplicationClass(String className) {
		for (String groovyPackage : NGRINDER_GROOVY_PACKAGE) {
			if (className.startsWith(groovyPackage)) {
				return false;
			}
		}
		return true;
	}

}
