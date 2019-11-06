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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.console.communication.AgentProcessControlImplementation;
import net.grinder.engine.controller.AgentControllerIdentityImplementation;
import net.grinder.message.console.AgentControllerState;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.ngrinder.agent.model.AgentRequest;
import org.ngrinder.agent.repository.AgentManagerRepository;
import org.ngrinder.agent.store.AgentInfoStore;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.hazelcast.HazelcastService;
import org.ngrinder.infra.hazelcast.topic.listener.TopicListener;
import org.ngrinder.infra.hazelcast.topic.message.TopicEvent;
import org.ngrinder.infra.hazelcast.topic.subscriber.TopicSubscriber;
import org.ngrinder.infra.schedule.ScheduledTaskService;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.model.User;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.perftest.service.AgentManager;
import org.ngrinder.region.service.RegionService;
import org.ngrinder.service.IAgentManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.ngrinder.agent.model.AgentRequest.RequestType.STOP_AGENT;
import static org.ngrinder.agent.model.AgentRequest.RequestType.UPDATE_AGENT;
import static org.ngrinder.common.constant.CacheConstants.AGENT_TOPIC_LISTENER_NAME;
import static org.ngrinder.common.constant.CacheConstants.AGENT_TOPIC_NAME;
import static org.ngrinder.common.constant.ControllerConstants.PROP_CONTROLLER_ENABLE_AGENT_AUTO_APPROVAL;
import static org.ngrinder.common.util.CollectionUtils.newHashMap;
import static org.ngrinder.common.util.TypeConvertUtils.cast;
import static org.ngrinder.infra.config.Config.NONE_REGION;

/**
 * Agent manager service.
 *
 * @since 3.0
 */
@Service
@RequiredArgsConstructor
public class AgentService implements TopicListener<AgentRequest>, IAgentManagerService {
	protected static final Logger LOGGER = LoggerFactory.getLogger(AgentService.class);

	protected final AgentManager agentManager;

	protected final AgentManagerRepository agentManagerRepository;

	@Getter
	private final Config config;

	private final RegionService regionService;

	private final HazelcastService hazelcastService;

	private final TopicSubscriber topicSubscriber;

	protected final AgentInfoStore agentInfoStore;

	protected final ScheduledTaskService scheduledTaskService;

	private Runnable runnable;

	@PostConstruct
	public void init() {
		initAgentInfoStore();
		runnable = this::updateSharedAgentInfo;
		scheduledTaskService.addFixedDelayedScheduledTaskInTransactionContext(runnable, 1000);
		topicSubscriber.addListener(AGENT_TOPIC_LISTENER_NAME, this);
	}

	@PreDestroy
	public void destroy() {
		scheduledTaskService.removeScheduledJob(runnable);
	}

	private void initAgentInfoStore() {
		boolean approved = config.getControllerProperties().getPropertyBoolean(PROP_CONTROLLER_ENABLE_AGENT_AUTO_APPROVAL);
		for (AgentProcessControlImplementation.AgentStatus status : agentManager.getAllAttachedAgentStatusSet()) {
			AgentInfo agentInfo = new AgentInfo();
			fillUpAgentInfo(agentInfo, status);
			agentInfo.setApproved(approved);
			agentInfoStore.updateAgentInfo(agentInfo.getAgentKey(), agentInfo);
		}

		List<AgentInfo> agentsInDB = agentManagerRepository.findAll();
		for (AgentInfo agentInfoInBD : agentsInDB) {
			AgentInfo agentInfo = agentInfoStore.getAgentInfo(agentInfoInBD.getAgentKey());
			if (agentInfo != null) {
				agentInfo.setApproved(agentInfoInBD.getApproved());
				agentInfoStore.updateAgentInfo(agentInfo.getAgentKey(), agentInfo);
			}
		}
	}

