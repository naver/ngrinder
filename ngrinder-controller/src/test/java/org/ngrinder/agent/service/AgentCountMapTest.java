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

import net.grinder.engine.controller.AgentControllerIdentityImplementation;
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

import java.util.*;

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

	private AgentInfo createAgentInfo(String region, boolean approved, AgentControllerState status) {
		AgentInfo agentInfo = new AgentInfo();
		agentInfo.setRegion(region);
		agentInfo.setApproved(approved);
		agentInfo.setState(status);
		return agentInfo;
	}

	private AgentInfo createAgentInfo(String region, String subregion, boolean approved, AgentControllerState status) {
		AgentInfo agentInfo = createAgentInfo(region, approved, status);
		AgentControllerIdentityImplementation agentIdentity = new AgentControllerIdentityImplementation("", "");
		agentIdentity.setSubregion(subregion);
		agentInfo.setAgentIdentity(agentIdentity);
		return agentInfo;
	}

	private AgentInfo createAgentInfo(String region, String subregion, boolean approved, AgentControllerState status, String owner) {
		AgentInfo agentInfo = createAgentInfo(region, subregion, approved, status);
		((AgentControllerIdentityImplementation) agentInfo.getAgentIdentity()).setOwner(owner);
		return agentInfo;
	}

	@Before
	public void init() {
		AgentInfoStore mockAgentInfoStore = mock(AgentInfoStore.class);
		RegionService mockRegionService = mock(RegionService.class);

		List<AgentInfo> agents = asList(
			createAgentInfo("hello", true, READY),
			createAgentInfo("hello", true, READY),
			createAgentInfo("hello", "", true, READY, "wow"),

			createAgentInfo("haha", true, READY),
			createAgentInfo("haha", true, READY),
			createAgentInfo("haha", true, READY),
			createAgentInfo("haha", "sub1",true, READY),
			createAgentInfo("haha", "sub2", true, READY),
			createAgentInfo("haha", false, READY),
			createAgentInfo("haha", "", true, READY, "my"),
			createAgentInfo("haha", "sub1",true, READY, "my"),

			createAgentInfo("woowo", "", true, READY, "my"),

			createAgentInfo("wowo", true, READY),
			createAgentInfo("wowo", true, READY),
			createAgentInfo("wowo", true, READY),
			createAgentInfo("wowo", false, READY),

			createAgentInfo("kiki", false, READY)
		);

		Map<String, RegionInfo> regionMap = new HashMap<>();
		Set<String> subregions = new HashSet<>();
		subregions.add("sub1");
		subregions.add("sub2");

		regionMap.put("hello", new RegionInfo("hello", subregions, null, null));
		regionMap.put("haha", new RegionInfo("haha", subregions, null, null));
		regionMap.put("wowo", new RegionInfo("wowo", subregions, null, null));

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
	public void testGetAvailableAgentCountMap() {
		User user = new User();
		user.setUserId("haha");
		Map<String, MutableInt> userAvailableAgentCountMap = agentService.getAvailableAgentCountMap(user.getUserId());
		assertThat(userAvailableAgentCountMap.containsKey("kiki"), is(false));
		assertThat(userAvailableAgentCountMap.get("hello").intValue(), is(2));
		assertThat(userAvailableAgentCountMap.get("haha").intValue(), is(3));
		assertThat(userAvailableAgentCountMap.get("haha.sub1").intValue(), is(1));
		assertThat(userAvailableAgentCountMap.get("haha.sub2").intValue(), is(1));

		user.setUserId("wow");
		userAvailableAgentCountMap = agentService.getAvailableAgentCountMap(user.getUserId());
		assertThat(userAvailableAgentCountMap.get("hello").intValue(), is(3));
		assertThat(userAvailableAgentCountMap.get("haha").intValue(), is(3));

		user.setUserId("my");
		userAvailableAgentCountMap = agentService.getAvailableAgentCountMap(user.getUserId());
		assertThat(userAvailableAgentCountMap.get("hello").intValue(), is(2));
		assertThat(userAvailableAgentCountMap.get("haha").intValue(), is(4));
		assertThat(userAvailableAgentCountMap.get("haha.sub1").intValue(), is(2));
		assertThat(userAvailableAgentCountMap.get("wowo").intValue(), is(3));
	}
}
