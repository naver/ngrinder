package org.ngrinder.user.controller;

import com.google.common.collect.Lists;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.common.controller.RestAPI;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.Permission;
import org.ngrinder.model.User;
import org.ngrinder.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.ngrinder.common.util.CollectionUtils.newArrayList;
import static org.ngrinder.common.util.Preconditions.checkNotEmpty;

@RestController
@RequestMapping("/user/api")
public class UserApiController extends BaseController {

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
	public HttpEntity<String> getOne(User user) {
		checkNotEmpty(user.getUserId(), "UserID should not be NULL!");
		Map<String, Object> model = new HashMap<>();
		User one = userService.getOneWithFollowers(user.getUserId());
		model.put("user", one);
		model.put("allowPasswordChange", !config.isDemo());
		model.put("allowRoleChange", false);
		model.put("showPasswordByDefault", false);
		attachCommonAttribute(one, model);
		return toJsonHttpEntity(model);
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

		model.put("followers", followers.stream().map(UserController.UserSearchResult::new).collect(Collectors.toList()));
		model.put("owners", owners.stream().map(UserController.UserSearchResult::new).collect(Collectors.toList()));
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
