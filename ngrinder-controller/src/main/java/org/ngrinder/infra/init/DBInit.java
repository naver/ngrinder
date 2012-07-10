package org.ngrinder.infra.init;

import javax.annotation.PostConstruct;

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

	private void createDefaultUserIfNecessary() {
		// If there is no users.. make admin and user and U, S, A roles.
		if (userRepository.count() == 0) {
			User adminUser = new User();
			adminUser.setUserId("admin");
			adminUser.setPassword("admin");
			adminUser.setRole(Role.ADMIN);
			userRepository.save(adminUser);

			User generalUser = new User();
			generalUser.setUserId("user");
			generalUser.setPassword("user");
			generalUser.setRole(Role.USER);
			userRepository.save(generalUser);
		}
	}
}
