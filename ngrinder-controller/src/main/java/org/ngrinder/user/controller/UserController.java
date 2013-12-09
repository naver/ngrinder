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
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.common.controller.RestAPI;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.Permission;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.web.PageableDefaults;
import org.springframework.http.HttpEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static org.ngrinder.common.util.Preconditions.*;

/**
 * User management controller.
 *
 * @author JunHo Yoon
 * @author Alex Quin
 * @since 3.0
 */
@Controller
@RequestMapping("/user")
public class UserController extends BaseController {

	@Autowired
	private UserService userService;
	@Autowired
	private Config config;

	/**
	 * Get user list on the given role.
	 *
	 * @param model    model
	 * @param role     role
	 * @param pageable page info
	 * @param keywords search keyword.
	 * @return user/userList
	 */
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping({"", "/"})
	public String getAll(ModelMap model, @RequestParam(required = false) Role role,
	                     @PageableDefaults(pageNumber = 0, value = 10) Pageable pageable,
	                     @RequestParam(required = false) String keywords) {

		PageRequest pageReq = ((PageRequest) pageable);
		Sort sort = pageReq == null ? null : pageReq.getSort();
		if (sort == null && pageReq != null) {
			sort = new Sort(Direction.ASC, "userName");
			pageable = new PageRequest(pageReq.getPageNumber(), pageReq.getPageSize(), sort);
		}
		Page<User> pagedUser;
		if (StringUtils.isEmpty(keywords)) {
			pagedUser = userService.getPagedAll(role, pageable);
		} else {
			pagedUser = userService.getPagedAll(keywords, pageable);
			model.put("keywords", keywords);
		}
		model.addAttribute("users", pagedUser);
		EnumSet<Role> roleSet = EnumSet.allOf(Role.class);
		model.addAttribute("roleSet", roleSet);
		model.addAttribute("role", role);
		model.addAttribute("page", pageable);
		if (sort != null) {
			Order sortProp = sort.iterator().next();
			model.addAttribute("sortColumn", sortProp.getProperty());
			model.addAttribute("sortDirection", sortProp.getDirection());
		}
		return "user/list";
	}

	/**
	 * Get user creation form page.
	 *
	 * @param user  current user
	 * @param model mode
	 * @return "user/userDetail"
	 */
	@RequestMapping("/new")
	@PreAuthorize("hasAnyRole('A') or #user.userId == #userId")
	public String openForm(User user, final ModelMap model) {
		model.addAttribute("roleSet", EnumSet.allOf(Role.class));
		return "user/detail";
	}

	/**
	 * Get user detail page.
	 *
	 * @param user   current user
	 * @param model  mode
	 * @param userId user to get
	 * @return "user/userDetail"
	 */
	@RequestMapping("/{userId}")
	@PreAuthorize("hasAnyRole('A') or #user.userId == #userId")
	public String getOne(User user, final ModelMap model, @PathVariable final String userId) {
		model.addAttribute("roleSet", EnumSet.allOf(Role.class));
		User userFromDB = userService.getOne(userId);
		model.addAttribute("user", userFromDB);
		getUserShareList(userFromDB, model);
		return "user/detail";
	}

	/**
	 * Save or Update user detail info.
	 *
	 * @param user         current user
	 * @param model        model
	 * @param updatedUser  user to be updated.
	 * @param followersStr user Id list that current will share his permission to.
	 * @return "redirect:/user/list" if current user change his info, otheriwise return "redirect:/"
	 */
	@RequestMapping("/save")
	@PreAuthorize("hasAnyRole('A') or #user.id == #updatedUser.id")
	public String save(User user, @ModelAttribute("user") User updatedUser, ModelMap model) {
		checkArgument(updatedUser.validate());
		if (user.getRole() == Role.USER) {
			// General user can not change their role.
			User updatedUserInDb = userService.getOne(updatedUser.getUserId());
			checkNotNull(updatedUserInDb);
			updatedUser.setRole(updatedUserInDb.getRole());

			// prevent user to modify with other user id
			checkArgument(updatedUserInDb.getId().equals(updatedUser.getId()), "Illegal request to update user:%s",
					updatedUser);
		}
		save(updatedUser);
		model.clear();
		if (user.getId().equals(updatedUser.getId())) {
			return "redirect:/";
		} else {
			return "redirect:/user/";
		}
	}

	private User save(User user) {
		if (StringUtils.isBlank(user.getPassword())) {
			return userService.saveWithoutPasswordEncoding(user);
		} else {
			return userService.save(user);
		}
	}

