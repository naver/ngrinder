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
import static org.ngrinder.common.util.Preconditions.checkNotEmpty;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.controller.NGrinderBaseController;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.Permission;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;

/**
 * User management controller.
 * 
 * @author JunHo Yoon
 * @author Alex Quin
 * @since 3.0
 * 
 */
@Controller
@RequestMapping("/user")
public class UserController extends NGrinderBaseController {

	@Autowired
	private UserService userService;

	@Autowired
	private Config config;

	/**
	 * Get user list on the given role.
	 * 
	 * @param model
	 *            model
	 * @param roleName
	 *            role
	 * @param keywords
	 *            search keyword.
	 * @return user/userList
	 */
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping({ "", "/" })
	public String getUserList(ModelMap model, @RequestParam(required = false) String roleName,
					@RequestParam(required = false) String keywords) {

		List<User> userList = null;
		if (StringUtils.isEmpty(keywords)) {
			userList = userService.getAllUserByRole(roleName);
		} else {
			userList = userService.getUserListByKeyWord(keywords);
			model.put("keywords", keywords);
		}

		model.addAttribute("userList", userList);
		EnumSet<Role> roleSet = EnumSet.allOf(Role.class);
		model.addAttribute("roleSet", roleSet);
		model.addAttribute("roleName", roleName);

		return "user/list";
	}

	/**
	 * Get user creation form page.
	 * 
	 * @param user
	 *            current user
	 * @param model
	 *            mode
	 * @return "user/userDetail"
	 */
	@RequestMapping("/new")
	@PreAuthorize("hasAnyRole('A') or #user.userId == #userId")
	public String getUserDetail(User user, final ModelMap model) {
		model.addAttribute("roleSet", EnumSet.allOf(Role.class));
		return "user/detail";
	}

	/**
	 * Get user detail page.
	 * 
	 * @param user
	 *            current user
	 * @param model
	 *            mode
	 * @param userId
	 *            user to get
	 * @return "user/userDetail"
	 */
	@RequestMapping("/{userId}")
	@PreAuthorize("hasAnyRole('A') or #user.userId == #userId")
	public String getUserDetail(User user, final ModelMap model, @PathVariable final String userId) {
		model.addAttribute("roleSet", EnumSet.allOf(Role.class));
		User userFromDB = userService.getUserByIdWithoutCache(userId);
		model.addAttribute("user", userFromDB);
		model.addAttribute("userSecurity", config.isUserSecurityEnabled());
		getUserShareList(userFromDB, model);
		return "user/detail";
	}

	/**
	 * Save or Update user detail info.
	 * 
	 * @param user
	 *            current user
	 * @param model
	 *            model
	 * @param updatedUser
	 *            user to be updated.
	 * @param followersStr
	 *            user Id list that current will share his permission to.
	 * @return "redirect:/user/list" if current user change his info, otheriwise return "redirect:/"
	 */
	@RequestMapping("/save")
	@PreAuthorize("hasAnyRole('A') or #user.id == #updatedUser.id")
	public String saveOrUpdateUserDetail(User user, ModelMap model, @ModelAttribute("user") User updatedUser,
					@RequestParam(required = false) String followersStr) {
		checkArgument(updatedUser.validate());
		if (user.getRole() == Role.USER) {
			// General user can not change their role.
			User updatedUserInDb = userService.getUserById(updatedUser.getUserId());
			checkNotNull(updatedUserInDb);
			updatedUser.setRole(updatedUserInDb.getRole());

			// prevent user to modify with other user id
			checkArgument(updatedUserInDb.getId().equals(updatedUser.getId()), "Illegal request to update user:%s",
							updatedUser);
		}
		if (updatedUser.exist()) {
			userService.modifyUser(updatedUser, followersStr);
		} else {
			userService.saveUser(updatedUser);
		}
		model.clear();
		if (user.getId().equals(updatedUser.getId())) {
			return "redirect:/";
		} else {
			return "redirect:/user/";
		}
	}

	/**
	 * Delete users.
	 * 
	 * @param model
	 *            model
	 * @param userIds
	 *            comma separated user ids.
	 * @return "redirect:/user/list"
	 */
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping("/delete")
	public String deleteUser(ModelMap model, @RequestParam String userIds) {
		String[] ids = userIds.split(",");
		ArrayList<String> aListNumbers = new ArrayList<String>(Arrays.asList(ids));
		userService.deleteUsers(aListNumbers);
		model.clear();
		return "redirect:/user/";
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
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping("/{userId}/checkUserId")
	@ResponseBody
	public String checkUserId(ModelMap model, @PathVariable String userId) {
		User user = userService.getUserById(userId);
		return (user == null) ? returnSuccess() : returnError();
	}

	/**
	 * Get the current user profile.
	 * 
	 * @param user
	 *            current user
	 * @param model
	 *            model
	 * @return "user/userInfo"
	 */
	@RequestMapping("/profile")
	public String userProfile(User user, ModelMap model) {
		checkNotEmpty(user.getUserId(), "UserID should not be NULL!");
		User currentUser = userService.getUserByIdWithoutCache(user.getUserId());
		model.addAttribute("user", currentUser);
		model.addAttribute("demo", config.isDemo());
		getUserShareList(currentUser, model);
		model.addAttribute("action", "profile");
		model.addAttribute("userSecurity", config.isUserSecurityEnabled());
		return "user/info";
	}

	/**
	 * Get the follower list.
	 * 
	 * @param user
	 *            current user
	 * @param model
	 *            model
	 * @return "user/switchOptions"
	 */
	@RequestMapping("/switchOptions")
	public String switchOptions(User user, ModelMap model) {
		if (user.getRole().hasPermission(Permission.SWITCH_TO_ANYONE)) {
			List<User> allUserByRole = userService.getAllUserByRole(Role.USER.getFullName());
			model.addAttribute("shareUserList", allUserByRole);
		} else {
			User currUser = userService.getUserByIdWithoutCache(user.getUserId());
			model.addAttribute("shareUserList", currUser.getOwners());
		}
		return "user/switchOptions";
	}

	/**
	 * Switch user identity.
	 * 
	 * @param user
	 *            current user
	 * @param model
	 *            model
	 * @param switchUser
	 *            the user who will switch
	 * @param request
	 *            request
	 * @param response
	 *            response
	 * 
	 * @return redirect:/perftest/list
	 */
	@RequestMapping("/switchUser")
	public String switchUser(User user, ModelMap model,
					@RequestParam(required = false, defaultValue = "") String switchUser, HttpServletRequest request,
					HttpServletResponse response) {
		Cookie cookie = new Cookie("switchUser", switchUser);
		cookie.setPath("/");
		// Delete Cookie if empty switchUser
		if (StringUtils.isEmpty(switchUser)) {
			cookie.setMaxAge(0);
		}
		response.addCookie(cookie);

		model.clear();
		return "redirect:/perftest/";
	}

	/**
	 * Get user list that current user will be shared, excluding current user.
	 * 
	 * @param user
	 *            current user
	 * @param model
	 *            model
	 */
	private void getUserShareList(User user, ModelMap model) {
		if (user == null) {
			model.addAttribute("followers", Lists.newArrayList());
			model.addAttribute("shareUserList", Lists.newArrayList());
			return;
		}

		List<User> users = Lists.newArrayList();
		String userId = user.getUserId();
		for (User u : userService.getAllUserByRole(Role.USER.getFullName())) {
			if (u.getUserId().equals(userId)) {
				continue;
			}
			users.add(u.getUserBaseInfo());
		}
		model.addAttribute("followers", user.getFollowers());
		model.addAttribute("shareUserList", users);

	}
}
