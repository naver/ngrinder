package org.ngrinder.user.model;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.perftest.repository.PerfTestRepository;
import org.ngrinder.perftest.repository.TagRepository;
import org.ngrinder.user.repository.UserRepository;
import org.ngrinder.user.repository.UserSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specifications;

public class UserTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	public UserRepository userRepository;
	@Autowired

	private PerfTestRepository perfTestRepository;

	@Autowired
	private TagRepository tagRepository;

	
	@Before
	public void before() {
		List<PerfTest> findAll = perfTestRepository.findAll();
		for (PerfTest perfTest : findAll) {
			perfTest.getTags().clear();
		}
		perfTestRepository.save(findAll);
		perfTestRepository.flush();
		perfTestRepository.deleteAll();
		tagRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	public void testUser() {
		User user = new User();
		user.setUserName("MyName1");
		user.setEmail("junoyoon@gmail.com");
		user.setCreatedUser(getUser("user"));
		user.setCreatedDate(new Date());
		user.setUserId("hello");
		user.setRole(Role.USER);
		userRepository.save(user);
		User user2 = new User();

		user2.setUserId("hello2");
		user2.setUserName("MyName2");
		user2.setEmail("junoyoon@paran.com");
		user2.setCreatedUser(getUser("user"));
		user2.setCreatedDate(new Date());
		user2.setRole(Role.USER);
		userRepository.save(user2);

		assertThat(userRepository.count(), is(2L));

		assertThat(userRepository.findAll(UserSpecification.emailLike("gmail")).size(), is(1));

		assertThat(userRepository.findAll(
						Specifications.where(UserSpecification.emailLike("@paran")).and(
										UserSpecification.nameLike("MyName2"))).size(), is(1));

	}
}
