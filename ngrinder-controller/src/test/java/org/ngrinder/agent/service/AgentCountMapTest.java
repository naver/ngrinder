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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AgentCountMapTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	AgentManager agentManager;

	@Autowired
	AgentManagerRepository agentManagerRepository;

	Config config;

	ClusteredAgentManagerService agentManagerService;

	public AgentInfo createAgentInfo(String region, boolean approved, AgentControllerState status) {
		AgentInfo agentInfo1 = new AgentInfo();
		agentInfo1.setRegion(region);
		agentInfo1.setApproved(approved);
		agentInfo1.setState(status);
		return agentInfo1;
	}

	@Before
	public void init() {
		agentManagerService = new ClusteredAgentManagerService() {
			@SuppressWarnings("serial")
			@Override
			public List<AgentInfo> getAllActive() {
				return new ArrayList<AgentInfo>() {
					{
						add(createAgentInfo("hello", true, AgentControllerState.READY));
						add(createAgentInfo("hello", true, AgentControllerState.READY));
						add(createAgentInfo("hello_owned_wow", true, AgentControllerState.READY));
						add(createAgentInfo("haha", true, AgentControllerState.READY));
						add(createAgentInfo("haha", true, AgentControllerState.READY));
						add(createAgentInfo("haha", true, AgentControllerState.READY));
						add(createAgentInfo("haha", false, AgentControllerState.READY));
						add(createAgentInfo("haha", true, AgentControllerState.READY));
						add(createAgentInfo("haha_owned_my", true, AgentControllerState.READY));
						add(createAgentInfo("woowo_owned_my", true, AgentControllerState.READY));
						add(createAgentInfo("wowo", true, AgentControllerState.READY));
						add(createAgentInfo("wowo", true, AgentControllerState.READY));
						add(createAgentInfo("wowo", true, AgentControllerState.READY));
						add(createAgentInfo("wowo", false, AgentControllerState.READY));
						add(createAgentInfo("kiki", false, AgentControllerState.READY));

					}
				};
			}

			@Override
			protected Set<String> getRegions() {
				Set<String> regions = Sets.newHashSet("hello", "haha", "wowo");
				return regions;
			}

			@Override
			int getMaxAgentSizePerConsole() {
				return 3;
			}
		};

		config = mock(Config.class);
		when(config.isClustered()).thenReturn(true);
		agentManagerService.setConfig(config);
		agentManagerService.setAgentManager(this.agentManager);
		agentManagerService.setAgentManagerRepository(this.agentManagerRepository);
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
