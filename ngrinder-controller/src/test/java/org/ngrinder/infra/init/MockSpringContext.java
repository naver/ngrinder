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
package org.ngrinder.infra.init;

import org.ngrinder.infra.spring.SpringContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Convenient class to determine spring context. It's mocked version which will be used in unit test
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@Profile("unit-test")
@Component
public class MockSpringContext extends SpringContext {
	/**
	 * Determine if this context is on unit test.
	 * 
	 * @see SpringContext
	 * @return always true.
	 */
	@Override
	public boolean isUnitTestContext() {
		return true;
	}
}
