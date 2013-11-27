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

import junit.framework.Assert;
import net.grinder.engine.controller.AgentControllerIdentityImplementation;
import net.grinder.message.console.AgentControllerState;
import net.grinder.util.NetworkUtil;
import org.apache.commons.lang.mutable.MutableInt;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.agent.repository.AgentManagerRepository;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.AgentInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Agent service test.
 *
 * @author Tobi
 * @since 3.0
 */
public class ClusteredAgentManagerServiceTest extends AbstractNGrinderTransactionalTest {

	private ClusteredAgentManagerService agentManagerService;

	@Autowired
	private AgentManagerRepository agentRepository;

	@Autowired
	private Config config;

	private CacheManager cacheManager;

	private Config spiedConfig;

	private boolean initialed = false;

	@Before
	public void before() {
		if (initialed) {
			return;
		}
		spiedConfig = spy(config);
		when(spiedConfig.isCluster()).thenReturn(true);
		when(spiedConfig.getRegion()).thenReturn("TestRegion");

		AgentManagerServiceConfig serviceConfig = new AgentManagerServiceConfig();
		serviceConfig.config = spiedConfig;
		serviceConfig.setApplicationContext(applicationContext);
		agentManagerService = (ClusteredAgentManagerService) serviceConfig.agentManagerService();
		agentManagerService.setConfig(spiedConfig);

		// set clustered cache manager.
		MockDynamicCacheConfig cacheConfig = new MockDynamicCacheConfig();
		cacheConfig.setConfig(spiedConfig);
		cacheManager = cacheConfig.dynamicCacheManager();
		((EhCacheCacheManager) cacheManager).afterPropertiesSet(); // it will not be called if we
		// create manually
		agentManagerService.cacheManager = cacheManager;
		assertThat(cacheConfig.getConfig(), not(nullValue()));

		agentManagerService.init();
		initialed = true;
	}

	@Test
	public void testOther() {
		agentManagerService.getAllVisibleAgentInfoFromDB();
		agentManagerService.getAllActiveAgentInfoFromDB();
		agentManagerService.stopAgent(0L);
		agentManagerService.requestShareAgentSystemDataModel(0L);
		agentManagerService.getAgentSystemDataModel("127.0.0.1", "127.0.0.1");
		AgentControllerIdentityImplementation monitor = new AgentControllerIdentityImplementation(NetworkUtil.DEFAULT_LOCAL_HOST_NAME,
				"127.0.0.1");
		monitor.setRegion(spiedConfig.getRegion());
		agentManagerService.addAgentMonitoringTarget(monitor);
		agentManagerService.stopAgent(new AgentControllerIdentityImplementation(NetworkUtil.DEFAULT_LOCAL_HOST_NAME, "127.0.0.1"));
		agentManagerService.collectAgentSystemData();
	}

	@Test
	public void testSaveGetDeleteAgent() {
		AgentInfo agent = saveAgent("save");
		AgentInfo agent2 = agentManagerService.getAgent(agent.getId(), false);
		Assert.assertNotNull(agent2);

		List<AgentInfo> agentListDB = agentManagerService.getLocalAgentListFromDB();
		agentListDB = agentManagerService.getLocalAgentListFromDB();
		Assert.assertNotNull(agentListDB);

		agentManagerService.approve(agent.getId(), true);

		agentManagerService.deleteAgent(agent.getId());
		agent2 = agentManagerService.getAgent(agent.getId(), false);
		Assert.assertNull(agent2);
	}

	private AgentInfo saveAgent(String key) {
		AgentInfo agent = new AgentInfo();
		agent.setIp("1.1.1.1");
		agent.setName("testAppName" + key);
		agent.setPort(8080);
		agent.setRegion("testRegion" + key);
		agent.setState(AgentControllerState.BUSY);
		agentManagerService.saveAgent(agent);
		return agent;
	}

	@Test
	public void testGetUserAvailableAgentCount() {
		Map<String, MutableInt> countMap = agentManagerService.getUserAvailableAgentCountMap(getTestUser());
		String currRegion = spiedConfig.getRegion();
		System.out.println(countMap.get(currRegion));
	}

	@Test
	public void testCheckAgentState() {
		AgentInfo agentInfo = new AgentInfo();
		agentInfo.setName("localhost");
		agentInfo.setRegion(spiedConfig.getRegion());
		agentInfo.setIp("127.127.127.127");
		agentInfo.setPort(1);
		agentInfo.setState(AgentControllerState.READY);
		agentManagerService.saveAgent(agentInfo);
		agentManagerService.checkAgentStateRegularly();

		AgentInfo agentInDB = agentRepository.findOne(agentInfo.getId());
		assertThat(agentInDB.getIp(), is(agentInfo.getIp()));
		assertThat(agentInDB.getName(), is(agentInfo.getName()));
		assertThat(agentInDB.getState(), is(AgentControllerState.INACTIVE));
	}

}
