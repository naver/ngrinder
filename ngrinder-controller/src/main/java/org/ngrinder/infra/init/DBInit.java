package org.ngrinder.infra.init;

import javax.annotation.PostConstruct;

import org.ngrinder.user.model.Role;
import org.ngrinder.user.model.User;
import org.ngrinder.user.repository.RoleRepository;
import org.ngrinder.user.repository.UserRepository;
import org.ngrinder.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DBInit {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserService userService;

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
			user.setPsw("admin");
			Role userRole = new Role("A");
			userRole = roleRepository.save(userRole);
			user.setRole(userRole);
			userService.saveUser(user);

			User user2 = new User();
			user2.setUserId("user");
			user2.setPsw("user");
			Role userRole2 = new Role("U");
			userRole2 = roleRepository.save(userRole2);
			user2.setRole(userRole2);
			userService.saveUser(user2);

			Role superUser = new Role("S");
			roleRepository.save(superUser);
		}
	}
}
