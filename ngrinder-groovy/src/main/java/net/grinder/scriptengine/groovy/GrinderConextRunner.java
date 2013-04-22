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
import net.grinder.scriptengine.groovy.GroovyScriptEngine.GroovyScriptExecutionException;
import net.grinder.scriptengine.groovy.junit.GrinderRunner;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * Grinder JUnit Runner.
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public class GrinderConextRunner extends GrinderRunner {
	/**
	 * Constructor
	 * 
	 * @param klass
	 *            klass
	 * @throws InitializationError
	 */
	public GrinderConextRunner(Class<?> klass) throws InitializationError {
		super(klass);
	}

	@Override
	protected void initialize() {
		noOp();
	}

	@Override
	protected void registerRunNotifierListener(RunNotifier notifier) {
		noOp();
	}

	public void runBeforeProcess() throws GroovyScriptExecutionException {
		try {
			Statement withBeforeProcess = withBeforeProcess(NullStatement.getInstance());
			withBeforeProcess.evaluate();
		} catch (Throwable t) {
			throw new GroovyScriptExecutionException("Exception occurs in @BeforeProcess block.", t);
		}
	}

	public void runAfterProcess() throws GroovyScriptExecutionException {
		try {
			Statement withAfterProcess = withAfterProcess(NullStatement.getInstance());
			withAfterProcess.evaluate();
		} catch (Throwable t) {
			throw new GroovyScriptExecutionException("Exception occurs in @AfterProcess block.", t);
		}
	}

	public void runBeforeThread() throws GroovyScriptExecutionException {
		try {
			Statement withBeforeThread = withBeforeThread(NullStatement.getInstance());
			withBeforeThread.evaluate();
		} catch (Throwable t) {
			throw new GroovyScriptExecutionException("Exception occurs in @BeforeThread block.", t);
		}
	}

	public void runAfterThread() throws GroovyScriptExecutionException {
		try {
			Statement withAfterThread = withAfterThread(NullStatement.getInstance());
			withAfterThread.evaluate();
		} catch (Throwable t) {
			throw new GroovyScriptExecutionException("Exception occurs in @AfterThread block.", t);
		}
	}

	@Override
	protected Statement classBlock(RunNotifier notifier) {
		Statement statement = childrenInvoker(notifier);
		return statement;
	}

	/**
	 * No support for repetition in grinder context.
	 */
	@Override
	protected Statement withRepeat(Statement statement) {
		return statement;
	}

}
