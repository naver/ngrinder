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
package org.ngrinder.perftest.service;

import net.grinder.AgentControllerServerDaemon;
import net.grinder.SingleConsole;
import net.grinder.common.GrinderProperties;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.console.communication.AgentDownloadRequestListener;
import net.grinder.console.communication.AgentProcessControlImplementation;
import net.grinder.console.communication.AgentProcessControlImplementation.AgentStatus;
import net.grinder.console.communication.LogArrivedListener;
import net.grinder.console.model.ConsoleCommunicationSetting;
import net.grinder.engine.communication.AgentUpdateGrinderMessage;
import net.grinder.engine.controller.AgentControllerIdentityImplementation;
import net.grinder.message.console.AgentControllerState;
import net.grinder.messages.console.AgentAddress;
import net.grinder.util.thread.ExecutorFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.agent.service.AgentPackageService;
import org.ngrinder.agent.service.LocalAgentService;
import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.common.util.CRC32ChecksumUtils;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.model.User;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.python.google.common.base.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Agent manager.
 * <p/>
 * This class has {@link AgentControllerServerDaemon} internally and manage to the agent connection.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
@SuppressWarnings("UnusedDeclaration")
@Component
public class AgentManager implements ControllerConstants, AgentDownloadRequestListener {
	public static final Logger LOGGER = LoggerFactory.getLogger(AgentManager.class);
	private AgentControllerServerDaemon agentControllerServerDaemon;
	private static final int NUMBER_OF_THREAD = 3;

	@Autowired
	private Config config;

	@Autowired
	private AgentPackageService agentPackageService;

	@Autowired
	private LocalAgentService cachedLocalAgentService;

	/**
	 * Initialize agent manager.
	 */
	@PostConstruct
	public void init() {
		int port = config.getControllerPort();

		ConsoleCommunicationSetting consoleCommunicationSetting = ConsoleCommunicationSetting.asDefault();
		if (config.getInactiveClientTimeOut() > 0) {
			consoleCommunicationSetting.setInactiveClientTimeOut(config.getInactiveClientTimeOut());
		}

		agentControllerServerDaemon = new AgentControllerServerDaemon(config.getCurrentIP(), port, consoleCommunicationSetting);
		agentControllerServerDaemon.start();
		agentControllerServerDaemon.setAgentDownloadRequestListener(this);
		agentControllerServerDaemon.addLogArrivedListener(new LogArrivedListener() {
			@Override
			public void logArrived(String testId, AgentAddress agentAddress, byte[] logs) {
				AgentControllerIdentityImplementation agentIdentity = convert(agentAddress.getIdentity());
				if (ArrayUtils.isEmpty(logs)) {
					LOGGER.error("Log is arrived from {} but no log content", agentIdentity.getIp());
				}
				File logFile = null;
				try {
					logFile = new File(config.getHome().getPerfTestLogDirectory(testId.replace("test_", "")),
							agentIdentity.getName() + "-" + agentIdentity.getRegion() + "-log.zip");
					FileUtils.writeByteArrayToFile(logFile, logs);
				} catch (IOException e) {
					LOGGER.error("Error while write logs from {} to {}", agentAddress.getIdentity().getName(),
							logFile.getAbsolutePath());
					LOGGER.error("Error is following", e);
				}
			}
		});
	}

	/**
	 * Shutdown agent controller server.
	 */
	@PreDestroy
	public void destroy() {
		agentControllerServerDaemon.shutdown();
	}

	/**
	 * Get the port which given agent is connecting to.
	 *
	 * @param agentIdentity agent identity
	 * @return port
	 */
	public int getAgentConnectingPort(AgentIdentity agentIdentity) {
		return agentControllerServerDaemon.getAgentConnectingPort(agentIdentity);
	}

	/**
	 * Get all agent status.
	 *
	 * @return {@link AgentStatus} set
	 * @since 3.1.2
	 */
	public Set<AgentStatus> getAllAgentStatusSet() {
		return agentControllerServerDaemon.getAgentStatusSet(new Predicate<AgentStatus>() {
			@Override
			public boolean apply(AgentStatus arg0) {
				return true;
			}
		});
	}

