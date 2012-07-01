package org.ngrinder.user.model;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.List;

import org.junit.Test;

import org.ngrinder.user.model.User;
import org.ngrinder.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

@ContextConfiguration("classpath:applicationContext.xml")
public class UserTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	public UserRepository userRepository;

	@Test
	public void testUser() {
		User user = new User();
		user.setName("MyName1");
		user.setEmail("junoyoon@gmail.com");
		user.setLastModifiedUser("hello");
		user.setCreateUser("hello");

		userRepository.save(user);
		User user2 = new User();
		user2.setName("MyName2");
		user2.setEmail("junoyoon@paran.com");
		user2.setLastModifiedUser("hello");
		user2.setCreateUser("hello");

		userRepository.save(user2);
		System.out.println(user2);

		assertThat(userRepository.count(), is(2L));
		List<User> findAll = userRepository.findAll(User.emailLike("gmail"));
		assertThat(findAll.size(), is(2));
		findAll = userRepository.findAll(Specifications.where(User.emailLike("@paran")).and(User.nameLike("MyName2")));

		assertThat(findAll.size(), is(1));

	}
}
