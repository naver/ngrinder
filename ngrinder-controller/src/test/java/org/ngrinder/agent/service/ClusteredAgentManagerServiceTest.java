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
import net.grinder.util.NetworkUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.agent.repository.AgentManagerRepository;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.region.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.grinder.util.NetworkUtils.DEFAULT_LOCAL_ADDRESSES;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Agent service test.
 *
 * @since 3.0
 */
@Ignore
public class ClusteredAgentManagerServiceTest extends AbstractNGrinderTransactionalTest {

	private ClusteredAgentManagerService agentManagerService;

	@Autowired
	private AgentManagerRepository agentRepository;

	@Autowired
	private Config config;

	private CacheManager cacheManager;

	private Config spiedConfig;

	@Autowired
	private RegionService regionService;

	String curAddress;

	@Before
	public void before() {
		spiedConfig = spy(config);
		when(spiedConfig.isClustered()).thenReturn(true);
		when(spiedConfig.getRegion()).thenReturn("TestRegion");

		curAddress = DEFAULT_LOCAL_ADDRESSES.get(0).getHostAddress();
		when(spiedConfig.getClusterURIs()).thenReturn(new String[]{curAddress, "210.10.10.1"});
		AgentManagerServiceConfig serviceConfig = new AgentManagerServiceConfig();
		serviceConfig.config = spiedConfig;
		serviceConfig.setApplicationContext(applicationContext);
		agentManagerService = (ClusteredAgentManagerService) serviceConfig.agentManagerService();
		agentManagerService.setConfig(spiedConfig);

		// set clustered cache manager.
		MockDynamicCacheConfig cacheConfig = new MockDynamicCacheConfig();
		cacheConfig.setConfig(spiedConfig);
		cacheManager = cacheConfig.dynamicCacheManager();
		((EhCacheCacheManager) cacheManager).afterPropertiesSet();
		agentManagerService.cacheManager = cacheManager;
		assertThat(cacheConfig.getConfig(), not(nullValue()));
		agentManagerService.init();
		agentManagerService.expireLocalCache();
		regionService.setCache(cacheManager.getCache("regions"));
		regionService.setConfig(spiedConfig);
		regionService.checkRegionUpdate();
	}

	@Test
	public void testSaveGetDeleteAgent() {
		String currRegion = spiedConfig.getRegion();
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
		Map<String, MutableInt> countMap = agentManagerService.getAvailableAgentCountMap(getTestUser());
		String currRegion = spiedConfig.getRegion();
		assertThat(countMap.get(currRegion), notNullValue());
	}

	@Test
	public void testCheckAgentState() {
		AgentInfo agentInfo = new AgentInfo();
		agentInfo.setName("localhost");
		agentInfo.setRegion(spiedConfig.getRegion());
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
	public void getReadyAgentCountOwnedAdmin() throws Exception {
		String currRegion = spiedConfig.getRegion();
		int oriCount = getAvailableAgentCountBy(currRegion);

		saveAgent("_test_1", currRegion, AgentControllerState.READY);
		saveAgent("_test_2", currRegion + "_owned_admin", AgentControllerState.READY);
		saveAgent("_test_3", currRegion + "_owned_user", AgentControllerState.READY);
		saveAgent("_test_4", currRegion + "", AgentControllerState.BUSY);
		saveAgent("_test_5", currRegion + "_owned_admin", AgentControllerState.BUSY);
		agentManagerService.expireLocalCache();

		int newCount = agentManagerService.getReadyAgentCount(getAdminUser(),
			spiedConfig.getRegion());
		assertThat(newCount, is(oriCount + 2));
	}

	@Test
	public void getReadyAgentCountOtherRegion() throws Exception {
		String currRegion = spiedConfig.getRegion();
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

	private void saveAgent(String name, String region, AgentControllerState status) {
		AgentInfo agent = new AgentInfo();
		agent.setIp("1.1.1.1");
		agent.setName(spiedConfig.getRegion() + name);
		agent.setPort(8080);
		agent.setRegion(region);
		agent.setState(status);
		agent.setApproved(true);
		agentRepository.save(agent);
	}

	private int getAvailableAgentCountBy(String currRegion) {
		int oriCount = 0;
		Map<String, MutableInt> countMap = agentManagerService.getAvailableAgentCountMap(getTestUser());
		oriCount = countMap.get(currRegion).intValue();
		return oriCount;
	}

}
