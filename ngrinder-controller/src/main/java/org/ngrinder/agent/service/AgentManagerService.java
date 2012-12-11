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
import static org.ngrinder.agent.repository.AgentManagerSpecification.startWithRegion;
import static org.ngrinder.common.util.TypeConvertUtil.convert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.engine.controller.AgentControllerIdentityImplementation;
import net.grinder.message.console.AgentControllerState;
import net.grinder.util.thread.InterruptibleRunnable;
import net.sf.ehcache.Ehcache;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.ngrinder.agent.model.AgentInfo;
import org.ngrinder.agent.model.AgentRequest;
import org.ngrinder.agent.model.AgentRequest.RequestType;
import org.ngrinder.agent.repository.AgentManagerRepository;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.logger.CoreLogger;
import org.ngrinder.infra.schedule.ScheduledTask;
import org.ngrinder.model.User;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.perftest.service.AgentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
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

	@Autowired
	private Config config;

	@Autowired
	private CacheManager cacheManager;

	private Cache agentRequestCache;

	private Cache agentMonitorCache;

	private Cache agentMonioringTargetsCache;

	@Autowired
	private ScheduledTask scheduledTask;

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void init() {
		if (getConfig().isCluster()) {
			agentRequestCache = getCacheManager().getCache("agent_request");
			agentMonitorCache = getCacheManager().getCache("agent_monitoring");
			agentMonioringTargetsCache = getCacheManager().getCache("agent_monitoring_targets");
			scheduledTask.addScheduledTaskEvery3Sec(new InterruptibleRunnable() {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				@Override
				public void interruptibleRun() {
					List keysWithExpiryCheck = ((Ehcache) agentRequestCache.getNativeCache()).getKeysWithExpiryCheck();
					String region = getConfig().getRegion() + "_";
					for (String each : (List<String>) keysWithExpiryCheck) {
						try {
							if (each.startsWith(region) && agentRequestCache.get(each) != null) {
								AgentRequest agentRequest = (AgentRequest) (agentRequestCache.get(each).get());
								AgentControllerIdentityImplementation agentIdentity = getAgentIdentityByIp(agentRequest
												.getAgentIp());
								if (agentIdentity != null) {
									agentRequest.getRequestType().process(agentRequest.getAgentId(),
													agentRequest.getAgentIp(), getAgentManager(),
													AgentManagerService.this, agentIdentity);
								}
							}

						} catch (Exception e) {
							CoreLogger.LOGGER.error(e.getMessage(), e);
						}
						agentRequestCache.evict(each);
					}
				}
			});
		}
	}

	/**
	 * Run a scheduled task to check the agent status.
	 * 
	 * This method has some
	 * 
	 * @since 3.1
	 */
	@Scheduled(fixedDelay = 5000)
	public void checkAgentStatus() {
		List<AgentInfo> changeAgentList = new ArrayList<AgentInfo>();

		Set<AgentIdentity> allAttachedAgents = getAgentManager().getAllAttachedAgents();
		Map<String, AgentControllerIdentityImplementation> attachedAgentMap = createMap(allAttachedAgents);
		for (AgentIdentity agentIdentity : allAttachedAgents) {
			AgentControllerIdentityImplementation agentControllerIdentity = convert(agentIdentity);
			attachedAgentMap.put(agentControllerIdentity.getIp(), agentControllerIdentity);
		}

		String region = getConfig().getRegion();
		List<AgentInfo> agentsInDB = (StringUtils.equals(region, "NONE")) ? getAgentRepository().findAll()
						: getAgentRepository().findAll(startWithRegion(region));
		Map<String, AgentInfo> agentsInDBMap = new HashMap<String, AgentInfo>(agentsInDB.size());
		// step1. check all agents in DB, whether they are attached to
		// controller.
		for (AgentInfo agentInfoInDB : agentsInDB) {
			agentsInDBMap.put(agentInfoInDB.getIp(), agentInfoInDB);
			AgentControllerIdentityImplementation agentIdentity = attachedAgentMap.get(agentInfoInDB.getIp());
			if (agentIdentity == null) {
				// this agent is not attached to controller
				agentInfoInDB.setStatus(AgentControllerState.INACTIVE);
			} else {
				agentInfoInDB.setStatus(getAgentManager().getAgentState(agentIdentity));
				agentInfoInDB.setNumber(agentIdentity.getNumber());
				agentInfoInDB.setRegion(agentIdentity.getRegion());
				agentInfoInDB.setPort(getAgentManager().getAgentConnectingPort(agentIdentity));
			}
			changeAgentList.add(agentInfoInDB);
		}

		// step2. check all attached agents, whether they are new, and not saved
		// in DB.
		for (AgentControllerIdentityImplementation agentIdentity : attachedAgentMap.values()) {
			if (!agentsInDBMap.containsKey(agentIdentity.getIp())
							&& StringUtils.equals(agentIdentity.getRegion(), region)) {
				changeAgentList.add(fillUpAgentInfo(new AgentInfo(), agentIdentity));
			}
		}

		// step3. update into DB
		getAgentRepository().save(changeAgentList);
	}

	/**
	 * collect agent system data every 1 sec.
	 * 
	 */
	@Scheduled(fixedDelay = 1000)
	public void collectAgentSystemData() {
		Ehcache nativeCache = (Ehcache) agentMonioringTargetsCache.getNativeCache();
		List<String> keysWithExpiryCheck = convert(nativeCache.getKeysWithExpiryCheck());
		for (String each : keysWithExpiryCheck) {
			ValueWrapper value = agentMonioringTargetsCache.get(each);
			if (value != null && value.get() != null) {
				agentMonitorCache.put(each, getAgentManager().getSystemDataModel((AgentIdentity) value.get()));
			}
		}
	}

	private HashMap<String, AgentControllerIdentityImplementation> createMap(Set<AgentIdentity> allAttachedAgents) {
		return new HashMap<String, AgentControllerIdentityImplementation>(allAttachedAgents.size());
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
	public Map<String, MutableInt> getUserAvailableAgentCountMap(List<String> regions, User user) {
		Map<String, MutableInt> availableShareAgents = new HashMap<String, MutableInt>(regions.size());
		Map<String, MutableInt> availableUserOwnAgent = new HashMap<String, MutableInt>(regions.size());
		for (String region : regions) {
			availableShareAgents.put(region, new MutableInt(0));
			availableUserOwnAgent.put(region, new MutableInt(0));
		}
		String myAgentSuffix = "_owned_" + user.getUserId();
		boolean clusterMode = config.isCluster();
		for (AgentInfo agentInfo : getAllActiveAgentInfoFromDB()) {
			// Skip the all agents which doesn't approved, is inactive or
			// doesn't have region
			// prefix.
			if (!agentInfo.isApproved()) {
				continue;
			}
			String fullRegion = agentInfo.getRegion();
			String region = extractRegionFromAgentRegion(fullRegion);
			if (StringUtils.isBlank(region) && clusterMode) {
				continue;
			}
			// It's my own agent
			if (fullRegion.endsWith(myAgentSuffix)) {
				incrementAgentCount(availableUserOwnAgent, region, user.getUserId());
			} else if (fullRegion.contains("_owned_")) {
				// If it's the others agent.. skip..
				continue;
			} else {
				incrementAgentCount(availableShareAgents, region, user.getUserId());
			}
		}

		int maxAgentSizePerConsole = getMaxAgentSizePerConsole();

		for (String region : regions) {
			MutableInt mutableInt = availableShareAgents.get(region);
			int shareAgentCount = mutableInt.intValue();
			mutableInt.setValue(Math.min(shareAgentCount, maxAgentSizePerConsole));
			mutableInt.add(availableUserOwnAgent.get(region));
		}
		return availableShareAgents;
	}

	int getMaxAgentSizePerConsole() {
		return getAgentManager().getMaxAgentSizePerConsole();
	}

	private void incrementAgentCount(Map<String, MutableInt> agentMap, String region, String userId) {
		if (!agentMap.containsKey(region)) {
			LOGGER.warn("Region :{} not exist in cluster nor owned by user:{}.", region, userId);
		} else {
			agentMap.get(region).increment();
		}
	}

	String extractRegionFromAgentRegion(String agentRegion) {
		if (agentRegion.contains("_owned_")) {
			return agentRegion.substring(0, agentRegion.lastIndexOf("_owned_"));
		}
		return agentRegion;
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
		Set<AgentIdentity> allAttachedAgents = getAgentManager().getAllAttachedAgents();
		List<AgentInfo> agents = getAgentRepository().findAll(startWithRegion(getConfig().getRegion()));
		List<AgentInfo> agentList = new ArrayList<AgentInfo>(allAttachedAgents.size());
		for (AgentIdentity eachAgentIdentity : allAttachedAgents) {
			AgentControllerIdentityImplementation agentControllerIdentity = convert(eachAgentIdentity);
			agentList.add(creatAgentInfo(agentControllerIdentity, agents));
		}
		return agentList;
	}

	/**
	 * Get agent identity by ip.
	 * 
	 * @param ip
	 *            ip
	 * @return {@link AgentControllerIdentityImplementation} instance.
	 */
	public AgentControllerIdentityImplementation getAgentIdentityByIp(String ip) {
		Set<AgentIdentity> allAttachedAgents = getAgentManager().getAllAttachedAgents();
		for (AgentIdentity eachAgentIdentity : allAttachedAgents) {
			AgentControllerIdentityImplementation convert = convert(eachAgentIdentity);
			if (ip.equals(convert.getIp())) {
				return convert;
			}
		}
		return null;
	}

	/**
	 * Get all agents attached of this region from DB.
	 * 
	 * @return agent list
	 */
	public List<AgentInfo> getAgentListInThisRegionFromDB() {
		return getAgentRepository().findAll(startWithRegion(getConfig().getRegion()));
	}

	/**
	 * Get all active agents from DB.
	 * 
	 * @return agent list
	 */
	public List<AgentInfo> getAllActiveAgentInfoFromDB() {
		return getAgentRepository().findAll(active());
	}

	private AgentInfo creatAgentInfo(AgentControllerIdentityImplementation agentIdentity, List<AgentInfo> agents) {
		AgentInfo agentInfo = new AgentInfo();
		for (AgentInfo each : agents) {
			// should use IP and number to identify an agent, but now number is
			// not used, it is
			// always -1.
			if (StringUtils.equals(each.getIp(), agentIdentity.getIp())) {
				agentInfo = each;
				break;
			}
		}
		return fillUpAgentInfo(agentInfo, agentIdentity);
	}

	private AgentInfo fillUpAgentInfo(AgentInfo agentInfo, AgentControllerIdentityImplementation agentIdentity) {
		if (agentIdentity != null) {
			agentInfo.setAgentIdentity(agentIdentity);
			agentInfo.setHostName(agentIdentity.getName());
			agentInfo.setNumber(agentIdentity.getNumber());
			agentInfo.setRegion(agentIdentity.getRegion());
			agentInfo.setIp(agentIdentity.getIp());
			agentInfo.setPort(getAgentManager().getAgentConnectingPort(agentIdentity));
			agentInfo.setStatus(getAgentManager().getAgentState(agentIdentity));
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
		AgentInfo findOne = getAgentRepository().findOne(id);
		AgentControllerIdentityImplementation agentIdentityByIp = getAgentIdentityByIp(findOne.getIp());
		return fillUpAgentInfo(findOne, agentIdentityByIp);
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
	public void stopAgent(Long id) {
		AgentInfo agent = getAgent(id);
		if (agent == null) {
			return;
		}
		if (getConfig().isCluster()) {
			agentRequestCache.put(
							extractRegionFromAgentRegion(agent.getRegion()) + "_" + agent.getId() + "_stop_agent",
							new AgentRequest(agent.getId(), agent.getIp(), RequestType.STOP_AGENT));
		} else {
			getAgentManager().stopAgent(agent.getAgentIdentity());
		}
	}

	/**
	 * Add the agent system data model share request on cache.
	 * 
	 * @param id
	 *            agent id.
	 */
	public void requestShareAgentSystemDataModel(Long id) {
		if (getConfig().isCluster()) {
			AgentInfo agent = getAgent(id);
			agentRequestCache.put(
							extractRegionFromAgentRegion(agent.getRegion()) + "_" + agent.getId() + "_monitoring",
							new AgentRequest(agent.getId(), agent.getIp(), RequestType.SHARE_AGENT_SYSTEM_DATA_MODEL));
		}
	}

	/**
	 * Get agent system data model for the given ip. This method is cluster aware.
	 * 
	 * @param ip
	 *            agent ip.
	 * @return {@link SystemDataModel} instance.
	 */
	public SystemDataModel getAgentSystemDataModel(String ip) {

		if (getConfig().isCluster()) {
			ValueWrapper valueWrapper = agentMonitorCache.get(ip);
			return valueWrapper == null ? new SystemDataModel() : (SystemDataModel) valueWrapper.get();
		} else {
			return getAgentManager().getSystemDataModel(getAgentIdentityByIp(ip));
		}
	}

	/**
	 * Register agent monitoring target. This method should be called in the controller in which the
	 * given agent exists.
	 * 
	 * @param agentId
	 *            agent id
	 * @param agentIp
	 *            agent ip
	 * @param agentIdentity
	 *            agent identity
	 */
	public void addAgentMonitoringTarget(Long agentId, String agentIp,
					AgentControllerIdentityImplementation agentIdentity) {
		agentMonioringTargetsCache.put(agentIp, agentIdentity);
	}

	AgentManager getAgentManager() {
		return agentManager;
	}

	void setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
	}

	CacheManager getCacheManager() {
		return cacheManager;
	}

	void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
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
