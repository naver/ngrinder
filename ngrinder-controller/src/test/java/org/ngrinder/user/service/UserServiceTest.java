package org.ngrinder.user.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.ngrinder.NGrinderIocTransactionalTestBase;
import org.ngrinder.user.model.Role;
import org.ngrinder.user.model.User;
import org.ngrinder.user.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class UserServiceTest extends NGrinderIocTransactionalTestBase {

	@Autowired
	private UserService userService;

	@Autowired
	private RoleRepository roleRepository;

	@Before
	public void before() {

	}

	@Test
	public void testUserCreation() {
		User user = new User();
		user.setUserId("hello");
		user.setPsw("www");
		user = userService.saveUser(user);
		assertThat(user.getUserId(), is("hello"));
	}

	@Test
	public void testGroupTest() {
		User user = new User();
		user.setUserId("hello");
		user.setPsw("www");
		Role userRole = new Role("service");

		user.setRole(roleRepository.save(userRole));
		userService.saveUser(user);

		User user2 = new User();
		user2.setUserId("hello2");
		user2.setPsw("www2");
		Role userRole2 = roleRepository.save(new Role("admin"));
		user2.setRole(userRole2);
		userService.saveUser(user2);

		Map<String, List<User>> allUserInGroup = userService.getAllUserInGroup();

		assertThat(allUserInGroup.size(), is(2));

		List<User> userListByRole = userService.getUserListByRole("admin");
		assertThat(userListByRole.size(), is(1));

	}

	@Test
	public void testUpdateUser() {
		User user = new User();
		user.setUserId("hello");
		user.setPsw("www");
		Role userRole = new Role("service");

		user.setRole(roleRepository.save(userRole));
		user = userService.saveUser(user);

		User user2 = new User();
		user2.setUserId("hello");
		user2.setPsw("www222");

		userService.modifyUser(user2);
		User userById = userService.getUserById("hello");
		assertThat(userById.getPsw(), is("www222"));

		assertThat(userById.getId(), is(user.getId()));
	}
}
