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
package org.ngrinder.agent.service;

import com.google.common.collect.Sets;
import net.grinder.message.console.AgentControllerState;
import org.apache.commons.lang.mutable.MutableInt;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.agent.repository.AgentManagerRepository;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.model.User;
import org.ngrinder.perftest.service.AgentManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class AgentCountMapTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private AgentManager agentManager;

	@Autowired
	private AgentManagerRepository agentManagerRepository;

	private ClusteredAgentManagerService agentManagerService;


	public AgentInfo createAgentInfo(String region, boolean approved, AgentControllerState status) {
		AgentInfo agentInfo1 = new AgentInfo();
		agentInfo1.setRegion(region);
		agentInfo1.setApproved(approved);
		agentInfo1.setState(status);
		return agentInfo1;
	}

	@Before
	public void init() {
		Config config = mock(Config.class);
		when(config.isClustered()).thenReturn(true);

		AgentManagerServiceConfig serviceConfig = new AgentManagerServiceConfig(config, applicationContext);
		agentManagerService = (ClusteredAgentManagerService) spy(serviceConfig.agentManagerService());

		setField(agentManagerService, "config", config);
		setField(agentManagerService, "agentManager", agentManager);
		setField(agentManagerService, "agentManagerRepository", agentManagerRepository);

		List<AgentInfo> agents = asList(
			createAgentInfo("hello", true, AgentControllerState.READY),
			createAgentInfo("hello", true, AgentControllerState.READY),
			createAgentInfo("hello_owned_wow", true, AgentControllerState.READY),
			createAgentInfo("haha", true, AgentControllerState.READY),
			createAgentInfo("haha", true, AgentControllerState.READY),
			createAgentInfo("haha", true, AgentControllerState.READY),
			createAgentInfo("haha", false, AgentControllerState.READY),
			createAgentInfo("haha", true, AgentControllerState.READY),
			createAgentInfo("haha_owned_my", true, AgentControllerState.READY),
			createAgentInfo("woowo_owned_my", true, AgentControllerState.READY),
			createAgentInfo("wowo", true, AgentControllerState.READY),
			createAgentInfo("wowo", true, AgentControllerState.READY),
			createAgentInfo("wowo", true, AgentControllerState.READY),
			createAgentInfo("wowo", false, AgentControllerState.READY),
			createAgentInfo("kiki", false, AgentControllerState.READY)
		);

		doReturn(agents).when(agentManagerService).getAllActive();
		doReturn(Sets.newHashSet("hello", "haha", "wowo")).when(agentManagerService).getRegions();
		doReturn(3).when(agentManagerService).getMaxAgentSizePerConsole();
	}

	@Test
	public void test() {

		User user = new User();
		user.setUserId("haha");
		Map<String, MutableInt> userAvailableAgentCountMap = agentManagerService.getAvailableAgentCountMap(user);
		System.out.println(userAvailableAgentCountMap);
		assertThat(userAvailableAgentCountMap.containsKey("kiki"), is(false));
		assertThat(userAvailableAgentCountMap.get("hello").intValue(), is(2));
		assertThat(userAvailableAgentCountMap.get("haha").intValue(), is(3));

		user.setUserId("wow");
		userAvailableAgentCountMap = agentManagerService.getAvailableAgentCountMap(user);
		assertThat(userAvailableAgentCountMap.get("hello").intValue(), is(3));
		assertThat(userAvailableAgentCountMap.get("haha").intValue(), is(3));

		user.setUserId("my");
		userAvailableAgentCountMap = agentManagerService.getAvailableAgentCountMap(user);
		assertThat(userAvailableAgentCountMap.get("hello").intValue(), is(2));
		assertThat(userAvailableAgentCountMap.get("haha").intValue(), is(4));
		assertThat(userAvailableAgentCountMap.get("wowo").intValue(), is(3));

	}
}
