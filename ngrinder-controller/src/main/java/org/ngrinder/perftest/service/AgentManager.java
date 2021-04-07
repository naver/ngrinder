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

import lombok.RequiredArgsConstructor;
import net.grinder.AgentControllerServerDaemon;
import net.grinder.SingleConsole;
import net.grinder.common.GrinderProperties;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.console.communication.AgentDownloadRequestListener;
import net.grinder.console.communication.AgentProcessControlImplementation.AgentStatus;
import net.grinder.console.communication.AgentProcessControlImplementation.AgentStatusUpdateListener;
import net.grinder.console.communication.ConnectionAgentListener;
import net.grinder.console.communication.ConnectionAgentCommunicationListener;
import net.grinder.console.model.ConsoleCommunicationSetting;
import net.grinder.engine.communication.AgentUpdateGrinderMessage;
import net.grinder.engine.controller.AgentControllerIdentityImplementation;
import net.grinder.message.console.AgentControllerState;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.ngrinder.agent.service.AgentPackageService;
import org.ngrinder.agent.store.AgentInfoStore;
import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.common.util.CRC32ChecksumUtils;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Predicate;

import static net.grinder.common.GrinderProperties.CONSOLE_PORT;
import static net.grinder.message.console.AgentControllerState.BUSY;
import static net.grinder.util.thread.ExecutorFactory.createThreadPool;

/**
 * Agent manager.
 * <p/>
 * This class has {@link AgentControllerServerDaemon} internally and manage to the agent connection.
 *
 * @since 3.0
 */
@SuppressWarnings("UnusedDeclaration")
@Component
@RequiredArgsConstructor
public class AgentManager implements ControllerConstants, AgentDownloadRequestListener {
	public static final Logger LOGGER = LoggerFactory.getLogger(AgentManager.class);

	private static final int NUMBER_OF_THREAD = 3;

	private final Config config;

	private final AgentPackageService agentPackageService;

	private final AgentInfoStore agentInfoStore;

	private AgentControllerServerDaemon agentControllerServerDaemon;

