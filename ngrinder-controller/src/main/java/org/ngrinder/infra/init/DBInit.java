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

import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.security.SecuredUser;
import org.ngrinder.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Date;

/**
 * Database Initialization.
 * When the first boot-up, some data(ex: user account) should be inserted into DB.
 *
 * And... It's the perfect place to upgrade DB.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
@Service
public class DBInit {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private Config config;

	@Autowired
	private SaltSource saltSource;

	@Autowired
	private ShaPasswordEncoder passwordEncoder;

	@Autowired
	private FileEntryService fileEntryService;

	/**
	 * Initialize DB.
	 */
	@PostConstruct
	@Transactional
	public void init() {
		createDefaultUserIfNecessary();
		resetAdminPasswordIfNecessary();
	}

	private void resetAdminPasswordIfNecessary() {
		if (config.getControllerProperties().getPropertyBoolean(ControllerConstants
				.PROP_CONTROLLER_ADMIN_PASSWORD_RESET)) {
			final User admin = userRepository.findOneByUserId("admin");
			if (admin == null) {
				createUser("admin", "admin", Role.ADMIN, "admin", "admin@nhn.com");
			} else {
				SecuredUser securedUser = new SecuredUser(admin, null);
				Object salt = saltSource.getSalt(securedUser);
				admin.setRole(Role.ADMIN);
				admin.setPassword(passwordEncoder.encodePassword("admin", salt));
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
			SecuredUser securedUser = new SecuredUser(user, null);
			Object salt = saltSource.getSalt(securedUser);
			user.setPassword(passwordEncoder.encodePassword(password, salt));
			user.setRole(role);
			user.setUserName(userName);
			user.setEmail(email);
			user.setCreatedDate(new Date());
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
			createUser("admin", "admin", Role.ADMIN, "admin", "admin@nhn.com");
			createUser("user", "user", Role.USER, "user", "user@nhn.com");
			createUser("superuser", "superuser", Role.SUPER_USER, "superuser", "superuser@nhn.com");
			createUser("system", "system", Role.SYSTEM_USER, "system", "system@nhn.com");
		}
	}
}