	private void fillUpAgentInfo(AgentInfo agentInfo, AgentProcessControlImplementation.AgentStatus status) {
		if (agentInfo == null || status == null) {
			return;
		}

		AgentControllerIdentityImplementation agentIdentity = (AgentControllerIdentityImplementation) status.getAgentIdentity();
		AgentControllerState state = status.getAgentControllerState();

		agentInfo.setState(state);
		agentInfo.setIp(requireNonNull(agentIdentity).getIp());
		agentInfo.setRegion(resolveRegion(agentIdentity.getRegion()));
		agentInfo.setAgentIdentity(agentIdentity);
		agentInfo.setName(agentIdentity.getName());
		agentInfo.setVersion(agentManager.getAgentVersion(agentIdentity));
		agentInfo.setPort(agentManager.getAttachedAgentConnectingPort(agentIdentity));
	}

	private String resolveRegion(String attachedAgentRegion) {
		if (attachedAgentRegion == null) {
			return NONE_REGION;
		}

		String controllerRegion = config.getRegion();
		if (attachedAgentRegion.contains("_owned_")) {
			String[] regionTokens = attachedAgentRegion.split("_owned_", 2);
			if (StringUtils.equals(controllerRegion, regionTokens[0])) {
				return attachedAgentRegion;
			} else {
				return controllerRegion + "_owned_" + regionTokens[1];
			}
		}
		return controllerRegion;
	}

	public List<AgentInfo> getAllReady() {
		return agentInfoStore.getAllAgentInfo()
			.stream()
			.filter(agentInfo -> agentInfo.getState() != null && agentInfo.getState().isReady())
			.collect(toList());
	}

	public List<AgentInfo> getAllActive() {
		return agentInfoStore.getAllAgentInfo()
			.stream()
			.filter(agentInfo -> agentInfo.getState() != null && agentInfo.getState().isActive())
			.collect(toList());
	}

	/**
	 * Run a scheduled task to check the agent statuses.
	 *
	 * @since 3.5.0
	 */
	protected void updateSharedAgentInfo() {
		boolean approved = config.getControllerProperties().getPropertyBoolean(PROP_CONTROLLER_ENABLE_AGENT_AUTO_APPROVAL);
		Set<AgentInfo> agentInfoSet = new HashSet<>(agentInfoStore.getAllAgentInfo());

		for (AgentProcessControlImplementation.AgentStatus status : agentManager.getAllAttachedAgentStatusSet()) {
			AgentControllerIdentityImplementation agentIdentity = (AgentControllerIdentityImplementation) status.getAgentIdentity();
			AgentInfo agentInfo = agentInfoStore.getAgentInfo(createKey(agentIdentity));
			// check new agent
			if (agentInfo == null) {
				agentInfo = new AgentInfo();
				agentInfo.setApproved(approved);
			}

			fillUpAgentInfo(agentInfo, status);
			agentInfoStore.updateAgentInfo(agentInfo.getAgentKey(), agentInfo);
			agentInfoSet.remove(agentInfo);
		}

		// delete disconnected agent.
		for (AgentInfo agentInfo : agentInfoSet) {
			agentInfoStore.deleteAgentInfo(agentInfo.getAgentKey());
		}
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
			String region = agentManager.extractRegionKey(fullRegion);
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

		int maxAgentSizePerConsole = agentManager.getMaxAgentSizePerConsole();

		for (String region : regions) {
			MutableInt mutableInt = availShareAgents.get(region);
			int shareAgentCount = mutableInt.intValue();
			mutableInt.setValue(Math.min(shareAgentCount, maxAgentSizePerConsole));
			mutableInt.add(availUserOwnAgent.get(region));
		}
		return availShareAgents;
	}

	private void incrementAgentCount(Map<String, MutableInt> agentMap, String region, String userId) {
		if (!agentMap.containsKey(region)) {
			LOGGER.warn("Region: {} not exist in cluster nor owned by user: {}.", region, userId);
		} else {
			agentMap.get(region).increment();
		}
	}

	protected Set<String> getRegions() {
		return regionService.getAll().keySet();
	}

	@Override
	public AgentControllerIdentityImplementation getAgentIdentityByIpAndName(String ip, String name) {
		AgentInfo agentInfo = agentInfoStore.getAgentInfo(createAgentKey(ip, name));
		if (agentInfo != null) {
			return cast(agentInfo.getAgentIdentity());
		}
		return null;
	}

	@Override
	public AgentInfo getOne(String ip, String name) {
		return agentInfoStore.getAgentInfo(createAgentKey(ip, name));
	}

