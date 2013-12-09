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
package org.ngrinder.user.service;

import org.ngrinder.model.User;
import org.ngrinder.security.SecuredUser;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * User Context which return current user.
 *
 * @author Tobi
 * @author JunHo Yoon
 * @since 3.0
 */
@Profile("production")
@Component
public class UserContext {


	/**
	 * Get current user object from context.
	 *
	 * @return current user;
	 */
	public User getCurrentUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			throw new AuthenticationCredentialsNotFoundException("No authentication");
		}
		Object obj = auth.getPrincipal();
		if (!(obj instanceof SecuredUser)) {
			throw new AuthenticationCredentialsNotFoundException("Invalid authentication with " + obj);
		}
		SecuredUser securedUser = (SecuredUser) obj;
		return securedUser.getUser();
	}
}
