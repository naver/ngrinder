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

import net.grinder.engine.controller.AgentControllerIdentityImplementation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.ngrinder.agent.model.AgentRequest;
import org.ngrinder.agent.model.AgentRequest.RequestType;
import org.ngrinder.agent.repository.AgentManagerRepository;
import org.ngrinder.agent.store.AgentInfoStore;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.hazelcast.HazelcastService;
import org.ngrinder.infra.hazelcast.task.AgentStateTask;
import org.ngrinder.infra.hazelcast.topic.listener.TopicListener;
import org.ngrinder.infra.hazelcast.topic.message.TopicEvent;
import org.ngrinder.infra.hazelcast.topic.subscriber.TopicSubscriber;
import org.ngrinder.infra.schedule.ScheduledTaskService;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.model.User;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.perftest.service.AgentManager;
import org.ngrinder.region.service.RegionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static net.grinder.message.console.AgentControllerState.*;
import static org.ngrinder.agent.model.AgentRequest.RequestType.*;
import static org.ngrinder.common.constant.CacheConstants.*;
import static org.ngrinder.common.util.CollectionUtils.newHashMap;

/**
 * Cluster enabled version of {@link AgentManagerService}.
 *
 * @since 3.1
 */
public class ClusteredAgentManagerService extends AgentManagerService implements TopicListener<AgentRequest> {
	private final Logger LOGGER = LoggerFactory.getLogger(ClusteredAgentManagerService.class);

	private final TopicSubscriber topicSubscriber;

	private final RegionService regionService;

	private final HazelcastService hazelcastService;

	public ClusteredAgentManagerService(AgentManager agentManager, AgentManagerRepository agentManagerRepository,
										Config config, AgentInfoStore agentInfoStore, ScheduledTaskService scheduledTaskService,
										TopicSubscriber topicSubscriber, RegionService regionService, HazelcastService hazelcastService) {
		super(agentManager, agentManagerRepository, config, agentInfoStore, scheduledTaskService);
		this.topicSubscriber = topicSubscriber;
		this.regionService = regionService;
		this.hazelcastService = hazelcastService;
	}

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void init() {
		super.init();
		topicSubscriber.addListener(AGENT_TOPIC_LISTENER_NAME, this);
	}

	public List<AgentInfo> getAllActive() {
		return getAllAttached()
			.stream()
			.filter(agentInfo -> !agentInfo.getState().equals(UNKNOWN))
			.filter(this::activeRegionOnly)
			.collect(toList());
	}

	public List<AgentInfo> getAllVisible() {
		return getAllAttached()
			.stream()
			.filter(this::activeRegionOnly)
			.collect(toList());
	}

	@Override
	protected Set<AgentInfo> getVisibleAsSet() {
		return getAllAttached()
			.stream()
			.filter(agentInfo -> agentInfo.getRegion().equals(getConfig().getRegion()))
			.collect(toSet());
	}

	/**
	 * All ready state agent return
	 */
	@Override
	public List<AgentInfo> getAllReady() {
		return getAllAttached()
			.stream()
			.filter(agentInfo -> agentInfo.getState().equals(READY))
			.filter(this::activeRegionOnly)
			.collect(toList());
	}

	private boolean activeRegionOnly(AgentInfo agentInfo) {
		final Set<String> regions = getRegions();
		return agentInfo != null && regions.contains(extractRegionKey(agentInfo.getRegion()));
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

	private void incrementAgentCount(Map<String, MutableInt> agentMap, String region, String userId) {
		if (!agentMap.containsKey(region)) {
			LOGGER.warn("Region :{} not exist in cluster nor owned by user:{}.", region, userId);
		} else {
			agentMap.get(region).increment();
		}
	}

	/**
	 * Stop agent. In cluster mode, it queues the agent stop request to
	 * agentRequestCache.
	 *
	 * @param ip   agent ip
	 * @param name agent host name
	 */
	@Override
	public void stop(String ip, String name) {
		AgentInfo agentInfo = agentInfoStore.getAgentInfo(createAgentKey(ip, name));
		if (agentInfo == null) {
			return;
		}
		publishTopic(agentInfo, AGENT_TOPIC_LISTENER_NAME, STOP_AGENT);
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
	public void stop(AgentControllerIdentityImplementation agentIdentity) {
		agentManager.stopAgent(agentIdentity);
	}

	/**
	 * Update agent by id.
	 *
	 * @param ip   agent ip
	 * @param name agent host name
	 */
	@Override
	public void update(String ip, String name) {
		AgentInfo agentInfo = agentInfoStore.getAgentInfo(createAgentKey(ip, name));
		if (agentInfo == null) {
			return;
		}
		publishTopic(agentInfo, AGENT_TOPIC_LISTENER_NAME, UPDATE_AGENT);
	}

	protected void publishTopic(AgentInfo agentInfo, String listener, RequestType requestType) {
		hazelcastService.publish(AGENT_TOPIC_NAME, new TopicEvent<>(listener,
			extractRegionKey(agentInfo.getRegion()), new AgentRequest(agentInfo.getIp(), agentInfo.getName(), requestType)));
	}

	@Override
	public void execute(TopicEvent<AgentRequest> event) {
		if (event.getKey().equals(getConfig().getRegion())) {
			AgentRequest agentRequest = event.getData();
			AgentControllerIdentityImplementation agentIdentity = getAgentIdentityByIpAndName(agentRequest.getAgentIp(), agentRequest.getAgentName());
			if (agentIdentity != null) {
				agentRequest.getRequestType().process(ClusteredAgentManagerService.this, agentIdentity);
			}
		}
	}

}
