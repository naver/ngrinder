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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.engine.controller.AgentControllerIdentityImplementation;

import org.apache.commons.lang.StringUtils;
import org.ngrinder.agent.model.AgentInfo;
import org.ngrinder.agent.repository.AgentRepository;
import org.ngrinder.perftest.service.AgentManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * agent service.
 * 
 * @author Tobi
 * @author JunHo Yoon
 * @since 3.0
 */
@Service
public class AgentService {

	@Autowired
	private AgentManager agentManager;

	@Autowired
	private AgentRepository agentRepository;

	/**
	 * Get agents.
	 * 
	 * @param searchStr
	 *            search keyword. if empty, no search
	 * @param pageable
	 *            page
	 * @return agent list
	 */
	public List<AgentInfo> getAgents() {
		Set<AgentIdentity> allAttachedAgents = agentManager.getAllAttachedAgents();
		List<AgentInfo> agentList = new ArrayList<AgentInfo>();
		for (AgentIdentity eachAgentIdentity : allAttachedAgents) {
			AgentControllerIdentityImplementation eachAgentController = (AgentControllerIdentityImplementation) eachAgentIdentity;
			agentList.add(creatAgentInfo(eachAgentController));
		}
		return agentList;
	}

	private AgentInfo creatAgentInfo(AgentControllerIdentityImplementation agentIdentity) {
		AgentInfo agentInfo = agentRepository.findByIp(agentIdentity.getIp());
		agentInfo = agentInfo == null ? new AgentInfo() : agentInfo;
		agentInfo.setHostName(agentIdentity.getName());
		agentInfo.setRegion(agentIdentity.getRegion());
		agentInfo.setIp(agentIdentity.getIp());
		agentInfo.setPort(agentIdentity.getPort());
		agentInfo.setStatus(agentManager.getAgentControllerState(agentIdentity));
		if (!agentInfo.exist()) {
			agentInfo = agentRepository.save(agentInfo);
		}
		return agentInfo;
	}

	/**
	 * Get a agent on given id.
	 * 
	 * @param id
	 *            agent id
	 * @return agent
	 */
	public AgentInfo getAgent(long id) {
		AgentInfo agentInfo = agentRepository.findOne(id);
		AgentControllerIdentityImplementation agentIdentity = getAgentIdentityByIp(agentInfo.getIp());
		if (agentIdentity != null) {
			agentInfo.setStatus(agentManager.getAgentControllerState(agentIdentity));
			agentInfo.setPort(agentIdentity.getPort());
			agentInfo.setHostName(agentIdentity.getName());
			agentInfo.setRegion(agentIdentity.getRegion());
		}
		return agentInfo;
	}

	/**
	 * Get {@link AgentControllerIdentityImplementation} for give agentInfo
	 * 
	 * @param agentInfo
	 * @return
	 */
	private AgentControllerIdentityImplementation getAgentIdentityByIp(String agentIP) {
		for (AgentIdentity agentIdentity : agentManager.getAllAttachedAgents()) {
			AgentControllerIdentityImplementation eachAgentIdentity = (AgentControllerIdentityImplementation) agentIdentity;
			if (StringUtils.equals(eachAgentIdentity.getIp(), agentIP)) {
				return eachAgentIdentity;
			}
		}
		return null;
	}

	/**
	 * Save agent.
	 * 
	 * @param agent
	 *            saved agent
	 */
	public void saveAgent(AgentInfo agent) {
		agentRepository.save(agent);
	}

	/**
	 * Delete agent.
	 * 
	 * @param id
	 *            agent id to be deleted
	 */
	public void deleteAgent(long id) {
		agentRepository.delete(id);
	}

}
