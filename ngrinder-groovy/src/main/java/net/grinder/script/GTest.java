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
package net.grinder.script;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;

/**
 * {@link Test} extension to be distinguishable from {@link org.junit.Test} in JUnit4 tests.
 *
 * <pre>
 * request = new HTTPRequest();
 * GTest(1, &quot;Test&quot;).record(request);
 * </pre>
 *
 * @author JunHo Yoon
 * @since 3.2
 */
public class GTest extends Test {

	/**
	 * UID.
	 */
	private static final long serialVersionUID = 8370116882992463352L;

	private String context;
	/**
	 * Constructor.
	 *
	 * @param number      the test number
	 * @param description test description
	 */
	public GTest(int number, String description) {
		super(number, description);
		context = System.getProperty("ngrinder.context");
	}

	/**
	 * Instrument the supplied {@code target} object's method which has the given name. Subsequent
	 * calls to {@code target}'s given method will be recorded against the statistics for this
	 * {@code Test}.
	 *
	 * @param target     Object to instrument.
	 * @param methodName method name to instrument
	 * @throws NonInstrumentableTypeException If {@code target} could not be instrumented.
	 * @since 3.2.1
	 */
	public final void record(Object target, String methodName) throws NonInstrumentableTypeException {
		if (StringUtils.isNotEmpty(context)) {
			record(target, new MethodNameFilter(methodName));
		}
	}

	/**
	 * Method name filter.
	 *
	 * @author junoyoon
	 * @since 3.2.1
	 */
	static class MethodNameFilter implements InstrumentationFilter {
		private String methodName;

		/**
		 * Constructor.
		 *
		 * @param methodName method name
		 */
		public MethodNameFilter(String methodName) {
			this.methodName = methodName;
		}

		@Override
		public boolean matches(Object item) {
			return item instanceof Method && ((Method) item).getName().equals(methodName);
		}
	}
}
