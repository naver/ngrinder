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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.common.controller.NGrinderBaseController;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.ui.ModelMap;

/**
 * Class description.
 * 
 * @author Mavlarn
 * @since
 */
public class UserControllerTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private UserController userController;

	/**
	 * Test method for
	 * {@link org.ngrinder.user.controller.UserController#getUserList(org.springframework.ui.ModelMap, java.lang.String, java.lang.String)}
	 * .
	 */
	@Test
	public void testGetUserList() {
		Pageable page = new PageRequest(1, 10);

		ModelMap model = new ModelMap();
		userController.getUsers(model, null, page, null);

		model.clear();
		userController.getUsers(model, Role.ADMIN, page, null);

		model.clear();
		userController.getUsers(model, null, page, "user");

	}

	/**
	 * Test method for
	 * {@link org.ngrinder.user.controller.UserController#getUserDetail(org.ngrinder.model.User, org.springframework.ui.ModelMap, java.lang.String)}
	 * .
	 */
	@Test
	public void testGetUserDetail() {
		ModelMap model = new ModelMap();
		userController.getUserDetail(getTestUser(), model, getTestUser().getUserId());
		User user = (User) model.get("user");
		assertThat(user.getId(), is(getTestUser().getId()));
	}

	/**
	 * Test method for
	 * {@link org.ngrinder.user.controller.UserController#saveOrUpdateUserDetail(org.ngrinder.model.User, org.springframework.ui.ModelMap, org.ngrinder.model.User)}
	 * .
	 */
	@Test
	public void testSaveOrUpdateUserDetail() {
		// test update
		ModelMap model = new ModelMap();
		User currUser = getTestUser();
		currUser.setUserName("new name");
		userController.saveUser(currUser, model, currUser);
		userController.getUserDetail(getTestUser(), model, currUser.getUserId());
		User user = (User) model.get("user");
		assertThat(user.getUserName(), is("new name"));
		assertThat(user.getPassword(), is(currUser.getPassword()));

		User admin = getAdminUser();
		User temp = new User("temp1", "temp1", "temp1", "temp@nhn.com", Role.USER);
		userController.saveUser(admin, model, temp);
		temp = new User("temp2", "temp2", "temp2", "temp@nhn.com", Role.USER);
		userController.saveUser(admin, model, temp);
		model.clear();
		currUser.setFollowersStr("temp1, temp2");
		userController.saveUser(currUser, model, currUser);
		userController.getUserDetail(getTestUser(), model, currUser.getUserId());
		user = (User) model.get("user");
		assertThat(user.getFollowers().size(), is(2));
		assertThat(user.getFollowers().get(0).getUserId(), is("temp1"));
	}

	@Test
	public void testUpdateCurrentUserRole() {
		// test update the role of current user.
		ModelMap model = new ModelMap();
		User currUser = getTestUser();
		assertThat(currUser.getRole(), is(Role.USER)); // current test user is "USER"

		User updatedUser = new User(currUser.getUserId(), currUser.getUserName(), currUser.getPassword(),
						"temp@nhn.com", currUser.getRole());
		updatedUser.setId(currUser.getId());
		updatedUser.setEmail("test@test.com");
		updatedUser.setRole(Role.ADMIN); // Attempt to modify himself as ADMIN
		userController.saveUser(currUser, model, updatedUser);

		userController.getUserDetail(getTestUser(), model, currUser.getUserId());
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
		userController.saveUser(getAdminUser(), model, newUser);
	}

	/**
	 * Test method for
	 * {@link org.ngrinder.user.controller.UserController#deleteUser(org.springframework.ui.ModelMap, java.lang.String)}
	 * .
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteUser() {
		ModelMap model = new ModelMap();
		// save new user for test
		saveTestUser("NewUserId1", "NewUserName1");
		saveTestUser("NewUserId2", "NewUserName2");
		saveTestUser("NewUserId3", "NewUserName3");

		Pageable page = new PageRequest(1, 10);

		// search
		userController.getUsers(model, null, page, "NewUserName");
		List<User> userList = (List<User>) model.get("userList");
		assertThat(userList.size(), is(3));

		// test to delete one
		model.clear();
		userController.deleteUser(model, "NewUserId1");
		model.clear();
		userController.getUsers(model, Role.USER, page, "NewUserName");
		userList = (List<User>) model.get("userList");
		assertThat(userList.size(), is(2));

		// test to delete more
		model.clear();
		userController.deleteUser(model, "NewUserId2,NewUserId3");
		model.clear();
		userController.getUsers(model, Role.USER, page, "NewUserName");
		userList = (List<User>) model.get("userList");
		assertThat(userList.size(), is(0));
	}

	/**
	 * Test method for
	 * {@link org.ngrinder.user.controller.UserController#checkDuplication(org.springframework.ui.ModelMap, java.lang.String)}
	 * .
	 */
	@Test
	public void testCheckDuplicatedID() {
		NGrinderBaseController ngrinderBaseController = new NGrinderBaseController();
		ModelMap model = new ModelMap();
		HttpEntity<String> rtnStr = userController.checkDuplication(model, "not-exist");
		assertThat(rtnStr.getBody(), is(ngrinderBaseController.returnSuccess()));

		rtnStr = userController.checkDuplication(model, getTestUser().getUserId());
		assertThat(rtnStr.getBody(), is(ngrinderBaseController.returnError()));
	}

	@Test
	public void testUserProfile() {
		ModelMap model = new ModelMap();
		String viewName = userController.userProfile(getTestUser(), model);
		assertThat(viewName, is("user/info"));
	}

	@Test
	public void testSwitchOptions() {
		ModelMap model = new ModelMap();
		userController.switchOptions(getTestUser(), model);
		assertThat(model.containsAttribute("shareUserList"), is(true));
	}
}
