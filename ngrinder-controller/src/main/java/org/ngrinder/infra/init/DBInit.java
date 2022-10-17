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
package org.ngrinder.infra.init;

import org.ngrinder.infra.config.Config;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.user.repository.UserRepository;
import org.springframework.security.crypto.password.ShaPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;

import static java.time.Instant.now;
import static org.ngrinder.common.constant.ControllerConstants.PROP_CONTROLLER_ADMIN_PASSWORD_RESET;
import static org.ngrinder.model.Role.*;

import lombok.RequiredArgsConstructor;

/**
 * Database Initialization.
 * When the first boot-up, some data(ex: user account) should be inserted into DB.
 *
 * And... It's the perfect place to upgrade DB.
 *
 * @since 3.0
 */
@Component
@RequiredArgsConstructor
public class DBInit {

	private final UserRepository userRepository;

	private final Config config;

	private final ShaPasswordEncoder passwordEncoder;

	private final FileEntryService fileEntryService;

	/**
	 * Initialize DB.
	 */
	@PostConstruct
	public void init() {
		createDefaultUserIfNecessary();
		resetAdminPasswordIfNecessary();
	}

	private void resetAdminPasswordIfNecessary() {
		if (config.getControllerProperties().getPropertyBoolean(PROP_CONTROLLER_ADMIN_PASSWORD_RESET)) {
			User admin = userRepository.findOneByUserId("admin");
			if (admin == null) {
				createUser("admin", "admin", ADMIN, "admin", "ngrinder-admin@naver.com");
			} else {
				admin.setRole(ADMIN);
				admin.setPassword(passwordEncoder.encode("admin", "admin"));
				userRepository.saveAndFlush(admin);
			}
		}
	}

	/**
	 * Create users.
	 *
	 * @param userId   userId
	 * @param password raw user password
	 * @param role     role
	 * @param userName user name
	 * @param email    email
	 */
	private void createUser(String userId, String password, Role role, String userName, String email) {
		if (userRepository.findOneByUserId(userId) == null) {
			User user = new User();
			user.setUserId(userId);
			user.setPassword(passwordEncoder.encode(user.getUserId(), password));
			user.setRole(role);
			user.setUserName(userName);
			user.setEmail(email);

			Instant now = now();
			user.setCreatedAt(now);
			user.setLastModifiedAt(now);
			user.setCreatedBy(user);
			user.setLastModifiedBy(user);

			user = userRepository.save(user);
			fileEntryService.prepare(user);
		}
	}

	/**
	 * Create initial users.
	 */
	private void createDefaultUserIfNecessary() {
		// If there is no users.. make admin and user and U, S, A roles.
		if (userRepository.count() < 2) {
			createUser("admin", "admin", ADMIN, "admin", "ngrinder-admin@naver.com");
			createUser("superuser", "superuser", SUPER_USER, "superuser", "ngrinder-superuser@naver.com");
			createUser("system", "system", SYSTEM_USER, "system", "ngrinder-system@naver.com");
			createUser("user", "user", USER, "user", "user@naver.com");
		}
	}
}
