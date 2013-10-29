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


import static org.ngrinder.common.util.Preconditions.checkArgument;

import java.util.EnumSet;

import org.ngrinder.common.controller.NGrinderBaseController;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * User registration controller.
 * 
 * @author Matt
 * @since 3.3
 * 
 */
@Controller
@RequestMapping("/registration")
public class UserRegistrationController extends NGrinderBaseController {

	@Autowired
	private UserService userService;

	@Autowired
	private Config config;

	/**
	 * New user sign up form login page.
	 * 
	 * @param model
	 *            mode
	 * @return "user/user_sign_up_modal"
	 */
	@RequestMapping("/sign_up")
	public String getUserForm(ModelMap model) {
		model.addAttribute("roleSet", EnumSet.of(Role.USER));
		model.addAttribute("isSelfRegistration", config.isSelfUserRegistration());
		return "user/user_sign_up_modal";
	}
	
	/**
	 * Check the user id existence.
	 * 
	 * @param model
	 *            model
	 * @param userId
	 *            userId to be checked
	 * @return success json if true.
	 */
	@RequestMapping("/{userId}/duplication_check")
	@ResponseBody
	public String checkUserId(ModelMap model, @PathVariable String userId) {
		User user = userService.getUserById(userId);
		return (user == null) ? returnSuccess() : returnError();
	}
	
	/**
	 * Save user detail info.
	 * 
	 * @param model
	 *            model
	 * @param newUser
	 *            user to be created.
	 * @return "redirect:/home"
	 */
	@RequestMapping("/save")
	public String saveOrUpdateUserDetail(ModelMap model, @ModelAttribute("user") User newUser) {
		checkArgument(newUser.getRole().equals(Role.USER), "User role must be General user !");
		userService.createUser(newUser);
		return "redirect:/home";
	}
}
