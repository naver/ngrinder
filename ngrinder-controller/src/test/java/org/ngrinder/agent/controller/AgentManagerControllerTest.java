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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import net.grinder.message.console.AgentControllerState;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.agent.service.AgentManagerService;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.AgentInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Class description.
 * 
 * @author Mavlarn
 * @since 3.0
 */
public class AgentManagerControllerTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	AgentManagerController agentController;

	@Autowired
	AgentManagerService agentService;

	@Autowired
	private Config config;

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
		agentController.getAgentList("", model);

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
		agentController.getAgentList("", model);
		Collection<AgentInfo> agents = (Collection<AgentInfo>) model.get("agents");
		if (!agents.isEmpty()) {
			AgentInfo testAgt = agents.iterator().next();
			model.clear();
			agentController.getAgent(testAgt.getId(), model);
			AgentInfo agentInDB = (AgentInfo) model.get("agent");
			assertThat(agentInDB.getId(), is(testAgt.getId()));
			assertThat(agentInDB.getIp(), is(testAgt.getIp()));
			assertThat(agentInDB.getPort(), is(testAgt.getPort()));

		}
	}

	@Test
	public void testApproveAgent() {
		AgentInfo agent = new AgentInfo();
		agent.setApproved(false);
		agent.setName("Test-Host");
		agent.setIp("127.0.0.1");
		agent.setState(AgentControllerState.READY);
		agentService.saveAgent(agent);

		ModelMap model = new ModelMap();
		// test get agent
		agentController.getAgent(agent.getId(), model);
		AgentInfo agentInDB = (AgentInfo) model.get("agent");
		assertThat(agentInDB.getName(), is(agent.getName()));
		assertThat(agentInDB.getIp(), is(agent.getIp()));
		assertThat(agentInDB.isApproved(), is(false));

		// test approve agent
		model.clear();
		agentController.approveAgent(agentInDB.getId(), true, "", model);
		agentController.getAgent(agent.getId(), model);
		agentInDB = (AgentInfo) model.get("agent");
		assertThat(agentInDB.isApproved(), is(true));

		// test un-approve
		model.clear();
		agentController.approveAgent(agentInDB.getId(), false, "", model);
		agentController.getAgent(agent.getId(), model);
		agentInDB = (AgentInfo) model.get("agent");
		assertThat(agentInDB.isApproved(), is(false));
	}

	@Test
	public void testStopAgent() {
		agentController.stop("0");
	}

	@Test
	public void testGetCurrentMonitorData() {
		ModelMap model = new ModelMap();
		HttpEntity<String> rtnStr = agentController.getState(0L, "127.0.0.1", "127.0.0.1", model);
		assertTrue(rtnStr.getBody().contains("systemData"));
	}

}
