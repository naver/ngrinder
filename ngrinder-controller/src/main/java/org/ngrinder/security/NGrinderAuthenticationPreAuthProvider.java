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
package org.ngrinder.security;

import java.util.Date;
import java.util.HashMap;

import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.transaction.annotation.Transactional;

/**
 * nGrinder authentication provide. This class is for the plugin system of user authentication.
 * 
 * @author JunHo Yoon
 * @since 3.0
 * 
 */
public class NGrinderAuthenticationPreAuthProvider extends PreAuthenticatedAuthenticationProvider {

	protected static final Logger LOG = LoggerFactory.getLogger(NGrinderAuthenticationPreAuthProvider.class);

	private UserService userService;

	// ~ Methods
	// ========================================================================================================

	/**
	 * Authenticate the given PreAuthenticatedAuthenticationToken.
	 * <p>
	 * If the principal contained in the authentication object is null, the request will be ignored
	 * to allow other providers to authenticate it.
	 * 
	 * @param authentication
	 *            authentication
	 * @return authorized {@link Authentication}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Authentication authenticate(Authentication authentication) {
		Object details = authentication.getDetails();

		Authentication authenticate = super.authenticate(authentication);
		SecuredUser securedUser = (SecuredUser) authenticate.getPrincipal();
		if (details instanceof HashMap) {
			securedUser.getUser().setTimeZone(((HashMap<String, String>) details).get("user_timezone"));
			securedUser.getUser().setUserLanguage(((HashMap<String, String>) details).get("user_language"));
		} else if (details instanceof LanguageAndTimezone) {
			LanguageAndTimezone languageAndTimeZone = ((LanguageAndTimezone) details);
			securedUser.getUser().setTimeZone(languageAndTimeZone.getTimezone());
			securedUser.getUser().setUserLanguage(languageAndTimeZone.getLanguage());
		}
		// If It's the first time to login
		// means.. If the user info provider is not defaultLoginPlugin..
		if (securedUser.getUserInfoProviderClass() != null
						&& !isClassEqual(DefaultLoginPlugin.class, securedUser.getUserInfoProviderClass())) {

			addNewUserIntoLocal(securedUser);
		}
		return authenticate;
	}

	/**
	 * Check if given clazz has the given clazzName.
	 * 
	 * @param clazz
	 *            class
	 * @param clazzName
	 *            classname which is checked aginst
	 * @return true if same
	 */
	private boolean isClassEqual(Class<?> clazz, String clazzName) {
		return clazz.getName().equals(clazzName);
	}

	/**
	 * Add new user into local db.
	 * 
	 * @param securedUser
	 *            user
	 */
	@Transactional
	public void addNewUserIntoLocal(SecuredUser securedUser) {
		User user = securedUser.getUser();
		user.setAuthProviderClass(securedUser.getUserInfoProviderClass());
		user.setCreatedDate(new Date());
		User findOneByUserId = userService.getUserById(user.getUserId());
		if (findOneByUserId != null) {
			user = findOneByUserId.merge(user);
		}
		if (user.getRole() == null) {
			user.setRole(Role.USER);
		}
		User savedUser = userService.saveUser(user);
		securedUser.setUser(savedUser);
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

}
