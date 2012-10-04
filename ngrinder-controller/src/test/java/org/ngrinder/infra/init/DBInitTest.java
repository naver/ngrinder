package org.ngrinder.infra.init;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.User;
import org.ngrinder.perftest.repository.PerfTestRepository;
import org.ngrinder.perftest.repository.TagRepository;
import org.ngrinder.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class DBInitTest extends org.ngrinder.AbstractNGrinderTransactionalTest {
	@Autowired
	private DBInit dbInit;

	@Autowired
	private UserRepository userRepository;

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
	public void initUserDB() {
		dbInit.init(); 
		List<User> users = userRepository.findAll();
		
		// Two users should be exist
		assertThat(users.size(), is(4));
		assertThat(users.get(0).getUserId(), is("admin"));
		assertThat(users.get(1).getUserId(), is("user"));
	}
}