	/**
	 * Delete users.
	 *
	 * @param model   model
	 * @param userIds comma separated user ids.
	 * @return "redirect:/user/list"
	 */
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping("/delete")
	public String delete(ModelMap model, @RequestParam String userIds) {
		String[] ids = userIds.split(",");
		for (String eachId : Arrays.asList(ids)) {
			userService.delete(eachId);
		}
		model.clear();
		return "redirect:/user/";
	}

	/**
	 * Get the current user profile.
	 *
	 * @param user  current user
	 * @param model model
	 * @return "user/userInfo"
	 */
	@RequestMapping("/profile")
	public String getOne(User user, ModelMap model) {
		checkNotEmpty(user.getUserId(), "UserID should not be NULL!");
		User currentUser = userService.getOne(user.getUserId());
		model.addAttribute("user", currentUser);
		model.addAttribute("demo", config.isDemo());
		getUserShareList(currentUser, model);
		model.addAttribute("action", "profile");
		return "user/info";
	}

	/**
	 * Get the follower list.
	 *
	 * @param user  current user
	 * @param model model
	 * @return "user/switchOptions"
	 */
	@RequestMapping("/switch_options")
	public String switchOptions(User user, ModelMap model) {
		if (user.getRole().hasPermission(Permission.SWITCH_TO_ANYONE)) {
			List<User> allUserByRole = userService.getAll(Role.USER);
			model.addAttribute("shareUserList", allUserByRole);
		} else {
			User currUser = userService.getOne(user.getUserId());
			model.addAttribute("shareUserList", currUser.getOwners());
		}
		return "user/switch_options";
	}

	/**
	 * Switch user identity.
	 *
	 * @param model    model
	 * @param to       the user to whom a user will switch
	 * @param request  request
	 * @param response response
	 * @return redirect:/perftest/list
	 */
	@RequestMapping("/switch")
	public String switchUser(ModelMap model, @RequestParam(required = false, defaultValue = "") String to,
	                         HttpServletResponse response) {
		Cookie cookie = new Cookie("switchUser", to);
		cookie.setPath("/");
		// Delete Cookie if empty switchUser
		if (StringUtils.isEmpty(to)) {
			cookie.setMaxAge(0);
		}
		response.addCookie(cookie);
		model.clear();
		return "redirect:/perftest/";
	}

	/**
	 * Get user list that current user will be shared, excluding current user.
	 *
	 * @param user  current user
	 * @param model model
	 */
	private void getUserShareList(User user, ModelMap model) {
		if (user == null) {
			model.addAttribute("followers", Lists.newArrayList());
			model.addAttribute("shareUserList", Lists.newArrayList());
			return;
		}

		List<User> users = Lists.newArrayList();
		String userId = user.getUserId();
		for (User u : userService.getAll(Role.USER)) {
			if (u.getUserId().equals(userId)) {
				continue;
			}
			users.add(u.getUserBaseInfo());
		}
		model.addAttribute("followers", user.getFollowers());
		model.addAttribute("shareUserList", users);
	}

	/**
	 * Check the user id existence.
	 *
	 * @param userId userId to be checked
	 * @return success json if true.
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping("/api/{userId}/check_duplication")
	public HttpEntity<String> checkDuplication(@PathVariable String userId) {
		User user = userService.getOne(userId);
		return (user == null) ? successJsonHttpEntity() : errorJsonHttpEntity();
	}

	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = {"/api/", "/api"}, method = RequestMethod.GET)
	public HttpEntity<String> getAll(Role role) {
		return toJsonHttpEntity(userService.getAll(role));
	}

	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = "/api/{userId}", method = RequestMethod.GET)
	public HttpEntity<String> getOne(@PathVariable("userId") String userId) {
		return toJsonHttpEntity(userService.getOne(userId));
	}

	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = {"/api/", "/api"}, method = RequestMethod.POST)
	public HttpEntity<String> create(@ModelAttribute("user") User newUser) {
		checkNull(newUser.getId(), "User DB ID should be null");
		return toJsonHttpEntity(save(newUser));
	}

	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = "/api/{userId}", method = RequestMethod.PUT)
	public HttpEntity<String> update(@PathVariable("userId") String userId, User update) {
		update.setUserId(userId);
		checkNull(update.getId(), "User DB ID should be null");
		return toJsonHttpEntity(save(update));
	}

	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = "/api/{userId}", method = RequestMethod.DELETE)
	public HttpEntity<String> delete(@PathVariable("userId") String userId) {
		userService.delete(userId);
		return successJsonHttpEntity();
	}

}
