package org.ngrinder.infra.init;

import java.util.Date;

import javax.annotation.PostConstruct;

import org.ngrinder.infra.config.Config;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DBInit {
	@Autowired
	private UserRepository userRepository;

	@PostConstruct
	@Transactional
	public void init() {
		createDefaultUserIfNecessary();
	}

	private void createUser(String userId, String password, Role role, String userName, String email) {
		if (userRepository.findOneByUserId(userId) == null) {
			User adminUser = new User();
			adminUser.setUserId(userId);
			adminUser.setPassword(password);
			adminUser.setRole(role);
			adminUser.setUserName(userName);
			adminUser.setEmail(email);
			adminUser.setCreatedDate(new Date());
			userRepository.save(adminUser);
		}

	}

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
