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

import lombok.RequiredArgsConstructor;

import org.apache.commons.lang.StringUtils;
import org.ngrinder.extension.OnLoginRunnable;
import org.ngrinder.model.User;
import org.ngrinder.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.crypto.password.ShaPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * The default login plugin.
 *
 * This retrieves the user
 */
@Service
@RequiredArgsConstructor
public class DefaultLoginPlugin implements OnLoginRunnable {

	protected static final Logger LOG = LoggerFactory.getLogger(DefaultLoginPlugin.class);

	private static final MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

	private final UserService userService;

	@Override
	public User loadUser(String userId) {
		User user = userService.getOne(userId);
		if (user != null) {
			user.setAuthProviderClass(this.getClass().getName());
		}
		return user;
	}

	@Override
	public boolean validateUser(String userId, String password, String encPass, Object encoder, Object salt) {
		if (StringUtils.isEmpty(password) || !((ShaPasswordEncoder) encoder).matches(String.valueOf(salt), password, encPass)) {
			LOG.debug("Authentication failed: password does not match stored value");

			throw new BadCredentialsException(messages.getMessage(
					"AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
		}
		return true;
	}

	@Deprecated
	@Override
	public void saveUser(User user) {
		// Do nothing for default plugin
	}
}
