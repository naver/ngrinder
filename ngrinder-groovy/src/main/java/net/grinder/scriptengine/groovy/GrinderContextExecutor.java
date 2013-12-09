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

import static net.grinder.util.NoOp.noOp;

import net.grinder.engine.process.NullStatement;
import net.grinder.script.Grinder;
import net.grinder.scriptengine.groovy.GroovyScriptEngine.GroovyScriptExecutionException;
import net.grinder.scriptengine.groovy.junit.GrinderRunner;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * JUnit styled script runner in the Grinder context.
 *
 * This class is to let the its user call the pre/post methods by himself.
 *
 * This runner is *NOT* intended to run in the JUnit.
 *
 * @author JunHo Yoon
 * @since 3.2
 */
public class GrinderContextExecutor extends GrinderRunner {
	/**
	 * Constructor.
	 *
	 * @param klass class to be tested.
	 * @throws InitializationError initialization exception
	 */
	public GrinderContextExecutor(Class<?> klass) throws InitializationError {
		super(klass);
	}

	/**
	 * Constructor.
	 *
	 * @param klass  class to be tested.
	 * @param runner runner object
	 * @throws InitializationError initialization exception
	 */
	public GrinderContextExecutor(Class<?> klass, Object runner) throws InitializationError {
		super(klass, runner);
	}

	/**
	 * Do nothing on grinder context initialization.
	 */
	@Override
	protected void initializeGrinderContext() {
		noOp();
	}

	/**
	 * Do nothing.
	 *
	 * @param notifier notifier
	 */
	@Override
	protected void registerRunNotifierListener(RunNotifier notifier) {
		noOp();
	}

	/**
	 * Run {@link net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess} annotated methods.
	 *
	 * @throws GroovyScriptExecutionException script exception
	 */
	public void runBeforeProcess() throws GroovyScriptExecutionException {
		try {
			Statement withBeforeProcess = withBeforeProcess(NullStatement.getInstance());
			withBeforeProcess.evaluate();
		} catch (Throwable t) {
			throw new GroovyScriptExecutionException("Exception occurs in @BeforeProcess block.", t);
		}
	}

	/**
	 * Run {@link net.grinder.scriptengine.groovy.junit.annotation.AfterProcess} annotated methods.
	 *
	 * @throws GroovyScriptExecutionException script exception
	 */
	public void runAfterProcess() throws GroovyScriptExecutionException {
		try {
			Statement withAfterProcess = withAfterProcess(NullStatement.getInstance());
			withAfterProcess.evaluate();
		} catch (Throwable t) {
			throw new GroovyScriptExecutionException("Exception occurs in @AfterProcess block.", t);
		}
	}

	/**
	 * Run {@link net.grinder.scriptengine.groovy.junit.annotation.BeforeThread} annotated methods.
	 *
	 * @throws GroovyScriptExecutionException script exception.
	 */
	public void runBeforeThread() throws GroovyScriptExecutionException {
		try {
			Statement withBeforeThread = withBeforeThread(NullStatement.getInstance());
			withBeforeThread.evaluate();
		} catch (Throwable t) {
			throw new GroovyScriptExecutionException("Exception occurs in @BeforeThread block.", t);
		}
	}

	/**
	 * Run {@link net.grinder.scriptengine.groovy.junit.annotation.AfterThread} annotated methods.
	 *
	 * @throws GroovyScriptExecutionException script error.
	 */
	public void runAfterThread() throws GroovyScriptExecutionException {
		try {
			Statement withAfterThread = withAfterThread(NullStatement.getInstance());
			withAfterThread.evaluate();
		} catch (Throwable t) {
			throw new GroovyScriptExecutionException("Exception occurs in @AfterThread block.", t);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.grinder.scriptengine.groovy.junit.GrinderRunner#classBlock(org.junit.runner.notification
	 * .RunNotifier)
	 */
	@Override
	protected Statement classBlock(RunNotifier notifier) {
		return childrenInvoker(notifier);
	}

	protected boolean isRateRunnerEnabled() {
		Description description = getDescription();
		return description.testCount() > 1 && !isScriptValidation();
	}

	private boolean isScriptValidation() {
		try {
			return Grinder.grinder.getProperties().getBoolean("grinder.script.validation", false);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * No support for repetition in grinder context.
	 *
	 * @param statement statement.
	 * @return pass the given statement without modification
	 */
	@Override
	protected Statement withRepeat(Statement statement) {
		return statement;
	}

}
