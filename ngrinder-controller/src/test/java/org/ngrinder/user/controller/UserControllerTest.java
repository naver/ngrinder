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
package org.ngrinder.user.controller;

import static java.time.Instant.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.ngrinder.common.constant.WebConstants.JSON_SUCCESS;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public class UserControllerTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private UserApiController userApiController;

	/**
	 * Test method for
	 * {@link org.ngrinder.user.controller.UserApiController#getAll(org.ngrinder.model.Role,
	 * org.springframework.data.domain.Pageable, java.lang.String)}
	 * .
	 */
	@Test
	public void testGetAll() {
		Pageable page = PageRequest.of(1, 10);

		userApiController.getAll(null, page, null);
		userApiController.getAll(Role.ADMIN, page, null);
		userApiController.getAll(null, page, "user");
	}

	/**
	 * Test method for
	 * {@link org.ngrinder.user.controller.UserApiController#getOne(org.ngrinder.model.User)}
	 * .
	 */
	@Test
	public void testGetOne() {
		User user = userApiController.getOne(getTestUser().getUserId());
		assertThat(user.getId(), is(getTestUser().getId()));
	}

	/**
	 * Test method for
	 * {@link org.ngrinder.user.controller.UserApiController#save(org.ngrinder.model.User,
	 * org.ngrinder.model.User)}
	 * .
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testSave() {
		// test update
		User currUser = getTestUser();
		currUser.setUserName("new name");
		currUser.setOwners(null);
		userApiController.save(currUser, currUser);
		User user = userApiController.getOne(currUser.getUserId());
		assertThat(user.getUserName(), is("new name"));
		assertThat(user.getPassword(), is(currUser.getPassword()));

		User admin = getAdminUser();
		User temp = new User("temp1", "temp1", "temp1", "temp@nhn.com", Role.USER);
		userApiController.save(admin, temp);
		temp = new User("temp2", "temp2", "temp2", "temp@nhn.com", Role.USER);
		userApiController.save(admin, temp);

		currUser.setFollowersStr("temp1, temp2");
		userApiController.save(currUser, currUser);
		user = userApiController.getOne(currUser.getUserId());
		assertThat(user.getFollowers().size(), is(2));
		assertThat(user.getFollowers().get(0).getUserId(), is("temp1"));
	}

	@Test
	public void testUpdate() {
		// test update the role of current user.
		User currUser = getTestUser();
		assertThat(currUser.getRole(), is(Role.USER)); // current test user is "USER"

		User updatedUser = new User(currUser.getUserId(), currUser.getUserName(), currUser.getPassword(),
				"temp@nhn.com", currUser.getRole());
		updatedUser.setId(currUser.getId());
		updatedUser.setEmail("test@test.com");
		updatedUser.setRole(Role.ADMIN); // Attempt to modify himself as ADMIN
		userApiController.save(currUser, updatedUser);

		User user = userApiController.getOne(currUser.getUserId());
		assertThat(user.getUserName(), is(currUser.getUserName()));
		assertThat(user.getPassword(), is(currUser.getPassword()));
		assertThat(user.getRole(), is(Role.USER));
	}

	private void saveTestUser(String userId, String userName) {
		User newUser = new User();
		newUser.setUserId(userId);
		newUser.setUserName(userName);
		newUser.setEmail("junoyoon@gmail.com");
		newUser.setCreatedBy(getTestUser());
		newUser.setCreatedAt(now());
		newUser.setRole(Role.USER);
		userApiController.save(getAdminUser(), newUser);
	}

	/**
	 * Test method for
	 * {@link org.ngrinder.user.controller.UserApiController#delete(org.ngrinder.model.User user, java.lang.String)}
	 * .
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testDelete() {
		// save new user for test
		saveTestUser("NewUserId1", "NewUserName1");
		saveTestUser("NewUserId2", "NewUserName2");
		saveTestUser("NewUserId3", "NewUserName3");

		Pageable page = PageRequest.of(0, 10);

		// search
		Page<User> userList = userApiController.getAll(null, page, "NewUserName");
		assertThat(userList.getContent().size(), is(3));

		// test to delete one
		userApiController.delete(testUser, "NewUserId1");
		userList = userApiController.getAll(Role.USER, page, "NewUserName");
		assertThat(userList.getContent().size(), is(2));

		// test to delete more
		userApiController.deleteUsers(testUser, "NewUserId2,NewUserId3");
		userList = userApiController.getAll(Role.USER, page, "NewUserName");
		assertThat(userList.getContent().size(), is(0));
	}

	/**
	 * Test method for
	 * {@link UserApiController#checkDuplication(String)}
	 * .
	 */
	@Test
	public void testDuplication() {
		Map<String, Object> returnJson = userApiController.checkDuplication("not-exist");
		assertTrue((Boolean) returnJson.get(JSON_SUCCESS));

		returnJson = userApiController.checkDuplication(getTestUser().getUserId());
		assertFalse((Boolean) returnJson.get(JSON_SUCCESS));
	}

	@Test
	public void testProfile() {
		User user = (User) userApiController.getOne(getTestUser()).get("user");
		assertThat(user.getUserName(), is("TEST_USER"));
	}

	@Test
	public void testSwitchOptions() {
		User currUser = getTestUser();
		User temp = new User("temp1", "temp1", "temp1", "temp@nhn.com", Role.USER);
		User admin = getAdminUser();
		userApiController.save(admin, temp);
		currUser.setOwners(Lists.newArrayList(temp));
		currUser.setOwnerUser(temp);
		userApiController.save(currUser, currUser);
		List<User> shareUsers = userApiController.switchOptions(currUser, "");
		assertTrue(shareUsers.contains(temp));
	}
}
