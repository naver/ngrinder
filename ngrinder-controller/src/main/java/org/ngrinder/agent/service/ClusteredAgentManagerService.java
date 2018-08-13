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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.grinder.message.console.AgentControllerState.INACTIVE;
import static net.grinder.message.console.AgentControllerState.WRONG_REGION;
import static org.ngrinder.agent.model.ClusteredAgentRequest.RequestType.*;
import static org.ngrinder.agent.repository.AgentManagerSpecification.active;
import static org.ngrinder.agent.repository.AgentManagerSpecification.visible;
import static org.ngrinder.agent.repository.AgentManagerSpecification.ready;
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
	private RegionService regionService;

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void init() {
		super.init();
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
									if (agentRequest.getRequestType() ==
											ClusteredAgentRequest.RequestType.EXPIRE_LOCAL_CACHE) {
										expireLocalCache();
									} else {
										AgentControllerIdentityImplementation agentIdentity = getAgentIdentityByIpAndName(
												agentRequest.getAgentIp(), agentRequest.getAgentName());
										if (agentIdentity != null) {
											agentRequest.getRequestType().process(ClusteredAgentManagerService.this,
													agentIdentity);
										}
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

	@Override
	public void checkAgentStatePeriodically() {
		super.checkAgentStatePeriodically();
		collectAgentSystemData();
	}

	/**
	 * Run a scheduled task to check the agent statuses.
	 *
	 * @since 3.1
	 */
	@Override
	public void checkAgentState() {
		List<AgentInfo> newAgents = newArrayList(0);
		List<AgentInfo> updatedAgents = newArrayList(0);
		List<AgentInfo> stateUpdatedAgents = newArrayList(0);

		Set<AgentIdentity> allAttachedAgents = getAgentManager().getAllAttachedAgents();
		Map<String, AgentControllerIdentityImplementation> attachedAgentMap = newHashMap(allAttachedAgents);
		for (AgentIdentity agentIdentity : allAttachedAgents) {
			AgentControllerIdentityImplementation existingAgent = cast(agentIdentity);
			attachedAgentMap.put(createKey(existingAgent), existingAgent);
		}
		Map<String, AgentInfo> agentsInDBMap = Maps.newHashMap();
		// step1. check all agents in DB, whether they are attached to
		// controller.
		for (AgentInfo eachAgentInDB : getAllLocal()) {
			String keyOfAgentInDB = createKey(eachAgentInDB);
			agentsInDBMap.put(keyOfAgentInDB, eachAgentInDB);
			AgentControllerIdentityImplementation agentIdentity = attachedAgentMap.remove(keyOfAgentInDB);
			if (agentIdentity != null) {
				// if the agent attached to current controller
				if (!isCurrentRegion(agentIdentity)) {
					if (eachAgentInDB.getState() != WRONG_REGION) {
						eachAgentInDB.setApproved(false);
						eachAgentInDB.setRegion(getConfig().getRegion());
						eachAgentInDB.setState(WRONG_REGION);
						updatedAgents.add(eachAgentInDB);
					}
				} else if (!hasSameInfo(eachAgentInDB, agentIdentity)) {
					fillUp(eachAgentInDB, agentIdentity);
					updatedAgents.add(eachAgentInDB);
				} else if (!hasSameState(eachAgentInDB, agentIdentity)) {
					eachAgentInDB.setState(getAgentManager().getAgentState(agentIdentity));
					stateUpdatedAgents.add(eachAgentInDB);
				} else if (eachAgentInDB.getApproved() == null) {
					updatedAgents.add(fillUpApproval(eachAgentInDB));
				}
			} else { // the agent in DB is not attached to current controller
				if (eachAgentInDB.getState() != INACTIVE) {
					eachAgentInDB.setState(INACTIVE);
					stateUpdatedAgents.add(eachAgentInDB);
				}
			}
		}

		// step2. check all attached agents, whether they are new, and not saved
		// in DB.
		for (AgentControllerIdentityImplementation agentIdentity : attachedAgentMap.values()) {
			AgentInfo agentInfo = agentManagerRepository.findByIpAndHostName(
					agentIdentity.getIp(),
					agentIdentity.getName());
			if (agentInfo == null) {
				agentInfo = new AgentInfo();
				newAgents.add(fillUp(agentInfo, agentIdentity));
			} else {
				updatedAgents.add(fillUp(agentInfo, agentIdentity));
			}
			if (!isCurrentRegion(agentIdentity)) {
				agentInfo.setState(WRONG_REGION);
				agentInfo.setApproved(false);
			}
		}

		cachedLocalAgentService.updateAgents(newAgents, updatedAgents, stateUpdatedAgents, null);
		if (!newAgents.isEmpty() || !updatedAgents.isEmpty()) {
			expireLocalCache();
		}
	}


	private Gson gson = new Gson();

	/**
	 * Collect the agent system info every second.
	 */
	public void collectAgentSystemData() {
		if (agentMonitoringTargetsCache == null) {
			return;
		}
		Ehcache nativeCache = (Ehcache) agentMonitoringTargetsCache.getNativeCache();
		List<String> keysWithExpiryCheck = cast(nativeCache.getKeysWithExpiryCheck());
		for (String each : keysWithExpiryCheck) {
			ValueWrapper value = agentMonitoringTargetsCache.get(each);
			AgentControllerIdentityImplementation agentIdentity = cast(value.get());
			if (agentIdentity != null) {
				// Is Same Region
				if (isCurrentRegion(agentIdentity)) {
					try {
						updateSystemStat(agentIdentity);
					} catch (IllegalStateException e) {
						LOGGER.error("error while update system stat.");
					}
				}
			}
		}
	}

	public void updateSystemStat(final AgentControllerIdentityImplementation agentIdentity) {
		cachedLocalAgentService.doSthInTransaction(new Runnable() {
			public void run() {
				agentManagerRepository.updateSystemStat(agentIdentity.getIp(),
						agentIdentity.getName(),
						gson.toJson(getSystemDataModel(agentIdentity)));
			}
		});
	}

	private SystemDataModel getSystemDataModel(AgentIdentity agentIdentity) {
		return getAgentManager().getSystemDataModel(agentIdentity);
	}

	public List<AgentInfo> getAllActive() {
		return filterOnlyActiveRegion(agentManagerRepository.findAll(active()));
	}

	public List<AgentInfo> getAllVisible() {
		return filterOnlyActiveRegion(agentManagerRepository.findAll(visible()));
	}
	
	/**
	 * All ready state agent return
	 */
	@Override
	public List<AgentInfo> getAllReady() {
		return filterOnlyActiveRegion(agentManagerRepository.findAll(ready()));
	}

	private List<AgentInfo> filterOnlyActiveRegion(List<AgentInfo> agents) {
		final Set<String> regions = getRegions();
		return Lists.newArrayList(Iterables.filter(agents,
				new Predicate<AgentInfo>() {
					@Override
					public boolean apply(@Nullable AgentInfo input) {
						return input != null && regions.contains(extractRegionFromAgentRegion(input.getRegion()));
					}
				}));
	}


	/**
	 * Get the available agent count map in all regions of the user, including
	 * the free agents and user specified agents.
	 *
	 * @param user current user
	 * @return user available agent count map
	 */
	@Override
	public Map<String, MutableInt> getAvailableAgentCountMap(User user) {
		Set<String> regions = getRegions();
		Map<String, MutableInt> availShareAgents = newHashMap(regions);
		Map<String, MutableInt> availUserOwnAgent = newHashMap(regions);
		for (String region : regions) {
			availShareAgents.put(region, new MutableInt(0));
			availUserOwnAgent.put(region, new MutableInt(0));
		}
		String myAgentSuffix = "owned_" + user.getUserId();

		for (AgentInfo agentInfo : getAllActive()) {
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
			} else if (!fullRegion.contains("owned_")) {
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

	@Override
	public AgentInfo approve(Long id, boolean approve) {
		AgentInfo agent = super.approve(id, approve);
		if (agent != null) {
			agentRequestCache.put(extractRegionFromAgentRegion(agent.getRegion()) + "|" + createKey(agent),
					new ClusteredAgentRequest(agent.getIp(), agent.getName(), EXPIRE_LOCAL_CACHE));
		}
		return agent;
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
		agentRequestCache.put(extractRegionFromAgentRegion(agent.getRegion()) + "|" + createKey(agent),
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
		agentRequestCache.put(extractRegionFromAgentRegion(agent.getRegion()) + "|" + createKey(agent),
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
	public SystemDataModel getSystemDataModel(String ip, String name) {
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
		agentMonitoringTargetsCache.put(createKey(agentIdentity), agentIdentity);
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
		agentRequestCache.put(extractRegionFromAgentRegion(agent.getRegion()) + "|" + createKey(agent),
				new ClusteredAgentRequest(agent.getIp(), agent.getName(), UPDATE_AGENT));
	}

	/**
	 * Clean up the agents from db which belongs to the inactive regions.
	 */
	@Transactional
	public void cleanup() {
		super.cleanup();
		final Set<String> regions = getRegions();
		for (AgentInfo each : agentManagerRepository.findAll()) {
			if (!regions.contains(extractRegionFromAgentRegion(each.getRegion()))) {
				agentManagerRepository.delete(each);
			}
		}
	}

}
