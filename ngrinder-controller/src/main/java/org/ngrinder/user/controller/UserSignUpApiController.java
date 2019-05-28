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


import org.ngrinder.common.controller.RestAPI;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static org.ngrinder.common.util.Preconditions.checkTrue;

/**
 * User sign up api controller.
 * @since 3.5.0
 */
@RestController
@RequestMapping("/sign_up/api")
public class UserSignUpApiController extends UserController {

	@Autowired
	private Config config;

	/**
	 * New user sign up form login page.
	 */
	@RestAPI
	@GetMapping("/new")
	public HttpEntity<String> signUpInfo() {
		Map<String, Object> model = new HashMap<>();
		checkTrue(config.isSignUpEnabled(), "Access to this url is not allowed when sign up is disabled");
		User one = User.createNew();
		model.put("user", one);
		model.put("allowUserIdChange", true);
		model.put("allowPasswordChange", true);
		model.put("allowRoleChange", false);
		model.put("roleSet", EnumSet.allOf(Role.class));
		model.put("userSecurityEnabled", config.isUserSecurityEnabled());
		model.put("allowShareChange", false);
		model.put("showPasswordByDefault", true);
		model.put("newUser", true);
		return toJsonHttpEntity(model);
	}
}
