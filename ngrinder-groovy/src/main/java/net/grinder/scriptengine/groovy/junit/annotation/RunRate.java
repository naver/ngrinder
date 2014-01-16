package net.grinder.scriptengine.groovy.junit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * In nGrinder JUnit test, this annotation marks how frequently the designated test method will be
 * executed.
 *
 * This controls the frequency of each test in the Grinder.
 *
 * A user can assign how many times it executes in the repeated execution.
 * GrinderRunner decides which methods are subject to run by the value of each {@link RunRate} annotated method.
 * For example, &#064;RunRate(100) means that it will run 100% of total
 * runs. &#064;RunRate(2) means that it will run 2% of total runs.
 *
 * When the user run this in the JUnit context not Grinder text, {@link Repeat} annotation,
 * should be used to simulate the repeated execution.
 *
 * <pre>
 *
 * &#064;Repeat(100)
 * &#064;RunWith(GrinderRunner)
 * public class Example {
 * 	List empty;
 *
 * 	// This will be executed 100 times
 * 	&#064;RunRate(100)
 * 	&#064;Test public void testSize() {
 *       ...
 *    }
 *
 * 	// This will be executed 2 times in 50th and 100th run of the total repetition.
 * 	&#064;RunRate(2)
 * 	&#064;Test public void testRemove() {
 *       ...
 *    }
 * }
 * </pre>
 *
 * In case of there are only one test method, {@link RunRate} will be ignored because it's not
 * meaningful. In addition, in nGrinder script validation page, {@link RunRate} is also ignored.
 *
 * @see net.grinder.scriptengine.groovy.junit.GrinderRunner
 * @see Repeat
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RunRate {
	/**
	 * Run rate from 0 to 100.
	 *
	 * @return run rate
	 */
	int value();
}
