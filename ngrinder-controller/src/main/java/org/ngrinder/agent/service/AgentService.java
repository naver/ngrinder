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
import net.grinder.SingleConsole;
import net.grinder.common.GrinderProperties;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.console.communication.AgentProcessControlImplementation;
import net.grinder.console.communication.AgentProcessControlImplementation.AgentStatusUpdateListener;
import net.grinder.console.communication.ConnectionAgentListener;
import net.grinder.engine.controller.AgentControllerIdentityImplementation;
import net.grinder.message.console.AgentControllerState;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.ngrinder.agent.model.AgentRequest;
import org.ngrinder.agent.model.Connection;
import org.ngrinder.agent.repository.AgentManagerRepository;
import org.ngrinder.agent.repository.ConnectionRepository;
import org.ngrinder.agent.store.AgentInfoStore;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.hazelcast.HazelcastService;
import org.ngrinder.infra.hazelcast.task.AgentStateTask;
import org.ngrinder.infra.hazelcast.task.ConnectionAgentTask;
import org.ngrinder.infra.hazelcast.topic.listener.TopicListener;
import org.ngrinder.infra.hazelcast.topic.message.TopicEvent;
import org.ngrinder.infra.hazelcast.topic.subscriber.TopicSubscriber;
import org.ngrinder.infra.schedule.ScheduledTaskService;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.User;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.perftest.service.AgentManager;
import org.ngrinder.region.model.RegionInfo;
import org.ngrinder.region.service.RegionService;
import org.ngrinder.service.AbstractAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
import static org.apache.commons.lang.StringUtils.*;
import static org.ngrinder.agent.model.AgentRequest.RequestType.STOP_AGENT;
import static org.ngrinder.agent.model.AgentRequest.RequestType.UPDATE_AGENT;
import static org.ngrinder.common.constant.CacheConstants.*;
import static org.ngrinder.common.constant.ControllerConstants.PROP_CONTROLLER_ENABLE_AGENT_AUTO_APPROVAL;
import static org.ngrinder.common.util.CollectionUtils.newHashMap;
import static org.ngrinder.common.util.LoggingUtils.format;
import static org.ngrinder.common.util.TypeConvertUtils.cast;

/**
 * Agent manager service.
 *
 * @since 3.0
 */
