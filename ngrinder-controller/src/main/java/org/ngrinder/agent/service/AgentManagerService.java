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

import static org.ngrinder.agent.repository.AgentManagerSpecification.active;
import static org.ngrinder.common.util.TypeConvertUtil.convert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.engine.controller.AgentControllerIdentityImplementation;
import net.grinder.message.console.AgentControllerState;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.ngrinder.agent.model.AgentInfo;
import org.ngrinder.agent.repository.AgentManagerRepository;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.User;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.perftest.service.AgentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

/**
 * Agent manager service.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public class AgentManagerService {
	protected static final Logger LOGGER = LoggerFactory.getLogger(AgentManagerService.class);

	@Autowired
	private AgentManager agentManager;

	@Autowired
	private AgentManagerRepository agentRepository;

	@Autowired
	private Config config;

	/**
	 * Run a scheduled task to check the agent status.
	 * 
	 * This method has some
	 * 
	 * @since 3.1
	 */
	@Scheduled(fixedDelay = 5000)
	@Transactional
	public void checkAgentStatus() {
		List<AgentInfo> changeAgents = Lists.newArrayList();
		Set<AgentIdentity> allAttachedAgents = getAgentManager().getAllAttachedAgents();
		Map<String, AgentControllerIdentityImplementation> attachedAgentMap = Maps.newHashMap();
		for (AgentIdentity agentIdentity : allAttachedAgents) {
			AgentControllerIdentityImplementation agentControllerIdentity = convert(agentIdentity);
			attachedAgentMap.put(createAgentKey(agentControllerIdentity), agentControllerIdentity);
		}

		// If region is not specified retrieved all
		List<AgentInfo> agentsInDB = getAgentRepository().findAll();

		Multimap<String, AgentInfo> agentInDBMap = ArrayListMultimap.create();
		// step1. check all agents in DB, whether they are attached to
		// controller.
		for (AgentInfo each : agentsInDB) {
			agentInDBMap.put(createAgentKey(each), each);
		}

		List<AgentInfo> agentsToBeDeleted = Lists.newArrayList();
		for (String entryKey : agentInDBMap.keySet()) {
			Collection<AgentInfo> collection = agentInDBMap.get(entryKey);
			int count = 0;
			AgentInfo interestingAgentInfo = null;
			for (AgentInfo each : collection) {
				// Just select one element and delete others.
				if (count++ == 0) {
					interestingAgentInfo = each;
				} else {
					agentsToBeDeleted.add(each);
				}
			}
			if (interestingAgentInfo == null) {
				continue;
			}

			AgentControllerIdentityImplementation agentIdentity = attachedAgentMap.remove(entryKey);
			if (agentIdentity == null) {
				// this agent is not attached to controller
				interestingAgentInfo.setStatus(AgentControllerState.INACTIVE);
			} else {
				interestingAgentInfo.setStatus(getAgentManager().getAgentState(agentIdentity));
				interestingAgentInfo.setRegion(agentIdentity.getRegion());
				interestingAgentInfo.setPort(getAgentManager().getAgentConnectingPort(agentIdentity));
			}
			changeAgents.add(interestingAgentInfo);
		}

		// step2. check all attached agents, whether they are new, and not saved
		// in DB.
		for (AgentControllerIdentityImplementation agentIdentity : attachedAgentMap.values()) {
			changeAgents.add(fillUpAgentInfo(new AgentInfo(), agentIdentity));
		}

		// step3. update into DB
		getAgentRepository().save(changeAgents);
		getAgentRepository().delete(agentsToBeDeleted);
	}

	/**
	 * get the available agent count map in all regions of the user, including the free agents and
	 * user specified agents.
	 * 
	 * @param regions
	 *            current region list
	 * @param user
	 *            current user
	 * @return user available agent count map
	 */
	public Map<String, MutableInt> getUserAvailableAgentCountMap(User user) {
		int availableShareAgents = 0;
		int availableUserOwnAgent = 0;
		String myAgentSuffix = "owned_" + user.getUserId();
		for (AgentInfo agentInfo : getAllActiveAgentInfoFromDB()) {
			// Skip the all agents which doesn't approved, is inactive or
			// doesn't have region
			// prefix.
			if (!agentInfo.isApproved()) {
				continue;
			}
			String fullRegion = agentInfo.getRegion();

			// It's my own agent
			if (fullRegion.endsWith(myAgentSuffix)) {
				availableShareAgents++;
			} else if (fullRegion.contains("owned_")) {
				// If it's the others agent.. skip..
				continue;
			} else {
				availableUserOwnAgent++;
			}
		}

		int maxAgentSizePerConsole = getMaxAgentSizePerConsole();

		availableShareAgents = (Math.min(availableShareAgents, maxAgentSizePerConsole));

		Map<String, MutableInt> result = Maps.newHashMap();
		result.put(Config.NONE_REGION, new MutableInt(availableShareAgents + availableUserOwnAgent));
		return result;
	}

	int getMaxAgentSizePerConsole() {
		return getAgentManager().getMaxAgentSizePerConsole();
	}

	/**
	 * Get all agent agents. agent list is obtained from DB and {@link AgentManager}
	 * 
	 * This includes not persisted agent as well.
	 * 
	 * @return agent list
	 */
	@Transactional
	public List<AgentInfo> getLocalAgents() {
		Map<String, AgentInfo> agentInfoMap = createLocalAgentMapFromDB();
		Set<AgentIdentity> allAttachedAgents = getAgentManager().getAllAttachedAgents();
		List<AgentInfo> agentList = new ArrayList<AgentInfo>(allAttachedAgents.size());
		for (AgentIdentity eachAgentIdentity : allAttachedAgents) {
			AgentControllerIdentityImplementation agentControllerIdentity = convert(eachAgentIdentity);
			agentList.add(creatAgentInfo(agentControllerIdentity, agentInfoMap));
		}
		return agentList;
	}

	private Map<String, AgentInfo> createLocalAgentMapFromDB() {
		Map<String, AgentInfo> agentInfoMap = Maps.newHashMap();
		for (AgentInfo each : getLocalAgentListFromDB()) {
			agentInfoMap.put(createAgentKey(each), each);
		}
		return agentInfoMap;
	}

	public String createAgentKey(AgentInfo agentInfo) {
		return agentInfo.getIp() + "_" + agentInfo.getName();
	}

	public String createAgentKey(AgentControllerIdentityImplementation agentIdentity) {
		return agentIdentity.getIp() + "_" + agentIdentity.getName();
	}

	/**
	 * Get agent identity by ip and name
	 * 
	 * @param ip
	 *            ip
	 * @param name
	 *            name
	 * @return {@link AgentControllerIdentityImplementation} instance.
	 */
	public AgentControllerIdentityImplementation getLocalAgentIdentityByIpAndName(String ip, String name) {
		Set<AgentIdentity> allAttachedAgents = getAgentManager().getAllAttachedAgents();
		for (AgentIdentity eachAgentIdentity : allAttachedAgents) {
			AgentControllerIdentityImplementation convert = convert(eachAgentIdentity);
			if (StringUtils.equals(ip, convert.getIp()) && StringUtils.equals(name, convert.getName())) {
				return convert;
			}
		}
		return null;
	}

	/**
	 * Get all agents attached of this region from DB.
	 * 
	 * This method is cluster aware. If it's cluster mode it return all agents attached in this
	 * region.
	 * 
	 * @return agent list
	 */
	public List<AgentInfo> getLocalAgentListFromDB() {
		return getAgentRepository().findAll();
	}

	/**
	 * Get all active agents from DB.
	 * 
	 * @return agent list
	 */
	public List<AgentInfo> getAllActiveAgentInfoFromDB() {
		return getAgentRepository().findAll(active());
	}

	private AgentInfo creatAgentInfo(AgentControllerIdentityImplementation agentIdentity,
					Map<String, AgentInfo> agentInfoMap) {
		AgentInfo agentInfo = agentInfoMap.get(createAgentKey(agentIdentity));
		if (agentInfo == null) {
			agentInfo = new AgentInfo();
		}
		return fillUpAgentInfo(agentInfo, agentIdentity);
	}

	protected AgentInfo fillUpAgentInfo(AgentInfo agentInfo, AgentControllerIdentityImplementation agentIdentity) {
		if (agentIdentity != null) {
			agentInfo.setAgentIdentity(agentIdentity);
			agentInfo.setName(agentIdentity.getName());
			agentInfo.setNumber(agentIdentity.getNumber());
			agentInfo.setRegion(agentIdentity.getRegion());
			agentInfo.setIp(agentIdentity.getIp());
			agentInfo.setPort(getAgentManager().getAgentConnectingPort(agentIdentity));
			agentInfo.setStatus(getAgentManager().getAgentState(agentIdentity));
		}
		return agentInfo;
	}

	/**
	 * Get a agent on given id. If it's called from the other controller, only limited info
	 * available in db will be return.
	 * 
	 * @param id
	 *            agent id
	 * @param includeAgentIndentity
	 *            include agent identity
	 * @return agent
	 */
	public AgentInfo getAgent(long id, boolean includeAgentIndentity) {
		AgentInfo findOne = getAgentRepository().findOne(id);
		if (includeAgentIndentity) {
			AgentControllerIdentityImplementation agentIdentityByIp = getLocalAgentIdentityByIpAndName(findOne.getIp(),
							findOne.getName());
			return fillUpAgentInfo(findOne, agentIdentityByIp);
		} else {
			return findOne;
		}
	}

	/**
	 * Save agent.
	 * 
	 * @param agent
	 *            saved agent
	 */
	public void saveAgent(AgentInfo agent) {
		getAgentRepository().save(agent);
	}

	/**
	 * Delete agent.
	 * 
	 * @param id
	 *            agent id to be deleted
	 */
	public void deleteAgent(long id) {
		getAgentRepository().delete(id);
	}

	/**
	 * Approve/Unapprove the agent on given id.
	 * 
	 * @param id
	 *            id
	 * @param approve
	 *            true/false
	 */
	@Transactional
	public void approve(Long id, boolean approve) {
		AgentInfo found = getAgentRepository().findOne(id);
		if (found != null) {
			found.setApproved(approve);
			getAgentRepository().save(found);
			getAgentRepository().findOne(found.getId());
		}

	}

	/**
	 * Stop agent. If it's in cluster mode, it queue to agentRequestCache. ohterwise, it send stop
	 * message to the agent.
	 * 
	 * @param id
	 *            identity of agent to stop.
	 */
	@Transactional
	public void stopAgent(Long id) {
		AgentInfo agent = getAgent(id, true);
		if (agent == null) {
			return;
		}
		getAgentManager().stopAgent(agent.getAgentIdentity());
	}

	/**
	 * Add the agent system data model share request on cache.
	 * 
	 * @param id
	 *            agent id.
	 */
	public void requestShareAgentSystemDataModel(Long id) {

	}

	/**
	 * Get agent system data model for the given ip. This method is cluster aware.
	 * 
	 * @param ip
	 *            agent ip.
	 * @param name
	 *            agent name
	 * @return {@link SystemDataModel} instance.
	 */
	public SystemDataModel getAgentSystemDataModel(String ip, String name) {
		return getAgentManager().getSystemDataModel(getLocalAgentIdentityByIpAndName(ip, name));
	}

	AgentManager getAgentManager() {
		return agentManager;
	}

	void setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
	}

	AgentManagerRepository getAgentRepository() {
		return agentRepository;
	}

	void setAgentRepository(AgentManagerRepository agentRepository) {
		this.agentRepository = agentRepository;
	}

	Config getConfig() {
		return config;
	}

	void setConfig(Config config) {
		this.config = config;
	}
}
