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
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * In nGrinder JUnit test, this annotation marks the how many time the {@link org.junit.Test} marked methods
 * will be executed.
 * 
 * This simulates the execution count in the Grinder. This annotation is marked at the class level
 * not method level. The whole methods are executed during the designated times.
 * 
 * <pre>
 * // two methods (testSize() and testRemove()) will be executed 100 times.  
 * &#064;Repeat(100)
 * &#064;RunWith(GrinderRunner)
 * public class Example {
 * 	List empty;
 * 
 * 	// This will be executed 100 times
 * 	&#064;Test public void testSize() {
 *       ...
 *    }
 * 
 * 	// This will be executed 100 times
 * 	&#064;Test public void testRemove() {
 *       ...
 *    }
 * }
 * </pre>
 * 
 * @see net.grinder.scriptengine.groovy.junit.GrinderRunner
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface Repeat {
	/**
	 * Repetition count.
	 *
	 * @return repetition count
	 */
	int value();
}
