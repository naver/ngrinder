package org.ngrinder.user.controller;

import static java.util.stream.Collectors.toList;
import static org.ngrinder.common.util.CollectionUtils.buildMap;
import static org.ngrinder.common.util.CollectionUtils.newArrayList;
import static org.ngrinder.common.util.ObjectUtils.defaultIfNull;
import static org.ngrinder.common.util.Preconditions.checkNotEmpty;

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
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/api")
public class UserApiController extends BaseController {
	public static final Sort DEFAULT_SORT = new Sort(Sort.Direction.ASC, "userName");

	@Autowired
	protected Config config;

	@Autowired
	private UserService userService;

	/**
	 * Get the follower list.
	 *
	 * @param user     current user
	 * @param keywords search keyword.
	 * @return json message
	 */
	@RestAPI
	@GetMapping("/switch_options")
	public HttpEntity<String> switchOptions(User user, @RequestParam(defaultValue = "") final String keywords) {
		return toJsonHttpEntity(getSwitchableUsers(user, keywords));
	}

	/**
	 * Get the current user profile.
	 *
	 * @param user  current user
	 */
	@RestAPI
	@GetMapping("/profile")
	public Map<String, Object> getOne(User user) {
		checkNotEmpty(user.getUserId(), "UserID should not be NULL!");
		Map<String, Object> model = new HashMap<>();
		User one = userService.getOneWithFollowers(user.getUserId());
		model.put("user", one);
		model.put("allowPasswordChange", !config.isDemo());
		model.put("allowRoleChange", false);
		model.put("showPasswordByDefault", false);
		attachCommonAttribute(one, model);
		return model;
	}

	/**
	 * Get user creation form page.
	 *
	 * @param user current user
	 * @return app
	 */
	@RequestMapping("/new")
	@PreAuthorize("hasAnyRole('A') or #user.userId == #userId")
	public Map<String, Object> openForm(User user) {
		User one = User.createNew();

		Map<String, Object> model = new HashMap<>(7);
		model.put("user", one);
		model.put("allowUserIdChange", true);
		model.put("allowPasswordChange", true);
		model.put("allowRoleChange", false);
		model.put("roleSet", EnumSet.allOf(Role.class));
		model.put("showPasswordByDefault", true);

		attachCommonAttribute(one, model);
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
	public Map<String, Object> getOne(@PathVariable final String userId) {
		User one = userService.getOneWithFollowers(userId);
		Map<String, Object> model = buildMap(
			"user", one,
			"allowPasswordChange", true,
			"allowRoleChange", true,
			"roleSet", EnumSet.allOf(Role.class),
			"showPasswordByDefault", false
		);
		attachCommonAttribute(one, model);
		return model;
	}


	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping({"/list", "/list/"})
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
	 * Get user list that current user will be shared, excluding current user.
	 *
	 * @param user  current user
	 * @param model model
	 */
	private void attachCommonAttribute(User user, Map<String, Object> model) {
		List<User> followers = user.getFollowers() == null ? Lists.newArrayList() : user.getFollowers();
		List<User> owners = user.getOwners() == null ? Lists.newArrayList() : user.getOwners();

		// TODO handle this when remove Gson.
		// prevent stack overflow when serialize user list.
		user.setFollowers(null);
		user.setOwners(null);

		model.put("followers", followers.stream().map(UserController.UserSearchResult::new).collect(toList()));
		model.put("owners", owners.stream().map(UserController.UserSearchResult::new).collect(toList()));
		model.put("allowShareChange", true);
		model.put("userSecurityEnabled", config.isUserSecurityEnabled());
	}

	private List<UserController.UserSearchResult> getSwitchableUsers(User user, String keywords) {
		if (user.getRole().hasPermission(Permission.SWITCH_TO_ANYONE)) {
			List<UserController.UserSearchResult> result = newArrayList();
			for (User each : userService.getPagedAll(keywords, PageRequest.of(0, 10))) {
				result.add(new UserController.UserSearchResult(each));
			}
			return result;
		} else {
			return userService.getSharedUser(user);
		}
	}
}
