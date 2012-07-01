package org.ngrinder;

import javax.sql.DataSource;

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
public class NGrinderIocTransactionalTestBase extends AbstractTransactionalJUnit4SpringContextTests {
	protected static final Logger LOG = LoggerFactory.getLogger(NGrinderIocTransactionalTestBase.class);

	@Autowired
	@Override
	public void setDataSource(@Qualifier("dataSource") DataSource dataSource) {
		super.setDataSource(dataSource);
	}

}
