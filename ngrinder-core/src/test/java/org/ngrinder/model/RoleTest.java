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
package org.ngrinder.model;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * Class description.
 *
 * @author Mavlarn
 * @since
 */
public class RoleTest {
	
	@Test
	public void testHasPermission() {
		Role user = Role.USER;
		assertFalse(user.hasPermission(Permission.GET_ALL_TESTS));
		assertFalse(user.hasPermission(Permission.DELETE_TEST_OF_OTHER));
		assertFalse(user.hasPermission(Permission.CHECK_SCRIPT_OF_OTHER));
		assertFalse(user.hasPermission(Permission.VALIDATE_SCRIPT_OF_OTHER));
		assertFalse(user.hasPermission(Permission.STOP_TEST_OF_OTHER));
		assertFalse(user.hasPermission(Permission.SWITCH_TO_ANYONE));

		Role admin = Role.ADMIN;
		assertTrue(admin.hasPermission(Permission.GET_ALL_TESTS));
		assertTrue(admin.hasPermission(Permission.DELETE_TEST_OF_OTHER));
		assertTrue(admin.hasPermission(Permission.CHECK_SCRIPT_OF_OTHER));
		assertTrue(admin.hasPermission(Permission.VALIDATE_SCRIPT_OF_OTHER));
		assertTrue(admin.hasPermission(Permission.STOP_TEST_OF_OTHER));
		assertTrue(admin.hasPermission(Permission.SWITCH_TO_ANYONE));

		Role superUser = Role.SUPER_USER;
		assertTrue(superUser.hasPermission(Permission.GET_ALL_TESTS));
		assertFalse(superUser.hasPermission(Permission.DELETE_TEST_OF_OTHER));
		assertTrue(superUser.hasPermission(Permission.CHECK_SCRIPT_OF_OTHER));
		assertTrue(superUser.hasPermission(Permission.VALIDATE_SCRIPT_OF_OTHER));
		assertFalse(superUser.hasPermission(Permission.STOP_TEST_OF_OTHER));
		assertTrue(superUser.hasPermission(Permission.SWITCH_TO_ANYONE));
	}

}
