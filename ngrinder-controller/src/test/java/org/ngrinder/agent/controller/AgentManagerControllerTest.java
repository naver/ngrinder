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

import net.grinder.message.console.AgentControllerState;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.agent.repository.AgentManagerRepository;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.user.service.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AgentManagerControllerTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private AgentManagerController agentManagerController;

	@Autowired
	private AgentManagerApiController agentManagerApiController;

	@Autowired
	private AgentManagerRepository agentManagerRepository;

	@Autowired
	private Config config;

	@Autowired
	private UserContext userContext;

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

	@SuppressWarnings("unchecked")
	@Test
	public void testGetAgentList() {

		ModelMap model = new ModelMap();
		agentManagerController.getAll(userContext.getCurrentUser(), "", model);

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
			FileUtils.writeStringToFile(tmpDownFile, "test data");
			tmpDownFile.deleteOnExit();
		} catch (IOException e) {
			e.printStackTrace();
		}

		model.clear();
		agentManagerController.getAll(userContext.getCurrentUser(), "", model);
		Collection<AgentInfo> agents = (Collection<AgentInfo>) model.get("agents");
	}

	@Test
	public void testApproveAgent() {
		AgentInfo agent = new AgentInfo();
		agent.setApproved(false);
		agent.setName("Test-Host");
		agent.setIp("127.0.0.1");
		agent.setState(AgentControllerState.READY);
		agentManagerRepository.save(agent);

		ModelMap model = new ModelMap();
		// test get agent
		agentManagerController.getOne(agent.getId(), model);
		AgentInfo agentInDB = (AgentInfo) model.get("agent");
		assertThat(agentInDB.getName(), is(agent.getName()));
		assertThat(agentInDB.getIp(), is(agent.getIp()));
		assertThat(agentInDB.isApproved(), is(false));

		// test approve agent
		model.clear();
		agentManagerApiController.approve(agentInDB.getId());
		agentManagerController.getOne(agent.getId(), model);
		agentInDB = (AgentInfo) model.get("agent");
		assertThat(agentInDB.isApproved(), is(true));

		// test un-approve
		model.clear();
		agentManagerApiController.disapprove(agentInDB.getId());
		agentManagerController.getOne(agent.getId(), model);
		agentInDB = (AgentInfo) model.get("agent");
		assertThat(agentInDB.isApproved(), is(false));
	}

	@Test
	public void testStopAgent() {
		agentManagerApiController.stop("0");
	}

	@Test
	public void testGetCurrentMonitorData() {
		SystemDataModel systemDataModel = agentManagerApiController.getState("127.0.0.1", "127.0.0.1", "");
		assertThat(0L, is(systemDataModel.getFreeMemory()));

	}

	@Test
	public void testGetAvailableAgentCount() {
		String targetRegion = "test";
		Map<String, Object> result = agentManagerApiController.getAvailableAgentCount(getTestUser(), targetRegion);
		assertThat(0, is(result.get("availableAgentCount")));
	}
	
}
