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

import java.util.HashMap;
import java.util.Map;

import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.runners.model.TestClass;

/**
 * Abstract test object factory. This class is mainly responsible to keep the created object and
 * return the created one when it's created again.
 * 
 * This delegates the real object creation logic into sub class.
 * 
 * @author JunHo Yoon
 * @since 3.2
 */
abstract class TestObjectFactory {
	private Map<TestClass, Object> testObjectMap = new HashMap<TestClass, Object>();

	public TestObjectFactory() {
	}

	/**
	 * Get current test object
	 * 
	 * @return test object.
	 */
	public Object getTestObject() {
		Object testObject = testObjectMap.get(getTestClass());
		if (testObject == null) {
			try {
				ReflectiveCallable reflectiveCallable = new ReflectiveCallable() {
					@Override
					protected Object runReflectiveCall() throws Throwable {
						return createTest();
					}
				};
				testObject = reflectiveCallable.run();
				testObjectMap.put(getTestClass(), testObject);
			} catch (Throwable e) {
				return new Fail(e);
			}
		}
		return testObject;
	}

	public abstract TestClass getTestClass();

	public abstract Object createTest() throws Exception;
}
