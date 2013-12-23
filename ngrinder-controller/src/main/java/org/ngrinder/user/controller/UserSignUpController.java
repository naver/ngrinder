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
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.ngrinder.common.util.Preconditions.checkTrue;

/**
 * User sign up controller.
 *
 * @author Matt
 * @since 3.3
 */
@Controller
@RequestMapping("/sign_up")
public class UserSignUpController extends UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private Config config;

	/**
	 * New user sign up form login page.
	 *
	 * @param model mode
	 * @return "user/sign_up_modal"
	 */
	@RequestMapping("/new")
	public String openForm(ModelMap model) {
		checkTrue(config.isSignUpEnabled(), "Access to this url is not allowed when sign up is disabled");
		super.openForm(null, model);
		model.addAttribute("allowShareChange", false);
		model.addAttribute("showPasswordByDefault", true);
		model.addAttribute("newUser", true);
		return "user/sign_up_modal";
	}


	/**
	 * To block security issue.
	 *
	 * @deprecated
	 */
	@RequestMapping("/new_remap")
	public String openForm(User user, ModelMap model) {
		return null;
	}

	/**
	 * Get user list that current user will be shared, excluding current user.
	 *
	 * @param user  current user
	 * @param model model
	 */
	protected void attachCommonAttribute(User user, ModelMap model) {
		model.addAttribute("userSecurityEnabled", config.isUserSecurityEnabled());
	}

	/**
	 * Save a user.
	 *
	 * @param newUser user to be added.
	 * @param model   model
	 * @return "redirect:/"
	 */
	@RequestMapping("/save")
	public String save(@ModelAttribute("user") User newUser, ModelMap model) {
		checkTrue(config.isSignUpEnabled(), "Access to this url is not allowed when sign up is disabled");
		newUser.setRole(Role.USER);
		userService.createUser(newUser);
		model.clear();
		return "redirect:/";
	}

	/**
	 * To block security issue.
	 *
	 * @param user        current user
	 * @param updatedUser user to be updated.
	 * @param model       model
	 * @return
	 * @deprecated
	 */
	@RequestMapping("/save_remap")
	public String save(User user, @ModelAttribute("user") User updatedUser, ModelMap model) {
		return null;
	}

	/**
	 * To block security issue.
	 *
	 * @param userId userId to be checked
	 * @return
	 * @deprecated
	 */
	@RestAPI
	@RequestMapping("/api/{userId}/check_duplication_remap")
	public HttpEntity<String> checkDuplication(@PathVariable String userId) {
		return null;
	}

	/**
	 * Check the user id existence.
	 *
	 * @param userId userId to be checked
	 * @return success json if true.
	 */
	@RestAPI
	@RequestMapping("/api/{userId}/check_duplication")
	public HttpEntity<String> checkDuplicationForRegistration(@PathVariable String userId) {
		checkTrue(config.isSignUpEnabled(), "Access to this url is not allowed when sign up is disabled");
		User user = userService.getOne(userId);
		return (user == null) ? successJsonHttpEntity() : errorJsonHttpEntity();
	}
}
