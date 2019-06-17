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

import static org.ngrinder.common.util.ObjectUtils.defaultIfNull;

import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
	private static final Sort DEFAULT_SORT = new Sort(Sort.Direction.ASC, "userName");

	@Autowired
	private UserService userService;

	@Autowired
	protected Config config;

	/**
	 * Get user list page.
	 *
	 * @return app
	 */
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping({"", "/"})
	public String userList() {
		return "app";
	}

	/**
	 * Get user creation form page.
	 *
	 * @return app
	 */
	@RequestMapping("/new")
	@PreAuthorize("hasAnyRole('A') or #user.userId == #userId")
	public String openForm() {
		return "app";
	}

	/**
	 * Get user detail page.
	 *
	 * @param userId user to get
	 * @return app
	 */
	@RequestMapping("/{userId}")
	@PreAuthorize("hasAnyRole('A')")
	public String userDetail(@PathVariable final String userId) {
		return "app";
	}

	/**
	 * Switch user identity.
	 *
	 * @param to       the user to whom a user will switch
	 * @param response response
	 * @return redirect:/perftest/
	 */
	@RequestMapping("/switch")
	public String switchUser(@RequestParam(defaultValue = "") String to,
	                         HttpServletRequest request, HttpServletResponse response) {
		userService.evictUserCacheById(to);
		Cookie cookie = new Cookie("switchUser", to);
		cookie.setPath("/");
		// Delete Cookie if empty switchUser
		if (StringUtils.isEmpty(to)) {
			cookie.setMaxAge(0);
		}
		response.addCookie(cookie);
		return "redirect:" + StringUtils.defaultIfBlank(request.getHeader("referer"), "/");
	}
}