@Profile("production")
@Service
@RequiredArgsConstructor
public class AgentService extends AbstractAgentService
	implements TopicListener<AgentRequest>, AgentStatusUpdateListener, ConnectionAgentListener {
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

	private final ConnectionRepository connectionRepository;

	@Value("${ngrinder.version}")
	private String nGrinderVersion;

	@PostConstruct
	public void init() {
		agentManager.addAgentStatusUpdateListener(this);
		agentManager.addConnectionAgentListener(this);
		topicSubscriber.addListener(AGENT_TOPIC_LISTENER_NAME, this);
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::connectionAgentHealthCheck, 1L, 1L, TimeUnit.MINUTES);
	}

	private void connectionAgentHealthCheck() {
		connectionRepository.findAll()
			.stream()
			.filter(connection -> config.getRegion().equals(connection.getRegion()))
			.filter(connection -> agentInfoStore.getAgentInfo(createAgentKey(connection.getIp(), connection.getName())) == null)
			.forEach(connection -> {
				try {
					agentManager.addConnectionAgent(connection.getIp(), connection.getPort());
					LOGGER.info("Reconnected to connection agent {}:{}", connection.getIp(), connection.getPort());
				} catch (Exception e) {
					LOGGER.debug("Fail to reconnect to connection agent {}:{}", connection.getIp(), connection.getPort());
				}
			});
	}

	private void fillUpAgentInfo(AgentInfo agentInfo, AgentProcessControlImplementation.AgentStatus agentStatus) {
		if (agentInfo == null || agentStatus == null) {
			return;
		}

		AgentControllerIdentityImplementation agentIdentity = (AgentControllerIdentityImplementation) agentStatus.getAgentIdentity();
		AgentControllerState state = agentStatus.getAgentControllerState();

		agentInfo.setState(state);
		agentInfo.setIp(requireNonNull(agentIdentity).getIp());
		agentInfo.setRegion(config.getRegion());
		agentInfo.setAgentIdentity(agentIdentity);
		agentInfo.setName(agentIdentity.getName());
		agentInfo.setVersion(agentManager.getAgentVersion(agentIdentity));
		agentInfo.setPort(agentManager.getAttachedAgentConnectingPort(agentIdentity));

		if (!isValidSubregion(agentInfo.getSubregion())) {
			agentInfo.setSubregion("");
		}
	}

	private boolean isValidSubregion(String subregion) {
		String controllerRegion = config.getRegion();
		RegionInfo currentControllerRegion = regionService.getOne(controllerRegion);
		return currentControllerRegion.getSubregion().contains(subregion);
	}

	private List<AgentInfo> getAllReady() {
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
	 * Get the available agent count map in all regions of the user, including
	 * the free agents and user specified agents.
	 *
	 * @param userId current user id
	 * @return user available agent count map
	 */
	@Override
	public Map<String, MutableInt> getAvailableAgentCountMap(String userId) {
		Map<String, RegionInfo> regions = regionService.getAll();
		Map<String, MutableInt> availShareAgents = newHashMap();
		Map<String, MutableInt> availUserOwnAgent = newHashMap();

		regions.forEach((region, regionInfo) -> {
			regionInfo.getSubregion().forEach(subregion -> {
				availShareAgents.put(region + "." + subregion, new MutableInt(0));
				availUserOwnAgent.put(region + "." + subregion, new MutableInt(0));
			});
			availShareAgents.put(region, new MutableInt(0));
			availUserOwnAgent.put(region, new MutableInt(0));
		});

		for (AgentInfo agentInfo : getAllActive()) {
			// Skip all agents which are disapproved, inactive or
			// have no region prefix.
			if (!agentInfo.isApproved()) {
				continue;
			}

			String agentRegion = agentInfo.getRegion();
			String agentSubregion = agentInfo.getSubregion();

			if (isBlank(agentRegion) || !regions.containsKey(agentRegion)) {
				continue;
			}

			// It's my own agent
			String agentFullRegion = isEmpty(agentSubregion) ? agentRegion : agentRegion + "." + agentSubregion;
			if (isDedicatedAgent(agentInfo, userId)) {
				incrementAgentCount(availUserOwnAgent, agentFullRegion);
			} else if (isCommonAgent(agentInfo)) {
				incrementAgentCount(availShareAgents, agentFullRegion);
			}
		}

		int maxAgentSizePerConsole = agentManager.getMaxAgentSizePerConsole();

		regions.forEach((region, regionInfo) -> {
			regionInfo.getSubregion().forEach(subregion -> {
				String agentFullRegion = region + "." + subregion;
				MutableInt mutableInt = availShareAgents.get(agentFullRegion);
				int shareAgentCount = mutableInt.intValue();
				mutableInt.setValue(Math.min(shareAgentCount, maxAgentSizePerConsole));
				mutableInt.add(availUserOwnAgent.get(agentFullRegion));
			});
			MutableInt mutableInt = availShareAgents.get(region);
			int shareAgentCount = mutableInt.intValue();
			mutableInt.setValue(Math.min(shareAgentCount, maxAgentSizePerConsole));
			mutableInt.add(availUserOwnAgent.get(region));
		});

		return availShareAgents;
	}

	private void incrementAgentCount(Map<String, MutableInt> agentMap, String region) {
		if (agentMap.containsKey(region)) {
			agentMap.get(region).increment();
		}
	}

	@Override
	public AgentControllerIdentityImplementation getAgentIdentityByIpAndName(String ip, String name) {
		AgentInfo agentInfo = getAgent(ip, name);
		if (agentInfo != null) {
			return cast(agentInfo.getAgentIdentity());
		}
		return null;
	}

	public AgentInfo getAgent(String ip, String name) {
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

		updateApproveInStore(ip, name, approve);
	}

	private void updateApproveInStore(String ip, String name, boolean approve) {
		AgentInfo agentInfo = getAgent(ip, name);
		agentInfo.setApproved(approve);
		agentInfoStore.updateAgentInfo(agentInfo.getAgentKey(), agentInfo);
	}

	/**
	 * Get all free approved agents
	 *
	 * @return AgentInfo set
	 */
	private Set<AgentInfo> getAllFreeApprovedAgents() {
		return getAllReady()
			.stream()
			.filter(AgentInfo::isApproved)
			.collect(toSet());
	}

	/**
	 * Get all free approved agents attached to current node.
	 *
	 * @return AgentInfo set
	 *
	 */
	public Set<AgentInfo> getAllAttachedFreeApprovedAgents() {
		return getAllFreeApprovedAgents()
			.stream()
			.filter(this::isCurrentRegion)
			.collect(toSet());
	}

	/**
	 * Get all approved agents for given user which are not used now.
	 *
	 * @param  userId user id
	 * @return AgentInfo set
	 */
	public Set<AgentInfo> getAllAttachedFreeApprovedAgentsForUser(String userId, String fullRegion) {
		return getAllFreeApprovedAgents()
			.stream()
			.filter(this::isCurrentRegion)
			.filter(agentInfo -> StringUtils.equals(agentInfo.getSubregion(), extractSubregionFromFullRegion(fullRegion)))
			.filter(agentInfo -> isDedicatedAgent(agentInfo, userId) || isCommonAgent(agentInfo))
			.collect(toSet());
	}

	private boolean isCurrentRegion(AgentInfo agentInfo) {
		String region = config.getRegion();
		return StringUtils.equals(region, agentInfo.getRegion());
	}

	private boolean isDedicatedAgent(AgentInfo agentInfo, String userId) {
		if (isCommonAgent(agentInfo)) {
			return false;
		}
		return StringUtils.equals(agentInfo.getOwner(), userId);
	}

	private String extractSubregionFromFullRegion(String fullRegion) {
		if (isEmpty(fullRegion)) {
			return "";
		}

		String[] regionToken = fullRegion.split("\\.");
		return regionToken.length > 1 ? regionToken[1] : "";
	}

	private boolean isCommonAgent(AgentInfo agentInfo) {
		return StringUtils.isEmpty(agentInfo.getOwner());
	}

	/**
	 * Assign the agents on the given console.
	 *
	 * @param perfTest          current performance test.
	 * @param singleConsole     {@link SingleConsole} to which agents will be assigned.
	 * @param grinderProperties {@link GrinderProperties} to be distributed.
	 * @param agentCount        the count of agents.
	 */
	public synchronized void runAgent(PerfTest perfTest, final SingleConsole singleConsole,
									  final GrinderProperties grinderProperties, final Integer agentCount) {
		User user = perfTest.getCreatedBy();
		final Set<AgentInfo> allFreeAgents = getAllAttachedFreeApprovedAgentsForUser(user.getUserId(), perfTest.getRegion());
		final Set<AgentInfo> necessaryAgents = selectAgent(user, allFreeAgents, agentCount);

		if (hasOldVersionAgent(necessaryAgents)) {
			for (AgentInfo agentInfo : necessaryAgents) {
				if (!agentInfo.getVersion().equals(nGrinderVersion)) {
					update(agentInfo.getIp(), agentInfo.getName());
				}
			}
			throw new NGrinderRuntimeException("Old version agent is detected so, update message has been sent automatically." +
				"\nPlease restart perftest after few minutes.");
		}

		hazelcastService.put(DIST_MAP_NAME_RECENTLY_USED_AGENTS, user.getUserId(), necessaryAgents);

		LOGGER.info(format(perfTest, "{} agents are starting.", agentCount));
		for (AgentInfo agentInfo : necessaryAgents) {
			LOGGER.info(format(perfTest, "- Agent {}", agentInfo.getName()));
		}
		agentManager.runAgent(singleConsole, grinderProperties, necessaryAgents);
	}


	private boolean hasOldVersionAgent(Set<AgentInfo> agentInfos) {
		return agentInfos.stream().anyMatch(agentInfo -> !agentInfo.getVersion().equals(nGrinderVersion));
	}

	/**
	 * Select agent. This method return agent set which is belong to the given user first and then share agent set.
	 *
	 * Priority of agent selection.
	 * 1. dedicated agent of recently used.
	 * 2. dedicated agent.
	 * 3. public agent of recently used.
	 * 4. public agent.
	 *
	 * @param user          user
	 * @param allFreeAgents available agents
	 * @param agentCount    number of agents
	 * @return selected agents.
	 */
	Set<AgentInfo> selectAgent(User user, Set<AgentInfo> allFreeAgents, int agentCount) {
		Set<AgentInfo> recentlyUsedAgents = hazelcastService.getOrDefault(DIST_MAP_NAME_RECENTLY_USED_AGENTS, user.getUserId(), emptySet());

		Comparator<AgentInfo> recentlyUsedPriorityComparator = (agent1, agent2) -> {
			if (recentlyUsedAgents.contains(agent1)) {
				return -1;
			}
			if (recentlyUsedAgents.contains(agent2)) {
				return 1;
			}
			return 0;
		};

		Stream<AgentInfo> freeDedicatedAgentStream = allFreeAgents
			.stream()
			.filter(agentInfo -> isDedicatedAgent(agentInfo, user.getUserId()))
			.sorted(recentlyUsedPriorityComparator);

		Stream<AgentInfo> freeAgentStream = allFreeAgents
			.stream()
			.filter(this::isCommonAgent)
			.sorted(recentlyUsedPriorityComparator);

		return concat(freeDedicatedAgentStream, freeAgentStream)
			.limit(agentCount)
			.collect(toSet());
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
		AgentInfo agentInfo = getAgent(ip, name);
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
		AgentInfo agentInfo = getAgent(ip, name);
		if (agentInfo == null) {
			return;
		}
		publishTopic(agentInfo, UPDATE_AGENT);
	}

	@Override
	public SystemDataModel getSystemDataModel(String ip, String name, String region) {
		return hazelcastService.submitToRegion(AGENT_EXECUTOR_SERVICE_NAME, new AgentStateTask(ip, name), region);
	}

	/**
	 * Update the agent
	 *
	 * @param agentIdentity agent identity to be updated.
	 */
	public void updateAgent(AgentIdentity agentIdentity) {
		agentManager.updateAgent(agentIdentity, agentManager.getAgentForceUpdate() ? "99.99" : config.getVersion());
	}

	public void addConnectionAgent(String ip, int port, String region) {
		hazelcastService.submitToRegion(AGENT_EXECUTOR_SERVICE_NAME, new ConnectionAgentTask(ip, port), region);
	}


	private String createAgentKey(String ip, String name) {
		return ip + "_" + name;
	}

	/**
	 * Ready agent status count return
	 *
	 * @param userId       the login user id
	 * @param targetRegion the name of target region
	 *
	 * @return ready status agent count
	 */
	@Override
	public int getReadyAgentCount(String userId, String targetRegion) {
		return getReadyAgentInfos(userId, targetRegion, false).size();
	}

	/**
	 * Ready agent status count return
	 *
	 * @param userId          the login user id
	 * @param targetRegion    the name of target region
	 * @param targetSubregion the name of target subregion
	 *
	 * @return ready status agent count
	 */
	@Override
	public int getReadyAgentCount(String userId, String targetRegion, String targetSubregion) {
		return getReadyAgentInfos(userId, targetRegion, targetSubregion).size();
	}

	/**
	 * ${@link List} of ready status agents information return
	 *
	 * @param userId                   the login user id
	 * @param targetRegion             the name of target region
	 * @param isContainSubregionAgents the flag of if contains subregion agents or not
	 *
	 * @return ${@link List} of ready status agents information
	 */
	private List<AgentInfo> getReadyAgentInfos(String userId, String targetRegion, boolean isContainSubregionAgents) {
		return getAllFreeApprovedAgents()
			.stream()
			.filter(agentInfo -> StringUtils.equals(targetRegion, agentInfo.getRegion()))
			.filter(agentInfo -> isEmpty(agentInfo.getSubregion()) || isContainSubregionAgents)
			.filter(agentInfo -> {
				String agentOwner = agentInfo.getOwner();
				return isEmpty(agentOwner) || StringUtils.equals(userId, agentOwner);
			})
			.collect(toList());
	}

	/**
	 * ${@link List} of ready status agents information return
	 *
	 * @param userId          the login user id
	 * @param targetRegion    the name of target region
	 * @param targetSubregion the name of target subregion
	 *
	 * @return ${@link List} of ready status agents information
	 */
	private List<AgentInfo> getReadyAgentInfos(String userId, String targetRegion, String targetSubregion) {
		if (isEmpty(targetSubregion)) {
			return getReadyAgentInfos(userId, targetRegion, false);
		}

		return getReadyAgentInfos(userId, targetRegion, true)
			.stream()
			.filter(agentInfo -> StringUtils.equals(agentInfo.getSubregion(), targetSubregion))
			.collect(toList());
	}

	@Override
	public List<AgentInfo> getAllAttached() {
		return agentInfoStore.getAllAgentInfo();
	}

	@Override
	public List<AgentInfo> getLocalAgents() {
		String region = config.getRegion();
		return agentInfoStore.getAllAgentInfo()
			.stream()
			.filter(agentInfo -> region.equals(agentInfo.getRegion()))
			.collect(toList());
	}

	@Override
	public String createKey(AgentControllerIdentityImplementation agentIdentity) {
		return createAgentKey(agentIdentity.getIp(), agentIdentity.getName());
	}

	private void publishTopic(AgentInfo agentInfo, AgentRequest.RequestType requestType) {
		hazelcastService.publish(AGENT_TOPIC_NAME, new TopicEvent<>(AGENT_TOPIC_LISTENER_NAME,
			agentInfo.getRegion(), new AgentRequest(agentInfo.getIp(), agentInfo.getName(), requestType)));
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

	@Override
	public void update(Map<AgentIdentity, AgentProcessControlImplementation.AgentStatus> agentMap) {
		boolean approved = config.getControllerProperties().getPropertyBoolean(PROP_CONTROLLER_ENABLE_AGENT_AUTO_APPROVAL);
		Set<AgentInfo> agentInfoSet = agentInfoStore.getAllAgentInfo()
			.stream()
			.filter(agentInfo -> StringUtils.equals(agentInfo.getRegion(), config.getRegion()))
			.collect(toSet());

		for (AgentProcessControlImplementation.AgentStatus agentStatus : agentMap.values()) {
			AgentControllerIdentityImplementation agentIdentity = (AgentControllerIdentityImplementation) agentStatus.getAgentIdentity();
			AgentInfo agentInfo = agentInfoStore.getAgentInfo(createKey(requireNonNull(agentIdentity)));
			// check new agent
			if (agentInfo == null) {
				agentInfo = new AgentInfo();
			}

			fillUpAgentInfo(agentInfo, agentStatus);

			AgentInfo agentInfoInDB = agentManagerRepository.findByIpAndName(agentInfo.getIp(), agentInfo.getName());
			if (agentInfoInDB != null) {
				agentInfo.setApproved(agentInfoInDB.getApproved());
			} else {
				agentInfo.setApproved(approved);
			}

			agentInfoStore.updateAgentInfo(agentInfo.getAgentKey(), agentInfo);
			agentInfoSet.remove(agentInfo);
		}

		// delete disconnected agent.
		for (AgentInfo agentInfo : agentInfoSet) {
			agentInfoStore.deleteAgentInfo(agentInfo.getAgentKey());
		}
	}

	@Override
	public void onConnectionAgentMessage(String ip, String name, String subregion, int port) {
		Connection connection = connectionRepository.findByIpAndPort(ip, port);
		String connectedAgentRegion = getConnectedAgentRegion(subregion);
		if (connection == null) {
			connection = new Connection(ip, name, port, connectedAgentRegion);
		} else {
			connection.setName(name);
			connection.setRegion(connectedAgentRegion);
		}
		connectionRepository.save(connection);
	}

	private String getConnectedAgentRegion(String subregion) {
		String region = regionService.getCurrent();
		RegionInfo regionInfo = regionService.getOne(region);
		if (config.isClustered() && isValidSubregion(regionInfo, subregion)) {
			region = region + "." + subregion;
		}
		return region;
	}

	private boolean isValidSubregion(RegionInfo regionInfo, String subregion) {
		return regionInfo.getSubregion().contains(subregion);
	}
}
