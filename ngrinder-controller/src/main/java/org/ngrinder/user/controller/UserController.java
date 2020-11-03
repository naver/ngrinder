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

import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.controller.annotation.GlobalControllerModel;
import org.ngrinder.model.User;
import org.ngrinder.user.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

/**
 * User management controller.
 *
 * @since 3.0
 */
@SuppressWarnings({"unused", "SpringMVCViewInspection"})
@Controller
@RequestMapping("/user")
@GlobalControllerModel
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	/**
	 * Get user list page.
	 *
	 * @return app
	 */
	@GetMapping({"", "/"})
	@PreAuthorize("hasAnyRole('A')")
	public String userList(User user) {
		return "app";
	}

	/**
	 * Get user creation form page.
	 *
	 * @return app
	 */
	@SuppressWarnings({"SpringElInspection"})
	@GetMapping("/new")
	@PreAuthorize("hasAnyRole('A') or #user.userId == #userId")
	public String openForm(User user) {
		return "app";
	}

	/**
	 * Get user detail page.
	 *
	 * @param user   current user
	 * @param userId user to get
	 * @return app
	 */
	@GetMapping("/{userId}")
	@PreAuthorize("hasAnyRole('A')")
	public String userDetail(User user, @PathVariable final String userId) {
		return "app";
	}

	/**
	 * Switch user identity.
	 *
	 * @param user     current user
	 * @param to       the user to whom a user will switch
	 * @param response response
	 * @return redirect:/perftest/
	 */
	@GetMapping("/switch")
	public String switchUser(User user,
							 @RequestParam(defaultValue = "") String to,
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
