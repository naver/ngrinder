package com.nhncorp.ngrinder.model;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import com.nhncorp.ngrinder.core.model.User;
import com.nhncorp.ngrinder.core.user.UserRepository;

@ContextConfiguration("classpath:applicationContext.xml")
public class UserTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	public UserRepository userRepository;

	@Test
	public void testUser() {
		User user = new User();
		user.setName("Hello World");
		user.setTelNum("01-6255-0222");
		userRepository.save(user);
		User user2 = new User();
		user2.setName("Hello World2");
		user2.setTelNum("01-6255-0222");
		userRepository.save(user2);
		System.out.println(user2);
		
		assertThat(userRepository.count(), is(2L));
		List<User> findAll = userRepository.findAll(User.telNumLike("01-625"));
		assertThat(findAll.size(), is(2));
		findAll = userRepository
				.findAll(Specifications.where(User.telNumLike("01-625")).and(User.nameEndWtih("World")));
		assertThat(findAll.size(), is(1));

	}
}
