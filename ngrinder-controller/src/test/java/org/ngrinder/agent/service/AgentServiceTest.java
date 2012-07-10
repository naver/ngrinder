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

import junit.framework.Assert;

import org.junit.Test;
import org.ngrinder.AbstractNGNinderTransactionalTest;
import org.ngrinder.agent.model.Agent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

/**
 * Agent service test
 * 
 * @author Tobi
 * @since 3.0
 */
public class AgentServiceTest extends AbstractNGNinderTransactionalTest {

	@Autowired
	private AgentService agentService;

	@Test
	public void testSaveAgent() {
		Agent agent = this.saveAgent("save");
		Agent agent2 = agentService.getAgent(agent.getId());
		Assert.assertNotNull(agent2);
	}

	@Test
	public void testGetAgents() {
		Page<Agent> page1 = agentService.getAgents("testRegion", null);
		this.saveAgent("get1");
		this.saveAgent("get2");
		this.saveAgent("get3");
		Page<Agent> page2 = agentService.getAgents("testRegion", null);
		Assert.assertEquals(page1.getTotalElements() + 3, page2.getTotalElements());
	}

	@Test
	public void testGetAgent() {
		Agent agent = this.saveAgent("get");
		Agent agent2 = agentService.getAgent(agent.getId());
		Assert.assertEquals(agent.getAppName(), agent2.getAppName());
	}

	@Test
	public void testDeleteAgent() {
		Agent agent = this.saveAgent("delete");
		agentService.deleteAgent(agent.getId());
		Agent agent2 = agentService.getAgent(agent.getId());
		Assert.assertNull(agent2);
	}

	private Agent saveAgent(String key) {
		Agent agent = new Agent();
		agent.setIp("1.1.1.1" + key);
		agent.setAppName("testAppName" + key);
		agent.setAppPort(8080);
		agent.setRegion("testRegion" + key);
		agent.setStatus(1);
		agentService.saveAgent(agent);
		return agent;
	}
}
