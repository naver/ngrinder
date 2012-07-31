package org.ngrinder.user.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.springframework.beans.factory.annotation.Autowired;

public class UserServiceTest extends AbstractNGrinderTransactionalTest {

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
		user.setRole(Role.USER);
		user = userService.saveUser(user);
		assertThat(user.getUserId(), is("hello"));
	}

	@Test
	public void testUpdateUser() {
		User user = new User();
		user.setUserId("hello");
		user.setPassword("www");
		user.setRole(Role.SUPER_USER);
		user = userService.saveUser(user);

		User user2 = new User();
		user2.setId(user.getId());
		user2.setUserId("hello");
		user2.setPassword("www222");
		user2.setRole(Role.USER);
		userService.modifyUser(user2);
		User userById = userService.getUserById("hello");
		assertThat(userById.getPassword(), is("www222"));

		assertThat(userById.getId(), is(user.getId()));
	}
}