	/**
	 * Approve/disapprove the agent on given id.
	 *
	 * @param ip      ip
	 * @param name	  host name
	 * @param approve true/false
	 */
	@Transactional
	public void approve(String ip, String name, boolean approve) {
		AgentInfo agentInfoInDB = agentManagerRepository.findByIpAndName(ip, name);

		if (agentInfoInDB != null) {
			agentInfoInDB.setApproved(approve);
		} else {
			agentInfoInDB = new AgentInfo();
			agentInfoInDB.setIp(ip);
			agentInfoInDB.setName(name);
			agentInfoInDB.setApproved(approve);
			agentManagerRepository.save(agentInfoInDB);
		}
		agentManagerRepository.flush();

		AgentInfo agentInfo = agentInfoStore.getAgentInfo(createAgentKey(ip, name));
		agentInfo.setApproved(approve);
		agentInfoStore.updateAgentInfo(agentInfo.getAgentKey(), agentInfo);
	}

	/**
	 * Stop agent. If it's in cluster mode, it queue to agentRequestCache.
	 * otherwise, it send stop message to the agent.
	 *
	 * @param ip ip of agent to stop.
	 * @param name host name of agent to stop.
	 */
	@Override
	public void stop(String ip, String name) {
		AgentInfo agentInfo = agentInfoStore.getAgentInfo(createAgentKey(ip, name));
		if (agentInfo == null) {
			return;
		}
		publishTopic(agentInfo, STOP_AGENT);
	}

	public void stop(AgentControllerIdentityImplementation agentIdentity) {
		agentManager.stopAgent(agentIdentity);
	}


	@Override
	public void update(String ip, String name) {
		AgentInfo agentInfo = agentInfoStore.getAgentInfo(createAgentKey(ip, name));
		if (agentInfo == null) {
			return;
		}
		publishTopic(agentInfo, UPDATE_AGENT);
	}

	@Override
	public SystemDataModel getSystemDataModel(String ip, String name, String region) {
		AgentControllerIdentityImplementation agentIdentity = getAgentIdentityByIpAndName(ip, name);
		return agentIdentity != null ? agentManager.getSystemDataModel(agentIdentity) : new SystemDataModel();
	}

	/**
	 * Update the agent
	 *
	 * @param agentIdentity agent identity to be updated.
	 */
	public void updateAgent(AgentIdentity agentIdentity) {
		agentManager.updateAgent(agentIdentity, agentManager.getAgentForceUpdate() ? "99.99" : config.getVersion());
	}


	private String createAgentKey(String ip, String name) {
		return ip + "_" + name;
	}

	/**
	 * Ready agent state count return
	 *
	 * @param user The login user
	 * @param targetRegion targetRegion The name of target region
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

	@Override
	public List<AgentInfo> getAllVisible() {
		return getAllActive();
	}

	@Override
	public List<AgentInfo> getAllAttached() {
		return agentInfoStore.getAllAgentInfo();
	}

	@Override
	public List<AgentInfo> getLocalAgents() {
		return getAllAttached();
	}

	@Override
	public String createKey(AgentInfo agentInfo) {
		return agentInfo.getAgentKey();
	}

	@Override
	public String createKey(AgentControllerIdentityImplementation agentIdentity) {
		return createAgentKey(agentIdentity.getIp(), agentIdentity.getName());
	}

	private void publishTopic(AgentInfo agentInfo, AgentRequest.RequestType requestType) {
		hazelcastService.publish(AGENT_TOPIC_NAME, new TopicEvent<>(AGENT_TOPIC_LISTENER_NAME,
			agentManager.extractRegionKey(agentInfo.getRegion()), new AgentRequest(agentInfo.getIp(), agentInfo.getName(), requestType)));
	}

	@Override
	public void execute(TopicEvent<AgentRequest> event) {
		if (event.getKey().equals(config.getRegion())) {
			AgentRequest agentRequest = event.getData();
			AgentControllerIdentityImplementation agentIdentity = getAgentIdentityByIpAndName(agentRequest.getAgentIp(), agentRequest.getAgentName());
			if (agentIdentity != null) {
				agentRequest.getRequestType().process(AgentService.this, agentIdentity);
			}
		}
	}
}
