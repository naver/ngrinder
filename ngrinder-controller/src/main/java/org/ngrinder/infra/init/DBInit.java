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
			User user = new User();
			user.setUserId("admin");
			user.setPassword("admin");
			user.setRole(Role.ADMIN);
			userRepository.save(user);

			User user2 = new User();
			user2.setUserId("user");
			user2.setPassword("user");
			user2.setRole(Role.USER);
			userRepository.save(user2);
		}
	}
}
