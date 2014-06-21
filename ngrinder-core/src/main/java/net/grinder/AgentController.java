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
import net.grinder.engine.communication.AgentDownloadGrinderMessage;
import net.grinder.engine.communication.AgentUpdateGrinderMessage;
import net.grinder.engine.communication.LogReportGrinderMessage;
import net.grinder.engine.controller.AgentControllerIdentityImplementation;
import net.grinder.message.console.AgentControllerProcessReportMessage;
import net.grinder.message.console.AgentControllerState;
import net.grinder.messages.agent.StartGrinderMessage;
import net.grinder.messages.console.AgentAddress;
import net.grinder.util.LogCompressUtils;
import net.grinder.util.NetworkUtils;
import net.grinder.util.thread.Condition;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.ngrinder.common.constants.AgentConstants;
import org.ngrinder.infra.AgentConfig;
import org.ngrinder.monitor.collector.SystemDataCollector;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.monitor.share.domain.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import static org.ngrinder.common.constants.InternalConstants.PROP_INTERNAL_NGRINDER_VERSION;
import static org.ngrinder.common.util.NoOp.noOp;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

/**
 * Agent Controller which handles agent start and stop.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public class AgentController implements Agent, AgentConstants {

	private static final Logger LOGGER = LoggerFactory.getLogger("agent controller");
	private final AgentConfig agentConfig;

	private Timer m_timer;
	@SuppressWarnings("FieldCanBeLocal")
	private final Condition m_eventSynchronization = new Condition();
	private final AgentControllerIdentityImplementation m_agentIdentity;
	private final AgentControllerServerListener m_agentControllerServerListener;
	private FanOutStreamSender m_fanOutStreamSender;
	private final AgentControllerConnectorFactory m_connectorFactory = new AgentControllerConnectorFactory(
			ConnectionType.AGENT);
	private final Condition m_eventSyncCondition;
	private volatile AgentControllerState m_state = AgentControllerState.STARTED;

	private SystemDataCollector agentSystemDataCollector = new SystemDataCollector();

	private int m_connectionPort = 0;

	private static SystemDataModel emptySystemDataModel = new SystemDataModel();

	private AgentUpdateHandler agentUpdateHandler;

	private int retryCount = 0;

	private String version;

	/**
	 * Constructor.
	 *
	 * @param eventSyncCondition event sync condition to wait until agent start to run.
	 */
	public AgentController(Condition eventSyncCondition, AgentConfig agentConfig) throws GrinderException {
		this.m_eventSyncCondition = eventSyncCondition;

		this.agentConfig = agentConfig;
		this.version = agentConfig.getInternalProperties().getProperty(PROP_INTERNAL_NGRINDER_VERSION);
		this.m_agentControllerServerListener = new AgentControllerServerListener(m_eventSynchronization, LOGGER);
		// Set it with the default name
		this.m_agentIdentity = new AgentControllerIdentityImplementation(agentConfig.getAgentHostID(), NetworkUtils.DEFAULT_LOCAL_HOST_ADDRESS);
		this.m_agentIdentity.setRegion(agentConfig.getRegion());
		this.agentSystemDataCollector = new SystemDataCollector();
		this.agentSystemDataCollector.setAgentHome(agentConfig.getHome().getDirectory());
		this.agentSystemDataCollector.refresh();
	}


	/**
	 * Run the agent controller.
	 *
	 * @throws GrinderException occurs when the test execution is failed.
	 */
	@SuppressWarnings("ConstantConditions")
	public void run() throws GrinderException {
		synchronized (m_eventSyncCondition) {
			m_eventSyncCondition.notifyAll();
		}

		StartGrinderMessage startMessage = null;
		ConsoleCommunication consoleCommunication = null;
		m_fanOutStreamSender = new FanOutStreamSender(GrinderConstants.AGENT_CONTROLLER_FANOUT_STREAM_THREAD_COUNT);
		m_timer = new Timer(false);
		AgentDaemon agent = new AgentDaemon(checkNotNull(agentConfig,
				"agent.conf should be provided before agent daemon start."));
		try {
			while (true) {
				do {
					if (consoleCommunication == null) {
						final Connector connector = m_connectorFactory.create(agentConfig.getControllerIP(), agentConfig.getControllerPort());
						try {
							consoleCommunication = new ConsoleCommunication(connector);
							consoleCommunication.start();
							LOGGER.info("Connected to agent controller server at {}", connector.getEndpointAsString());
						} catch (CommunicationException e) {
							LOGGER.error("Error while connecting to agent controller server at {}",
									connector.getEndpointAsString());
							return;
						}
					}

					if (consoleCommunication != null && startMessage == null) {
						if (m_state == AgentControllerState.UPDATING) {
							m_agentControllerServerListener.waitForMessage();
							break;
						} else {
							LOGGER.info("Waiting for agent controller server signal");
							m_state = AgentControllerState.READY;
							m_agentControllerServerListener.waitForMessage();
							if (m_agentControllerServerListener.received(AgentControllerServerListener.START)) {
								startMessage = m_agentControllerServerListener.getLastStartGrinderMessage();
								LOGGER.info("Agent start message is received from controller {}", startMessage);
								continue;
							} else {
								break; // Another message, check at end of outer
								// while loop.
							}
						}
					}

					if (startMessage != null) {
						m_agentIdentity.setNumber(startMessage.getAgentNumber());
					}
				} while (checkNotNull(startMessage).getProperties() == null);

				// Here the agent run code goes..
				if (startMessage != null) {
					final String testId = startMessage.getProperties().getProperty("grinder.test.id", "unknown");
					LOGGER.info("Starting agent... for {}", testId);
					m_state = AgentControllerState.BUSY;
					m_connectionPort = startMessage.getProperties().getInt(GrinderProperties.CONSOLE_PORT, 0);
					agent.run(startMessage.getProperties());

					final ConsoleCommunication conCom = consoleCommunication;
					agent.resetListeners();
					agent.addListener(new AgentShutDownListener() {
						@Override
						public void shutdownAgent() {
							LOGGER.info("Send log for {}", testId);
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
					LOGGER.info("Agent is started. Waiting for agent controller signal");
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
					m_connectionPort = 0;
					break;
				} else if (m_agentControllerServerListener.received(AgentControllerServerListener.AGENT_UPDATE)) {
					// Do update agent by downloading new version.
					startMessage = null;
					m_connectionPort = 0;
					m_state = AgentControllerState.UPDATING;
					final AgentUpdateGrinderMessage message = m_agentControllerServerListener.getLastAgentUpdateGrinderMessage();
					m_agentControllerServerListener.discardMessages(AgentControllerServerListener.AGENT_UPDATE);
					AgentDownloadGrinderMessage agentDownloadGrinderMessage = new AgentDownloadGrinderMessage(message.getVersion());
					try {
						// If it's initial message
						if (agentUpdateHandler == null && message.getNext() == 0) {
							IOUtils.closeQuietly(agentUpdateHandler);
							agentUpdateHandler = new AgentUpdateHandler(agentConfig, message);
						} else if (agentUpdateHandler != null) {
							if (message.isValid()) {
								retryCount = 0;
								agentUpdateHandler.update(message);
								agentDownloadGrinderMessage.setNext(message.getNext());
							} else if (retryCount <= AgentDownloadGrinderMessage.MAX_RETRY_COUNT) {
								retryCount++;
								agentDownloadGrinderMessage.setNext(message.getOffset());
							} else {
								throw new CommunicationException("Error while getting the agent package from " +
										"controller");
							}
						} else {
							throw new CommunicationException("Error while getting the agent package from controller");
						}
						if (consoleCommunication != null) {
							consoleCommunication.sendMessage(agentDownloadGrinderMessage);
						} else {
							break;
						}

					} catch (IllegalArgumentException ex) {
						IOUtils.closeQuietly(agentUpdateHandler);
						agentUpdateHandler = null;
						retryCount = 0;
						LOGGER.info("same or old agent version {} is sent for update. skip this.",
								message.getVersion());
						m_state = AgentControllerState.READY;
					} catch (Exception e) {
						retryCount = 0;
						IOUtils.closeQuietly(agentUpdateHandler);
						agentUpdateHandler = null;
						LOGGER.error("While updating agent, the exception occurred.", e);
						m_state = AgentControllerState.READY;
					}

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

		if (logFiles == null || ArrayUtils.isEmpty(logFiles)) {
			LOGGER.error("No log exists under {}", logFolder.getAbsolutePath());
			return;
		}
		Arrays.sort(logFiles);
		// Take only one file... if agent.send.all.logs is not set.
		if (!agentConfig.getAgentProperties().getPropertyBoolean(PROP_AGENT_ALL_LOGS)) {
			logFiles = new File[]{logFiles[0]};
		}
		final byte[] compressedLog = LogCompressUtils.compress(logFiles,
				Charset.defaultCharset(), Charset.forName("UTF-8")
		);
		consoleCommunication.sendMessage(new LogReportGrinderMessage(testId, compressedLog, new AgentAddress(m_agentIdentity)));
		// Delete logs to clean up
		if (!agentConfig.getAgentProperties().getPropertyBoolean(PROP_AGENT_KEEP_LOGS)) {
			LOGGER.info("Clean up the perftest logs");
			FileUtils.deleteQuietly(logFolder);
		}
	}

	private void shutdownConsoleCommunication(ConsoleCommunication consoleCommunication) {
		sendCurrentState(consoleCommunication);
		if (consoleCommunication != null) {
			consoleCommunication.shutdown();
			//noinspection UnusedAssignment
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
		LOGGER.info("Agent controller shuts down");
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
			LOGGER.error("Error while getting system data model : {} ", e.getMessage());
			LOGGER.debug("The error detail is ", e);
			return emptySystemDataModel;
		}
	}

	public AgentConfig getAgentConfig() {
		return agentConfig;
	}


	public final class ConsoleCommunication {
		private final ClientSender m_sender;
		private final TimerTask m_reportRunningTask;
		private final MessagePump m_messagePump;

		public ConsoleCommunication(Connector connector) throws CommunicationException {
			final ClientReceiver receiver = ClientReceiver.connect(connector, new AgentAddress(m_agentIdentity));
			m_sender = ClientSender.connect(receiver);

			m_sender.send(new AgentControllerProcessReportMessage(AgentControllerState.STARTED, getSystemDataModel(),
					m_connectionPort, version));
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

		public void sendMessage(Message message) {
			try {
				m_sender.send(message);
			} catch (CommunicationException e) {
				LOGGER.error("{}. This error is not critical if it doesn't occur much.", e.getMessage());
			}
		}

		public void sendCurrentState() throws CommunicationException {
			sendMessage(new AgentControllerProcessReportMessage(m_state, getSystemDataModel(), m_connectionPort, version));
		}

		public void start() {
			m_messagePump.start();
			m_timer.schedule(m_reportRunningTask, 0, GrinderConstants.AGENT_CONTROLLER_HEARTBEAT_INTERVAL);
		}

		public void shutdown() {
			m_reportRunningTask.cancel();
			try {
				m_sender.send(new AgentControllerProcessReportMessage(AgentControllerState.FINISHED, null, 0, version));
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
