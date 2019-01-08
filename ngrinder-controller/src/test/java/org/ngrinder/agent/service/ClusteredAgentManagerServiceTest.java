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
import org.ngrinder.agent.repository.AgentManagerRepository;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.hazelcast.topic.message.TopicEvent;
import org.ngrinder.infra.hazelcast.topic.subscriber.TopicSubscriber;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.region.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.ngrinder.agent.model.ClusteredAgentRequest.RequestType.EXPIRE_LOCAL_CACHE;
import static org.springframework.test.util.ReflectionTestUtils.setField;

/**
 * Agent service test.
 *
 * @since 3.0
 */
public class ClusteredAgentManagerServiceTest extends AbstractNGrinderTransactionalTest {

	private ClusteredAgentManagerService agentManagerService;

	@Autowired
	private AgentManagerRepository agentRepository;

	@Autowired
	private Config config;

	@Autowired
	private RegionService regionService;

	@Autowired
	private TopicSubscriber topicSubscriber;

	@Before
	public void before() {
		Config spiedConfig = spy(config);
		when(spiedConfig.isClustered()).thenReturn(true);

		AgentManagerServiceConfig serviceConfig = new AgentManagerServiceConfig();
		setField(serviceConfig, "config", spiedConfig);
		serviceConfig.setApplicationContext(applicationContext);

		agentManagerService = (ClusteredAgentManagerService) serviceConfig.agentManagerService();
		setField(agentManagerService, "config", spiedConfig);
		setField(regionService, "config", spiedConfig);

		agentRepository.deleteAll();
		agentManagerService.expireLocalCache();
	}

	@Test
	public void testSaveGetDeleteAgent() {
		String currRegion = config.getRegion();
		int oriCount = agentManagerService.getAllLocal().size();

		saveAgent("agentSave", currRegion, AgentControllerState.BUSY);
		saveAgent("agentSave", currRegion, AgentControllerState.UNKNOWN);
		agentManagerService.expireLocalCache();
		List<AgentInfo> agents = agentManagerService.getAllLocal();
		assertThat(agents.size(), is(oriCount + 2));

		agentRepository.deleteAll();
		agentManagerService.expireLocalCache();
		agents = agentManagerService.getAllLocal();
		assertEquals("agent delete test error", agents.size(), 0);
	}

	@Test
	public void testAvailableAgentCount() {
		String currRegion = config.getRegion();
		saveAgent("owned_" + getTestUser().getUserId(), currRegion, AgentControllerState.READY);
		Map<String, MutableInt> countMap = agentManagerService.getAvailableAgentCountMap(getTestUser());
		assertThat(countMap.get(currRegion), notNullValue());
	}

	@Test
	public void testCheckAgentState() {
		AgentInfo agentInfo = new AgentInfo();
		agentInfo.setName("localhost");
		agentInfo.setRegion(config.getRegion());
		agentInfo.setIp("127.127.127.127");
		agentInfo.setPort(1);
		agentInfo.setState(AgentControllerState.READY);
		agentRepository.save(agentInfo);
		agentManagerService.expireLocalCache();
		agentManagerService.checkAgentState();
		AgentInfo agentInDB = agentRepository.findOne(agentInfo.getId());
		assertThat(agentInDB.getIp(), is(agentInfo.getIp()));
		assertThat(agentInDB.getName(), is(agentInfo.getName()));
		assertThat(agentInDB.getState(), is(AgentControllerState.INACTIVE));
	}

	@Test
	public void getReadyAgentCountOwnedAdmin() {
		String currRegion = config.getRegion();
		int oriCount = getAvailableAgentCountBy(currRegion);

		saveAgent("_test_1", currRegion, AgentControllerState.READY);
		saveAgent("_test_2", currRegion + "_owned_admin", AgentControllerState.READY);
		saveAgent("_test_3", currRegion + "_owned_user", AgentControllerState.READY);
		saveAgent("_test_4", currRegion + "", AgentControllerState.BUSY);
		saveAgent("_test_5", currRegion + "_owned_admin", AgentControllerState.BUSY);
		agentManagerService.expireLocalCache();

		int newCount = agentManagerService.getReadyAgentCount(getAdminUser(),
			config.getRegion());
		assertThat(newCount, is(oriCount + 2));
	}

	@Test
	public void getReadyAgentCountOtherRegion() {
		String currRegion = config.getRegion();
		int oriCount = getAvailableAgentCountBy(currRegion);

		// add vaild
		saveAgent("_test_1", currRegion, AgentControllerState.READY);
		saveAgent("_test_2", currRegion + "_owned_admin", AgentControllerState.READY);
		// add invalid
		saveAgent("_test_3", currRegion, AgentControllerState.INACTIVE);
		saveAgent("_test_4", "nhn_owned_admin", AgentControllerState.BUSY);
		saveAgent("_test_5", "nhn", AgentControllerState.READY);
		saveAgent("_test_6", "nhn_owned_admin", AgentControllerState.READY);
		agentManagerService.expireLocalCache();

		int newCount = agentManagerService.getReadyAgentCount(getAdminUser(), currRegion);
		assertThat(newCount, is(oriCount + 2));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPublishTopic() {
		AgentInfo agent_1 = createAgent("agent_1", config.getRegion(), AgentControllerState.READY);
		ClusteredAgentManagerService clusteredAgentManagerServiceMock = mock(ClusteredAgentManagerService.class);

		topicSubscriber.addListener("unit_test_listener", clusteredAgentManagerServiceMock);
		agentManagerService.publishTopic(agent_1, "unit_test_listener", EXPIRE_LOCAL_CACHE);

		sleep(3000);
		verify(clusteredAgentManagerServiceMock, times(1)).execute(any(TopicEvent.class));
	}

	private AgentInfo saveAgent(String name, String region, AgentControllerState status) {
		return agentRepository.save(createAgent(name, region, status));
	}

	private AgentInfo createAgent(String name, String region, AgentControllerState status) {
		AgentInfo agent = new AgentInfo();
		agent.setIp("1.1.1.1");
		agent.setName(config.getRegion() + name);
		agent.setPort(8080);
		agent.setRegion(region);
		agent.setState(status);
		agent.setApproved(true);
		return agent;
	}

	private int getAvailableAgentCountBy(String currRegion) {
		int oriCount = 0;
		Map<String, MutableInt> countMap = agentManagerService.getAvailableAgentCountMap(getTestUser());
		oriCount = countMap.get(currRegion).intValue();
		return oriCount;
	}

}
