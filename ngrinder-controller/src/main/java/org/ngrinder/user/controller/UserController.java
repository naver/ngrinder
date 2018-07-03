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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.constant.ControllerConstants;
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
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static org.ngrinder.common.util.CollectionUtils.newArrayList;
import static org.ngrinder.common.util.ObjectUtils.defaultIfNull;
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
	protected Config config;

	public static final Sort DEFAULT_SORT = new Sort(Direction.ASC, "userName");

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
						 @PageableDefault(page = 0, size = 10) Pageable pageable,
	                     @RequestParam(required = false) String keywords) {
		pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), defaultIfNull(pageable.getSort(), DEFAULT_SORT));
		Pageable defaultPageable = new PageRequest(0, pageable.getPageSize(), defaultIfNull(pageable.getSort(), DEFAULT_SORT));
		Page<User> pagedUser;
		if (StringUtils.isEmpty(keywords)) {
			pagedUser = userService.getPagedAll(role, pageable);
			if (pagedUser.getNumberOfElements() == 0) {
				pagedUser = userService.getPagedAll(role, defaultPageable);
			}
		} else {
			pagedUser = userService.getPagedAll(keywords, pageable);
			if (pagedUser.getNumberOfElements() == 0) {
				pagedUser = userService.getPagedAll(keywords, defaultPageable);
			}
			model.put("keywords", keywords);
		}


		model.addAttribute("users", pagedUser);
		EnumSet<Role> roleSet = EnumSet.allOf(Role.class);
		model.addAttribute("roleSet", roleSet);
		model.addAttribute("role", role);
		putPageIntoModelMap(model, pageable);
		return "user/list";
	}


	/**
	 * Get user creation form page.
	 *
	 * @param user  current user
	 * @param model mode
	 * @return "user/detail"
	 */
	@RequestMapping("/new")
	@PreAuthorize("hasAnyRole('A') or #user.userId == #userId")
	public String openForm(User user, final ModelMap model) {
		User one = User.createNew();
		model.addAttribute("user", one);
		model.addAttribute("allowUserIdChange", true);
		model.addAttribute("allowPasswordChange", true);
		model.addAttribute("allowRoleChange", false);
		model.addAttribute("newUser", true);
		model.addAttribute("roleSet", EnumSet.allOf(Role.class));
		model.addAttribute("showPasswordByDefault", true);
		attachCommonAttribute(one, model);
		return "user/detail";
	}

	/**
	 * Get user detail page.
	 *
	 * @param model  mode
	 * @param userId user to get
	 * @return "user/detail"
	 */
	@RequestMapping("/{userId}")
	@PreAuthorize("hasAnyRole('A')")
	public String getOne(@PathVariable final String userId, ModelMap model) {
		User one = userService.getOne(userId);
		model.addAttribute("user", one);
		model.addAttribute("allowPasswordChange", true);
		model.addAttribute("allowRoleChange", true);
		model.addAttribute("roleSet", EnumSet.allOf(Role.class));
		model.addAttribute("showPasswordByDefault", false);
		attachCommonAttribute(one, model);
		return "user/detail";
	}

	/**
	 * Get the current user profile.
	 *
	 * @param user  current user
	 * @param model model
	 * @return "user/info"
	 */
	@RequestMapping("/profile")
	public String getOne(User user, ModelMap model) {
		checkNotEmpty(user.getUserId(), "UserID should not be NULL!");
		User one = userService.getOneWithFollowers(user.getUserId());
		model.addAttribute("user", one);
		model.addAttribute("allowPasswordChange", !config.isDemo());
		model.addAttribute("allowRoleChange", false);
		model.addAttribute("showPasswordByDefault", false);
		attachCommonAttribute(one, model);
		return "user/info";
	}

	/**
	 * Save or Update user detail info.
	 *
	 * @param user        current user
	 * @param model       model
	 * @param updatedUser user to be updated.
	 * @return "redirect:/user/list" if current user change his info, otherwise return "redirect:/"
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
	 * @return "redirect:/user/"
	 */
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping("/delete")
	public String delete(User user, @RequestParam String userIds, ModelMap model) {
		String[] ids = userIds.split(",");
		for (String eachId : Arrays.asList(ids)) {
			if (!user.getUserId().equals(eachId)) {
				userService.delete(eachId);
			}
		}
		model.clear();
		return "redirect:/user/";
	}


	/**
	 * Get the follower list.
	 *
	 * @param user     current user
	 * @param keywords search keyword.
	 * @return json message
	 */
	@RestAPI
	@RequestMapping("/api/switch_options")
	public HttpEntity<String> switchOptions(User user,
	                                        @RequestParam(required = true) final String keywords) {
		return toJsonHttpEntity(getSwitchableUsers(user, keywords));
	}

	/**
	 * Get the follower list.
	 *
	 * @param user  current user
	 * @param model model
	 * @return json message
	 */
	@RequestMapping("/switch_options")
	public String switchOptions(User user,
	                            ModelMap model) {
		model.addAttribute("switchableUsers", getSwitchableUsers(user, ""));
		return "user/switch_options";
	}


	private List<UserSearchResult> getSwitchableUsers(User user, String keywords) {
		if (user.getRole().hasPermission(Permission.SWITCH_TO_ANYONE)) {
			List<UserSearchResult> result = newArrayList();
			for (User each : userService.getPagedAll(keywords, new PageRequest(0, 10))) {
				result.add(new UserSearchResult(each));
			}
			return result;
		} else {
			return userService.getSharedUser(user);
		}

	}


	/**
	 * Switch user identity.
	 *
	 * @param model    model
	 * @param to       the user to whom a user will switch
	 * @param response response
	 * @return redirect:/perftest/
	 */
	@RequestMapping("/switch")
	public String switchUser(@RequestParam(required = false, defaultValue = "") String to,
	                         HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		Cookie cookie = new Cookie("switchUser", to);
		cookie.setPath("/");
		// Delete Cookie if empty switchUser
		if (StringUtils.isEmpty(to)) {
			cookie.setMaxAge(0);
		}

		response.addCookie(cookie);
		model.clear();
		final String referer = request.getHeader("referer");
		return "redirect:" + StringUtils.defaultIfBlank(referer, "/");
	}

	/**
	 * Get user list that current user will be shared, excluding current user.
	 *
	 * @param user  current user
	 * @param model model
	 */
	protected void attachCommonAttribute(User user, ModelMap model) {
		List list = user.getFollowers() == null ? Lists.newArrayList() : user.getFollowers();
		model.addAttribute("followers", Lists.transform(list, new Function<User, UserSearchResult>() {
			public UserSearchResult apply(User user) {
				return new UserSearchResult(user);
			}
		}));
		model.addAttribute("allowShareChange", true);
		model.addAttribute("userSecurityEnabled", config.isUserSecurityEnabled());
	}

	/**
	 * Check if the given user id already exists.
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

	/**
	 * Get users by the given role.
	 *
	 * @param role user role
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = {"/api/", "/api"}, method = RequestMethod.GET)
	public HttpEntity<String> getAll(Role role) {
		return toJsonHttpEntity(userService.getAll(role));
	}

	/**
	 * Get the user by the given user id.
	 *
	 * @param userId user id
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = "/api/{userId}", method = RequestMethod.GET)
	public HttpEntity<String> getOne(@PathVariable("userId") String userId) {
		return toJsonHttpEntity(userService.getOne(userId));
	}

	/**
	 * Create an user.
	 *
	 * @param newUser new user
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = {"/api/", "/api"}, method = RequestMethod.POST)
	public HttpEntity<String> create(@ModelAttribute("user") User newUser) {
		checkNull(newUser.getId(), "User DB ID should be null");
		return toJsonHttpEntity(save(newUser));
	}

	/**
	 * Update the user.
	 *
	 * @param userId user id
	 * @param update update user
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = "/api/{userId}", method = RequestMethod.PUT)
	public HttpEntity<String> update(@PathVariable("userId") String userId, User update) {
		update.setUserId(userId);
		checkNull(update.getId(), "User DB ID should be null");
		return toJsonHttpEntity(save(update));
	}

	/**
	 * Delete the user by the given userId.
	 *
	 * @param userId user id
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = "/api/{userId}", method = RequestMethod.DELETE)
	public HttpEntity<String> delete(User user, @PathVariable("userId") String userId) {
		if (!user.getUserId().equals(userId)) {
			userService.delete(userId);
		}
		return successJsonHttpEntity();
	}

	/**
	 * Search user list on the given keyword.
	 *
	 * @param pageable page info
	 * @param keywords search keyword.
	 * @return json message
	 */
	@RestAPI
	@RequestMapping(value = "/api/search", method = RequestMethod.GET)
	public HttpEntity<String> search(User user, @PageableDefault Pageable pageable,
	                                 @RequestParam(required = true) String keywords) {
		pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(),
			defaultIfNull(pageable.getSort(),
				new Sort(Direction.ASC, "userName")));
		Page<User> pagedUser = userService.getPagedAll(keywords, pageable);
		List<UserSearchResult> result = newArrayList();
		for (User each : pagedUser) {
			result.add(new UserSearchResult(each));
		}

		final String currentUserId = user.getUserId();
		CollectionUtils.filter(result, new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				UserSearchResult each = (UserSearchResult) object;
				return !(each.getId().equals(currentUserId) || each.getId().equals(ControllerConstants.NGRINDER_INITIAL_ADMIN_USERID));
			}
		});

		return toJsonHttpEntity(result);
	}

	public static class UserSearchResult {
		@Expose
		final private String id;

		@Expose
		final private String text;

		public UserSearchResult(User user) {
			id = user.getUserId();
			final String email = user.getEmail();
			final String userName = user.getUserName();
			if (StringUtils.isEmpty(email)) {
				this.text = userName + " (" + id + ")";
			} else {
				this.text = userName + " (" + email + " / " + id + ")";
			}
		}

		public String getText() {
			return text;
		}

		public String getId() {
			return id;
		}
	}
}