	/**
	 * Get agent status set matching the given predicate.
	 *
	 * @param predicate predicate
	 * @return {@link AgentStatus} set
	 * @since 3.1.2
	 */
	public Set<AgentStatus> getAgentStatusSet(Predicate<AgentStatus> predicate) {
		return agentControllerServerDaemon.getAgentStatusSet(predicate);
	}

	/**
	 * Get the agent status of the given agent.
	 *
	 * @param agentIdentity agentIdentity of one agent
	 * @return status agent controller status of one agent
	 */
	public AgentControllerState getAgentState(AgentIdentity agentIdentity) {
		return agentControllerServerDaemon.getAgentState(agentIdentity);
	}

	/**
	 * Get all agents which is connected to agent controller.
	 *
	 * @return agents set
	 */
	public Set<AgentIdentity> getAllAttachedAgents() {
		return agentControllerServerDaemon.getAllAvailableAgents();
	}

	/**
	 * Get the max agent size per console.
	 *
	 * @return max agent size per console
	 */
	public int getMaxAgentSizePerConsole() {
		return config.getControllerProperties().getPropertyInt(PROP_CONTROLLER_MAX_AGENT_PER_TEST);
	}

	/**
	 * Get the max vuser per agent.
	 *
	 * @return max vuser per agent
	 */
	public int getMaxVuserPerAgent() {
		return config.getControllerProperties().getPropertyInt(PROP_CONTROLLER_MAX_VUSER_PER_AGENT);
	}

	/**
	 * Get the max run count per thread.
	 *
	 * @return max run count per thread
	 */
	public int getMaxRunCount() {
		return config.getControllerProperties().getPropertyInt(PROP_CONTROLLER_MAX_RUN_COUNT);
	}

	/**
	 * Get the max run hour.
	 *
	 * @return max run hour
	 */
	public int getMaxRunHour() {
		return config.getControllerProperties().getPropertyInt(PROP_CONTROLLER_MAX_RUN_HOUR);
	}

	/**
	 * Get the {@link AgentIdentity} which has the given ip.
	 *
	 * @param agentIP agent ip
	 * @return {@link AgentControllerIdentityImplementation}
	 */
	public AgentControllerIdentityImplementation getAgentIdentityByIp(String agentIP) {
		for (AgentIdentity agentIdentity : getAllAttachedAgents()) {
			if (StringUtils.equals(convert(agentIdentity).getIp(), agentIP)) {
				return convert(agentIdentity);
			}
		}
		return null;
	}

	/**
	 * Convert {@link AgentIdentity} to {@link AgentControllerIdentityImplementation} type.
	 *
	 * @param identity identity
	 * @return converted identity.
	 */
	AgentControllerIdentityImplementation convert(AgentIdentity identity) {
		return (AgentControllerIdentityImplementation) identity;
	}

	/**
	 * Get all agents which are not used now.
	 *
	 * @return AgentIdentity set
	 */
	public Set<AgentIdentity> getAllFreeAgents() {
		return agentControllerServerDaemon.getAllFreeAgents();
	}

	/**
	 * Get all approved agents for given user which are not used now.
	 *
	 * @param user user
	 * @return AgentIdentity set
	 */
	public Set<AgentIdentity> getAllFreeApprovedAgentsForUser(User user) {
		if (user == null) {
			return Collections.emptySet();
		}
		return filterUserAgents(getAllFreeApprovedAgents(), user.getUserId());
	}

	/**
	 * Get all approved agents which are not used now.
	 *
	 * @return AgentIdentity set
	 */
	public Set<AgentIdentity> getAllFreeApprovedAgents() {
		Set<AgentIdentity> allFreeAgents = agentControllerServerDaemon.getAllFreeAgents();
		return filterApprovedAgents(allFreeAgents);
	}

	/**
	 * Get all approved agents for given user.
	 *
	 * @param user user
	 * @return AgentIdentity set
	 */
	public Set<AgentIdentity> getAllApprovedAgents(User user) {
		if (user == null) {
			return Collections.emptySet();
		}
		return filterUserAgents(getAllApprovedAgents(), user.getUserId());
	}

	/**
	 * Get all shared approved agents.
	 *
	 * @return AgentIdentity set
	 */
	public Set<AgentIdentity> getAllSharedAgents() {
		return filterSharedAgents(getAllApprovedAgents());
	}

