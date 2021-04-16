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
package org.ngrinder.agent.controller;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.agent.repository.AgentManagerRepository;
import org.ngrinder.agent.store.AgentInfoStore;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.user.service.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;

public class AgentManagerApiControllerTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private AgentManagerApiController agentApiController;

	@Autowired
	private AgentManagerRepository agentManagerRepository;

	@Autowired
	private Config config;

	@Autowired
	private UserContext userContext;

	@Autowired
	private AgentInfoStore agentInfoStore;

	@Before
	public void setMockRequest() {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addHeader("User-Agent", "Win");
		SecurityContextHolderAwareRequestWrapper reqWrapper = new SecurityContextHolderAwareRequestWrapper(req, "U");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(reqWrapper));
	}

	@After
	public void reSetRequest() {
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	public void testGetAgentList() {
		agentApiController.getAll(userContext.getCurrentUser(), "NONE", "");

		// create a temp download dir and file for this function
		File directory = config.getHome().getDownloadDirectory();
		if (!directory.exists()) {
			try {
				FileUtils.forceMkdir(directory);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		File tmpDownFile;
		try {
			tmpDownFile = File.createTempFile("ngrinder", "zip", directory);
			FileUtils.writeStringToFile(tmpDownFile, "test data", defaultCharset());
			tmpDownFile.deleteOnExit();
		} catch (IOException e) {
			e.printStackTrace();
		}

		agentApiController.getAll(userContext.getCurrentUser(), "NONE", "");
	}

	@Test
	public void testApproveAgent() {
		AgentInfo agent = new AgentInfo();
		agent.setName("Test-Host");
		agent.setIp("127.0.0.1");
		agent.setApproved(false);
		agentManagerRepository.save(agent);
		agentInfoStore.updateAgentInfo(agent.getAgentKey(), agent);

		// test get agent
		AgentInfo agentInDB = agentApiController.getOneByIpAndName(agent.getIp(), agent.getName());
		assertThat(agentInDB.getName(), is(agent.getName()));
		assertThat(agentInDB.getIp(), is(agent.getIp()));
		assertThat(agentInDB.isApproved(), is(false));

		// test approve agent
		agentApiController.approve(agent.getIp(), agent.getName());
		agentInDB = agentApiController.getOneByIpAndName(agent.getIp(), agent.getName());
		assertThat(agentInDB.isApproved(), is(true));

		// test un-approve
		agentApiController.disapprove(agent.getIp(), agent.getName());
		agentInDB = agentApiController.getOneByIpAndName(agent.getIp(), agent.getName());
		assertThat(agentInDB.isApproved(), is(false));
	}

	@Test
	public void testStopAgent() {
		agentApiController.stop("1.1.1.1", "test-agent-ncl");
	}

	@Test
	public void testGetCurrentMonitorData() {
		SystemDataModel systemDataModel = agentApiController.getState("127.0.0.1", "127.0.0.1", "NONE");
		assertNotNull(systemDataModel);
	}

	@Test
	public void testGetAvailableAgentCount() {
		String targetRegion = "test";
		Map<String, Integer> response = agentApiController.getAvailableAgentCount(getTestUser(), targetRegion, "");
		assertThat(0, is(response.get("availableAgentCount")));
	}
}
