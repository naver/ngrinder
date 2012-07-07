package org.ngrinder;

import javax.sql.DataSource;

import org.ngrinder.model.User;
import org.ngrinder.user.repository.UserRepository;
import org.ngrinder.user.service.MockUserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

/**
 * This class is used as base class for test case,and it will initialize the DB related config, like datasource, and it
 * will start a transaction for every test function, and rollback after the execution.
 * 
 * @author Mavlarn
 * 
 */
@ContextConfiguration({ "classpath:applicationContext.xml" })
abstract public class AbstractNGNinderTransactionalTest extends AbstractTransactionalJUnit4SpringContextTests {
	protected static final Logger LOG = LoggerFactory.getLogger(AbstractNGNinderTransactionalTest.class);

	@Autowired
	protected UserRepository userRepository;

	@Autowired
	@Override
	public void setDataSource(@Qualifier("dataSource") DataSource dataSource) {
		super.setDataSource(dataSource);
	}

	public User getUser(String userId) {
		return userRepository.findOneByUserId(userId);
	}

	public User getTestUser() {
		return getUser(MockUserContext.TEST_USER_ID);
	}

}
