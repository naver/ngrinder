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
package net.grinder;

import net.grinder.common.GrinderException;
import net.grinder.common.GrinderProperties;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.console.common.Resources;
import net.grinder.console.common.ResourcesImplementation;
import net.grinder.console.communication.*;
import net.grinder.console.communication.AgentProcessControlImplementation.AgentStatus;
import net.grinder.console.model.ConsoleCommunicationSetting;
import net.grinder.console.model.ConsoleProperties;
import net.grinder.engine.communication.AgentUpdateGrinderMessage;
import net.grinder.message.console.AgentControllerState;
import net.grinder.messages.agent.StartGrinderMessage;
import net.grinder.messages.agent.StopGrinderMessage;
import net.grinder.messages.console.AgentAddress;
import net.grinder.util.ConsolePropertiesFactory;
import net.grinder.util.thread.Condition;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.python.google.common.base.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static org.ngrinder.common.util.ExceptionUtils.processException;

/**
 * Daemon for AgentControllerServer.
 *
 * @author JunHo Yoon
 */
public class AgentControllerServerDaemon {
	private final ConsoleProperties consoleProperties;
	private Thread thread;
	private AgentControllerServer agentControllerServer;
	public static final Resources RESOURCES = new ResourcesImplementation(
			"net.grinder.console.common.resources.Console");
	private final Condition m_eventSyncCondition = new Condition();

	public static final Logger LOGGER = LoggerFactory.getLogger(RESOURCES.getString("shortTitle"));
	private static final long AGENT_CONTROLLER_SERVER_SHUTDOWN_WAITING_TIMEOUT = 10000;
	private static final long AGENT_CONTROLLER_SERVER_EVENT_EXPIRATION_TIMEOUT = 10000;

	/**
	 * Agent controller daemon constructor with default
	 * {@link ConsoleProperties}.
	 *
	 * @param ip   IP
	 * @param port port
	 */
	public AgentControllerServerDaemon(String ip, int port, ConsoleCommunicationSetting consoleCommunicationSetting) {
		this(ip, port, ConsolePropertiesFactory.createEmptyConsoleProperties(), consoleCommunicationSetting);
	}

	/**
	 * Agent controller daemon constructor.
	 *
	 * @param ip                IP
	 * @param port              port
	 * @param consoleProperties default property.
	 */
	public AgentControllerServerDaemon(String ip, int port, ConsoleProperties consoleProperties,
									   ConsoleCommunicationSetting consoleCommunicationSetting) {
		this.consoleProperties = consoleProperties;
		try {
			this.consoleProperties.setConsoleHost(ip);
			this.consoleProperties.setConsolePort(port);
			this.agentControllerServer = new AgentControllerServer(RESOURCES, LOGGER, consoleProperties,
					m_eventSyncCondition, consoleCommunicationSetting);
		} catch (GrinderException e) {
			throw processException("Exception occurred while initiating the agent controller server daemon", e);
		}
	}

	/**
	 * Get port.
	 *
	 * @return port
	 */
	public int getPort() {
		return this.consoleProperties.getConsolePort();
	}

	/**
	 * Constructor.
	 *
	 * @param port port used.
	 */
	public AgentControllerServerDaemon(int port) {
		this("", port, ConsoleCommunicationSetting.asDefault());
	}

	/**
	 * Start {@link AgentControllerServer} in the daemon thread.
	 */
	public void start() {
		thread = new Thread(new Runnable() {
			public void run() {
				startSync();
			}
		});
		thread.setName("Agent controller server thread");
		thread.setDaemon(true);
		thread.start();
		synchronized (m_eventSyncCondition) {
			m_eventSyncCondition.waitNoInterrruptException(AGENT_CONTROLLER_SERVER_EVENT_EXPIRATION_TIMEOUT);
		}
	}

	/**
	 * For test.
	 */
	public void startSync() {
		agentControllerServer.run();
	}

	/**
	 * Shutdown {@link AgentControllerServer}.
	 */
	public void shutdown() {
		try {
			agentControllerServer.shutdown();
			if (thread != null) {
				thread.join(AGENT_CONTROLLER_SERVER_SHUTDOWN_WAITING_TIMEOUT);
				thread.interrupt();
			}
		} catch (Exception e) {
			throw processException("Exception occurred during shutting down the agent controller server daemon", e);
		} finally {
			thread = null;
		}
	}

	public int getAllAttachedAgentsCount() {
		return agentControllerServer.getComponent(AgentProcessControlImplementation.class)
				.getNumberOfLiveAgents();
	}

	/**
	 * Add Listener which will be used to save log in somewhere.
	 *
	 * @param logArrivedListener listener
	 */
	public void addLogArrivedListener(LogArrivedListener logArrivedListener) {
		getComponent(AgentProcessControlImplementation.class).addLogArrivedListener(logArrivedListener);
	}

