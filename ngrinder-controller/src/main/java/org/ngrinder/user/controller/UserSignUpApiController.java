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


import org.ngrinder.common.controller.RestAPI;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static org.ngrinder.common.util.Preconditions.checkTrue;

/**
 * User sign up api controller.
 * @since 3.5.0
 */
@RestController
@RequestMapping("/sign_up/api")
public class UserSignUpApiController extends UserApiController {

	@Autowired
	private UserService userService;

	@Autowired
	private Config config;

	/**
	 * New user sign up form login page.
	 */
	@RestAPI
	@GetMapping("/new")
	public Map<String, Object> signUpInfo() {
		Map<String, Object> model = new HashMap<>();
		checkTrue(config.isSignUpEnabled(), "Access to this url is not allowed when sign up is disabled");
		User one = User.createNew();
		model.put("user", one);
		model.put("allowUserIdChange", true);
		model.put("allowPasswordChange", true);
		model.put("allowRoleChange", false);
		model.put("roleSet", EnumSet.allOf(Role.class));
		model.put("userSecurityEnabled", config.isUserSecurityEnabled());
		model.put("allowShareChange", false);
		model.put("showPasswordByDefault", true);
		model.put("newUser", true);
		return model;
	}

	/**
	 * Save a user.
	 *
	 * @param newUser user to be added.
	 * @return success
	 */
	@RestAPI
	@PostMapping("/save")
	public String save(@RequestBody User newUser) {
		checkTrue(config.isSignUpEnabled(), "Access to this url is not allowed when sign up is disabled");
		newUser.setRole(Role.USER);
		userService.createUser(newUser);
		return returnSuccess();
	}


	/**
	 * To block security issue.
	 *
	 * @deprecated
	 */
	@Override
	@GetMapping("/new_remap")
	public Map<String, Object> openForm(User user) {
		return null;
	}

	/**
	 * To block security issue.
	 *
	 * @param user        current user
	 * @param updatedUser user to be updated.
	 * @return
	 * @deprecated
	 */
	@Override
	@GetMapping("/save_remap")
	public String save(User user, @ModelAttribute("user") User updatedUser) {
		return null;
	}

	/**
	 * To block security issue.
	 *
	 * @param userId userId to be checked
	 * @return
	 * @deprecated
	 */
	@Override
	@RestAPI
	@GetMapping("/{userId}/check_duplication_remap")
	public Map<String, Boolean> checkDuplication(@PathVariable String userId) {
		return null;
	}

	/**
	 * Check the user id existence.
	 *
	 * @param userId userId to be checked
	 * @return success json if true.
	 */
	@RestAPI
	@GetMapping("/{userId}/check_duplication")
	public Map<String, Boolean> checkDuplicationForRegistration(@PathVariable String userId) {
		checkTrue(config.isSignUpEnabled(), "Access to this url is not allowed when sign up is disabled");
		User user = userService.getOne(userId);
		return (user == null) ? successJson() : errorJson();
	}
}
