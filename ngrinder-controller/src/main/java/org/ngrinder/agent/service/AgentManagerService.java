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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.console.communication.AgentProcessControlImplementation;
import net.grinder.engine.controller.AgentControllerIdentityImplementation;
import net.grinder.message.console.AgentControllerState;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.ngrinder.agent.repository.AgentManagerRepository;
import org.ngrinder.agent.store.AgentInfoStore;
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
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;

import static java.util.Objects.requireNonNull;
import static org.ngrinder.common.util.TypeConvertUtils.cast;

/**
 * Agent manager service.
 *
 * @since 3.0
 */
@RequiredArgsConstructor
public class AgentManagerService extends AbstractAgentManagerService {
	protected static final Logger LOGGER = LoggerFactory.getLogger(AgentManagerService.class);

	protected final AgentManager agentManager;

	protected final AgentManagerRepository agentManagerRepository;

	@Getter
	private final Config config;

	protected final AgentInfoStore agentInfoStore;

	protected final ScheduledTaskService scheduledTaskService;

	private Runnable runnable;

	@PostConstruct
	public void init() {
		initAgentIMap();
		runnable = this::updateSharedAgentInfo;
		scheduledTaskService.addFixedDelayedScheduledTaskInTransactionContext(runnable, 1000);
	}

	@PreDestroy
	public void destroy() {
		scheduledTaskService.removeScheduledJob(runnable);
	}

	protected void initAgentIMap() {
		boolean approved = getConfig().getControllerProperties().getPropertyBoolean(ControllerConstants.PROP_CONTROLLER_ENABLE_AGENT_AUTO_APPROVAL);
		for (AgentProcessControlImplementation.AgentStatus status : agentManager.getAllAgentStatusSet()) {
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

	/**
	 * Run a scheduled task to check the agent statuses.
	 *
	 * @since 3.5.0
	 */
	protected void updateSharedAgentInfo() {
		boolean approved = getConfig().getControllerProperties().getPropertyBoolean(ControllerConstants.PROP_CONTROLLER_ENABLE_AGENT_AUTO_APPROVAL);
		Set<AgentInfo> agentInfoSet = getVisibleAsSet();

		for (AgentProcessControlImplementation.AgentStatus status : agentManager.getAllAgentStatusSet()) {
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
		Map<String, MutableInt> result = new HashMap<>(1);
		result.put(Config.NONE_REGION, new MutableInt(availableShareAgents + availableUserOwnAgent));
		return result;
	}

	int getMaxAgentSizePerConsole() {
		return agentManager.getMaxAgentSizePerConsole();
	}

	@Override
	public String createKey(AgentInfo agentInfo) {
		return createAgentKey(agentInfo.getIp(), agentInfo.getName());
	}

	@Override
	public String createKey(AgentControllerIdentityImplementation agentIdentity) {
		return createAgentKey(agentIdentity.getIp(), agentIdentity.getName());
	}

	protected String createAgentKey(String ip, String name) {
		return ip + "_" + name;
	}

	@Override
	public AgentControllerIdentityImplementation getAgentIdentityByIpAndName(String ip, String name) {
		Set<AgentIdentity> allAttachedAgents = agentManager.getAllAttachedAgents();
		for (AgentIdentity eachAgentIdentity : allAttachedAgents) {
			AgentControllerIdentityImplementation agentIdentity = cast(eachAgentIdentity);
			if (StringUtils.equals(ip, agentIdentity.getIp()) && StringUtils.equals(name, agentIdentity.getName())) {
				return agentIdentity;
			}
		}
		return null;
	}


	public List<AgentInfo> getAllAttached() {
		return Collections.unmodifiableList(agentInfoStore.getAllAgentInfo());
	}

	@Override
	public List<AgentInfo> getAllActive() {
		List<AgentInfo> agents = Lists.newArrayList();
		for (AgentInfo agentInfo : getAllAttached()) {
			final AgentControllerState state = agentInfo.getState();
			if (state != null && state.isActive()) {
				agents.add(agentInfo);
			}
		}
		return agents;
	}

	@Override
	public List<AgentInfo> getAllVisible() {
		List<AgentInfo> agents = Lists.newArrayList();
		for (AgentInfo agentInfo : getAllAttached()) {
			final AgentControllerState state = agentInfo.getState();
			if (state != null && state.isActive()) {
				agents.add(agentInfo);
			}
		}
		return agents;
	}

	protected Set<AgentInfo> getVisibleAsSet() {
		return new HashSet<>(getAllAttached());
	}

	private String resolveRegion(String attachedAgentRegion) {
		String controllerRegion = getConfig().getRegion();

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

	private void fillUpAgentInfo(AgentInfo agentInfo, AgentProcessControlImplementation.AgentStatus status) {
		if (agentInfo == null || status == null) {
			return;
		}

		AgentControllerIdentityImplementation agentIdentity = (AgentControllerIdentityImplementation) status.getAgentIdentity();
		AgentControllerState state = status.getAgentControllerState();

		agentInfo.setState(state);
		agentInfo.setIp(requireNonNull(agentIdentity).getIp());
		agentInfo.setRegion(resolveRegion(agentIdentity.getRegion()));
		agentInfo.setName(agentIdentity.getName());
		agentInfo.setVersion(agentManager.getAgentVersion(agentIdentity));
		agentInfo.setPort(agentManager.getAgentConnectingPort(agentIdentity));
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
		agentManager.stopAgent(getAgentIdentityByIpAndName(ip, name));
	}

	@Override
	public void update(String ip, String name) {
		AgentInfo agentInfo = agentInfoStore.getAgentInfo(createAgentKey(ip, name));
		if (agentInfo == null) {
			return;
		}
		updateAgent(getAgentIdentityByIpAndName(ip, name));
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
		agentManager.updateAgent(agentIdentity, shouldUpdateAgentAlways() ? "99.99" : config.getVersion());
	}


	private boolean shouldUpdateAgentAlways() {
		return config.getControllerProperties().getPropertyBoolean(ControllerConstants.PROP_CONTROLLER_AGENT_FORCE_UPDATE);
	}

	/**
	 * All ready state agent return
	 */
	List<AgentInfo> getAllReady() {
		List<AgentInfo> agents = Lists.newArrayList();
		for (AgentInfo agentInfo : getAllAttached()) {
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
}
