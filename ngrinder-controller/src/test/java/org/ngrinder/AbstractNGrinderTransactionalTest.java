package org.ngrinder;

import javax.sql.DataSource;

import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.model.User;
import org.ngrinder.user.repository.UserRepository;
import org.ngrinder.user.service.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
abstract public class AbstractNGrinderTransactionalTest extends AbstractTransactionalJUnit4SpringContextTests
		implements NGrinderConstants {
	protected static final Logger LOG = LoggerFactory.getLogger(AbstractNGrinderTransactionalTest.class);

	@Autowired
	protected UserRepository userRepository;

	protected User testUser = null;
	
	@Autowired
	private UserContext userContext;
	
	@Autowired
	@Override
	public void setDataSource(@Qualifier("dataSource") DataSource dataSource) {
		super.setDataSource(dataSource);
	}

	public User getUser(String userId) {
		return userRepository.findOneByUserId(userId);
	}

	public User getTestUser() {
		if (testUser == null) {
			testUser = userContext.getCurrentUser();
		}
		return testUser;
	}
	
	public User getAdminUser() {
		return userRepository.findOneByUserId("admin");
	}

	public void sleep(long miliseconds) {
		try {
			Thread.sleep(miliseconds);
		} catch (InterruptedException e) {
			LOG.error("error:", e);
		}
	}

}
