/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.grinder;

import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.io.File;
import java.io.FilenameFilter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import net.grinder.AgentDaemon.AgentShutDownListener;
import net.grinder.common.GrinderBuild;
import net.grinder.common.GrinderException;
import net.grinder.common.GrinderProperties;
import net.grinder.communication.AddressAwareMessage;
import net.grinder.communication.AgentControllerCommunicationDefauts;
import net.grinder.communication.ClientReceiver;
import net.grinder.communication.ClientSender;
import net.grinder.communication.CommunicationException;
import net.grinder.communication.ConnectionType;
import net.grinder.communication.Connector;
import net.grinder.communication.FanOutStreamSender;
import net.grinder.communication.MessageDispatchSender;
import net.grinder.communication.MessagePump;
import net.grinder.engine.agent.Agent;
import net.grinder.engine.common.AgentControllerConnectorFactory;
import net.grinder.engine.communication.AgentControllerServerListener;
import net.grinder.engine.communication.LogReportGrinderMessage;
import net.grinder.engine.communication.UpdateAgentGrinderMessage;
import net.grinder.engine.controller.AgentControllerIdentityImplementation;
import net.grinder.message.console.AgentControllerProcessReportMessage;
import net.grinder.message.console.AgentControllerState;
import net.grinder.messages.agent.StartGrinderMessage;
import net.grinder.messages.console.AgentAddress;
import net.grinder.util.LogCompressUtil;
import net.grinder.util.thread.Condition;

import org.apache.commons.io.FileUtils;
import org.ngrinder.infra.AgentConfig;
import org.ngrinder.monitor.agent.collector.AgentJavaDataCollector;
import org.ngrinder.monitor.agent.collector.AgentSystemDataCollector;
import org.ngrinder.monitor.controller.model.JavaDataModel;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.monitor.share.domain.JavaInfo;
import org.ngrinder.monitor.share.domain.SystemInfo;
import org.slf4j.Logger;

