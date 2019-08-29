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
package org.ngrinder.security;

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.ShaPasswordEncoder;

import static org.junit.Assert.assertTrue;
import static org.ngrinder.user.service.MockUserContext.TEST_USER_ID;
import static org.ngrinder.user.service.MockUserContext.TEST_USER_PASSWORD;

public class DefaultLoginPluginTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private ShaPasswordEncoder passwordEncoder;

	@Test
	public void testValidateUser() {
		DefaultLoginPlugin plugin = new DefaultLoginPlugin(null);
		String salt = TEST_USER_ID;

		try {
			plugin.validateUser(TEST_USER_ID, TEST_USER_PASSWORD, TEST_USER_PASSWORD, passwordEncoder, salt);
		} catch (BadCredentialsException e) {
			assertTrue(true);
		}
		plugin.validateUser(TEST_USER_ID, TEST_USER_PASSWORD, passwordEncoder.encode(salt, TEST_USER_PASSWORD), passwordEncoder, salt);
		assertTrue(true);
	}
}
