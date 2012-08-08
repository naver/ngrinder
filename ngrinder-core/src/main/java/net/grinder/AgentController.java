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

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import net.grinder.common.GrinderBuild;
import net.grinder.common.GrinderException;
import net.grinder.common.GrinderProperties;
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
import net.grinder.util.ReflectionUtil;
import net.grinder.util.thread.Condition;

import org.ngrinder.monitor.controller.model.JavaDataModel;
import org.ngrinder.monitor.controller.model.SystemDataModel;
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
	private FanOutStreamSender m_fanOutStreamSender = new FanOutStreamSender(3);
	private final AgentControllerConnectorFactory m_connectorFactory = new AgentControllerConnectorFactory(
			ConnectionType.AGENT);
	private boolean m_agentStart = false;

	private final Condition m_eventSyncCondition;

	/**
	 * Constructor.
	 * 
	 * @param logger
	 *            Logger.
	 * @param m_eventSyncCondition
	 * 
	 * @throws GrinderException
	 *             If an error occurs.
	 */
	public AgentController(Logger logger, Condition eventSyncCondition) throws GrinderException {
		m_logger = logger;
		m_eventSyncCondition = eventSyncCondition;
		m_agentControllerServerListener = new AgentControllerServerListener(m_eventSynchronisation, m_logger);
		m_agentIdentity = new AgentControllerIdentityImplementation(getHostName(), getHostAddress());
	}

	/**
	 * Get host address
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
	 */
	public void run() throws GrinderException {
		GrinderProperties grinderProperties = new GrinderProperties();
		grinderProperties.setInt(GrinderProperties.CONSOLE_PORT,
				AgentControllerCommunicationDefauts.DEFAULT_AGENT_CONTROLLER_SERVER_PORT);
		synchronized (m_eventSyncCondition) {
			m_eventSyncCondition.notifyAll();
		}
		run(grinderProperties);
	}

	/**
	 * Run the agent controller.
	 * 
	 * @throws GrinderException
	 *             If an error occurs.
	 */
	public void run(GrinderProperties grinderProperties) throws GrinderException {

		StartGrinderMessage startMessage = null;
		ConsoleCommunication consoleCommunication = null;
		m_fanOutStreamSender = new FanOutStreamSender(6);
		m_timer = new Timer(false);
		AgentDaemon agent = new AgentDaemon();
		try {
			while (true) {
				m_logger.info(GrinderBuild.getName());

				GrinderProperties properties;
				do {
					properties = grinderProperties;
					m_agentIdentity.setName(properties.getProperty("grinder.hostID", getHostName()));
					m_agentIdentity.setRegion(grinderProperties.getProperty("grinder.region"));
					final Connector connector = m_connectorFactory.create(properties);

					if (consoleCommunication == null) {
						try {
							consoleCommunication = new ConsoleCommunication(connector);
							consoleCommunication.start();
							m_logger.info("connected to waiting for agent controller server at {}",
									connector.getEndpointAsString());
						} catch (CommunicationException e) {
							m_logger.error(e.getMessage());
							return;
						}
					}
					try {
						m_agentIdentity.setPort(getSocket(
								ReflectionUtil.getFieldValue(consoleCommunication, "m_sender")).getPort());
					} catch (Exception e) {
						e.printStackTrace();
					}

					if (consoleCommunication != null && startMessage == null) {
						m_logger.info("waiting for agent controller server signal");
						m_agentControllerServerListener.waitForMessage();

						if (m_agentControllerServerListener.received(AgentControllerServerListener.START)) {
							startMessage = m_agentControllerServerListener.getLastStartGrinderMessage();
							continue;
						} else {
							break; // Another message, check at end of outer
									// while loop.
						}

					}

					if (startMessage != null) {
						m_agentIdentity.setNumber(startMessage.getAgentNumber());
					}
				} while (startMessage.getProperties() == null);

				// Here the agent run code goes..
				if (startMessage != null) {
					m_logger.info("starting agent...");
					agent.run(startMessage.getProperties());
					m_agentStart = true;
				}
				// Ignore any pending start messages.
				m_agentControllerServerListener.discardMessages(AgentControllerServerListener.START);

				if (!m_agentControllerServerListener.received(AgentControllerServerListener.ANY)) {
					// We've got here naturally, without a console signal.
					m_logger.info("agent started. waiting for agent controller signal");
					System.out.println("Waiting for message!!!");
					m_agentControllerServerListener.waitForMessage();

				}

				if (m_agentControllerServerListener.received(AgentControllerServerListener.START)) {
					System.out.println("Start message!!!");
					startMessage = m_agentControllerServerListener.getLastStartGrinderMessage();
				} else if (m_agentControllerServerListener.received(AgentControllerServerListener.STOP)) {
					agent.shutdown();
					startMessage = null;
					m_agentStart = false;
					m_agentControllerServerListener.discardMessages(AgentControllerServerListener.STOP);

				} else if (m_agentControllerServerListener.received(AgentControllerServerListener.SHUTDOWN)) {
					startMessage = null;
					break;
				} else if (m_agentControllerServerListener.received(AgentControllerServerListener.UPDATE_AGENT)) {
					// Do update agent itself.
					startMessage = null;
					updateAgent(m_agentControllerServerListener.getLastUpdateAgentGrinderMessage());

				} else if (m_agentControllerServerListener.received(AgentControllerServerListener.LOG_REPORT)) {
					startMessage = null;
					sendLog(m_agentControllerServerListener.getLastLogReportGrinderMessage());
					// Do update
				} else {
					// ConsoleListener.RESET or natural death.
					startMessage = null;
					m_agentStart = false;
				}
			}

		} finally {
			agent.shutdown();
			m_agentStart = false;
			m_timer.cancel();
			shutdownConsoleCommunication(consoleCommunication);
		}
	}

	private Socket getSocket(Object sender) {
		Object socketWrapper = ReflectionUtil.getFieldValue(sender, "m_socketWrapper");
		Socket socket = (Socket) ReflectionUtil.getFieldValue(socketWrapper, "m_socket");
		return socket;
	}

	private void updateAgent(UpdateAgentGrinderMessage lastUpdateAgentGrinderMessage) {
	}

	private void sendLog(LogReportGrinderMessage logReportGrinderMessage) {

	}

	// /////////////////////////////////////////////////////

	private void shutdownConsoleCommunication(ConsoleCommunication consoleCommunication) {

		if (consoleCommunication != null) {
			consoleCommunication.shutdown();
		}

		m_agentControllerServerListener.discardMessages(AgentControllerServerListener.ANY);
	}

	/**
	 * Clean up resources.
	 */
	public void shutdown() {
		if (m_timer != null) {
			m_timer.cancel();
		}
		m_fanOutStreamSender.shutdown();
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

	public SystemDataModel getSystemDataModel() {
		SystemDataModel systemDataModel = new SystemDataModel();
		systemDataModel.setCollectTime(10000);
		systemDataModel.setCpuUsedPercentage(20f);
		// FIXME
		return systemDataModel;
	}

	public JavaDataModel getJavaDataModel() {
		JavaDataModel javaDataModel = new JavaDataModel();
		javaDataModel.setCollectTime(10000);
		javaDataModel.setCpuUsedPercentage(20f);
		// FIXME
		return javaDataModel;
	}

	private final class ConsoleCommunication {
		private final ClientSender m_sender;
		private final TimerTask m_reportRunningTask;
		private final MessagePump m_messagePump;

		public ConsoleCommunication(Connector connector) throws CommunicationException {
			final ClientReceiver receiver = ClientReceiver.connect(connector, new AgentAddress(m_agentIdentity));
			m_sender = ClientSender.connect(receiver);

			m_agentIdentity.setPort(getSocket(m_sender).getPort());
			m_sender.send(new AgentControllerProcessReportMessage(AgentControllerState.START, getJavaDataModel(),
					getSystemDataModel()));

			final MessageDispatchSender messageDispatcher = new MessageDispatchSender();
			m_agentControllerServerListener.registerMessageHandlers(messageDispatcher);

			m_messagePump = new MessagePump(receiver, messageDispatcher, 1);

			m_reportRunningTask = new TimerTask() {
				public void run() {
					try {
						m_sender.send(new AgentControllerProcessReportMessage(
								(m_agentStart ? AgentControllerState.AGENT_RUN : AgentControllerState.RUNNING),
								getJavaDataModel(), getSystemDataModel()));
					} catch (CommunicationException e) {
						cancel();
						m_logger.error(e.getMessage());
					}

				}

			};
		}

		public void start() {
			m_messagePump.start();
			m_timer.schedule(m_reportRunningTask, 0, 2000);
		}

		public void shutdown() {
			m_reportRunningTask.cancel();

			try {
				m_sender.send(new AgentControllerProcessReportMessage(AgentControllerState.FINISHED, null, null));
			} catch (CommunicationException e) {
				// Ignore - peer has probably shut down.
			} finally {
				m_messagePump.shutdown();
			}
		}
	}
}
