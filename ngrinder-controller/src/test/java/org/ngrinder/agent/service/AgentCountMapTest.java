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

import net.grinder.message.console.AgentControllerState;
import org.apache.commons.lang.mutable.MutableInt;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.agent.store.AgentInfoStore;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.model.User;
import org.ngrinder.perftest.service.AgentManager;
import org.ngrinder.region.model.RegionInfo;
import org.ngrinder.region.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static net.grinder.message.console.AgentControllerState.READY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ngrinder.common.util.NoOp.noOp;

public class AgentCountMapTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private AgentManager agentManager;

	private AgentService agentService;

	public AgentInfo createAgentInfo(String region, boolean approved, AgentControllerState status) {
		AgentInfo agentInfo1 = new AgentInfo();
		agentInfo1.setRegion(region);
		agentInfo1.setApproved(approved);
		agentInfo1.setState(status);
		return agentInfo1;
	}

	@Before
	public void init() {
		AgentInfoStore mockAgentInfoStore = mock(AgentInfoStore.class);
		RegionService mockRegionService = mock(RegionService.class);

		List<AgentInfo> agents = asList(
			createAgentInfo("hello", true, READY),
			createAgentInfo("hello", true, READY),
			createAgentInfo("hello_owned_wow", true, READY),
			createAgentInfo("haha", true, READY),
			createAgentInfo("haha", true, READY),
			createAgentInfo("haha", true, READY),
			createAgentInfo("haha", false, READY),
			createAgentInfo("haha_owned_my", true, READY),
			createAgentInfo("woowo_owned_my", true, READY),
			createAgentInfo("wowo", true, READY),
			createAgentInfo("wowo", true, READY),
			createAgentInfo("wowo", true, READY),
			createAgentInfo("wowo", false, READY),
			createAgentInfo("kiki", false, READY)
		);

		Map<String, RegionInfo> regionMap = new HashMap<>();
		regionMap.put("hello", null);
		regionMap.put("haha", null);
		regionMap.put("wowo", null);

		when(mockAgentInfoStore.getAllAgentInfo()).thenReturn(agents);
		when(mockRegionService.getAll()).thenReturn(regionMap);

		agentService = new AgentService(agentManager,
			null, null, mockRegionService, null,
			null, mockAgentInfoStore, null, null) {
			@Override
			public void init() {
				noOp();
			}
		};
	}

	@Test
	public void test() {
		User user = new User();
		user.setUserId("haha");
		Map<String, MutableInt> userAvailableAgentCountMap = agentService.getAvailableAgentCountMap(user.getUserId());
		System.out.println(userAvailableAgentCountMap);
		assertThat(userAvailableAgentCountMap.containsKey("kiki"), is(false));
		assertThat(userAvailableAgentCountMap.get("hello").intValue(), is(2));
		assertThat(userAvailableAgentCountMap.get("haha").intValue(), is(3));

		user.setUserId("wow");
		userAvailableAgentCountMap = agentService.getAvailableAgentCountMap(user.getUserId());
		assertThat(userAvailableAgentCountMap.get("hello").intValue(), is(3));
		assertThat(userAvailableAgentCountMap.get("haha").intValue(), is(3));

		user.setUserId("my");
		userAvailableAgentCountMap = agentService.getAvailableAgentCountMap(user.getUserId());
		assertThat(userAvailableAgentCountMap.get("hello").intValue(), is(2));
		assertThat(userAvailableAgentCountMap.get("haha").intValue(), is(4));
		assertThat(userAvailableAgentCountMap.get("wowo").intValue(), is(3));
	}
}
