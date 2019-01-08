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
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.engine.controller.AgentControllerIdentityImplementation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.ngrinder.agent.model.ClusteredAgentRequest;
import org.ngrinder.agent.model.ClusteredAgentRequest.RequestType;
import org.ngrinder.infra.hazelcast.HazelcastService;
import org.ngrinder.infra.hazelcast.task.AgentStateTask;
import org.ngrinder.infra.hazelcast.topic.listener.TopicListener;
import org.ngrinder.infra.hazelcast.topic.message.TopicEvent;
import org.ngrinder.infra.hazelcast.topic.subscriber.TopicSubscriber;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.model.User;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.region.service.RegionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.grinder.message.console.AgentControllerState.INACTIVE;
import static net.grinder.message.console.AgentControllerState.WRONG_REGION;
import static org.ngrinder.agent.model.ClusteredAgentRequest.RequestType.*;
import static org.ngrinder.agent.repository.AgentManagerSpecification.*;
import static org.ngrinder.common.constant.CacheConstants.*;
import static org.ngrinder.common.util.CollectionUtils.newArrayList;
import static org.ngrinder.common.util.CollectionUtils.newHashMap;
import static org.ngrinder.common.util.TypeConvertUtils.cast;

/**
 * Cluster enabled version of {@link AgentManagerService}.
 *
 * @author JunHo Yoon
 * @since 3.1
 */
public class ClusteredAgentManagerService extends AgentManagerService implements TopicListener<ClusteredAgentRequest> {
	private final Logger LOGGER = LoggerFactory.getLogger(ClusteredAgentManagerService.class);

	@Autowired
	private TopicSubscriber topicSubscriber;

	@Autowired
	private HazelcastService hazelcastService;

	@Autowired
	private RegionService regionService;

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void init() {
		super.init();
		if (getConfig().isClustered()) {
			topicSubscriber.addListener(AGENT_TOPIC_LISTENER_NAME, this);
		}
	}

	@Override
	public void checkAgentStatePeriodically() {
		super.checkAgentStatePeriodically();
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
					public boolean apply(AgentInfo input) {
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
			String region = extractRegionKey(fullRegion);
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
		return StringUtils.equals(extractRegionKey(agentIdentity.getRegion()), getConfig().getRegion());
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
			publishTopic(agent, AGENT_TOPIC_LISTENER_NAME, EXPIRE_LOCAL_CACHE);
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
		publishTopic(agent, AGENT_TOPIC_LISTENER_NAME, STOP_AGENT);
	}

	/**
	 * Get the agent system data model for the given IP. This method is cluster aware.
	 *
	 * @param ip   agent ip
	 * @param name agent name
	 * @return {@link SystemDataModel} instance.
	 */
	@Override
	public SystemDataModel getSystemDataModel(String ip, String name, String region) {
		SystemDataModel systemDataModel = hazelcastService.submitToRegion(AGENT_EXECUTOR_SERVICE_NAME, new AgentStateTask(ip, name), region);
		return systemDataModel != null ? systemDataModel : new SystemDataModel();
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
		publishTopic(agent, AGENT_TOPIC_LISTENER_NAME, UPDATE_AGENT);
	}

	protected void publishTopic(AgentInfo agent, String listener, RequestType requestType) {
		hazelcastService.publish(AGENT_TOPIC_NAME, new TopicEvent<>(listener,
			extractRegionKey(agent.getRegion()), new ClusteredAgentRequest(agent.getIp(), agent.getName(), requestType)));
	}

	/**
	 * Clean up the agents from db which belongs to the inactive regions.
	 */
	@Transactional
	public void cleanup() {
		super.cleanup();
		final Set<String> regions = getRegions();
		for (AgentInfo each : agentManagerRepository.findAll()) {
			if (!regions.contains(extractRegionKey(each.getRegion()))) {
				agentManagerRepository.delete(each);
			}
		}
	}

	@Override
	public void execute(TopicEvent<ClusteredAgentRequest> event) {
		ClusteredAgentRequest agentRequest = event.getData();
		if (agentRequest.getRequestType().equals(EXPIRE_LOCAL_CACHE)) {
			expireLocalCache();
			return;
		}

		if (event.getKey().equals(getConfig().getRegion())) {
			AgentControllerIdentityImplementation agentIdentity = getAgentIdentityByIpAndName(agentRequest.getAgentIp(), agentRequest.getAgentName());
			if (agentIdentity != null) {
				agentRequest.getRequestType().process(ClusteredAgentManagerService.this, agentIdentity);
			}
		}
	}

}
