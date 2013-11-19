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

import net.grinder.AgentDaemon.AgentShutDownListener;
import net.grinder.common.GrinderException;
import net.grinder.common.GrinderProperties;
import net.grinder.communication.*;
import net.grinder.engine.agent.Agent;
import net.grinder.engine.common.AgentControllerConnectorFactory;
import net.grinder.engine.communication.AgentControllerServerListener;
import net.grinder.engine.communication.LogReportGrinderMessage;
import net.grinder.engine.controller.AgentControllerIdentityImplementation;
import net.grinder.message.console.AgentControllerProcessReportMessage;
import net.grinder.message.console.AgentControllerState;
import net.grinder.messages.agent.StartGrinderMessage;
import net.grinder.messages.console.AgentAddress;
import net.grinder.util.LogCompressUtil;
import net.grinder.util.NetworkUtil;
import net.grinder.util.thread.Condition;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.ngrinder.infra.AgentConfig;
import org.ngrinder.monitor.agent.collector.AgentSystemDataCollector;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.monitor.share.domain.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import static org.ngrinder.common.util.NoOp.noOp;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

/**
 * Agent Controller which handles agent start and stop.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public class AgentController implements Agent {

	private static final Logger LOGGER = LoggerFactory.getLogger("agent controller");

	private Timer m_timer;
	private final Condition m_eventSynchronisation = new Condition();
	private final AgentControllerIdentityImplementation m_agentIdentity;
	private final AgentControllerServerListener m_agentControllerServerListener;
	private FanOutStreamSender m_fanOutStreamSender;
	private final AgentControllerConnectorFactory m_connectorFactory = new AgentControllerConnectorFactory(
					ConnectionType.AGENT);
	private AgentConfig agentConfig;
	private final Condition m_eventSyncCondition;
	private volatile AgentControllerState m_state = AgentControllerState.STARTED;

	private GrinderProperties m_grinderProperties;

	private AgentSystemDataCollector agentSystemDataCollector = new AgentSystemDataCollector();

	private int m_connectionPort = 0;

	private static SystemDataModel emptySystemDataModel = new SystemDataModel();

	private AgentUpdateHandler agentUpdateHandler;

	private String version;

	/**
	 * Constructor.
	 * 
	 * @param eventSyncCondition
	 *            event sync condition to wait until agent start to run.
	 * @param currentIp
	 *            current ip
	 * @throws GrinderException
	 *             If an error occurs.
	 */
	public AgentController(Condition eventSyncCondition, String currentIp) throws GrinderException {
		m_eventSyncCondition = eventSyncCondition;
		m_agentControllerServerListener = new AgentControllerServerListener(m_eventSynchronisation, LOGGER);
		// Set it with the default name 
		m_agentIdentity = new AgentControllerIdentityImplementation(NetworkUtil.getLocalHostName(), currentIp);
		agentSystemDataCollector = new AgentSystemDataCollector();
		agentSystemDataCollector.refresh();
	}

	/**
	 * Run agent controller. This method use default server port (for test)
	 * 
	 * @exception GrinderException
	 *                occurs when there is a problem.
	 */
	public void run() throws GrinderException {
		GrinderProperties grinderProperties = new GrinderProperties();
		grinderProperties.setInt(AgentConfig.AGENT_CONTROLLER_SERVER_PORT,
						AgentControllerCommunicationDefauts.DEFAULT_AGENT_CONTROLLER_SERVER_PORT);
		synchronized (m_eventSyncCondition) {
			m_eventSyncCondition.notifyAll();
		}
		run(grinderProperties, 1);
	}

	/**
	 * Run the agent controller.
	 * 
	 * @param grinderProperties
	 *            {@link GrinderProperties} used.
	 * @param logCount
	 *            log count
	 * @throws GrinderException
	 *             occurs when the test execution is failed.
	 */
	public void run(GrinderProperties grinderProperties, long logCount) throws GrinderException {
		StartGrinderMessage startMessage = null;
		ConsoleCommunication consoleCommunication = null;
		m_fanOutStreamSender = new FanOutStreamSender(GrinderConstants.AGENT_CONTROLLER_FANOUT_STREAM_THREAD_COUNT);
		m_timer = new Timer(false);
		AgentDaemon agent = new AgentDaemon(checkNotNull(getAgentConfig(),
						"agentconfig should be provided before agent daemon start."));

		m_grinderProperties = grinderProperties;
		try {
			while (true) {
				do {
					m_agentIdentity.setName(agentConfig.getProperty(AgentConfig.AGENT_HOSTID,
									NetworkUtil.getLocalHostName()));
					m_agentIdentity.setRegion(agentConfig.getProperty(AgentConfig.AGENT_REGION, ""));
					final Connector connector = m_connectorFactory.create(m_grinderProperties);

					if (consoleCommunication == null) {
						try {
							consoleCommunication = new ConsoleCommunication(connector);
							consoleCommunication.start();
							LOGGER.info("connected to agent controller server at {}", connector.getEndpointAsString());
						} catch (CommunicationException e) {
							LOGGER.error("Error while connecting to controller {}", connector.getEndpointAsString());
							LOGGER.debug(e.getMessage(), e);
							return;
						}
					}

					if (consoleCommunication != null && startMessage == null) {
						LOGGER.info("waiting for agent controller server signal");
						m_state = AgentControllerState.READY;
						m_agentControllerServerListener.waitForMessage();

						if (m_agentControllerServerListener.received(AgentControllerServerListener.START)) {
							startMessage = m_agentControllerServerListener.getLastStartGrinderMessage();

							LOGGER.info("agent start message is received from controller {}", startMessage);
							continue;
						} else {
							break; // Another message, check at end of outer
									// while loop.
						}
					}

					if (startMessage != null) {
						m_agentIdentity.setNumber(startMessage.getAgentNumber());
					}
				} while (checkNotNull(startMessage).getProperties() == null);

				// Here the agent run code goes..
				if (startMessage != null) {
					final String testId = startMessage.getProperties().getProperty("grinder.test.id", "");
					LOGGER.info("starting agent... for {}", testId);
					m_state = AgentControllerState.BUSY;
					m_connectionPort = startMessage.getProperties().getInt(GrinderProperties.CONSOLE_PORT, 0);
					agent.run(startMessage.getProperties());

					final ConsoleCommunication conCom = consoleCommunication;
					agent.resetListeners();
					agent.addListener(new AgentShutDownListener() {
						@Override
						public void shutdownAgent() {
							LOGGER.info("send log for {}", testId);
							sendLog(conCom, testId);
							m_state = AgentControllerState.READY;
							m_connectionPort = 0;
						}
					});
				}
				// Ignore any pending start messages.
				m_agentControllerServerListener.discardMessages(AgentControllerServerListener.START);

				if (!m_agentControllerServerListener.received(AgentControllerServerListener.ANY)) {
					// We've got here naturally, without a console signal.
					LOGGER.info("agent is started. waiting for agent controller signal");
					m_agentControllerServerListener.waitForMessage();
				}

				if (m_agentControllerServerListener.received(AgentControllerServerListener.START)) {
					startMessage = m_agentControllerServerListener.getLastStartGrinderMessage();
				} else if (m_agentControllerServerListener.received(AgentControllerServerListener.STOP)) {
					agent.shutdown();
					startMessage = null;
					m_connectionPort = 0;
					m_agentControllerServerListener.discardMessages(AgentControllerServerListener.STOP);
				} else if (m_agentControllerServerListener.received(AgentControllerServerListener.SHUTDOWN)) {
					startMessage = null;
					m_connectionPort = 0;
					break;
				} else if (m_agentControllerServerListener.received(AgentControllerServerListener.UPDATE_AGENT)) {
					// Do update agent by downloading new version.
					startMessage = null;
					m_connectionPort = 0;
					m_state = AgentControllerState.UPDATING;
					sendCurrentState(consoleCommunication);
					agentUpdateHandler = new AgentUpdateHandler(agentConfig);
					agentUpdateHandler.updateAgent(m_agentControllerServerListener.getLastUpdateAgentGrinderMessage());
				} else if (m_agentControllerServerListener.received(AgentControllerServerListener.LOG_REPORT)) {
					startMessage = null;
					m_state = AgentControllerState.BUSY;
					sendCurrentState(consoleCommunication);
					// Do update
				} else {
					// ConsoleListener.RESET or natural death.
					startMessage = null;
				}
			}

		} finally {
			m_connectionPort = 0;
			// Abnormal state.
			agent.shutdown();
			m_state = AgentControllerState.FINISHED;
			shutdownConsoleCommunication(consoleCommunication);
			m_timer.cancel();
		}
	}

	private void sendLog(ConsoleCommunication consoleCommunication, String testId) {
		File logFolder = new File(agentConfig.getHome().getLogDirectory(), testId);
		if (!logFolder.exists()) {
			return;
		}
		File[] logFiles = logFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return (name.endsWith(".log"));
			}
		});
		Arrays.sort(logFiles);
		if (ArrayUtils.isEmpty(logFiles)) {
			LOGGER.error("No log exists under {}", logFolder.getAbsolutePath());
			return;
		}

		// Take only one file... if agent.send.all.logs is not set.
		if (!agentConfig.getPropertyBoolean("agent.send.all.logs", false)) {
			logFiles = new File[] { logFiles[0] };
		}

		consoleCommunication.sendMessage(new LogReportGrinderMessage(testId, LogCompressUtil.compressFile(logFiles),
						new AgentAddress(m_agentIdentity)));
		// Delete logs to clean up
		FileUtils.deleteQuietly(logFolder);
	}

	private void shutdownConsoleCommunication(ConsoleCommunication consoleCommunication) {
		sendCurrentState(consoleCommunication);
		if (consoleCommunication != null) {
			consoleCommunication.shutdown();
			consoleCommunication = null;
		}
		m_agentControllerServerListener.discardMessages(AgentControllerServerListener.ANY);
	}

	private void sendCurrentState(ConsoleCommunication consoleCommunication) {
		if (consoleCommunication != null) {
			try {
				consoleCommunication.sendCurrentState();
			} catch (CommunicationException e) {
				LOGGER.error("Error while sending current state : {}.", e.getMessage());
				LOGGER.debug("The error detail is ", e);
			}
		}
	}

	/**
	 * Clean up resources.
	 */
	public void shutdown() {
		if (m_timer != null) {
			m_timer.cancel();
		}
		if (m_fanOutStreamSender != null) {
			m_fanOutStreamSender.shutdown();
		}
		m_agentControllerServerListener.shutdown();
		LOGGER.info("finished");
	}

	/**
	 * Get current System performance.
	 * 
	 * @return {@link SystemDataModel} instance
	 */
	public SystemDataModel getSystemDataModel() {
		try {
			SystemInfo systemInfo = agentSystemDataCollector.execute();
			return new SystemDataModel(systemInfo, this.version);
		} catch (Exception e) {
			LOGGER.error("Error while getting system perf data model : {} ", e.getMessage());
			LOGGER.debug("The error detail is ", e);
			return emptySystemDataModel;
		}
	}

	public AgentConfig getAgentConfig() {
		return agentConfig;
	}

	/**
	 * Set agent config.
	 * 
	 * @param agentConfig
	 *            agent config
	 */
	public void setAgentConfig(AgentConfig agentConfig) {
		this.agentConfig = agentConfig;
		this.version = agentConfig.getInternalProperty("ngrinder.version", "3.1.3");
		this.agentSystemDataCollector.setAgentHome(agentConfig.getHome().getDirectory());
	}

	private final class ConsoleCommunication {
		private final ClientSender m_sender;
		private final TimerTask m_reportRunningTask;
		private final MessagePump m_messagePump;

		public ConsoleCommunication(Connector connector) throws CommunicationException {
			final ClientReceiver receiver = ClientReceiver.connect(connector, new AgentAddress(m_agentIdentity));
			m_sender = ClientSender.connect(receiver);

			m_sender.send(new AgentControllerProcessReportMessage(AgentControllerState.STARTED, getSystemDataModel(),
							m_connectionPort));
			final MessageDispatchSender messageDispatcher = new MessageDispatchSender();
			m_agentControllerServerListener.registerMessageHandlers(messageDispatcher);

			m_messagePump = new MessagePump(receiver, messageDispatcher, 1);

			m_reportRunningTask = new TimerTask() {
				public void run() {
					try {
						sendCurrentState();
					} catch (CommunicationException e) {
						cancel();
						LOGGER.error("Error while sending current state:" + e.getMessage());
						LOGGER.debug("The error detail is", e);
					}
				}
			};
		}

		public void sendMessage(AddressAwareMessage message) {
			try {
				m_sender.send(message);
			} catch (CommunicationException e) {
				LOGGER.error("{}. This error is ignorable if it doesn't occur much.", e.getMessage());
			}
		}

		public void sendCurrentState() throws CommunicationException {
			sendMessage(new AgentControllerProcessReportMessage(m_state, getSystemDataModel(), m_connectionPort));
		}

		public void start() {
			m_messagePump.start();
			m_timer.schedule(m_reportRunningTask, 0, GrinderConstants.AGENT_CONTROLLER_HEARTBEAT_INTERVAL);
		}

		public void shutdown() {
			m_reportRunningTask.cancel();
			try {
				m_sender.send(new AgentControllerProcessReportMessage(AgentControllerState.FINISHED, null, 0));
			} catch (CommunicationException e) {
				// Fall through
				// Ignore - peer has probably shut down.
				noOp();
			} finally {
				m_messagePump.shutdown();
			}
		}
	}
}
