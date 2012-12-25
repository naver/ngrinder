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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Class description.
 * 
 * @author Mavlarn
 * @since
 */
public class UserTest {

	@Test
	public void testValidate() {
		User user1 = new User("Uid1", "name1", "pwd1", "user1@nhn.com", Role.USER);
		user1.setEmail("aa@bb.com");
		assertTrue(user1.validate());

		User user2 = new User();
		user2 = new User("Uid1", null, "pwd1", "user2@nhn.com", Role.USER);
		assertTrue(!user2.validate());

		user2 = new User("Uid1", "name", "pwd1", "user2@nhn.com", null);
		assertTrue(user2.validate());
		assertTrue(user2.getRole() == null);

		@SuppressWarnings("deprecation")
		User user3 = new User("Uid1", "name", "pwd1", null);
		assertTrue(!user3.validate());

	}

	@Test
	public void testEqualsObject() {
		User user1 = new User("Uid1", "name1", "pwd1", "user1@nhn.com", Role.USER);
		User user2 = new User("Uid1", "name2", "pwd2", "user2@nhn.com", Role.USER);
		assertThat(user1, is(user2));
		assertThat(user1.hashCode(), is(user2.hashCode()));

		user2.setUserId("Uid2");
		assertThat(user1, not(user2));
		assertThat(user1.hashCode(), not(user2.hashCode()));

		user2.setUserId(null);
		assertThat(user1, not(user2));
		assertThat(user1.hashCode(), not(user2.hashCode()));

		assertTrue(!user1.equals(null));
	}

}