	/**
	 * Get all approved agents.
	 *
	 * @return AgentIdentity set
	 */
	public Set<AgentIdentity> getAllApprovedAgents() {
		Set<AgentIdentity> allAgents = agentControllerServerDaemon.getAllAvailableAgents();
		return filterApprovedAgents(allAgents);
	}

	/**
	 * Filter the approved agents from given agents.
	 *
	 * @param agents all agents
	 * @return approved agents.
	 */
	public Set<AgentIdentity> filterApprovedAgents(Set<AgentIdentity> agents) {
		if (agents.size() == 0) {
			return agents;
		}


		Set<String> ips = new HashSet<String>();

		for (AgentInfo each : cachedLocalAgentService.getLocalAgents()) {
			if (each.isApproved()) {
				ips.add(each.getIp() + each.getName());
			}
		}
		Set<AgentIdentity> approvedAgent = new HashSet<AgentIdentity>();
		for (AgentIdentity each : agents) {
			if (ips.contains(((AgentControllerIdentityImplementation) each).getIp() + each.getName())) {
				approvedAgent.add(each);
			}
		}
		return approvedAgent;
	}

	/**
	 * Filter the shared agents from given agents.
	 *
	 * @param agents all agents
	 * @return userOwned agents.
	 */
	public Set<AgentIdentity> filterSharedAgents(Set<AgentIdentity> agents) {

		Set<AgentIdentity> userAgent = new HashSet<AgentIdentity>();
		for (AgentIdentity each : agents) {
			String region = ((AgentControllerIdentityImplementation) each).getRegion();

			if (StringUtils.containsNone(region, "owned_")) {
				userAgent.add(each);
			}
		}
		return userAgent;
	}

	/**
	 * Filter the user owned agents from given agents.
	 *
	 * @param agents all agents
	 * @param userId userId
	 * @return userOwned agents.
	 */
	public Set<AgentIdentity> filterUserAgents(Set<AgentIdentity> agents, String userId) {

		Set<AgentIdentity> userAgent = new HashSet<AgentIdentity>();
		for (AgentIdentity each : agents) {
			String region = ((AgentControllerIdentityImplementation) each).getRegion();
			if (StringUtils.endsWith(region, "owned_" + userId) || !StringUtils.contains(region, "owned_")) {
				userAgent.add(each);
			}
		}
		return userAgent;
	}

	/**
	 * Get the current system performance of the given agent.
	 *
	 * @param agentIdentity {@link AgentIdentity}
	 * @return {@link SystemDataModel} instance.
	 */
	public SystemDataModel getSystemDataModel(AgentIdentity agentIdentity) {
		return agentControllerServerDaemon.getSystemDataModel(agentIdentity);
	}

	/**
	 * Get the agent version.
	 *
	 * @param agentIdentity {@link AgentIdentity}
	 * @return version.
	 */
	public String getAgentVersion(AgentControllerIdentityImplementation agentIdentity) {
		return agentControllerServerDaemon.getAgentVersion(agentIdentity);
	}

	/**
	 * Assign the agents on the given console.
	 *
	 * @param user              user
	 * @param singleConsole     {@link SingleConsole} to which agents will be assigned
	 * @param grinderProperties {@link GrinderProperties} to be distributed.
	 * @param agentCount        the count of agents.
	 */
	public synchronized void runAgent(User user, final SingleConsole singleConsole,
	                                  final GrinderProperties grinderProperties, final Integer agentCount) {
		final Set<AgentIdentity> allFreeAgents = getAllFreeApprovedAgentsForUser(user);
		final Set<AgentIdentity> necessaryAgents = selectAgent(user, allFreeAgents, agentCount);
		LOGGER.info("{} agents are starting for user {}", agentCount, user.getUserId());
		for (AgentIdentity each : necessaryAgents) {
			LOGGER.info("- Agent {}", each.getName());
		}
		ExecutorService execService = null;
		try {
			// Make the agents connect to console.
			grinderProperties.setInt(GrinderProperties.CONSOLE_PORT, singleConsole.getConsolePort());
			execService = ExecutorFactory.createThreadPool("agentStarter", NUMBER_OF_THREAD);
			for (final AgentIdentity eachAgentIdentity : necessaryAgents) {
				execService.submit(new Runnable() {
					@Override
					public void run() {
						agentControllerServerDaemon.startAgent(grinderProperties, eachAgentIdentity);
					}
				});
			}
		} finally {
			if (execService != null) {
				execService.shutdown();
			}
		}
	}

