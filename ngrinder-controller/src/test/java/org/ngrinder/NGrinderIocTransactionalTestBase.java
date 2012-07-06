package org.ngrinder;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.ngrinder.user.model.Role;
import org.ngrinder.user.model.SecuredUser;
import org.ngrinder.user.model.User;
import org.ngrinder.user.repository.RoleRepository;
import org.ngrinder.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

/**
 * This class is used as base class for test case,and it will initialize the DB
 * related config, like datasource, and it will start a transaction for every
 * test function, and rollback after the execution.
 * 
 * @author Mavlarn
 * 
 */
@ContextConfiguration({ "classpath:applicationContext.xml" })
public class NGrinderIocTransactionalTestBase extends AbstractTransactionalJUnit4SpringContextTests {
	protected static final Logger LOG = LoggerFactory.getLogger(NGrinderIocTransactionalTestBase.class);

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RoleRepository roleRepository;
	
	private static final String TEST_USER_ID = "Test_User";
	
	@Autowired
	@Override
	public void setDataSource(@Qualifier("dataSource") DataSource dataSource) {
		super.setDataSource(dataSource);
	}

	@PostConstruct
	public void initTestUser () {
		User user = userRepository.findOneByUserId(TEST_USER_ID);
		if (user == null) {
			user = new User();
			user.setUserId("TEST_USER");
			user.setPsw("123");
			Role role = roleRepository.findOneByName("U");
			user.setRole(role);
			user.setUserLanguage("en");
			userRepository.save(user);
		}
		
		SecuredUser secUser = new SecuredUser(user);
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(secUser, null, secUser.getAuthorities()));
		LOG.info("Test User added for unit test:{}", secUser);
	}
}
