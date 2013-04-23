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
package net.grinder.scriptengine.groovy.junit;

import static net.grinder.scriptengine.groovy.GroovyExceptionUtils.filterException1;

import java.lang.annotation.Annotation;
import java.util.List;

import net.grinder.engine.process.JUnitThreadContextInitializer;
import net.grinder.scriptengine.groovy.junit.annotation.AfterProcess;
import net.grinder.scriptengine.groovy.junit.annotation.AfterThread;
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess;
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread;
import net.grinder.scriptengine.groovy.junit.annotation.RepeatInDevContext;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.internal.runners.model.MultipleFailureException;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.rules.MethodRule;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

/**
 * Grinder JUnit Runner.
 * 
 * @author JunHo Yoon
 * @since 1.0
 */
public class GrinderRunner extends BlockJUnit4ClassRunner {
	private JUnitThreadContextInitializer threadContextInitializer;
	private TestObjectFactory testTargetFactory;
	private PerThreadStatement defaultPerThreadStat;

	/**
	 * Constructor
	 * 
	 * @param klass
	 *            klass
	 * @throws InitializationError
	 */
	public GrinderRunner(Class<?> klass) throws InitializationError {
		super(klass);
		this.testTargetFactory = new TestObjectFactory() {
			@Override
			public TestClass getTestClass() {
				return GrinderRunner.this.getTestClass();
			}

			@Override
			public Object createTest() throws Exception {
				return GrinderRunner.this.createTest();
			}
		};

		initialize();
	}

	protected void initialize() {
		this.threadContextInitializer = new JUnitThreadContextInitializer();
		this.threadContextInitializer.initialize();
		this.defaultPerThreadStat = new PerThreadStatement() {
			@Override
			void before() {
				attachWorker();
			}

			@Override
			void after() {
				detachWorker();
			}
		};
	}

	@Override
	protected List<FrameworkMethod> getChildren() {
		List<FrameworkMethod> children = super.getChildren();
		return children;
	}

	@Override
	public void run(RunNotifier notifier) {
		registerRunNotifierListener(notifier);
		EachTestNotifier testNotifier = new EachTestNotifier(notifier, getDescription());
		try {
			Statement statement = classBlock(notifier);
			statement.evaluate();
		} catch (AssumptionViolatedException e) {
			testNotifier.fireTestIgnored();
		} catch (StoppedByUserException e) {
			throw e;
		} catch (Throwable e) {
			testNotifier.addFailure(e);
		}

	}

	@Override
	protected Statement classBlock(RunNotifier notifier) {
		Statement statement = childrenInvoker(notifier);
		statement = withRepeat(statement);
		statement = withBeforeThread(statement);
		statement = withBeforeProcess(statement);
		statement = withAfterThread(statement);
		statement = withAfterProcess(statement);
		return statement;
	}

	protected Statement withRepeat(Statement statement) {
		Annotation[] annotations = getTestClass().getAnnotations();
		int repeatation = 1;
		for (Annotation each : annotations) {
			if (each.annotationType().equals(RepeatInDevContext.class)) {
				repeatation = ((RepeatInDevContext) each).value();
			}
		}
		return repeatation == 1 ? statement : new RepetitionStatment(statement, repeatation);
	}

	@SuppressWarnings("deprecation")
	protected Statement methodBlock(FrameworkMethod method) {
		Object testObject = testTargetFactory.getTestObject();
		Statement statement = methodInvoker(method, testObject);
		statement = possiblyExpectingExceptions(method, testObject, statement);
		statement = withPotentialTimeout(method, testObject, statement);
		statement = withBefores(method, testObject, statement);
		statement = withAfters(method, testObject, statement);
		statement = withRules(method, testObject, statement);
		return statement;
	}

	private Statement withRules(FrameworkMethod method, Object target, Statement statement) {
		Statement result = statement;
		for (MethodRule each : getTestClass().getAnnotatedFieldValues(target, Rule.class, MethodRule.class))
			result = each.apply(result, method, target);
		return result;
	}

	/**
	 * Returns a {@link Statement}: run all non-overridden {@code @BeforeClass} methods on this
	 * class and superclasses before executing {@code statement}; if any throws an Exception, stop
	 * execution and pass the exception on.
	 */
	protected Statement withBeforeProcess(Statement statement) {
		TestClass testClass = getTestClass();
		List<FrameworkMethod> befores = testClass.getAnnotatedMethods(BeforeProcess.class);
		befores.addAll(testClass.getAnnotatedMethods(BeforeClass.class));
		return befores.isEmpty() ? statement : new RunBefores(statement, befores, null);
	}

	/**
	 * Returns a {@link Statement}: run all non-overridden {@code @AfterClass} methods on this class
	 * and superclasses before executing {@code statement}; all AfterClass methods are always
	 * executed: exceptions thrown by previous steps are combined, if necessary, with exceptions
	 * from AfterClass methods into a {@link MultipleFailureException}.
	 */
	protected Statement withAfterProcess(Statement statement) {
		TestClass testClass = getTestClass();
		List<FrameworkMethod> afters = testClass.getAnnotatedMethods(AfterProcess.class);
		afters.addAll(testClass.getAnnotatedMethods(AfterClass.class));
		return afters.isEmpty() ? statement : new RunAfters(statement, afters, null);
	}

	protected Statement withAfterThread(Statement statement) {
		List<FrameworkMethod> afterThreads = getTestClass().getAnnotatedMethods(AfterThread.class);
		return new RunAfterThreads(statement, afterThreads, testTargetFactory, defaultPerThreadStat);
	}

	protected Statement withBeforeThread(Statement statement) {
		List<FrameworkMethod> beforeThreads = getTestClass().getAnnotatedMethods(BeforeThread.class);
		return new RunBeforeThreads(statement, beforeThreads, testTargetFactory, defaultPerThreadStat);
	}

	protected void registerRunNotifierListener(RunNotifier notifier) {
		notifier.addFirstListener(new RunListener() {
			@Override
			public void testStarted(Description description) throws Exception {

			}

			@Override
			public void testRunStarted(Description description) throws Exception {
				attachWorker();
			}

			@Override
			public void testRunFinished(Result result) throws Exception {
				detachWorker();
			}

			@Override
			public void testFailure(Failure failure) throws Exception {
				Throwable exception = failure.getException();
				Throwable filtered = filterException1(exception);
				if (exception != filtered) {
					exception.initCause(filtered);
				}
			}
		});
	}

	void attachWorker() {
		this.threadContextInitializer.attachWorkerThreadContext();
	}

	void detachWorker() {
		this.threadContextInitializer.detachWorkerThreadContext();
	}
}