/**
 * Agent Controller which handles agent start and stop.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public class AgentController implements Agent {

	private final Logger m_logger;

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

	private AgentJavaDataCollector agentJavaDataCollector = new AgentJavaDataCollector();
	private AgentSystemDataCollector agentSystemDataCollector = new AgentSystemDataCollector();

	private int m_connectionPort = 0;

	/**
	 * Constructor.
	 * 
	 * @param logger
	 *            Logger.
	 * @param eventSyncCondition
	 *            event sync condition to wait until agent start to run.
	 * 
	 * @throws GrinderException
	 *             If an error occurs.
	 */
	public AgentController(Logger logger, Condition eventSyncCondition) throws GrinderException {
		m_logger = logger;
		m_eventSyncCondition = eventSyncCondition;
		m_agentControllerServerListener = new AgentControllerServerListener(m_eventSynchronisation, m_logger);
		m_agentIdentity = new AgentControllerIdentityImplementation(getHostName(), getHostAddress());

		agentJavaDataCollector.refresh();
		agentSystemDataCollector.refresh();
	}

	/**
	 * Get host address.
	 * 
	 * @return ip form of host address
	 */
	private String getHostAddress() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			return "UNNAMED HOST ADDRESS";
		}
	}

	/**
	 * Run agent controller. This method use default server port (for test)
	 * 
	 * @exception GrinderException
	 *                occurs when there is a problem.
	 */
	public void run() throws GrinderException {
		GrinderProperties grinderProperties = new GrinderProperties();
		grinderProperties.setInt(AgentConfig.AGENT_HOSTID,
						AgentControllerCommunicationDefauts.DEFAULT_AGENT_CONTROLLER_SERVER_PORT);
		synchronized (m_eventSyncCondition) {
			m_eventSyncCondition.notifyAll();
		}
		run(grinderProperties);
	}

	/**
	 * Run the agent controller.
	 * 
	 * @param grinderProperties
	 *            {@link GrinderProperties} used.
	 * @throws GrinderException
	 *             If an error occurs.
	 */
	public void run(GrinderProperties grinderProperties) throws GrinderException {

		StartGrinderMessage startMessage = null;
		ConsoleCommunication consoleCommunication = null;
		m_fanOutStreamSender = new FanOutStreamSender(
						GrinderConstants.AGENT_CONTROLLER_FANOUT_STREAM_THREAD_COUNT);
		m_timer = new Timer(false);
		AgentDaemon agent = new AgentDaemon(checkNotNull(getAgentConfig(),
						"agentconfig should be provided before agent daemon start."));

		m_grinderProperties = grinderProperties;
		try {
			while (true) {
				m_logger.info(GrinderBuild.getName());

				// GrinderProperties properties;
				do {
					// properties = grinderProperties;
					m_agentIdentity.setName(agentConfig.getProperty(AgentConfig.AGENT_HOSTID, getHostName()));
					m_agentIdentity.setRegion(agentConfig.getProperty(AgentConfig.AGENT_REGION, ""));
					final Connector connector = m_connectorFactory.create(m_grinderProperties);

					if (consoleCommunication == null) {
						try {
							consoleCommunication = new ConsoleCommunication(connector);
							consoleCommunication.start();
							m_logger.info("connected to agent controller server at {}",
											connector.getEndpointAsString());
						} catch (CommunicationException e) {
							m_logger.error(e.getMessage());
							return;
						}
					}

					if (consoleCommunication != null && startMessage == null) {
						m_logger.info("waiting for agent controller server signal");
						m_state = AgentControllerState.READY;
						m_agentControllerServerListener.waitForMessage();

						if (m_agentControllerServerListener.received(AgentControllerServerListener.START)) {
							startMessage = m_agentControllerServerListener.getLastStartGrinderMessage();

							m_logger.info("agent start message is revcieved from console " + startMessage);
							continue;
						} else {
							break; // Another message, check at end of outer
									// while loop.
						}

					}

					if (startMessage != null) {
						m_agentIdentity.setNumber(startMessage.getAgentNumber());
					}
				} while (checkNotNull(startMessage, "start method should be exist in messaging loop")
								.getProperties() == null);

				// Here the agent run code goes..
				if (startMessage != null) {
					final String testId = startMessage.getProperties().getProperty("grinder.test.id", "");
					m_logger.info("starting agent... for {}", testId);
					m_state = AgentControllerState.BUSY;
					m_connectionPort = startMessage.getProperties().getInt(GrinderProperties.CONSOLE_PORT, 0);
					agent.run(startMessage.getProperties());

					final ConsoleCommunication conCom = consoleCommunication;
					// It's normal shutdown..
					// FIXME : Is it safe to add listener here? no possibility for duplicated
					// listener?
					agent.addListener(new AgentShutDownListener() {
						@Override
						public void shutdownAgent() {
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
					m_logger.info("agent started. waiting for agent controller signal");
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
				} else if (m_agentControllerServerListener
								.received(AgentControllerServerListener.UPDATE_AGENT)) {
					// Do update agent itself.
					startMessage = null;
					m_connectionPort = 0;
					m_state = AgentControllerState.BUSY;
					sendCurrentState(consoleCommunication);
					updateAgent(m_agentControllerServerListener.getLastUpdateAgentGrinderMessage());
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

	private void updateAgent(UpdateAgentGrinderMessage lastUpdateAgentGrinderMessage) {
		// FIXME
	}

	private void sendLog(ConsoleCommunication consoleCommunication, String testId) {
		File logFolder = new File(agentConfig.getHome().getLogDirectory(), testId);
		if (!logFolder.exists()) {
			return;
		}
		String[] list = logFolder.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return (name.endsWith("-0.log"));
			}
		});
		if (list == null || list.length != 1) {
			m_logger.error("there is no log exists under {}", logFolder.getAbsolutePath());
			return;
		}
		consoleCommunication.sendMessage(new LogReportGrinderMessage(testId, LogCompressUtil
						.compressFile(new File(logFolder, list[0])), new AgentAddress(m_agentIdentity)));
		// Delete logs to clean up
		FileUtils.deleteQuietly(logFolder);
	}

	// /////////////////////////////////////////////////////

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
				m_logger.error("Error while sending current state", e.getMessage());
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
		m_logger.info("finished");
	}

	/**
	 * Get host name.
	 * 
	 * @return host name
	 */
	private static String getHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return "UNNAMED HOST";
		}
	}

	/**
	 * Get current System performance.
	 * 
	 * @return {@link SystemDataModel} instance
	 */
	public SystemDataModel getSystemDataModel() {
		try {
			SystemInfo systemInfo = agentSystemDataCollector.execute();
			return new SystemDataModel(systemInfo);
		} catch (Exception e) {
			m_logger.error("Error while get system perf data model : " + e.getMessage());
			return emptySystemDataModel;
		}
	}

	private static JavaDataModel emptyJavaDataModel = new JavaDataModel();
	private static SystemDataModel emptySystemDataModel = new SystemDataModel();

	/**
	 * Get current java performance.
	 * 
	 * @return {@link JavaDataModel} instance
	 */
	public JavaDataModel getJavaDataModel() {
		try {
			JavaInfo javaInfo = agentJavaDataCollector.execute();
			JavaDataModel javaDataModel = new JavaDataModel(javaInfo);

			javaDataModel.setKey(getHostAddress());
			javaDataModel.setIp(getHostAddress());
			javaDataModel.setPort(m_grinderProperties.getInt(AgentConfig.AGENT_CONTROLER_SERVER_PORT,
							AgentControllerCommunicationDefauts.DEFAULT_AGENT_CONTROLLER_SERVER_PORT));

			return javaDataModel;
		} catch (Exception e) {
			m_logger.error("Error while get java perf data model : " + e.getMessage());
			return emptyJavaDataModel;
		}

	}

	public AgentConfig getAgentConfig() {
		return agentConfig;
	}

	public void setAgentConfig(AgentConfig agentConfig) {
		this.agentConfig = agentConfig;
	}

	private final class ConsoleCommunication {
		private final ClientSender m_sender;
		private final TimerTask m_reportRunningTask;
		private final MessagePump m_messagePump;

		public ConsoleCommunication(Connector connector) throws CommunicationException {
			final ClientReceiver receiver = ClientReceiver.connect(connector, new AgentAddress(
							m_agentIdentity));
			m_sender = ClientSender.connect(receiver);

			m_sender.send(new AgentControllerProcessReportMessage(AgentControllerState.STARTED,
							getJavaDataModel(), getSystemDataModel(), m_connectionPort));
			final MessageDispatchSender messageDispatcher = new MessageDispatchSender();
			m_agentControllerServerListener.registerMessageHandlers(messageDispatcher);

			m_messagePump = new MessagePump(receiver, messageDispatcher, 1);

			m_reportRunningTask = new TimerTask() {
				public void run() {
					try {
						sendCurrentState();
					} catch (CommunicationException e) {
						cancel();
						m_logger.error(e.getMessage());
					}

				}

			};
		}

		public void sendMessage(AddressAwareMessage message) {
			try {
				m_sender.send(message);
			} catch (CommunicationException e) {
				m_logger.error(e.getMessage());
			}
		}

		public void sendCurrentState() throws CommunicationException {
			sendMessage(new AgentControllerProcessReportMessage(m_state, getJavaDataModel(),
							getSystemDataModel(), m_connectionPort));
		}

		public void start() {
			m_messagePump.start();
			m_timer.schedule(m_reportRunningTask, 0, GrinderConstants.AGENT_CONTROLLER_HEARTBEAT_INTERVAL);
		}

		public void shutdown() {
			m_reportRunningTask.cancel();
			try {
				m_sender.send(new AgentControllerProcessReportMessage(AgentControllerState.FINISHED, null,
								null, 0));
			} catch (CommunicationException e) {
				// Fall through
				// Ignore - peer has probably shut down.
			} finally {
				m_messagePump.shutdown();
			}
		}
	}
}
