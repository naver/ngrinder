package net.grinder;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import net.grinder.common.GrinderBuild;
import net.grinder.common.GrinderException;
import net.grinder.common.GrinderProperties;
import net.grinder.common.processidentity.ProcessReport;
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
import net.grinder.messages.agent.StartGrinderMessage;
import net.grinder.messages.console.AgentAddress;
import net.grinder.messages.console.AgentProcessReportMessage;
import net.grinder.util.thread.Condition;

import org.slf4j.Logger;

/**
 * This is Agent Controller which handles agent start and stop
 * 
 * @author JunHo Yoon
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

	/**
	 * Constructor.
	 * 
	 * @param logger
	 *            Logger.
	 * @param alternateFile
	 *            Alternative properties file, or <code>null</code>.
	 * @param proceedWithoutConsole
	 *            <code>true</code> => proceed if a console connection could not
	 *            be made.
	 * @throws GrinderException
	 *             If an error occurs.
	 */
	public AgentController(Logger logger) throws GrinderException {
		m_logger = logger;
		m_agentControllerServerListener = new AgentControllerServerListener(
				m_eventSynchronisation, m_logger);
		m_agentIdentity = new AgentControllerIdentityImplementation(
				getHostName());
	}

	/**
	 * Run agent controller. This method use default server port (for test)
	 */
	public void run() throws GrinderException {
		GrinderProperties grinderProperties = new GrinderProperties();
		grinderProperties
				.setInt(GrinderProperties.CONSOLE_PORT,
						AgentControllerCommunicationDefauts.DEFAULT_AGENT_CONTROLLER_SERVER_PORT);
		run(grinderProperties);
	}

	/**
	 * Run the agent controller.
	 * 
	 * @throws GrinderException
	 *             If an error occurs.
	 */
	public void run(GrinderProperties grinderProperties)
			throws GrinderException {

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

					m_agentIdentity.setName(properties.getProperty(
							"grinder.hostID", getHostName()));

					final Connector connector = m_connectorFactory
							.create(properties);

					if (consoleCommunication == null) {
						try {
							consoleCommunication = new ConsoleCommunication(
									connector);
							consoleCommunication.start();
							m_logger.info(
									"connected to waiting for agent controller server at {}",
									connector.getEndpointAsString());
						} catch (CommunicationException e) {
							m_logger.error(e.getMessage());
							return;
						}
					}

					if (consoleCommunication != null && startMessage == null) {
						m_logger.info("waiting for agent controller server signal");
						m_agentControllerServerListener.waitForMessage();

						if (m_agentControllerServerListener
								.received(AgentControllerServerListener.START)) {
							startMessage = m_agentControllerServerListener
									.getLastStartGrinderMessage();
							continue;
						} else {
							break; // Another message, check at end of outer
									// while loop.
						}

					}

					if (startMessage != null) {
						m_agentIdentity
								.setNumber(startMessage.getAgentNumber());
					}
				} while (startMessage.getProperties() == null);

				// Here the agent run code goes..
				if (startMessage != null) {
					m_logger.info("starting agent");
					agent.run(startMessage.getProperties());
				}
				// Ignore any pending start messages.
				m_agentControllerServerListener
						.discardMessages(AgentControllerServerListener.START);

				if (!m_agentControllerServerListener
						.received(AgentControllerServerListener.ANY)) {
					// We've got here naturally, without a console signal.
					m_logger.info("agent started. waiting for agent controller signal");
					m_agentControllerServerListener.waitForMessage();
				}

				if (m_agentControllerServerListener
						.received(AgentControllerServerListener.START)) {
					startMessage = m_agentControllerServerListener
							.getLastStartGrinderMessage();
				} else if (m_agentControllerServerListener
						.received(AgentControllerServerListener.STOP)) {
					agent.shutdown();
					startMessage = null;
				} else if (m_agentControllerServerListener
						.received(AgentControllerServerListener.SHUTDOWN)) {
					startMessage = null;
					break;
				} else if (m_agentControllerServerListener
						.received(AgentControllerServerListener.UPDATE_AGENT)) {
					// Do update agent itself.
					startMessage = null;
					updateAgent(m_agentControllerServerListener
							.getLastUpdateAgentGrinderMessage());

				} else if (m_agentControllerServerListener
						.received(AgentControllerServerListener.LOG_REPORT)) {
					startMessage = null;
					sendLog(m_agentControllerServerListener
							.getLastLogReportGrinderMessage());
					// Do update
				} else {
					// ConsoleListener.RESET or natural death.
					startMessage = null;
				}
			}

		} finally {
			agent.shutdown();
			m_timer.cancel();
			shutdownConsoleCommunication(consoleCommunication);
		}
	}

	private void updateAgent(
			UpdateAgentGrinderMessage lastUpdateAgentGrinderMessage) {
	}

	private void sendLog(LogReportGrinderMessage logReportGrinderMessage) {

	}

	// /////////////////////////////////////////////////////

	private void shutdownConsoleCommunication(
			ConsoleCommunication consoleCommunication) {

		if (consoleCommunication != null) {
			consoleCommunication.shutdown();
		}

		m_agentControllerServerListener
				.discardMessages(AgentControllerServerListener.ANY);
	}

	/**
	 * Clean up resources.
	 */
	public void shutdown() {
		if (m_timer != null)
			m_timer.cancel();
		m_fanOutStreamSender.shutdown();
		m_agentControllerServerListener.shutdown();
		m_logger.info("finished");
	}

	private static String getHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return "UNNAMED HOST";
		}
	}

	private final class ConsoleCommunication {
		private final ClientSender m_sender;
		private final TimerTask m_reportRunningTask;
		private final MessagePump m_messagePump;

		public ConsoleCommunication(Connector connector)
				throws CommunicationException {

			final ClientReceiver receiver = ClientReceiver.connect(connector,
					new AgentAddress(m_agentIdentity));
			m_sender = ClientSender.connect(receiver);

			m_sender.send(new AgentProcessReportMessage(
					ProcessReport.STATE_STARTED, null));

			final MessageDispatchSender messageDispatcher = new MessageDispatchSender();
			m_agentControllerServerListener
					.registerMessageHandlers(messageDispatcher);

			m_messagePump = new MessagePump(receiver, messageDispatcher, 1);

			m_reportRunningTask = new TimerTask() {
				public void run() {
					try {
						m_sender.send(new AgentProcessReportMessage(
								ProcessReport.STATE_RUNNING, null));
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
				m_sender.send(new AgentProcessReportMessage(
						ProcessReport.STATE_FINISHED, null));
			} catch (CommunicationException e) {
				// Ignore - peer has probably shut down.
			} finally {
				m_messagePump.shutdown();
			}
		}
	}
}
