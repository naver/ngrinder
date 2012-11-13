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
import org.ngrinder.agent.repository.AgentManagerRepository;
import org.ngrinder.perftest.service.AgentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * agent service.
 * 
 * @author Tobi
 * @author JunHo Yoon
 * @since 3.0
 */
@Service
public class AgentManagerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AgentManagerService.class);
	@Autowired
	private AgentManager agentManager;

	@Autowired
	private AgentManagerRepository agentRepository;

	/**
	 * Get agents. agent list is obtained from DB and {@link AgentManager}
	 * 
	 * This includes not persisted agent as well.
	 * 
	 * @return agent list
	 */
	@Transactional
	public List<AgentInfo> getAgentList() {
		Set<AgentIdentity> allAttachedAgents = agentManager.getAllAttachedAgents();
		List<AgentInfo> agents = agentRepository.findAll();
		List<AgentInfo> agentList = new ArrayList<AgentInfo>(allAttachedAgents.size());
		for (AgentIdentity eachAgentIdentity : allAttachedAgents) {
			AgentControllerIdentityImplementation agentControllerIdentity = 
					(AgentControllerIdentityImplementation) eachAgentIdentity;
			agentList.add(creatAgentInfo(agentControllerIdentity, agents));
		}
		return agentList;
	}

	/**
	 * Get agents. agent list is obtained only from DB
	 * 
	 * @return agent list
	 */
	@Cacheable("agents")
	public List<AgentInfo> getAgentListOnDB() {
		return agentRepository.findAll();
	}

	@CacheEvict(allEntries = true, value = "agents")
	private AgentInfo creatAgentInfo(AgentControllerIdentityImplementation agentIdentity,
					List<AgentInfo> agents) {
		AgentInfo agentInfo = new AgentInfo();
		for (AgentInfo each : agents) {
			if (each != null && StringUtils.equals(each.getIp(), agentIdentity.getIp())) {
				agentInfo = each;
				break;
			}
		}
		if (!StringUtils.equals(agentInfo.getHostName(), agentIdentity.getName())
						|| !StringUtils.equals(agentInfo.getRegion(), agentIdentity.getRegion())) {
			agentInfo.setHostName(agentIdentity.getName());
			agentInfo.setRegion(agentIdentity.getRegion());
			agentInfo.setIp(agentIdentity.getIp());
			agentInfo = agentRepository.save(agentInfo);
		}
		agentInfo.setPort(agentManager.getAgentConnectingPort(agentIdentity));
		agentInfo.setStatus(agentManager.getAgentState(agentIdentity));
		// need to save agent info into DB, like ip and port maybe changed.
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
		if (agentInfo == null) {
			return null;
		}
		AgentControllerIdentityImplementation agentIdentity = agentManager.getAgentIdentityByIp(agentInfo
						.getIp());
		if (agentIdentity != null) {
			agentInfo.setStatus(agentManager.getAgentState(agentIdentity));
			agentInfo.setPort(agentManager.getAgentConnectingPort(agentIdentity));
			agentInfo.setHostName(agentIdentity.getName());
			agentInfo.setRegion(agentIdentity.getRegion());
			agentInfo.setAgentIdentity(agentIdentity);
		}
		return agentInfo;
	}

	/**
	 * Save agent.
	 * 
	 * @param agent
	 *            saved agent
	 */
	@CacheEvict(allEntries = true, value = "agents")
	public void saveAgent(AgentInfo agent) {
		agentRepository.save(agent);
	}

	/**
	 * Delete agent.
	 * 
	 * @param id
	 *            agent id to be deleted
	 */
	@CacheEvict(allEntries = true, value = "agents")
	public void deleteAgent(long id) {
		agentRepository.delete(id);
	}

	/**
	 * Approve/Unapprove the agent on given ip.
	 * @param ip ip
	 * @param approve true/false
	 */
	@CacheEvict(allEntries = true, value = "agents")
	public void approve(String ip, boolean approve) {
		List<AgentInfo> found = agentRepository.findAllByIp(ip);
		for (AgentInfo each : found) {
			each.setApproved(approve);
			agentRepository.save(each);
			agentRepository.findOne(each.getId());
			if (approve) {
				LOGGER.info("agent {} is approved", ip);
			} else {
				LOGGER.info("agent {} is not approved", ip);
			}
		}

	}

	public void stopAgent(Long id) {
		AgentInfo agent = getAgent(id);
		if (agent == null) {
			return;
		}
		agentManager.stopAgent(agent.getAgentIdentity());
	}
}
