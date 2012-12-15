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
package org.ngrinder.infra.spring;

import org.ngrinder.infra.annotation.RuntimeOnlyComponent;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Convenient class to determine if the current runtime is in the spring context.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@RuntimeOnlyComponent
public class SpringContext {
	/**
	 * Determine if the current thread is from servlet context.
	 * 
	 * @return true if it's servlet context.
	 */
	public boolean isServletRequestContext() {
		return RequestContextHolder.getRequestAttributes() != null;
	}

	/**
	 * Determine if this context is on unit test.
	 * 
	 * @see MockSpringContext
	 * @return always false.
	 */
	public boolean isUnitTestContext() {
		return false;
	}
}
