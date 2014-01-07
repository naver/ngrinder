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
import com.google.gson.Gson;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.engine.controller.AgentControllerIdentityImplementation;
import net.sf.ehcache.Ehcache;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.ngrinder.agent.model.ClusteredAgentRequest;
import org.ngrinder.infra.logger.CoreLogger;
import org.ngrinder.infra.schedule.ScheduledTaskService;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.model.User;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.region.service.RegionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.grinder.message.console.AgentControllerState.INACTIVE;
import static net.grinder.message.console.AgentControllerState.WRONG_REGION;
import static org.ngrinder.agent.model.ClusteredAgentRequest.RequestType.*;
import static org.ngrinder.agent.repository.AgentManagerSpecification.startWithRegion;
import static org.ngrinder.agent.repository.AgentManagerSpecification.visible;
import static org.ngrinder.common.util.CollectionUtils.newArrayList;
import static org.ngrinder.common.util.CollectionUtils.newHashMap;
import static org.ngrinder.common.util.TypeConvertUtils.cast;

/**
 * Cluster enabled version of {@link AgentManagerService}.
 *
 * @author JunHo Yoon
 * @since 3.1
 */
public class ClusteredAgentManagerService extends AgentManagerService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClusteredAgentManagerService.class);

	@Autowired
	CacheManager cacheManager;

	private Cache agentRequestCache;

	private Cache agentMonitoringTargetsCache;

	@Autowired
	private ScheduledTaskService scheduledTaskService;

	@Autowired
	private RegionService regionService;

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void init() {
		agentMonitoringTargetsCache = cacheManager.getCache("agent_monitoring_targets");
		if (getConfig().isClustered()) {
			agentRequestCache = cacheManager.getCache("agent_request");
			scheduledTaskService.addFixedDelayedScheduledTask(new Runnable() {
				@Override
				public void run() {
					List<String> keys = cast(((Ehcache) agentRequestCache.getNativeCache())
							.getKeysWithExpiryCheck());
					String region = getConfig().getRegion() + "|";
					for (String each : keys) {
						if (each.startsWith(region)) {
							if (agentRequestCache.get(each) != null) {
								try {
									ClusteredAgentRequest agentRequest = cast(agentRequestCache.get(each).get());
									AgentControllerIdentityImplementation agentIdentity = getLocalAgentIdentityByIpAndName(
											agentRequest.getAgentIp(), agentRequest.getAgentName());
									if (agentIdentity != null) {
										agentRequest.getRequestType().process(ClusteredAgentManagerService.this,
												agentIdentity);
									}
									agentRequestCache.evict(each);
								} catch (Exception e) {
									CoreLogger.LOGGER.error(e.getMessage(), e);
								}
							}
						}
					}
				}
			}, 3000);
		}
	}

	/**
	 * Run a scheduled task to check the agent statuses.
	 *
	 * @since 3.1
	 */

	public void checkAgentState() {
		List<AgentInfo> changeAgents = newArrayList();

		Set<AgentIdentity> allAttachedAgents = getAgentManager().getAllAttachedAgents();
		Map<String, AgentControllerIdentityImplementation> attachedAgentMap = newHashMap(allAttachedAgents);
		for (AgentIdentity agentIdentity : allAttachedAgents) {
			AgentControllerIdentityImplementation existingAgent = cast(agentIdentity);
			attachedAgentMap.put(createAgentKey(existingAgent), existingAgent);
		}
		Map<String, AgentInfo> agentsInDBMap = Maps.newHashMap();
		// step1. check all agents in DB, whether they are attached to
		// controller.
		for (AgentInfo eachAgentInDB : getLocalAgentsFromDB()) {
			String keyOfAgentInDB = createAgentKey(eachAgentInDB);
			agentsInDBMap.put(keyOfAgentInDB, eachAgentInDB);
			AgentControllerIdentityImplementation agentIdentity = attachedAgentMap.remove(keyOfAgentInDB);
			if (agentIdentity != null) {
				// if the agent attached to current controller
				if (!hasSameInfo(eachAgentInDB, agentIdentity)) {
					fillUp(eachAgentInDB, agentIdentity);
					changeAgents.add(eachAgentInDB);
				} else if (!isCurrentRegion(agentIdentity)) {
					fillUp(eachAgentInDB, agentIdentity);
					eachAgentInDB.setState(WRONG_REGION);
					eachAgentInDB.setApproved(false);
					changeAgents.add(eachAgentInDB);
				}
			} else { // the agent in DB is not attached to current controller
				if (eachAgentInDB.getState() != INACTIVE) {
					eachAgentInDB.setState(INACTIVE);
					changeAgents.add(eachAgentInDB);
				}
			}
		}

		// step2. check all attached agents, whether they are new, and not saved
		// in DB.
		for (AgentControllerIdentityImplementation agentIdentity : attachedAgentMap.values()) {
			AgentInfo agentInfo = agentManagerRepository.findByIpAndHostName(agentIdentity.getIp(),
					agentIdentity.getName());
			if (agentInfo == null) {
				agentInfo = new AgentInfo();
			}
			if (isCurrentRegion(agentIdentity)) {
				AgentInfo newAgentInfo = fillUp(agentInfo, agentIdentity);
				changeAgents.add(newAgentInfo);
			} else {
				if (agentInfo.getState() != WRONG_REGION) {
					AgentInfo newAgentInfo = fillUp(agentInfo, agentIdentity);
					agentInfo.setState(WRONG_REGION);
					agentInfo.setApproved(false);
					changeAgents.add(newAgentInfo);
				}
			}
		}
		if (!changeAgents.isEmpty()) {
			// step3. update into DB
			agentManagerRepository.save(changeAgents);
			localAgents = null;
		}
	}

	private Gson gson = new Gson();

	/**
	 * Collect the agent system info every second.
	 */
	@Scheduled(fixedDelay = 1000)
	@Transactional
	public void collectAgentSystemData() {
		Ehcache nativeCache = (Ehcache) agentMonitoringTargetsCache.getNativeCache();
		List<String> keysWithExpiryCheck = cast(nativeCache.getKeysWithExpiryCheck());
		for (String each : keysWithExpiryCheck) {
			ValueWrapper value = agentMonitoringTargetsCache.get(each);
			AgentControllerIdentityImplementation agentIdentity = cast(value.get());
			if (agentIdentity != null) {
				// Is Same Region
				if (isCurrentRegion(agentIdentity)) {
					try {
						((ClusteredAgentManagerService) (AopContext.currentProxy())).updateSystemStat
								(agentIdentity);
					} catch (IllegalStateException e) {
						LOGGER.error("error while update system stat.");
					}
				}
			}
		}
	}

	@Transactional
	public void updateSystemStat(AgentControllerIdentityImplementation agentIdentity) {
		agentManagerRepository.updateSystemStat(agentIdentity.getIp(),
				agentIdentity.getName(),
				gson.toJson(getSystemDataModel(agentIdentity)));
	}

	private SystemDataModel getSystemDataModel(AgentIdentity agentIdentity) {
		return getAgentManager().getSystemDataModel(agentIdentity);
	}

	/**
	 * Get all visible agents from DB.
	 *
	 * @return agent list
	 */
	@Override
	public List<AgentInfo> getAllVisibleAgentInfoFromDB() {
		List<AgentInfo> result = Lists.newArrayList();
		Set<String> regions = getRegions();
		for (AgentInfo each : agentManagerRepository.findAll(visible())) {
			if (regions.contains(extractRegionFromAgentRegion(each.getRegion()))) {
				result.add(each);
			}
		}
		return result;
	}

	/**
	 * Get the available agent count map in all regions of the user, including
	 * the free agents and user specified agents.
	 *
	 * @param user current user
	 * @return user available agent count map
	 */
	@Override
	@Transactional
	public Map<String, MutableInt> getAvailableAgentCountMap(User user) {
		Set<String> regions = getRegions();
		Map<String, MutableInt> availShareAgents = newHashMap(regions);
		Map<String, MutableInt> availUserOwnAgent = newHashMap(regions);
		for (String region : regions) {
			availShareAgents.put(region, new MutableInt(0));
			availUserOwnAgent.put(region, new MutableInt(0));
		}
		String myAgentSuffix = "_owned_" + user.getUserId();

		for (AgentInfo agentInfo : getAllActiveAgentInfoFromDB()) {
			// Skip all agents which are disapproved, inactive or
			// have no region prefix.
			if (!agentInfo.isApproved()) {
				continue;
			}

			String fullRegion = agentInfo.getRegion();
			String region = extractRegionFromAgentRegion(fullRegion);
			if (StringUtils.isBlank(region) || !regions.contains(region)) {
				continue;
			}
			// It's my own agent
			if (fullRegion.endsWith(myAgentSuffix)) {
				incrementAgentCount(availUserOwnAgent, region, user.getUserId());
			} else if (!fullRegion.contains("_owned_")) {
				incrementAgentCount(availShareAgents, region, user.getUserId());
			}
		}

		int maxAgentSizePerConsole = getMaxAgentSizePerConsole();

		for (String region : regions) {
			MutableInt mutableInt = availShareAgents.get(region);
			int shareAgentCount = mutableInt.intValue();
			mutableInt.setValue(Math.min(shareAgentCount, maxAgentSizePerConsole));
			mutableInt.add(availUserOwnAgent.get(region));
		}
		return availShareAgents;
	}

	protected Set<String> getRegions() {
		return regionService.getAll().keySet();
	}

	protected boolean isCurrentRegion(AgentControllerIdentityImplementation agentIdentity) {
		return StringUtils.equals(extractRegionFromAgentRegion(agentIdentity.getRegion()), getConfig().getRegion());
	}


	private void incrementAgentCount(Map<String, MutableInt> agentMap, String region, String userId) {
		if (!agentMap.containsKey(region)) {
			LOGGER.warn("Region :{} not exist in cluster nor owned by user:{}.", region, userId);
		} else {
			agentMap.get(region).increment();
		}
	}

	/**
	 * Get all agents attached of this region from DB.
	 * <p/>
	 * This method is cluster aware. If it's cluster mode it return all agents
	 * attached in this region.
	 *
	 * @return agent list
	 */
	@Override
	public List<AgentInfo> getLocalAgentsFromDB() {
		List<AgentInfo> result = localAgents;
		if (result == null) {
			result = agentManagerRepository.findAll(startWithRegion(getConfig().getRegion()));
			localAgents = result;
		}
		return result;
	}

	/**
	 * Stop agent. In cluster mode, it queues the agent stop request to
	 * agentRequestCache.
	 *
	 * @param id agent id in db
	 */
	@Override
	public void stopAgent(Long id) {
		AgentInfo agent = getOne(id);
		if (agent == null) {
			return;
		}
		agentRequestCache.put(extractRegionFromAgentRegion(agent.getRegion()) + "|" + createAgentKey(agent),
				new ClusteredAgentRequest(agent.getIp(), agent.getName(), STOP_AGENT));
	}

	/**
	 * Add the agent system data model share request on cache.
	 *
	 * @param id agent id in db.
	 */
	@Override
	public void requestShareAgentSystemDataModel(Long id) {
		AgentInfo agent = getOne(id);
		if (agent == null) {
			return;
		}
		agentRequestCache.put(extractRegionFromAgentRegion(agent.getRegion()) + "|" + createAgentKey(agent),
				new ClusteredAgentRequest(agent.getIp(), agent.getName(), SHARE_AGENT_SYSTEM_DATA_MODEL));
	}

	/**
	 * Get the agent system data model for the given IP. This method is cluster
	 * aware.
	 *
	 * @param ip   agent ip
	 * @param name agent name
	 * @return {@link SystemDataModel} instance.
	 */
	@Override
	public SystemDataModel getAgentSystemDataModel(String ip, String name) {
		AgentInfo found = agentManagerRepository.findByIpAndHostName(ip, name);
		String systemStat = (found == null) ? null : found.getSystemStat();
		return (StringUtils.isEmpty(systemStat)) ? new SystemDataModel() : gson.fromJson(systemStat,
				SystemDataModel.class);
	}

	/**
	 * Register agent monitoring target. This method should be called in the
	 * controller in which the given agent exists.
	 *
	 * @param agentIdentity agent identity
	 */
	public void addAgentMonitoringTarget(AgentControllerIdentityImplementation agentIdentity) {
		agentMonitoringTargetsCache.put(createAgentKey(agentIdentity), agentIdentity);
	}

	/**
	 * Stop agent.
	 *
	 * @param agentIdentity agent identity to be stopped.
	 */
	public void stopAgent(AgentControllerIdentityImplementation agentIdentity) {
		getAgentManager().stopAgent(agentIdentity);
	}

	/**
	 * Update the agent
	 *
	 * @param agentIdentity agent identity to be updated.
	 */
	public void updateAgent(AgentControllerIdentityImplementation agentIdentity) {
		getAgentManager().updateAgent(agentIdentity, getConfig().getVersion());
	}

	/**
	 * Update agent by id.
	 *
	 * @param id agent id
	 */
	@Override
	public void update(Long id) {
		AgentInfo agent = getOne(id);
		if (agent == null) {
			return;
		}
		agentRequestCache.put(extractRegionFromAgentRegion(agent.getRegion()) + "|" + createAgentKey(agent),
				new ClusteredAgentRequest(agent.getIp(), agent.getName(), UPDATE_AGENT));
	}

}