	/**
	 * Select agent. This method return agent set which is belong to the given user first and then share agent set.
	 *
	 * @param user          user
	 * @param allFreeAgents agents
	 * @param agentCount    number of agent
	 * @return selected agent.
	 */
	public Set<AgentIdentity> selectAgent(User user, Set<AgentIdentity> allFreeAgents, int agentCount) {
		Set<AgentIdentity> userAgent = new HashSet<AgentIdentity>();
		for (AgentIdentity each : allFreeAgents) {
			String region = ((AgentControllerIdentityImplementation) each).getRegion();
			if (StringUtils.endsWith(region, "owned_" + user.getUserId())) {
				userAgent.add(each);
				if (userAgent.size() == agentCount) {
					return userAgent;
				}
			}
		}

		for (AgentIdentity each : allFreeAgents) {
			String region = ((AgentControllerIdentityImplementation) each).getRegion();
			if (!StringUtils.contains(region, "owned_")) {
				userAgent.add(each);
				if (userAgent.size() == agentCount) {
					return userAgent;
				}
			}
		}
		return userAgent;
	}

	/**
	 * Stop agent by force.
	 *
	 * @param agentIdentity agent identity
	 */
	public void stopAgent(AgentIdentity agentIdentity) {
		agentControllerServerDaemon.stopAgent(agentIdentity);
	}

	/**
	 * Stop agents which uses the given console port by force.
	 *
	 * @param consolePort console port.
	 */
	public void stopAgent(int consolePort) {
		Set<AgentStatus> agentStatusSetConnectingToPort = getAgentStatusSetConnectingToPort(consolePort);
		for (AgentStatus each : agentStatusSetConnectingToPort) {
			if (each.getAgentControllerState() == AgentControllerState.BUSY) {
				agentControllerServerDaemon.stopAgent(each.getAgentIdentity());
			}
		}
	}


	/**
	 * Update the given agent.
	 *
	 * @param agentIdentity agent identity
	 */
	public void updateAgent(AgentIdentity agentIdentity, String version) {
		agentControllerServerDaemon.updateAgent(agentIdentity, version);
	}

	/**
	 * Get the set of {@link AgentStatus} from agents belong to the given single console port.
	 *
	 * @param singleConsolePort port
	 * @return {@link AgentStatus} set
	 */
	public Set<AgentStatus> getAgentStatusSetConnectingToPort(final int singleConsolePort) {
		return getAgentStatusSet(new Predicate<AgentProcessControlImplementation.AgentStatus>() {
			@Override
			public boolean apply(AgentStatus status) {
				return status.getConnectingPort() == singleConsolePort;
			}
		});
	}

	@Override
	public synchronized AgentUpdateGrinderMessage onAgentDownloadRequested(String version, int offset) {
		final int updateChunkSize = getUpdateChunkSize();
		byte[] buffer = new byte[updateChunkSize];
		RandomAccessFile agentPackageReader = null;
		try {
			agentPackageReader = new RandomAccessFile(agentPackageService.createAgentPackage(), "r");
			agentPackageReader.seek(offset);
			int count = agentPackageReader.read(buffer, 0, updateChunkSize);
			byte[] bytes = buffer;
			int next = offset + count;
			if (count != updateChunkSize) {
				bytes = Arrays.copyOf(buffer, count);
				next = 0;
			}
			return new AgentUpdateGrinderMessage(version, bytes, offset, next,
					CRC32ChecksumUtils.getCRC32Checksum(bytes));
		} catch (Exception e) {
			LOGGER.error("Error while reading agent package, its offset is {} and details {}:", offset, e);
		} finally {
			IOUtils.closeQuietly(agentPackageReader);
		}
		return AgentUpdateGrinderMessage.getNullAgentUpdateGrinderMessage(version);
	}

	private int getUpdateChunkSize() {
		return config.getControllerProperties().getPropertyInt(ControllerConstants.PROP_CONTROLLER_UPDATE_CHUNK_SIZE);
	}
}
