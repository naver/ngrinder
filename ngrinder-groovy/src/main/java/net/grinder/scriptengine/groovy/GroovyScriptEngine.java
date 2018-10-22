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
package net.grinder.scriptengine.groovy;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovySystem;
import net.grinder.engine.common.EngineException;
import net.grinder.engine.common.ScriptLocation;
import net.grinder.script.Grinder;
import net.grinder.script.Statistics.StatisticsForTest;
import net.grinder.scriptengine.ScriptEngineService;
import net.grinder.scriptengine.ScriptEngineService.ScriptEngine;
import net.grinder.scriptengine.ScriptExecutionException;
import net.grinder.scriptengine.exception.AbstractExceptionProcessor;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import java.io.IOException;

import static net.grinder.util.NoOp.noOp;

/**
 * Groovy implementation of {@link ScriptEngine}.
 *
 * @author Ryan Gardner
 * @author JunHo Yoon (modified by)
 */
public class GroovyScriptEngine implements ScriptEngine {
	private AbstractExceptionProcessor exceptionProcessor = new GroovyExceptionProcessor();
	// For unit test, make it package protected.
	Class<?> m_groovyClass;
	private GrinderContextExecutor m_grinderRunner;

	/**
	 * Construct a GroovyScriptEngine that will use the supplied ScriptLocation.
	 *
	 * @param script location of the .groovy script file
	 * @throws EngineException if there is an exception loading, parsing, or constructing the test from the
	 *                         file.
	 */
	public GroovyScriptEngine(ScriptLocation script) throws EngineException {
		// Get groovy to compile the script and access the callable closure
		final ClassLoader parent = getClass().getClassLoader();
		CompilerConfiguration configuration = new CompilerConfiguration();
		configuration.setSourceEncoding("UTF-8");
		final GroovyClassLoader loader = new GroovyClassLoader(parent, configuration, true);
		try {
			m_groovyClass = loader.parseClass(script.getFile());
			m_grinderRunner = new GrinderContextExecutor(m_groovyClass);
			m_grinderRunner.runBeforeProcess();
			assert m_grinderRunner.testCount() > 0;
		} catch (IOException io) {
			throw new EngineException("Unable to parse groovy script at: " + script.getFile().getAbsolutePath(), io);
		} catch (Throwable e) {
			throw new EngineException("Error while initialize test runner", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ScriptEngineService.WorkerRunnable createWorkerRunnable() throws EngineException {
		try {
			return new GroovyWorkerRunnable(new GrinderContextExecutor(m_groovyClass));
		} catch (InitializationError e) {
			throw new EngineException("Exception occurred during initializing runner", exceptionProcessor.sanitize(e));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ScriptEngineService.WorkerRunnable createWorkerRunnable(Object testRunner) throws EngineException {
		try {
			return new GroovyWorkerRunnable(new GrinderContextExecutor(m_groovyClass, testRunner));
		} catch (InitializationError e) {
			throw new EngineException("Exception occurred during initializing runner", exceptionProcessor.sanitize(e));
		}
	}

	/**
	 * Wrapper for groovy's testRunner closure.
	 */
	public final class GroovyWorkerRunnable implements ScriptEngineService.WorkerRunnable {
		private final GrinderContextExecutor m_groovyThreadRunner;
		private RunNotifier notifier = new RunNotifier() {
			@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
			public void fireTestFailure(Failure failure) {
				if (exceptionProcessor.isGenericShutdown(failure.getException())) {
					if (failure.getException() instanceof RuntimeException) {
						throw (RuntimeException) failure.getException();
					} else {
						throw new RuntimeException("Wrapped", failure.getException());
					}
				}
				super.fireTestFailure(failure);
			}
		};

		private GroovyWorkerRunnable(GrinderContextExecutor groovyRunner) throws EngineException {
			this.m_groovyThreadRunner = groovyRunner;
			this.notifier.addListener(new RunListener() {
				@Override
				public void testFailure(Failure failure) throws Exception {
					// Skip Generic Shutdown... It's not failure.
					Throwable rootCause = exceptionProcessor.getRootCause(failure.getException());
					if (exceptionProcessor.isGenericShutdown(rootCause)) {
						return;
					}
					Grinder.grinder.getLogger().error(failure.getMessage(),
							exceptionProcessor.filterException(rootCause));
					// In case of exception, set test failed.
					try {
						StatisticsForTest forLastTest = Grinder.grinder.getStatistics().getForLastTest();
						if (forLastTest != null) {
							forLastTest.setSuccess(false);
						}
					} catch (Throwable t) {
						noOp();
					}
				}
			});
			this.m_groovyThreadRunner.runBeforeThread();
		}

		@Override
		public void run() throws ScriptExecutionException {
			try {
				this.m_groovyThreadRunner.run(notifier);
			} catch (RuntimeException e) {
				if (exceptionProcessor.isGenericShutdown(e)) {
					throw new GroovyScriptExecutionException("Shutdown",
							e.getMessage().equals("Wrapped") ? e.getCause() : e);
				}
			}
		}

		@Override
		public void shutdown() throws ScriptExecutionException {
			notifier.pleaseStop();
			this.m_groovyThreadRunner.runAfterThread();
		}
	}

	/**
	 * Shut down the engine.
	 *
	 * @throws net.grinder.engine.common.EngineException
	 *          If the engine could not be shut down.
	 */
	@Override
	public void shutdown() throws EngineException {
		m_grinderRunner.runAfterProcess();
	}

	/**
	 * Returns a description of the script engine for the log.
	 *
	 * @return The description.
	 */
	@Override
	public String getDescription() {
		return String.format("GroovyScriptEngine running with groovy version: %s", GroovySystem.getVersion());
	}

	/**
	 * Exception thrown when an error occurs executing a GroovyScript.
	 */
	public static final class GroovyScriptExecutionException extends ScriptExecutionException {

		/**
		 * UUID.
		 */
		private static final long serialVersionUID = -1789749790500700831L;

		/**
		 * Construct an exception with the supplied message.
		 *
		 * @param message the message for the exception
		 */
		@SuppressWarnings("UnusedDeclaration")
		public GroovyScriptExecutionException(String message) {
			super(message);
		}

		/**
		 * Construct an exception with the supplied message and throwable.
		 *
		 * @param s the message for the exception
		 * @param t another throwable that this exception wraps
		 */
		public GroovyScriptExecutionException(String s, Throwable t) {
			super(s, t);
		}
	}
}
