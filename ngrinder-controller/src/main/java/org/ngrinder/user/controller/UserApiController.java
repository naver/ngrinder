package org.ngrinder.user.controller;

import static java.util.stream.Collectors.toList;
import static org.ngrinder.common.constant.ControllerConstants.NGRINDER_INITIAL_ADMIN_USERID;
import static org.ngrinder.common.constant.WebConstants.JSON_SUCCESS;
import static org.ngrinder.common.util.CollectionUtils.buildMap;
import static org.ngrinder.common.util.ObjectUtils.defaultIfNull;
import static org.ngrinder.common.util.Preconditions.*;

import lombok.RequiredArgsConstructor;

import org.apache.commons.lang.StringUtils;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.Permission;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/api")
@RequiredArgsConstructor
public class UserApiController {
	private static final Sort DEFAULT_SORT = new Sort(Sort.Direction.ASC, "userName");

	private final Config config;

	private final UserService userService;

	/**
	 * Get the follower list.
	 *
	 * @param user     current user
	 * @param keywords search keyword.
	 * @return json message
	 */
	@GetMapping("/switch_options")
	public List<User> switchOptions(User user, @RequestParam(defaultValue = "") final String keywords) {
		if (user.getRole().hasPermission(Permission.SWITCH_TO_ANYONE)) {
			return userService.getPagedAll(keywords, PageRequest.of(0, 10)).getContent();
		} else {
			return userService.getSharedUser(user);
		}
	}

	/**
	 * Get the current user profile.
	 *
	 * @param user  current user
	 */
	@GetMapping("/profile")
	public Map<String, Object> getOne(User user) {
		checkNotEmpty(user.getUserId(), "UserID should not be NULL!");
		User one = userService.getOneWithFollowers(user.getUserId());

		Map<String, Object> viewConfig = new HashMap<>();
		viewConfig.put("allowPasswordChange", !config.isDemo());
		viewConfig.put("allowRoleChange", false);
		viewConfig.put("allowShareChange", true);
		viewConfig.put("showPasswordByDefault", false);
		viewConfig.put("userSecurityEnabled", config.isUserSecurityEnabled());

		Map<String, Object> model = new HashMap<>();
		model.put("user", one);
		model.put("config", viewConfig);

		return model;
	}

	/**
	 * Get user creation form page.
	 *
	 * @param user current user
	 * @return app
	 */
	@GetMapping("/new")
	@PreAuthorize("hasAnyRole('A') or #user.userId == #userId")
	public Map<String, Object> openForm(User user) {
		User one = User.createNew();

		Map<String, Object> viewConfig = new HashMap<>();
		viewConfig.put("allowUserIdChange", true);
		viewConfig.put("allowPasswordChange", true);
		viewConfig.put("allowRoleChange", false);
		viewConfig.put("roleSet", EnumSet.allOf(Role.class));
		viewConfig.put("allowShareChange", true);
		viewConfig.put("showPasswordByDefault", true);
		viewConfig.put("userSecurityEnabled", config.isUserSecurityEnabled());

		Map<String, Object> model = new HashMap<>();
		model.put("user", one);
		model.put("config", viewConfig);
		return model;
	}

	/**
	 * Get user detail.
	 *
	 * @param userId user to get
	 * @return user details
	 */
	@GetMapping("/{userId}/detail")
	@PreAuthorize("hasAnyRole('A')")
	public Map<String, Object> getOneDetail(@PathVariable final String userId) {
		User one = userService.getOneWithFollowers(userId);

		Map<String, Object> viewConfig = new HashMap<>();
		viewConfig.put("allowPasswordChange", true);
		viewConfig.put("allowRoleChange", true);
		viewConfig.put("roleSet", EnumSet.allOf(Role.class));
		viewConfig.put("showPasswordByDefault", false);
		viewConfig.put("allowShareChange", true);
		viewConfig.put("userSecurityEnabled", config.isUserSecurityEnabled());


		Map<String, Object> model = new HashMap<>();
		model.put("user", one);
		model.put("config", viewConfig);
		return model;
	}

