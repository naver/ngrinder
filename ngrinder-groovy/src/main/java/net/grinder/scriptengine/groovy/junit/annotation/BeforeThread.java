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
}
