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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.engine.controller.AgentControllerIdentityImplementation;
import net.grinder.message.console.AgentControllerState;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.ngrinder.agent.repository.AgentManagerRepository;
import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.schedule.ScheduledTaskService;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.model.User;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.perftest.service.AgentManager;
import org.ngrinder.service.AbstractAgentManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;

import static org.ngrinder.common.util.CollectionUtils.newArrayList;
import static org.ngrinder.common.util.CollectionUtils.newHashMap;
import static org.ngrinder.common.util.NoOp.noOp;
import static org.ngrinder.common.util.TypeConvertUtils.cast;

/**
 * Agent manager service.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public class AgentManagerService extends AbstractAgentManagerService {
	protected static final Logger LOGGER = LoggerFactory.getLogger(AgentManagerService.class);

	@Autowired
	private AgentManager agentManager;

	@Autowired
	protected AgentManagerRepository agentManagerRepository;

	@Autowired
	protected LocalAgentService cachedLocalAgentService;

	@Autowired
	private Config config;

	@Autowired
	protected ScheduledTaskService scheduledTaskService;

	private Runnable runnable;

	@PostConstruct
	public void init() {
		runnable = new Runnable() {
			@Override
			public void run() {
				checkAgentStatePeriodically();
			}
		};
		scheduledTaskService.addFixedDelayedScheduledTaskInTransactionContext(runnable, 1000);
	}

	@PreDestroy
	public void destroy() {
		scheduledTaskService.removeScheduledJob(runnable);
	}

	public void checkAgentStatePeriodically() {
		checkAgentState();
	}

	public void checkAgentState() {
		List<AgentInfo> newAgents = newArrayList(0);
		List<AgentInfo> updatedAgents = newArrayList(0);
		List<AgentInfo> stateUpdatedAgents = newArrayList(0);
		List<AgentInfo> removedAgents = newArrayList(0);

		Set<AgentIdentity> allAttachedAgents = getAgentManager().getAllAttachedAgents();
		Map<String, AgentControllerIdentityImplementation> attachedAgentMap = Maps.newHashMap();
		for (AgentIdentity agentIdentity : allAttachedAgents) {
			AgentControllerIdentityImplementation agentControllerIdentity = cast(agentIdentity);
			attachedAgentMap.put(createKey(agentControllerIdentity), agentControllerIdentity);
		}

		// If region is not specified retrieved all
		Map<String, AgentInfo> agentInDBMap = newHashMap();
		// step1. check all agents in DB, whether they are attached to
		// controller.
		for (AgentInfo each : getAllLocal()) {
			final String agentKey = createKey(each);
			if (!agentInDBMap.containsKey(agentKey)) {
				agentInDBMap.put(agentKey, each);
			} else {
				removedAgents.add(each);
			}
		}

		for (Map.Entry<String, AgentInfo> each : agentInDBMap.entrySet()) {
			String agentKey = each.getKey();
			AgentInfo agentInfo = each.getValue();
			AgentControllerIdentityImplementation agentIdentity = attachedAgentMap.remove(agentKey);
			if (agentIdentity == null) {
				// this agent is not attached to controller
				agentInfo.setState(AgentControllerState.INACTIVE);
				stateUpdatedAgents.add(agentInfo);
			} else if (!hasSameInfo(agentInfo, agentIdentity)) {
				agentInfo.setRegion(agentIdentity.getRegion());
				agentInfo.setPort(agentManager.getAgentConnectingPort(agentIdentity));
				agentInfo.setVersion(agentManager.getAgentVersion(agentIdentity));
				updatedAgents.add(agentInfo);
			} else if (!hasSameState(agentInfo, agentIdentity)) {
				agentInfo.setState(agentManager.getAgentState(agentIdentity));
				stateUpdatedAgents.add(agentInfo);
			}
		}

		// step2. check all attached agents, whether they are new, and not saved
		// in DB.
		for (AgentControllerIdentityImplementation agentIdentity : attachedAgentMap.values()) {
			final AgentInfo agentInfo = fillUp(new AgentInfo(), agentIdentity);
			newAgents.add(agentInfo);
		}
		cachedLocalAgentService.updateAgents(newAgents, updatedAgents, stateUpdatedAgents, removedAgents);
		if (!newAgents.isEmpty() || !removedAgents.isEmpty()) {
			expireLocalCache();
		}
	}

	public String extractRegionFromAgentRegion(String agentRegion) {
		if (agentRegion != null && agentRegion.contains("_owned_")) {
			return agentRegion.substring(0, agentRegion.indexOf("_owned_"));
		}
		if (agentRegion != null && agentRegion.contains("owned_")) {
			return agentRegion.substring(0, agentRegion.indexOf("owned_"));
		}
		if (StringUtils.isEmpty(agentRegion)) {
			return Config.NONE_REGION;
		}
		return agentRegion;
	}

	protected boolean hasSameInfo(AgentInfo agentInfo, AgentControllerIdentityImplementation agentIdentity) {
		return agentInfo != null &&
				agentInfo.getPort() == agentManager.getAgentConnectingPort(agentIdentity) &&
				StringUtils.equals(agentInfo.getRegion(), agentIdentity.getRegion()) &&
				StringUtils.equals(StringUtils.trimToNull(agentInfo.getVersion()),
						StringUtils.trimToNull(agentManager.getAgentVersion(agentIdentity)));
	}

	protected boolean hasSameState(AgentInfo agentInfo, AgentControllerIdentityImplementation agentIdentity) {
		return agentInfo != null &&
				agentInfo.getState() == agentManager.getAgentState(agentIdentity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ngrinder.service.IAgentManagerService#getAvailableAgentCountMap
	 * (org.ngrinder .model.User)
	 */
	@Override
	public Map<String, MutableInt> getAvailableAgentCountMap(User user) {
		int availableShareAgents = 0;
		int availableUserOwnAgent = 0;
		String myAgentSuffix = "owned_" + user.getUserId();
		for (AgentInfo agentInfo : getAllActive()) {
			// Skip all agents which are disapproved, inactive or
			// have no region prefix.
			if (!agentInfo.isApproved()) {
				continue;
			}
			String fullRegion = agentInfo.getRegion();
			// It's this controller's agent
			if (StringUtils.endsWithIgnoreCase(fullRegion, myAgentSuffix)) {
				availableUserOwnAgent++;
			} else if (!StringUtils.containsIgnoreCase(fullRegion, "owned_")) {
				availableShareAgents++;
			}
		}

		int maxAgentSizePerConsole = getMaxAgentSizePerConsole();
		availableShareAgents = (Math.min(availableShareAgents, maxAgentSizePerConsole));
		Map<String, MutableInt> result = new HashMap<String, MutableInt>(1);
		result.put(Config.NONE_REGION, new MutableInt(availableShareAgents + availableUserOwnAgent));
		return result;
	}

	int getMaxAgentSizePerConsole() {
		return getAgentManager().getMaxAgentSizePerConsole();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ngrinder.service.IAgentManagerService#getAllLocalWithFullInfo()
	 */
	@Override
	@Transactional
	public List<AgentInfo> getAllLocalWithFullInfo() {
		Map<String, AgentInfo> agentInfoMap = createLocalAgentMap();
		Set<AgentIdentity> allAttachedAgents = getAgentManager().getAllAttachedAgents();
		List<AgentInfo> agentList = new ArrayList<AgentInfo>(allAttachedAgents.size());
		for (AgentIdentity eachAgentIdentity : allAttachedAgents) {
			AgentControllerIdentityImplementation agentControllerIdentity = cast(eachAgentIdentity);
			agentList.add(createAgentInfo(agentControllerIdentity, agentInfoMap));
		}
		return agentList;
	}

	private Map<String, AgentInfo> createLocalAgentMap() {
		Map<String, AgentInfo> agentInfoMap = Maps.newHashMap();
		for (AgentInfo each : getAllLocal()) {
			agentInfoMap.put(createKey(each), each);
		}
		return agentInfoMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ngrinder.service.IAgentManagerService#createKey(org.ngrinder
	 * .agent.model.AgentInfo )
	 */
	@Override
	public String createKey(AgentInfo agentInfo) {
		return createAgentKey(agentInfo.getIp(), agentInfo.getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ngrinder.service.IAgentManagerService#createKey(net.grinder
	 * .engine.controller .AgentControllerIdentityImplementation)
	 */
	@Override
	public String createKey(AgentControllerIdentityImplementation agentIdentity) {
		return createAgentKey(agentIdentity.getIp(), agentIdentity.getName());
	}

	protected String createAgentKey(String ip, String name) {
		return ip + "_" + name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.service.IAgentManagerService#
	 * getAgentIdentityByIpAndName(java.lang .String, java.lang.String)
	 */
	@Override
	public AgentControllerIdentityImplementation getAgentIdentityByIpAndName(String ip, String name) {
		Set<AgentIdentity> allAttachedAgents = getAgentManager().getAllAttachedAgents();
		for (AgentIdentity eachAgentIdentity : allAttachedAgents) {
			AgentControllerIdentityImplementation agentIdentity = cast(eachAgentIdentity);
			if (StringUtils.equals(ip, agentIdentity.getIp()) && StringUtils.equals(name, agentIdentity.getName())) {
				return agentIdentity;
			}
		}
		return null;
	}


	public List<AgentInfo> getAllLocal() {
		return Collections.unmodifiableList(cachedLocalAgentService.getLocalAgents());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ngrinder.service.IAgentManagerService#getAllActive
	 * ()
	 *
	 */
	@Override
	public List<AgentInfo> getAllActive() {
		List<AgentInfo> agents = Lists.newArrayList();
		for (AgentInfo agentInfo : getAllLocal()) {
			final AgentControllerState state = agentInfo.getState();
			if (state != null && state.isActive()) {
				agents.add(agentInfo);
			}
		}
		return agents;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ngrinder.service.IAgentManagerService#getAllVisible
	 * ()
	 */
	@Override
	public List<AgentInfo> getAllVisible() {
		List<AgentInfo> agents = Lists.newArrayList();
		for (AgentInfo agentInfo : getAllLocal()) {
			final AgentControllerState state = agentInfo.getState();
			if (state != null && state.isActive()) {
				agents.add(agentInfo);
			}
		}
		return agents;
	}

	private AgentInfo createAgentInfo(AgentControllerIdentityImplementation agentIdentity,
	                                  Map<String, AgentInfo> agentInfoMap) {
		AgentInfo agentInfo = agentInfoMap.get(createKey(agentIdentity));
		if (agentInfo == null) {
			agentInfo = new AgentInfo();
		}
		return fillUp(agentInfo, agentIdentity);
	}

	protected AgentInfo fillUp(AgentInfo agentInfo, AgentControllerIdentityImplementation agentIdentity) {
		fillUpApproval(agentInfo);
		if (agentIdentity != null) {
			agentInfo.setAgentIdentity(agentIdentity);
			agentInfo.setName(agentIdentity.getName());
			agentInfo.setRegion(agentIdentity.getRegion());
			agentInfo.setIp(agentIdentity.getIp());
			AgentManager agentManager = getAgentManager();
			agentInfo.setPort(agentManager.getAgentConnectingPort(agentIdentity));
			agentInfo.setState(agentManager.getAgentState(agentIdentity));
			agentInfo.setVersion(agentManager.getAgentVersion(agentIdentity));
		}
		return agentInfo;
	}

	protected AgentInfo fillUpApproval(AgentInfo agentInfo) {
		if (agentInfo.getApproved() == null) {
			final boolean approved = config.getControllerProperties().getPropertyBoolean(ControllerConstants
					.PROP_CONTROLLER_ENABLE_AGENT_AUTO_APPROVAL);
			agentInfo.setApproved(approved);
		}
		return agentInfo;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ngrinder.service.IAgentManagerService#getAll(long,
	 * boolean)
	 */
	@Override
	public AgentInfo getOne(Long id) {
		return getOne(id, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.service.IAgentManagerService#getAll(long,
	 * boolean)
	 */
	@Override
	public AgentInfo getOne(Long id, boolean includeAgentIdentity) {
		AgentInfo findOne = agentManagerRepository.findOne(id);
		if (findOne == null) {
			return null;
		}
		if (includeAgentIdentity) {
			AgentControllerIdentityImplementation agentIdentityByIp = getAgentIdentityByIpAndName(findOne.getIp(),
					findOne.getName());
			return fillUp(findOne, agentIdentityByIp);
		} else {
			return findOne;
		}
	}

	/**
	 * Approve/disapprove the agent on given id.
	 *
	 * @param id      id
	 * @param approve true/false
	 */
	@Transactional
	public AgentInfo approve(Long id, boolean approve) {
		AgentInfo found = agentManagerRepository.findOne(id);
		if (found != null) {
			found.setApproved(approve);
			agentManagerRepository.save(found);
			expireLocalCache();
		}
		return found;
	}

	/**
	 * Stop agent. If it's in cluster mode, it queue to agentRequestCache.
	 * otherwise, it send stop message to the agent.
	 *
	 * @param id identity of agent to stop.
	 */
	@Transactional
	public void stopAgent(Long id) {
		AgentInfo agent = getOne(id, true);
		if (agent == null) {
			return;
		}
		getAgentManager().stopAgent(agent.getAgentIdentity());
	}

	/**
	 * Add the agent system data model share request on cache.
	 *
	 * @param id agent id.
	 */
	public void requestShareAgentSystemDataModel(Long id) {
		noOp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ngrinder.service.IAgentManagerService#getSystemDataModel
	 * (java.lang.String, java.lang.String)
	 */
	@Override
	public SystemDataModel getSystemDataModel(String ip, String name) {
		AgentControllerIdentityImplementation agentIdentity = getAgentIdentityByIpAndName(ip, name);
		return agentIdentity != null ? getAgentManager().getSystemDataModel(agentIdentity) : new SystemDataModel();
	}

	AgentManager getAgentManager() {
		return agentManager;
	}

	void setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
	}

	public void setAgentManagerRepository(AgentManagerRepository agentManagerRepository) {
		this.agentManagerRepository = agentManagerRepository;
	}

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.ngrinder.service.IAgentManagerService#updateAgentLib
	 * (java.lang.String)
	 */
	@Override
	public void update(Long id) {
		AgentInfo agent = getOne(id, true);
		if (agent == null) {
			return;
		}
		updateAgent(agent.getAgentIdentity());
	}

	/**
	 * Update the agent
	 *
	 * @param agentIdentity agent identity to be updated.
	 */
	public void updateAgent(AgentIdentity agentIdentity) {
		agentManager.updateAgent(agentIdentity, shouldUpdateAgentAlways() ? "99.99" : config.getVersion());
	}


	protected boolean shouldUpdateAgentAlways() {
		return config.getControllerProperties().getPropertyBoolean(ControllerConstants.PROP_CONTROLLER_AGENT_FORCE_UPDATE);
	}


	public void expireLocalCache() {
		cachedLocalAgentService.expireCache();
	}

	/**
	 * Clean up the agents from db which belongs to the inactive regions.
	 * Do nothing in not cluster mode.
	 */
	@Transactional
	public void cleanup() {
		for (AgentInfo each : agentManagerRepository.findAll()) {
			if (!each.getState().isActive()) {
				agentManagerRepository.delete(each);
			}
		}
	}

	/**
	 * All ready state agent return
	 */
	List<AgentInfo> getAllReady() {
		List<AgentInfo> agents = Lists.newArrayList();
		for (AgentInfo agentInfo : getAllLocal()) {
			final AgentControllerState state = agentInfo.getState();
			if (state != null && state.isReady()) {
				agents.add(agentInfo);
			}
		}
		return agents;
	}

	/**
	 * Ready agent state count return
	 *
	 * @param user The login user
	 * @param String targetRegion The name of target region
	 * @return ready Agent count
	 */
	@Override
	public int getReadyAgentCount(User user, String targetRegion) {
		int readyAgentCnt = 0;
		String myOwnAgent = targetRegion + "_owned_" + user.getUserId();
		for (AgentInfo agentInfo : getAllReady()) {
			if (!agentInfo.isApproved()) {
				continue;
			}
			String fullRegion = agentInfo.getRegion();
			if (StringUtils.equals(fullRegion, targetRegion) || StringUtils.equals(fullRegion, myOwnAgent)) {
				readyAgentCnt++;
			}
		}
		return readyAgentCnt;
	}
	
}
