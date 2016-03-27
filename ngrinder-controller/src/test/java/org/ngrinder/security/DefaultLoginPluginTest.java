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

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

/**
 * Class description.
 * 
 * @author Mavlarn
 * @since
 */
public class DefaultLoginPluginTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private ShaPasswordEncoder passwordEncoder;

	@Test
	public void testValidateUser() {
		DefaultLoginPlugin plugin = new DefaultLoginPlugin();
		Object salt = null;

		try {
			plugin.validateUser("testUserId", "123", "123", passwordEncoder, salt);
		} catch (BadCredentialsException e) {
			assertTrue(true);
		}
		plugin.validateUser("testUserId", "123", passwordEncoder.encodePassword("123", salt), passwordEncoder, salt);
		assertTrue(true);
	}
}
