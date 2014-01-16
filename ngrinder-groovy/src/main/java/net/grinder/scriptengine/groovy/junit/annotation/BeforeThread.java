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
package net.grinder.scriptengine.groovy.junit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * In nGrinder JUnit test, this annotation marks the method which should be executed just after each
 * thread initialization.
 *
 * Here is a simple example:
 *
 * <pre>
 * public class TestRunner {
 * 	List empty;
 *
 * 	&#064;BeforeThread
 * 	public void beforeThread() {
 * 		empty = new ArrayList();
 *    }
 *
 * 	&#064;Test public void test1() {
 *       ...
 *    }
 *
 * 	&#064;Test public void test2() {
 *       ...
 *    }
 * }
 * </pre>
 *
 * @see AfterThread
 * @see BeforeProcess
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BeforeThread {
	/**
	 * Ramp up interval in millisecond before executing each thread. This enables the thread ramp up.
	 * Each thread will sleep (rampUp  * threadNumber) before test methods are executed.
	 *
	 * If 1 agent, 2 processes per an agent and 2 threads per a process and
	 * 1000 is provided here and 4 threads(vusers) will be activated every seconds.
	 *
	 * This will be only applied when the test is executed in nGrinder not each IDE's JUnit runner.
	 *
	 * @return ramp up interval in milliseconds.
	 */
	int interval() default 0;

	/**
	 * Ramp up step. If step is assigned as 2, the 2 threads will be invoked every designated interval.
	 *
	 * @return Ramp up tep
	 */
	int step() default 1;

	/**
	 * initial threads which should be invoked from beginning
	 *
	 * @return initial thread count
	 */
	int initialThread() default 0;

	/**
	 * initial sleep before starting ramp up
	 *
	 * @return initial sleep time in millisecond
	 */
	int initialSleep() default 0;

}
