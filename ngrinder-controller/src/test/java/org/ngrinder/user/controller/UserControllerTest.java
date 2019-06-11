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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.ui.ModelMap;

import java.util.Date;
import java.util.List;

public class UserControllerTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private UserController userController;

	@Autowired
	private UserApiController userApiController;

	private Gson gson = new Gson();
	/**
	 * Test method for
	 * {@link org.ngrinder.user.controller.UserController#getAll(org.springframework.ui.ModelMap, org.ngrinder.model.Role,
	 * org.springframework.data.domain.Pageable, java.lang.String)}
	 * .
	 */
	@Test
	public void testGetAll() {
		Pageable page = PageRequest.of(1, 10);

		ModelMap model = new ModelMap();
		userController.getAll(null, page, null);

		model.clear();
		userController.getAll(Role.ADMIN, page, null);

		model.clear();
		userController.getAll(null, page, "user");

	}

	/**
	 * Test method for
	 * {@link org.ngrinder.user.controller.UserController#getOne(org.ngrinder.model.User,
	 * org.springframework.ui.ModelMap)}
	 * .
	 */
	@Test
	public void testGetOne() {
		ModelMap model = new ModelMap();
		userController.getOne(getTestUser().getUserId(), model);
		User user = (User) model.get("user");
		assertThat(user.getId(), is(getTestUser().getId()));
	}

	/**
	 * Test method for
	 * {@link org.ngrinder.user.controller.UserController#save(org.ngrinder.model.User,
	 * org.ngrinder.model.User)}
	 * .
	 */
	@Test
	public void testSave() {		// TODO: Resolve lazy initialize exception
		// test update
		User currUser = getTestUser();
		currUser.setUserName("new name");
		currUser.setOwners(null);
		userController.save(currUser, currUser);
		String userJson = userController.getOne(currUser.getUserId()).getBody();
		User user = gson.fromJson(userJson, User.class);
		assertThat(userJson, containsString("new name"));
		assertThat(user.getPassword(), is(currUser.getPassword()));

		User admin = getAdminUser();
		User temp = new User("temp1", "temp1", "temp1", "temp@nhn.com", Role.USER);
		userController.save(admin, temp);
		temp = new User("temp2", "temp2", "temp2", "temp@nhn.com", Role.USER);
		userController.save(admin, temp);

		currUser.setFollowersStr("temp1, temp2");
		userController.save(currUser, currUser);
		userJson = userController.getOne(currUser.getUserId()).getBody();
		user = gson.fromJson(userJson, User.class);
		assertThat(user.getFollowers().size(), is(2));
		assertThat(user.getFollowers().get(0).getUserId(), is("temp1"));
	}

	@Test
	public void testUpdate() {		// TODO: Resolve lazy initialize exception
		// test update the role of current user.
		User currUser = getTestUser();
		assertThat(currUser.getRole(), is(Role.USER)); // current test user is "USER"

		User updatedUser = new User(currUser.getUserId(), currUser.getUserName(), currUser.getPassword(),
				"temp@nhn.com", currUser.getRole());
		updatedUser.setId(currUser.getId());
		updatedUser.setEmail("test@test.com");
		updatedUser.setRole(Role.ADMIN); // Attempt to modify himself as ADMIN
		userController.save(currUser, updatedUser);

		String userJson = userController.getOne(currUser.getUserId()).getBody();
		User user = gson.fromJson(userJson, User.class);
		assertThat(user.getUserName(), is(currUser.getUserName()));
		assertThat(user.getPassword(), is(currUser.getPassword()));
		assertThat(user.getRole(), is(Role.USER));
	}

	private void saveTestUser(String userId, String userName) {
		User newUser = new User();
		newUser.setUserId(userId);
		newUser.setUserName(userName);
		newUser.setEmail("junoyoon@gmail.com");
		newUser.setCreatedUser(getTestUser());
		newUser.setCreatedDate(new Date());
		newUser.setRole(Role.USER);
		userController.save(getAdminUser(), newUser);
	}

	/**
	 * Test method for
	 * {@link org.ngrinder.user.controller.UserController#delete(org.ngrinder.model.User user, java.lang.String)}
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
		Page<User> userList = userController.getAll(null, page, "NewUserName");
		assertThat(userList.getContent().size(), is(3));

		// test to delete one
		userController.delete(testUser, "NewUserId1");
		userList = userController.getAll(Role.USER, page, "NewUserName");
		assertThat(userList.getContent().size(), is(2));

		// test to delete more
		userController.deleteUsers(testUser, "NewUserId2,NewUserId3");
		userList = userController.getAll(Role.USER, page, "NewUserName");
		assertThat(userList.getContent().size(), is(0));
	}

	/**
	 * Test method for
	 * {@link UserController#checkDuplication(String)}
	 * .
	 */
	@Test
	public void testDuplication() {
		BaseController ngrinderBaseController = new BaseController();
		HttpEntity<String> rtnStr = userController.checkDuplication("not-exist");
		assertThat(rtnStr.getBody(), is(ngrinderBaseController.returnSuccess()));

		rtnStr = userController.checkDuplication(getTestUser().getUserId());
		assertThat(rtnStr.getBody(), is(ngrinderBaseController.returnError()));
	}

	@Test
	public void testProfile() {
		ModelMap model = new ModelMap();
		String viewName = userController.getOne(getTestUser(), model);
		assertThat(viewName, is("user/info"));
	}

	@Test
	public void testSwitchOptions() {
		User currUser = getTestUser();
		User temp = new User("temp1", "temp1", "temp1", "temp@nhn.com", Role.USER);
		User admin = getAdminUser();
		userController.save(admin, temp);
		currUser.setOwners(Lists.newArrayList(temp));
		currUser.setOwnerUser(temp);
		userController.save(currUser, currUser);
		HttpEntity<String> shareUsersStr = userApiController.switchOptions(currUser, "");
		assertTrue(shareUsersStr.getBody().contains("id"));
	}
}
