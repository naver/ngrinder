/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ngrinder.agent.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import net.grinder.message.console.AgentControllerState;

import org.apache.commons.lang.mutable.MutableInt;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.agent.model.AgentInfo;
import org.ngrinder.agent.repository.AgentManagerRepository;
import org.ngrinder.infra.config.Config;
import org.ngrinder.region.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;

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
	private RegionService regionService;

	@Autowired
	private AgentManagerRepository agentRepository;

	private Config config;

	@Before
	public void before() {
		config = mock(Config.class);
		when(config.isCluster()).thenReturn(true);
		agentManagerService.setConfig(config);
	}

	@Test
	public void testSaveGetDeleteAgent() {
		AgentInfo agent = saveAgent("save");
		AgentInfo agent2 = agentManagerService.getAgent(agent.getId());
		Assert.assertNotNull(agent2);

		List<AgentInfo> agentListDB = agentManagerService.getAgentListInThisRegionFromDB();
		agentListDB = agentManagerService.getAgentListInThisRegionFromDB();
		Assert.assertNotNull(agentListDB);

		agentManagerService.approve(agent.getId(), true);

		agentManagerService.deleteAgent(agent.getId());
		agent2 = agentManagerService.getAgent(agent.getId());
		Assert.assertNull(agent2);
	}

	private AgentInfo saveAgent(String key) {
		AgentInfo agent = new AgentInfo();
		agent.setIp("1.1.1.1");
		agent.setHostName("testAppName" + key);
		agent.setPort(8080);
		agent.setRegion("testRegion" + key);
		agent.setStatus(AgentControllerState.BUSY);
		agentManagerService.saveAgent(agent);
		return agent;
	}

	@Test
	public void testGetUserAvailableAgentCount() {
		List<String> regionList = regionService.getRegions();
		Map<String, MutableInt> countMap = agentManagerService.getUserAvailableAgentCountMap(regionList, getTestUser());
		int oriCount = countMap.get(config.getRegion()).intValue();

		AgentInfo agentInfo = new AgentInfo();
		agentInfo.setHostName("localhost");
		agentInfo.setNumber(-1);
		agentInfo.setRegion(config.getRegion());
		agentInfo.setIp("127.127.127.127");
		agentInfo.setPort(1);
		agentInfo.setStatus(AgentControllerState.READY);
		agentInfo.setApproved(true);
		agentManagerService.saveAgent(agentInfo);
		countMap = agentManagerService.getUserAvailableAgentCountMap(regionList, getTestUser());

		int newCount = countMap.get(config.getRegion()).intValue();
		assertThat(newCount, is(oriCount + 1));
	}

	@Test
	public void testCheckAgentStatus() {
		AgentInfo agentInfo = new AgentInfo();
		agentInfo.setHostName("localhost");
		agentInfo.setNumber(-1);
		agentInfo.setRegion(config.getRegion());
		agentInfo.setIp("127.127.127.127");
		agentInfo.setPort(1);
		agentInfo.setStatus(AgentControllerState.READY);
		agentManagerService.saveAgent(agentInfo);
		agentManagerService.checkAgentStatus();

		AgentInfo agentInDB = agentRepository.findOne(agentInfo.getId());
		assertThat(agentInDB.getIp(), is(agentInfo.getIp()));
		assertThat(agentInDB.getHostName(), is(agentInfo.getHostName()));
		assertThat(agentInDB.getStatus(), is(AgentControllerState.INACTIVE));
	}

}
