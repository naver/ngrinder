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

import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.security.SecuredUser;
import org.ngrinder.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.dao.ReflectionSaltSource;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * user util
 *
 * @author Tobi
 * @date 2012-6-28
 */
@Profile("unit-test")
@Component
public class MockUserContext extends UserContext {
	public static final String TEST_USER_ID = "TEST_USER";
	public static final String TEST_USER_TIMEZONE_US = "America/New_York";
	public static final String TEST_USER_TIMEZONE_ZH = "Asia/Shanghai";

	@Autowired
	protected UserRepository userRepository;

	@Autowired
	private Config config;

	@PostConstruct
	public void init() {
		ReflectionSaltSource saltSource = new ReflectionSaltSource();
		saltSource.setUserPropertyToUse("username");

		boolean useEnhancedEncoding = config.getControllerProperties().getPropertyBoolean(ControllerConstants.PROP_CONTROLLER_USER_PASSWORD_SHA256);
		ShaPasswordEncoder passwordEncoder = useEnhancedEncoding ? new ShaPasswordEncoder(256) : new ShaPasswordEncoder();

		User user = userRepository.findOneByUserId(TEST_USER_ID);
		if (user == null) {
			user = new User();
			user.setUserId(TEST_USER_ID);
			user.setUserName("TEST_USER");
			user.setEmail("TEST_USER@nhn.com");
			user.setPassword("123");
			user.setRole(Role.USER);

			SecuredUser securedUser = new SecuredUser(user, null);
			String encodePassword = passwordEncoder.encodePassword(user.getPassword(), saltSource.getSalt(securedUser));
			user.setPassword(encodePassword);

			userRepository.save(user);
		}
	}

	public User getCurrentUser() {
		User user = userRepository.findOneByUserId(TEST_USER_ID);
		user.setUserLanguage("en");
		user.setTimeZone(TEST_USER_TIMEZONE_US);
		return user;
	}
}
