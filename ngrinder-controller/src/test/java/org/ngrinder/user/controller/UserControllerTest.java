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

import com.google.common.collect.Lists;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.ui.ModelMap;

import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class UserControllerTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private UserController userController;

	/**
	 * Test method for
	 * {@link org.ngrinder.user.controller.UserController#getAll(org.springframework.ui.ModelMap, org.ngrinder.model.Role,
	 * org.springframework.data.domain.Pageable, java.lang.String)}
	 * .
	 */
	@Test
	public void testGetAll() {
		Pageable page = new PageRequest(1, 10);

		ModelMap model = new ModelMap();
		userController.getAll(model, null, page, null);

		model.clear();
		userController.getAll(model, Role.ADMIN, page, null);

		model.clear();
		userController.getAll(model, null, page, "user");

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
	 * org.ngrinder.model.User, org.springframework.ui.ModelMap)}
	 * .
	 */
	@Test
	public void testSave() {
		// test update
		ModelMap model = new ModelMap();
		User currUser = getTestUser();
		currUser.setUserName("new name");
		userController.save(currUser, currUser, model);
		userController.getOne(currUser.getUserId(), model);
		User user = (User) model.get("user");
		assertThat(user.getUserName(), is("new name"));
		assertThat(user.getPassword(), is(currUser.getPassword()));

		User admin = getAdminUser();
		User temp = new User("temp1", "temp1", "temp1", "temp@nhn.com", Role.USER);
		userController.save(admin, temp, model);
		temp = new User("temp2", "temp2", "temp2", "temp@nhn.com", Role.USER);
		userController.save(admin, temp, model);
		model.clear();
		currUser.setFollowersStr("temp1, temp2");
		userController.save(currUser, currUser, model);
		userController.getOne(currUser.getUserId(), model);
		user = (User) model.get("user");
		assertThat(user.getFollowers().size(), is(2));
		assertThat(user.getFollowers().get(0).getUserId(), is("temp1"));
	}

	@Test
	public void testUpdate() {
		// test update the role of current user.
		ModelMap model = new ModelMap();
		User currUser = getTestUser();
		assertThat(currUser.getRole(), is(Role.USER)); // current test user is "USER"

		User updatedUser = new User(currUser.getUserId(), currUser.getUserName(), currUser.getPassword(),
				"temp@nhn.com", currUser.getRole());
		updatedUser.setId(currUser.getId());
		updatedUser.setEmail("test@test.com");
		updatedUser.setRole(Role.ADMIN); // Attempt to modify himself as ADMIN
		userController.save(currUser, updatedUser, model);

		userController.getOne(currUser.getUserId(), model);
		User user = (User) model.get("user");
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
		ModelMap model = new ModelMap();
		userController.save(getAdminUser(), newUser, model);
	}

	/**
	 * Test method for
	 * {@link org.ngrinder.user.controller.UserController#delete(org.springframework.ui.ModelMap, java.lang.String)}
	 * .
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testDelete() {
		ModelMap model = new ModelMap();
		// save new user for test
		saveTestUser("NewUserId1", "NewUserName1");
		saveTestUser("NewUserId2", "NewUserName2");
		saveTestUser("NewUserId3", "NewUserName3");

		Pageable page = new PageRequest(0, 10);

		// search
		userController.getAll(model, null, page, "NewUserName");
		PageImpl userList = (PageImpl<User>) model.get("users");
		assertThat(userList.getContent().size(), is(3));

		// test to delete one
		model.clear();
		userController.delete(testUser, "NewUserId1", model);
		model.clear();
		userController.getAll(model, Role.USER, page, "NewUserName");
		userList = (PageImpl<User>) model.get("users");
		assertThat(userList.getContent().size(), is(2));

		// test to delete more
		model.clear();
		userController.delete(testUser, "NewUserId2,NewUserId3", model);
		model.clear();
		userController.getAll(model, Role.USER, page, "NewUserName");
		userList = (PageImpl<User>) model.get("users");
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
		ModelMap model = new ModelMap();
		User currUser = getTestUser();
		User temp = new User("temp1", "temp1", "temp1", "temp@nhn.com", Role.USER);
		User admin = getAdminUser();
		userController.save(admin, temp, model);
		currUser.setOwners(Lists.newArrayList(temp));
		currUser.setOwnerUser(temp);
		userController.save(currUser, currUser, model);
		HttpEntity<String> shareUsersStr = userController.switchOptions(currUser, "");
		assertTrue(shareUsersStr.getBody().contains("id"));
	}
}
