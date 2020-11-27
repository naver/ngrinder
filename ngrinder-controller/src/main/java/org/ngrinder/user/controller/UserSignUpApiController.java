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

import lombok.RequiredArgsConstructor;

import org.ngrinder.infra.config.Config;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.user.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static org.ngrinder.common.constant.WebConstants.JSON_SUCCESS;
import static org.ngrinder.common.util.CollectionUtils.buildMap;
import static org.ngrinder.common.util.Preconditions.checkTrue;

/**
 * User sign up api controller.
 *
 * @since 3.5.0
 */
@RestController
@RequestMapping("/sign_up/api")
@RequiredArgsConstructor
public class UserSignUpApiController {

	private final UserService userService;

	private final Config config;

	/**
	 * New user sign up form login page.
	 */
	@GetMapping("/new")
	public Map<String, Object> signUpInfo() {
		Map<String, Object> model = new HashMap<>();
		checkTrue(config.isSignUpEnabled(), "Access to this url is not allowed when sign up is disabled");
		User one = User.createNew();
		model.put("user", one);

		Map<String, Object> viewConfig = new HashMap<>();
		viewConfig.put("newUser", true);
		viewConfig.put("roleSet", EnumSet.allOf(Role.class));
		viewConfig.put("allowUserIdChange", true);
		viewConfig.put("allowPasswordChange", true);
		viewConfig.put("allowRoleChange", false);
		viewConfig.put("userSecurityEnabled", config.isUserSecurityEnabled());
		viewConfig.put("allowShareChange", false);
		viewConfig.put("showPasswordByDefault", true);

		model.put("config", viewConfig);
		return model;
	}

	/**
	 * Save a user.
	 *
	 * @param newUser user to be added.
	 */
	@PostMapping("/save")
	public void save(@RequestBody User newUser) {
		checkTrue(config.isSignUpEnabled(), "Access to this url is not allowed when sign up is disabled");
		newUser.setRole(Role.USER);
		userService.createUser(newUser);
	}

	/**
	 * Check the user id existence.
	 *
	 * @param userId userId to be checked
	 * @return success json if true.
	 */
	@GetMapping("/{userId}/check_duplication")
	public Map<String, Object> checkDuplicationForRegistration(@PathVariable String userId) {
		checkTrue(config.isSignUpEnabled(), "Access to this url is not allowed when sign up is disabled");
		User user = userService.getOne(userId);
		return buildMap(JSON_SUCCESS, user == null);
	}
}
