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

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.grinder.engine.process.JUnitThreadContextInitializer;
import net.grinder.engine.process.JUnitThreadContextUpdater;
import net.grinder.scriptengine.exception.AbstractExceptionProcessor;
import net.grinder.scriptengine.groovy.GroovyExceptionProcessor;
import net.grinder.scriptengine.groovy.junit.annotation.AfterProcess;
import net.grinder.scriptengine.groovy.junit.annotation.AfterThread;
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess;
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread;
import net.grinder.scriptengine.groovy.junit.annotation.Repeat;
import net.grinder.scriptengine.groovy.junit.annotation.RunRate;

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
import org.junit.runner.Runner;
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
 * Grinder JUnit Runner. Grinder JUnit Runner is the custom {@link Runner} which lets the user can
 * run the Grinder script in the JUnit context.
 *
 * This runner has a little bit different characteristic from conventional JUnit test.
 * <ul>
 * <li>All Test annotated tests are executed with a single instance.</li>
 * <li>{@link BeforeProcess} and {@link AfterProcess} annotated methods are executed per each
 * process.</li>
 * <li>{@link BeforeThread} and {@link AfterThread} annotated methods are executed per each thread.</li>
 * <li>{@link Repeat} annotated
 * </ul>
 *
 * In addition, it contains a little different behavior from generic grinder test script.
 * <ul>
 * <li>It only initiates only 1 process and 1 thread.</li>
 * <li>Each <code>&#064;test</code> annotated method are independent to run. So one failure from one
 * method doesn't block the other methods' runs</li>
 * </ul>
 *
 * @author JunHo Yoon
 * @author Mavlarn
 * @see BeforeProcess
 * @see BeforeThread
 * @see AfterThread
 * @see AfterProcess
 * @see Repeat
 * @since 1.0
 */
public class GrinderRunner extends BlockJUnit4ClassRunner {
	private JUnitThreadContextInitializer threadContextInitializer;
	private JUnitThreadContextUpdater threadContextUpdater;
	private TestObjectFactory testTargetFactory;
	private PerThreadStatement finalPerThreadStatement;
	private AbstractExceptionProcessor exceptionProcessor = new GroovyExceptionProcessor();
	private boolean enableRateRunner = true;
	private Map<FrameworkMethod, Statement> frameworkMethodCache = new HashMap<FrameworkMethod, Statement>();

	/**
	 * Constructor.
	 *
	 * @param klass klass
	 * @throws InitializationError class initialization error.
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

		initializeGrinderContext();
	}

	/**
	 * Constructor.
	 *
	 * @param klass  klass
	 * @param runner runner class
	 * @throws InitializationError class initialization error.
	 */
	public GrinderRunner(Class<?> klass, final Object runner) throws InitializationError {
		super(klass);
		this.testTargetFactory = new TestObjectFactory() {
			@Override
			public TestClass getTestClass() {
				return GrinderRunner.this.getTestClass();
			}

			@Override
			public Object createTest() throws Exception {
				return runner;
			}
		};

		initializeGrinderContext();
	}


	protected void initializeGrinderContext() {
		this.threadContextInitializer = new JUnitThreadContextInitializer();
		this.threadContextInitializer.initialize();
		this.threadContextUpdater = threadContextInitializer.getThreadContextUpdater();
		this.finalPerThreadStatement = new PerThreadStatement() {
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
		return super.getChildren();
	}

	@Override
	public void run(RunNotifier notifier) {
		registerRunNotifierListener(notifier);
		Description description = getDescription();
		enableRateRunner = isRateRunnerEnabled();
		EachTestNotifier testNotifier = new EachTestNotifier(notifier, description);
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

	/**
	 * Check if the rate runner should be enabled.
	 *
	 * @return true if enabled;
	 */
	protected boolean isRateRunnerEnabled() {
		Description description = getDescription();
		return description.testCount() > 1 && isRepeatRunnerEnabled();
	}

	private boolean isRepeatRunnerEnabled() {
		Annotation[] annotations = getTestClass().getAnnotations();
		boolean repeatAnnotation = false;
		for (Annotation each : annotations) {
			if (each.annotationType().equals(Repeat.class)) {
				repeatAnnotation = true;
			}
		}
		return repeatAnnotation;
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
		int repetition = 1;
		for (Annotation each : annotations) {
			if (each.annotationType().equals(Repeat.class)) {
				repetition = ((Repeat) each).value();
			}
		}
		return new RepetitionStatement(statement, repetition, threadContextUpdater);
	}

	@SuppressWarnings("deprecation")
	protected Statement methodBlock(FrameworkMethod method) {
		Statement statement = frameworkMethodCache.get(method);
		if (statement != null) {
			return statement;
		}
		Object testObject = testTargetFactory.getTestObject();
		statement = methodInvoker(method, testObject);
		statement = possiblyExpectingExceptions(method, testObject, statement);
		statement = withPotentialTimeout(method, testObject, statement);
		statement = withBefores(method, testObject, statement);
		statement = withAfters(method, testObject, statement);
		statement = withRules(method, testObject, statement);
		if (enableRateRunner) {
			statement = withRunRate(method, testObject, statement);
		}
		frameworkMethodCache.put(method, statement);
		return statement;
	}

	protected Statement withRunRate(FrameworkMethod method, @SuppressWarnings("UnusedParameters") Object target, Statement statement) {
		RunRate runRate = method.getAnnotation(RunRate.class);
		return runRate == null ? statement : new RunRateStatement(statement, runRate.value());
	}

	private Statement withRules(FrameworkMethod method, Object target, Statement statement) {
		Statement result = statement;
		for (MethodRule each : getTestClass().getAnnotatedFieldValues(target, Rule.class, MethodRule.class)) {
			result = each.apply(result, method, target);
		}
		return result;
	}

	/**
	 * Returns a {@link Statement}: run all non-overridden {@code @BeforeClass} methods on this
	 * class and superclasses before executing {@code statement}; if any throws an Exception, stop
	 * execution and pass the exception on.
	 *
	 * @param statement statement
	 * @return wrapped statement
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
	 *
	 * @param statement statement
	 * @return wrapped statement
	 */
	protected Statement withAfterProcess(Statement statement) {
		TestClass testClass = getTestClass();
		List<FrameworkMethod> afters = testClass.getAnnotatedMethods(AfterProcess.class);
		afters.addAll(testClass.getAnnotatedMethods(AfterClass.class));
		return afters.isEmpty() ? statement : new RunAfters(statement, afters, null);
	}

	protected Statement withAfterThread(Statement statement) {
		List<FrameworkMethod> afterThreads = getTestClass().getAnnotatedMethods(AfterThread.class);
		return new RunAfterThreads(statement, afterThreads, testTargetFactory, finalPerThreadStatement);
	}

	protected Statement withBeforeThread(Statement statement) {
		List<FrameworkMethod> beforeThreads = getTestClass().getAnnotatedMethods(BeforeThread.class);
		return new RunBeforeThreads(statement, beforeThreads, testTargetFactory, finalPerThreadStatement);
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
				Throwable filtered = exceptionProcessor.filterException(exception);
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
