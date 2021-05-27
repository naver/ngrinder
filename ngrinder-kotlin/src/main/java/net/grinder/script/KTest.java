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

public class KTest extends Test {

	private static final long serialVersionUID = 837011688299241362L;

	private final String context;

	public KTest(int number, String description) {
		super(number, description);
		context = System.getProperty("ngrinder.context");
	}

	public final void record(Object target, String methodName) throws NonInstrumentableTypeException {
		if (StringUtils.isNotEmpty(context)) {
			record(target, new MethodNameFilter(methodName));
		}
	}

	static class MethodNameFilter implements InstrumentationFilter {
		private final String methodName;

		public MethodNameFilter(String methodName) {
			this.methodName = methodName;
		}

		@Override
		public boolean matches(Object item) {
			return item instanceof Method && ((Method) item).getName().equals(methodName);
		}
	}
}
