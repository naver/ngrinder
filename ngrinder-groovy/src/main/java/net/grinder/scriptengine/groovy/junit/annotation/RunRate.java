package net.grinder.scriptengine.groovy.junit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * In nGrinder JUnit test, this annotation marks the how frequently the test method will be
 * executed.
 * 
 * This controls the frequency of each test in the Grinder.
 * 
 * If a user want to run multiple test methods which have different run rate, previously a user
 * assigns each to different thread to control the frequency in the jython context. It's same as the
 * groovy context, However In groovy in the IDE's JUnit runner, it uses only one thread to run whole
 * tests. So it's impossible to simulate run frequency by the thread technique in the JUnit Runner
 * in IDE. Instead, with {@link RunRate} annotation, a user can make each test method decide to run
 * depending on the current run count.
 * 
 * With {@link Repeat} annotation, a user can assign how many times it executes based on the
 * repetition value. GrinderRunner decides which methods are subject to run by analyzing
 * {@link RunRate}'s value. For example, &#064;RunRate(100) means that it will run 100% of total
 * runs. &#064;RunRate(2) means that it will run 2% of total runs.
 * 
 * 
 * In case of there are only one test method, {@link RunRate} will be ignored because it's not
 * meaningful. In addition, in the nGrinder script validation page, {@link RunRate} is also ignored.
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
