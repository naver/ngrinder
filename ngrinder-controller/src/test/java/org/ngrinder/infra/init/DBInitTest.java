package org.ngrinder.infra.init;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ngrinder.model.User;
import org.ngrinder.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class DBInitTest extends org.ngrinder.AbstractNGrinderTransactionalTest {
	@Autowired
	private DBInit dbInit;

	@Autowired
	private UserRepository userReoRepository;

	@Before
	public void before() {
		userReoRepository.deleteAll();
	}

	@Test
	public void initUserDB() {
		dbInit.init(); 
		List<User> users = userReoRepository.findAll();
		
		// Two users should be exist
		assertThat(users.size(), is(4));
		assertThat(users.get(0).getUserId(), is("admin"));
		assertThat(users.get(0).getPassword(), is("admin"));
		assertThat(users.get(1).getUserId(), is("user"));
		assertThat(users.get(1).getPassword(), is("user"));
	}
}