	/**
	 * Add Listener which will be used to send the agent download request.
	 *
	 * @param agentDownloadRequestListener listener
	 */
	public void setAgentDownloadRequestListener(AgentDownloadRequestListener agentDownloadRequestListener) {
		getComponent(AgentProcessControlImplementation.class).setAgentDownloadListener(agentDownloadRequestListener);
	}

	/**
	 * Get the console port which the given controller's agent is using.
	 *
	 * @param agentIdentity agent identity
	 * @return port
	 */
	public int getAgentConnectingPort(AgentIdentity agentIdentity) {
		return agentControllerServer.getComponent(AgentProcessControlImplementation.class).getAgentConnectingPort(
				agentIdentity);
	}

	/**
	 * Get the agent version of the given agent.
	 *
	 * @param agentIdentity agent identity
	 * @return version
	 */
	public String getAgentVersion(AgentIdentity agentIdentity) {
		return agentControllerServer.getComponent(AgentProcessControlImplementation.class).getAgentVersion(agentIdentity);
	}

	/**
	 * Get agent status set matching the given predicate.
	 *
	 * @param predicate predicate
	 * @return {@link AgentStatus} set
	 * @since 3.1.2
	 */
	public Set<AgentStatus> getAgentStatusSet(Predicate<AgentStatus> predicate) {
		return agentControllerServer.getComponent(AgentProcessControlImplementation.class).getAgentStatusSet(predicate);
	}

	/**
	 * Get the agent status of the given agent controller.
	 *
	 * @param agentIdentity agent identity
	 * @return agent controller status
	 */
	public AgentControllerState getAgentState(AgentIdentity agentIdentity) {
		return agentControllerServer.getComponent(AgentProcessControlImplementation.class).getAgentControllerState(
				agentIdentity);
	}

	/**
	 * Get all free agents which is not used yet.
	 *
	 * @return free agent list
	 */
	public Set<AgentIdentity> getAllFreeAgents() {
		return agentControllerServer.getComponent(AgentProcessControlImplementation.class).getAgents(
				AgentControllerState.READY, 0);
	}

	/**
	 * Get {@link SystemDataModel} of the given agent.
	 *
	 * @param agentIdentity agent identity
	 * @return {@link SystemDataModel} instance.
	 */
	public SystemDataModel getSystemDataModel(AgentIdentity agentIdentity) {
		return agentControllerServer.getComponent(AgentProcessControlImplementation.class).getSystemDataModel(
				agentIdentity);
	}

	/**
	 * Get all available agents.
	 *
	 * @return agent set
	 */
	public Set<AgentIdentity> getAllAvailableAgents() {
		return agentControllerServer.getComponent(AgentProcessControlImplementation.class).getAllAgents();
	}

	/**
	 * Get component used in {@link AgentControllerServer}.
	 *
	 * @param componentType component type class
	 * @param <T>           component type class
	 * @return <T> the component in consoleFoundation
	 */
	public <T> T getComponent(Class<T> componentType) {
		return agentControllerServer.getComponent(componentType);
	}

	/**
	 * Start agent using {@link GrinderProperties}.
	 *
	 * @param grinderProperties base grinder properties
	 * @param agentIdentity     agent controller identity
	 */
	public void startAgent(GrinderProperties grinderProperties, AgentIdentity agentIdentity) {
		LOGGER.info("{} agent is started.", agentIdentity);
		final ConsoleCommunicationImplementationEx component = getComponent(ConsoleCommunicationImplementationEx.class);
		final AgentAddress address = new AgentAddress(agentIdentity);
		final String localConnectingAddress = component.getLocalConnectingAddress(address);
		final GrinderProperties prop = (GrinderProperties) grinderProperties.clone();
		prop.setProperty(GrinderProperties.CONSOLE_HOST, localConnectingAddress);
		component.sendToAddressedAgents(address,
				new StartGrinderMessage(prop, agentIdentity.getNumber()));
	}

	/**
	 * Stop agent.
	 *
	 * @param agentIdentity agent controller identity
	 */
	public void stopAgent(AgentIdentity agentIdentity) {
		LOGGER.info("{} agent is stopped.", agentIdentity);
		getComponent(ConsoleCommunication.class).sendToAddressedAgents(new AgentAddress(agentIdentity),
				new StopGrinderMessage());
	}

	/**
	 * Send agent update message to agent
	 *
	 * @param agentIdentity agentIdentity
	 * @param version       version
	 */
	public void updateAgent(AgentIdentity agentIdentity, String version) {
		getComponent(ConsoleCommunication.class).sendToAddressedAgents(new AgentAddress(agentIdentity),
				AgentUpdateGrinderMessage.getStartAgentUpdateGrinderMessage(version));
	}
}
