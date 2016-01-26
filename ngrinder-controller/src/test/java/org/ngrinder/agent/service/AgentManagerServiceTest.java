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
import net.grinder.message.console.AgentControllerState;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.agent.repository.AgentManagerRepository;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.model.User;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;
import static org.ngrinder.common.util.TypeConvertUtils.cast;

/**
 * Agent service test.
 *
 * @author Tobi
 * @since 3.0
 */
public class AgentManagerServiceTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private AgentManagerService agentManagerService;

	@Autowired
	private AgentPackageService agentPackageService;

	@Autowired
	private AgentManagerRepository agentRepository;

	@Autowired
	private LocalAgentService localAgentService;

	@Autowired
	private Config config;

	@Before
	public void before() {
		agentRepository.deleteAll();
		localAgentService.expireCache();
	}
	
	@Test
	public void testSaveGetDeleteAgent() {
		AgentInfo agent = saveAgent("save");
		AgentInfo agent2 = agentManagerService.getOne(agent.getId());
		Assert.assertNotNull(agent2);

		List<AgentInfo> agentListDB = agentManagerService.getAllLocal();
		Assert.assertNotNull(agentListDB);

		agentManagerService.approve(agent.getId(), true);

		agentRepository.delete(agent.getId());
		agent2 = agentManagerService.getOne(agent.getId());
		Assert.assertNull(agent2);
	}

	private AgentInfo saveAgent(String key) {
		AgentInfo agent = new AgentInfo();
		agent.setIp("1.1.1.1");
		agent.setName("testAppName" + key);
		agent.setPort(8080);
		agent.setRegion("testRegion" + key);
		agent.setState(AgentControllerState.BUSY);
		agentRepository.save(agent);
		return agent;
	}
	
	private void saveAgent(String key , AgentControllerState agentStatus) {
		AgentInfo agent = new AgentInfo();
		agent.setIp("1.1.1.1");
		agent.setName(key);
		agent.setPort(8080);
		agent.setRegion(key);
		agent.setState(agentStatus);
		agent.setApproved(true);
		agentRepository.save(agent);
	}

	@Test
	public void testGetUserAvailableAgentCount() {
		Map<String, MutableInt> countMap = agentManagerService.getAvailableAgentCountMap(getTestUser());
		String currRegion = config.getRegion();
		int oriCount = countMap.get(currRegion).intValue();

		AgentInfo agentInfo = new AgentInfo();
		agentInfo.setName("localhost");
		agentInfo.setRegion(config.getRegion());
		agentInfo.setIp("127.127.127.127");
		agentInfo.setPort(1);
		agentInfo.setState(AgentControllerState.READY);
		agentInfo.setApproved(true);
		agentRepository.save(agentInfo);
		localAgentService.expireCache();
		countMap = agentManagerService.getAvailableAgentCountMap(getTestUser());

		int newCount = countMap.get(config.getRegion()).intValue();
		assertThat(newCount, is(oriCount + 1));
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
		localAgentService.expireCache();
		agentManagerService.checkAgentState();

		AgentInfo agentInDB = agentRepository.findOne(agentInfo.getId());
		assertThat(agentInDB.getIp(), is(agentInfo.getIp()));
		assertThat(agentInDB.getName(), is(agentInfo.getName()));
		assertThat(agentInDB.getState(), is(AgentControllerState.INACTIVE));
	}

	@Test
	public void testCompressAgentFolder() throws IOException, URISyntaxException {
		URLClassLoader loader = (URLClassLoader) this.getClass().getClassLoader();
		URL core = this.getClass().getClassLoader().getResource("lib/ngrinder-core-test.jar");
		URL sh = this.getClass().getClassLoader().getResource("lib/ngrinder-sh-test.jar");
		URL[] ls = {core, sh};
		URL[] urls = loader.getURLs();
		URL[] allLib = cast(ArrayUtils.addAll(urls, ls));
		URLClassLoader child = new URLClassLoader(allLib, this.getClass().getClassLoader());
		File agentUpgrade = agentPackageService.createAgentPackage(child, null, null, 10000, null);
		FileUtils.deleteQuietly(agentUpgrade);
	}

	@Test
	public void testGetAllReady() {
		List<AgentInfo> agents = new ArrayList<AgentInfo>();
		String currRegion = config.getRegion();
		int oriCount = getAvailableAgentCountBy(currRegion);

		saveAgent("nhn", AgentControllerState.BUSY);
		saveAgent("nhn_owned_userNm", AgentControllerState.INACTIVE);
		saveAgent("google", AgentControllerState.UNKNOWN);
		localAgentService.expireCache();
		agents = agentManagerService.getAllReady();
		assertThat(agents.size(), is(oriCount + 0));

		saveAgent("nhn", AgentControllerState.READY);
		saveAgent("nhn_owned_userNm", AgentControllerState.READY);
		saveAgent("google", AgentControllerState.READY);
		localAgentService.expireCache();
		agents = agentManagerService.getAllReady();
		assertThat(agents.size(), is(oriCount + 3));
	}

	@Test
	public void testReadyAgentCountPublicAgent() {
		String currRegion = config.getRegion();
		int oriCount = getAvailableAgentCountBy(currRegion);

		saveAgent("nhn", AgentControllerState.READY);
		saveAgent("google", AgentControllerState.READY);
		localAgentService.expireCache();

		int newCount = agentManagerService.getReadyAgentCount(getTestUser(), "nhn");
		assertThat(newCount, is(oriCount + 1));
	}

	@Test
	public void testReadyAgentCountPublicAgentAllInactivy() {
		String currRegion = config.getRegion();
		int oriCount = getAvailableAgentCountBy(currRegion);

		saveAgent("nhn", AgentControllerState.INACTIVE);
		saveAgent("nhn", AgentControllerState.UNKNOWN);
		saveAgent("nhn", AgentControllerState.BUSY);
		saveAgent("google", AgentControllerState.INACTIVE);
		saveAgent("google", AgentControllerState.UNKNOWN);
		saveAgent("google", AgentControllerState.BUSY);
		localAgentService.expireCache();

		int newCount = agentManagerService.getReadyAgentCount(getTestUser(), "nhn");
		assertThat(newCount, is(oriCount + 0));
	}

	@Test
	public void testReadyAgentCountOwnedAgent() {
		String currRegion = config.getRegion();
		int oriCount = getAvailableAgentCountBy(currRegion);
		String owendAgentName = "nhn_owned_" + getTestUser().getUserName();

		saveAgent(owendAgentName, AgentControllerState.READY);
		saveAgent("nhn_owned_otherUserNm", AgentControllerState.READY);
		saveAgent("nhn_owned_admin", AgentControllerState.READY);
		localAgentService.expireCache();

		int newCount = agentManagerService.getReadyAgentCount(getTestUser(), "nhn");
		assertThat(newCount, is(oriCount + 1));
	}

	@Test
	public void testReadyAgentCountPublicAgentInactivy() {
		String currRegion = config.getRegion();
		int oriCount = getAvailableAgentCountBy(currRegion);

		saveAgent("nhn", AgentControllerState.INACTIVE);
		saveAgent("google", AgentControllerState.READY);
		localAgentService.expireCache();

		int newCount = agentManagerService.getReadyAgentCount(getTestUser(), "nhn");
		assertThat(newCount, is(oriCount + 0));
	}

	@Test
	public void testReadyAgentCountOwnedAgentInactivy() {
		String currRegion = config.getRegion();
		int oriCount = getAvailableAgentCountBy(currRegion);

		saveAgent("nhn_owned_otherUser", AgentControllerState.INACTIVE);
		saveAgent("google_owned_admin", AgentControllerState.READY);
		localAgentService.expireCache();

		int newCount = agentManagerService.getReadyAgentCount(getTestUser(), "nhn");
		assertThat(newCount, is(oriCount + 0));
	}

	private int getAvailableAgentCountBy(String currRegion) {
		int oriCount = 0;
		Map<String, MutableInt> countMap = agentManagerService.getAvailableAgentCountMap(getTestUser());
		oriCount = countMap.get(currRegion).intValue();
		return oriCount;
	}

}