	@GetMapping({"/list", "/list/"})
	@PreAuthorize("hasAnyRole('A')")
	public Page<User> getAll(@RequestParam(required = false) Role role,
							 @PageableDefault(page = 0, size = 10) Pageable pageable,
							 @RequestParam(required = false) String keywords) {
		pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), defaultIfNull(pageable.getSort(), DEFAULT_SORT));
		Pageable defaultPageable = PageRequest.of(0, pageable.getPageSize(), defaultIfNull(pageable.getSort(), DEFAULT_SORT));
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
		}

		return pagedUser;
	}

	/**
	 * Save or Update user detail info.
	 *
	 * @param user        current user
	 * @param updatedUser user to be updated.
	 */
	@PostMapping("/save")
	@PreAuthorize("hasAnyRole('A') or #user.id == #updatedUser.id")
	public void save(User user, @RequestBody User updatedUser) {
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
	}

	private User save(User user) {
		if (StringUtils.isBlank(user.getPassword())) {
			return userService.saveWithoutPasswordEncoding(user);
		} else {
			return userService.save(user);
		}
	}


	@GetMapping({"/role", "/role/"})
	@PreAuthorize("hasAnyRole('A')")
	public EnumSet<Role> roleSet() {
		return EnumSet.allOf(Role.class);
	}

	/**
	 * Delete users.
	 *
	 * @param userIds comma separated user ids.
	 * @return "redirect:/user/"
	 */
	@DeleteMapping({"", "/"})
	@PreAuthorize("hasAnyRole('A')")
	public void deleteUsers(User user, @RequestParam String userIds) {
		String[] ids = userIds.split(",");
		for (String eachId : ids) {
			if (!user.getUserId().equals(eachId)) {
				userService.delete(eachId);
			}
		}
	}

	/**
	 * Check if the given user id already exists.
	 *
	 * @param userId userId to be checked
	 * @return success json if true.
	 */
	@GetMapping("/{userId}/check_duplication")
	@PreAuthorize("hasAnyRole('A')")
	public Map<String, Object> checkDuplication(@PathVariable String userId) {
		User user = userService.getOne(userId);
		return buildMap(JSON_SUCCESS, user == null);
	}

	/**
	 * Get users by the given role.
	 *
	 * @param role user role
	 * @return json message
	 */
	@GetMapping({"/", ""})
	@PreAuthorize("hasAnyRole('A')")
	public List<User> getAll(Role role) {
		return userService.getAll(role);
	}

	/**
	 * Get the user by the given user id.
	 *
	 * @param userId user id
	 * @return json message
	 */
	@GetMapping("/{userId}")
	@PreAuthorize("hasAnyRole('A')")
	public User getOne(@PathVariable("userId") String userId) {
		return userService.getOne(userId);
	}

	/**
	 * Create an user.
	 *
	 * @param newUser new user
	 * @return json message
	 */
	@PostMapping({"/", ""})
	@PreAuthorize("hasAnyRole('A')")
	public User create(@ModelAttribute("user") User newUser) {
		checkNull(newUser.getId(), "User DB ID should be null");
		return save(newUser);
	}

	/**
	 * Update the user.
	 *
	 * @param userId user id
	 * @param update update user
	 * @return json message
	 */
	@PutMapping("/{userId}")
	@PreAuthorize("hasAnyRole('A')")
	public User update(@PathVariable("userId") String userId, User update) {
		update.setUserId(userId);
		checkNull(update.getId(), "User DB ID should be null");
		return save(update);
	}

	/**
	 * Delete the user by the given userId.
	 *
	 * @param userId user id
	 * @return json message
	 */
	@DeleteMapping("/{userId}")
	@PreAuthorize("hasAnyRole('A')")
	public void delete(User user, @PathVariable("userId") String userId) {
		if (!user.getUserId().equals(userId)) {
			userService.delete(userId);
		}
	}

	/**
	 * Search user list on the given keyword.
	 *
	 * @param pageable page info
	 * @param keywords search keyword.
	 * @return json message
	 */
	@GetMapping("/search")
	public List<User> search(User user,
							 @PageableDefault Pageable pageable,
							 @RequestParam String keywords) {
		pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), defaultIfNull(pageable.getSort(), DEFAULT_SORT));
		Page<User> pagedUsers = userService.getPagedAll(keywords, pageable);
		final String currentUserId = user.getUserId();

		return pagedUsers.stream()
			.filter(each -> !each.getUserId().equals(currentUserId))
			.filter(each -> !each.getUserId().equals(NGRINDER_INITIAL_ADMIN_USERID))
			.collect(toList());
	}
}
