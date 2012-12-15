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
package org.ngrinder.sm;

/**
 * Used in test cases, enable the network check only in SecurityManagerTest class, and disable the
 * check in other classes.
 * 
 * @author Mavlarn
 * @since 3.0
 */
public class MockNGrinderSecurityManager extends NGrinderSecurityManager {

	@Override
	public void checkConnect(String host, int port) {
		try {
			super.checkConnect(host, port);
		} catch (SecurityException e) {
			if (SecurityManagerTest.SM_TEST) {
				throw e;
			}
		}
	}

	@Override
	public void checkConnect(String host, int port, Object context) {
		try {
			super.checkConnect(host, port, context);
		} catch (SecurityException e) {
			if (SecurityManagerTest.SM_TEST) {
				throw e;
			}
		}
	}

}