	/**
	 * Initialize agent manager.
	 */
	@PostConstruct
	public void init() {
		ConsoleCommunicationSetting consoleCommunicationSetting = ConsoleCommunicationSetting.asDefault();
		if (config.getInactiveClientTimeOut() > 0) {
			consoleCommunicationSetting.setInactiveClientTimeOut(config.getInactiveClientTimeOut());
		}

		agentControllerServerDaemon = new AgentControllerServerDaemon(config.getCurrentIP(), config.getControllerPort(), consoleCommunicationSetting);
		agentControllerServerDaemon.start();
		agentControllerServerDaemon.addAgentDownloadRequestListener(this);
		agentControllerServerDaemon.addLogArrivedListener((testId, agentAddress, logs) -> {
			AgentControllerIdentityImplementation agentIdentity = convert(agentAddress.getIdentity());
			if (ArrayUtils.isEmpty(logs)) {
				LOGGER.error("Log is arrived from {} but no log content", agentIdentity.getIp());
			}
			File logFile = null;
			try {
				logFile = new File(config.getHome().getPerfTestLogDirectory(testId.replace("test_", "")),
						agentIdentity.getName() + "-" + agentIdentity.getSubregion() + "-log.zip");
				FileUtils.writeByteArrayToFile(logFile, logs);
			} catch (IOException e) {
				LOGGER.error("Error while write logs from {} to {}", agentAddress.getIdentity().getName(),
						logFile.getAbsolutePath());
				LOGGER.error("Error is following", e);
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
	public int getAttachedAgentConnectingPort(AgentIdentity agentIdentity) {
		return agentControllerServerDaemon.getAgentConnectingPort(agentIdentity);
	}

	/**
	 * Get all agent status.
	 *
	 * @return {@link AgentStatus} set
	 * @since 3.1.2
	 */
	public Set<AgentStatus> getAllAttachedAgentStatusSet() {
		return agentControllerServerDaemon.getAgentStatusSet(agentStatus -> true);
	}

	/**
	 * Get agent status set matching the given predicate.
	 *
	 * @param predicate predicate
	 * @return {@link AgentStatus} set
	 * @since 3.1.2
	 */
	public Set<AgentStatus> getAttachedAgentStatusSet(Predicate<AgentStatus> predicate) {
		return agentControllerServerDaemon.getAgentStatusSet(predicate);
	}

	/**
	 * Get the agent status of the given agent.
	 *
	 * @param agentIdentity agentIdentity of one agent
	 * @return status agent controller status of one agent
	 */
	public AgentControllerState getAttachedAgentState(AgentIdentity agentIdentity) {
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

	public boolean getAgentForceUpdate() {
		return config.getControllerProperties().getPropertyBoolean(PROP_CONTROLLER_AGENT_FORCE_UPDATE);
	}

	private int getUpdateChunkSize() {
		return config.getControllerProperties().getPropertyInt(PROP_CONTROLLER_UPDATE_CHUNK_SIZE);
	}

	/**
	 * Convert {@link AgentIdentity} to {@link AgentControllerIdentityImplementation} type.
	 *
	 * @param identity identity
	 * @return converted identity.
	 */
	public static AgentControllerIdentityImplementation convert(AgentIdentity identity) {
		return (AgentControllerIdentityImplementation) identity;
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
	 * @param singleConsole     {@link SingleConsole} to which agents will be assigned
	 * @param grinderProperties {@link GrinderProperties} to be distributed.
	 * @param necessaryAgents   necessary agents for running test.
	 */
	public synchronized void runAgent(final SingleConsole singleConsole,
									  final GrinderProperties grinderProperties,
									  final Set<AgentInfo> necessaryAgents) {
		ExecutorService execService = null;
		try {
			// Make the agents connect to console.
			grinderProperties.setInt(CONSOLE_PORT, singleConsole.getConsolePort());
			execService = createThreadPool("agentStarter", NUMBER_OF_THREAD);
			for (final AgentInfo agentInfo : necessaryAgents) {
				execService.submit(() -> agentControllerServerDaemon.startAgent(grinderProperties, agentInfo.getAgentIdentity()));
			}
		} finally {
			if (execService != null) {
				execService.shutdown();
			}
		}
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
		Set<AgentStatus> agentStatusSetConnectingToPort = getAttachedAgentStatusSetConnectingToPort(consolePort);
		for (AgentStatus each : agentStatusSetConnectingToPort) {
			if (each.getAgentControllerState() == BUSY) {
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

	public void addConnectionAgent(String ip, int port) throws IOException {
		Socket socket = new Socket(ip, port);
		agentControllerServerDaemon.discriminateConnection(socket);
	}

	/**
	 * Get the set of {@link AgentStatus} from agents belong to the given single console port.
	 *
	 * @param singleConsolePort port
	 * @return {@link AgentStatus} set
	 */
	public Set<AgentStatus> getAttachedAgentStatusSetConnectingToPort(final int singleConsolePort) {
		return getAttachedAgentStatusSet(status -> status.getConnectingPort() == singleConsolePort);
	}

	@Override
	public synchronized AgentUpdateGrinderMessage onAgentDownloadRequested(String version, int offset) {
		final int updateChunkSize = getUpdateChunkSize();
		byte[] buffer = new byte[updateChunkSize];

		try (RandomAccessFile agentPackageReader = new RandomAccessFile(agentPackageService.createAgentPackage(), "r")) {
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
		}
		return AgentUpdateGrinderMessage.getNullAgentUpdateGrinderMessage(version);
	}

	public void addAgentStatusUpdateListener(AgentStatusUpdateListener agentStatusUpdateListener) {
		agentControllerServerDaemon.addAgentStatusUpdateListener(agentStatusUpdateListener);
	}

    public void addConnectionAgentListener(ConnectionAgentListener connectionAgentListener) {
		agentControllerServerDaemon.addConnectionAgentListener(connectionAgentListener);
    }

    public void addConnectionAgentCommunicationListener(ConnectionAgentCommunicationListener listener) {
		agentControllerServerDaemon.addConnectionAgentCommunicationListener(listener);
	}
}
