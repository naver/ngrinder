/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.ngrinder;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.sql.DataSource;

import net.grinder.AgentControllerDaemon;
import net.grinder.communication.AgentControllerCommunicationDefauts;

import org.junit.Before;
import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.infra.AgentConfig;
import org.ngrinder.infra.config.MockAgentConfigInControllerSide;
import org.ngrinder.model.User;
import org.ngrinder.monitor.MonitorConstants;
import org.ngrinder.monitor.agent.AgentMonitorServer;
import org.ngrinder.user.repository.UserRepository;
import org.ngrinder.user.service.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
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
abstract public class AbstractNGrinderTransactionalTest extends AbstractTransactionalJUnit4SpringContextTests implements
		NGrinderConstants {
	protected static final Logger LOG = LoggerFactory.getLogger(AbstractNGrinderTransactionalTest.class);

	@Autowired
	protected UserRepository userRepository;

	protected User testUser = null;

	static {
		setupSigar();
		LOG.info("* Start nGrinder Agent *");
		AgentConfig agentConfig = new MockAgentConfigInControllerSide(1).init();
		AgentControllerDaemon agentControllerDaemon = new AgentControllerDaemon("127.0.0.1");
		agentControllerDaemon.setAgentConfig(agentConfig);
		agentControllerDaemon.run(AgentControllerCommunicationDefauts.DEFAULT_AGENT_CONTROLLER_SERVER_PORT);

		LOG.info("* Start nGrinder Monitor *");
		try {
			Set<String> collector = MonitorConstants.SYSTEM_DATA_COLLECTOR;
			AgentMonitorServer.getInstance().init(MonitorConstants.DEFAULT_MONITOR_PORT, collector, agentConfig);
			AgentMonitorServer.getInstance().start();
		} catch (Exception e) {
			LOG.error("ERROR: {}", e.getMessage());
			LOG.debug("Error while starting Monitor", e);
		}
	}

	private static void setupSigar() {
		try {
			ClassPathResource classPathResource = new ClassPathResource("native_lib/.sigar_shellrc");
			String nativeLib = classPathResource.getFile().getParentFile().getAbsolutePath();
			String javaLib = System.getProperty("java.library.path");
			if (!javaLib.contains("native_lib")) {
				System.setProperty("java.library.path", nativeLib + File.pathSeparator + javaLib);
			}
			System.out.println("Java Lib Path : " + System.getProperty("java.library.path"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Before
	public void beforeSetSecurity() {
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("admin", null);
		SecurityContextImpl context = new SecurityContextImpl();
		context.setAuthentication(token);
		SecurityContextHolder.setContext(context);
	}

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

	public void sleep(long milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			LOG.error("error:", e);
		}
	}

}
