package org.ngrinder.user.controller;

import org.ngrinder.common.controller.BaseController;
import org.ngrinder.common.controller.RestAPI;
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

import java.util.List;

import static org.ngrinder.common.util.CollectionUtils.newArrayList;

@RestController
@RequestMapping("/user/api")
public class UserApiController extends BaseController {

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
