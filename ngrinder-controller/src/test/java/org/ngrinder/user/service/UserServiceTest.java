package org.ngrinder.user.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGNinderTransactionalTest;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.springframework.beans.factory.annotation.Autowired;

public class UserServiceTest extends AbstractNGNinderTransactionalTest {

	@Autowired
	private UserService userService;

	@Before
	public void before() {

	}

	@Test
	public void testUserCreation() {
		User user = new User();
		user.setUserId("hello");
		user.setPassword("www");
		user = userService.saveUser(user);
		assertThat(user.getUserId(), is("hello"));
	}

	@Test
	public void testGroupTest() {
		userRepository.deleteAll();
		User user = new User();
		user.setUserId("hello");
		user.setPassword("www");
		user.setRole(Role.SUPER_USER);
		userService.saveUser(user);

		User user2 = new User();
		user2.setUserId("hello2");
		user2.setPassword("www2");
		user2.setRole(Role.ADMIN);
		userService.saveUser(user2);

		Map<Role, List<User>> allUserInGroup = userService.getUserInGroupFromList();

		assertThat(allUserInGroup.size(), is(2));

		List<User> userListByRole = userService.getUserListByRole(Role.ADMIN);
		assertThat(userListByRole.size(), is(1));

	}

	@Test
	public void testUpdateUser() {
		User user = new User();
		user.setUserId("hello");
		user.setPassword("www");
		user.setRole(Role.SUPER_USER);
		user = userService.saveUser(user);

		User user2 = new User();
		user2.setUserId("hello");
		user2.setPassword("www222");
		user2.setRole(Role.USER);
		userService.modifyUser(user2);
		User userById = userService.getUserById("hello");
		assertThat(userById.getPassword(), is("www222"));

		assertThat(userById.getId(), is(user.getId()));
	}
}
