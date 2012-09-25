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
package net.grinder.engine.agent;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import net.grinder.GrinderConstants;
import net.grinder.common.GrinderBuild;
import net.grinder.common.GrinderException;
import net.grinder.common.GrinderProperties;
import net.grinder.common.GrinderProperties.PersistenceException;
import net.grinder.common.processidentity.ProcessReport;
import net.grinder.communication.ClientReceiver;
import net.grinder.communication.ClientSender;
import net.grinder.communication.CommunicationException;
import net.grinder.communication.ConnectionType;
import net.grinder.communication.Connector;
import net.grinder.communication.FanOutStreamSender;
import net.grinder.communication.IgnoreShutdownSender;
import net.grinder.communication.MessageDispatchSender;
import net.grinder.communication.MessagePump;
import net.grinder.communication.TeeSender;
import net.grinder.engine.common.ConnectorFactory;
import net.grinder.engine.common.EngineException;
import net.grinder.engine.common.ScriptLocation;
import net.grinder.engine.communication.ConsoleListener;
import net.grinder.messages.agent.StartGrinderMessage;
import net.grinder.messages.console.AgentAddress;
import net.grinder.messages.console.AgentProcessReportMessage;
import net.grinder.util.Directory;
import net.grinder.util.GrinderClassPathUtils;
import net.grinder.util.thread.Condition;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.infra.AgentConfig;
import org.slf4j.Logger;

/**
 * This is the entry point of The Grinder agent process.
 * 
 * @author Paco Gomez
 * @author Philip Aston
 * @author Bertrand Ave
 * @author Pawel Lacinski
 */
public class AgentImplementationEx implements Agent {

	private final Logger m_logger;
	private final boolean m_proceedWithoutConsole;

	private Timer m_timer;
	private final Condition m_eventSynchronisation = new Condition();
	private final AgentIdentityImplementation m_agentIdentity;
	private final ConsoleListener m_consoleListener;
	private FanOutStreamSender m_fanOutStreamSender;
	private final ConnectorFactory m_connectorFactory = new ConnectorFactory(ConnectionType.AGENT);
	/**
	 * We use an most one file store throughout an agent's life, but can't
	 * Initialize it until we've read the properties and connected to the
	 * console.
	 */

	private volatile FileStore m_fileStore;

	private final AgentConfig m_agentConfig;

	/**
	 * Constructor.
	 * 
	 * @param logger
	 *            Logger.
	 * @param agentConfig
	 *            which contains basic agent configuration
	 * @param proceedWithoutConsole
	 *            <code>true</code> => proceed if a console connection could not
	 *            be made.
	 * @throws GrinderException
	 *             If an error occurs.
	 */
	public AgentImplementationEx(Logger logger, AgentConfig agentConfig, boolean proceedWithoutConsole) throws GrinderException {

		m_logger = logger;
		m_agentConfig = agentConfig;
		m_proceedWithoutConsole = proceedWithoutConsole;

		m_consoleListener = new ConsoleListener(m_eventSynchronisation, m_logger);
		m_agentIdentity = new AgentIdentityImplementation(getHostName());

	}

	/**
	 * Constructor with connection to console.
	 * 
	 * @param logger
	 *            logger
	 * @throws GrinderException
	 *             occurs when initialization is failed.
	 */
	public AgentImplementationEx(Logger logger, AgentConfig agentConfig) throws GrinderException {
		this(logger, agentConfig, false);
	}

	/**
	 * Run grinder with empty {@link GrinderProperties}.
	 * 
	 * @throws GrinderException
	 *             occurs when initialization is failed.
	 */
	public void run() throws GrinderException {
		run(new GrinderProperties());
	}

	/**
	 * Run the Grinder agent process.
	 * 
	 * @param grinderProperties
	 *            {@link GrinderProperties} which contains grinder agent base
	 *            configuration.
	 * @throws GrinderException
	 *             If an error occurs.
	 */
	public void run(GrinderProperties grinderProperties) throws GrinderException {
		StartGrinderMessage startMessage = null;
		ConsoleCommunication consoleCommunication = null;
		m_fanOutStreamSender = new FanOutStreamSender(GrinderConstants.AGENT_FANOUT_STREAM_THREAD_COUNT);
		m_timer = new Timer(false);
		try {
			while (true) {
				m_logger.info(GrinderBuild.getName());

				ScriptLocation script = null;
				GrinderProperties properties;

				do {
					properties = createAndMergeProperties(grinderProperties, startMessage != null ? startMessage.getProperties() : null);

					m_agentIdentity.setName(properties.getProperty("grinder.hostID", getHostName()));

					final Connector connector = properties.getBoolean("grinder.useConsole", true) ? m_connectorFactory.create(properties)
							: null;
					// We only reconnect if the connection details have changed.
					if (consoleCommunication != null && !consoleCommunication.getConnector().equals(connector)) {
						shutdownConsoleCommunication(consoleCommunication);
						consoleCommunication = null;
						// Accept any startMessage from previous console - see
						// bug 2092881.
					}

					if (consoleCommunication == null && connector != null) {
						try {
							consoleCommunication = new ConsoleCommunication(connector);
							consoleCommunication.start();
							m_logger.info("connected to console at {}", connector.getEndpointAsString());
						} catch (CommunicationException e) {
							if (m_proceedWithoutConsole) {
								m_logger.warn("{}, proceeding without the console; set "
										+ "grinder.useConsole=false to disable this warning.", e.getMessage());
							} else {
								m_logger.error(e.getMessage());
								return;
							}
						}
					}

					if (consoleCommunication != null && startMessage == null) {
						m_logger.info("waiting for console signal");
						m_consoleListener.waitForMessage();

						if (m_consoleListener.received(ConsoleListener.START)) {
							startMessage = m_consoleListener.getLastStartGrinderMessage();
							continue; // Loop to handle new properties.
						} else {
							break; // Another message, check at end of outer
									// while loop.
						}

					}

					if (startMessage != null) {
						final GrinderProperties messageProperties = startMessage.getProperties();
						final Directory fileStoreDirectory = m_fileStore.getDirectory();

						// Convert relative path to absolute path.
						messageProperties.setAssociatedFile(fileStoreDirectory.getFile(messageProperties.getAssociatedFile()));

						final File consoleScript = messageProperties.resolveRelativeFile(messageProperties.getFile(
								GrinderProperties.SCRIPT, GrinderProperties.DEFAULT_SCRIPT));

						// We only fall back to the agent properties if the
						// start message
						// doesn't specify a script and there is no default
						// script.
						if (messageProperties.containsKey(GrinderProperties.SCRIPT) || consoleScript.canRead()) {
							// The script directory may not be the file's direct
							// parent.
							script = new ScriptLocation(fileStoreDirectory, consoleScript);
						}

						m_agentIdentity.setNumber(startMessage.getAgentNumber());
					} else {
						m_agentIdentity.setNumber(-1);
					}

					if (script == null) {
						final File scriptFile = properties.resolveRelativeFile(properties.getFile(GrinderProperties.SCRIPT,
								GrinderProperties.DEFAULT_SCRIPT));

						script = new ScriptLocation(scriptFile);
					}

					if (!script.getFile().canRead()) {
						m_logger.error("The script file '" + script + "' does not exist or is not readable.");
						script = null;
						break;
					}
				} while (script == null);

				if (script != null) {
					String jvmArguments = properties.getProperty("grinder.jvm.arguments", "");
					if (properties.getBoolean("grinder.security", false)) {
						jvmArguments = addCurrentAgentPath(jvmArguments);
						jvmArguments = addConsoleIP(jvmArguments);
						jvmArguments = addDNSIP(jvmArguments);
					}
					jvmArguments = addCustomDns(properties, jvmArguments);
					rebaseCustomClassPath(properties, script.getDirectory());
					final WorkerFactory workerFactory;
					m_logger.info("grinder properties {}", properties);
					
					// To be safe...
					if (properties.containsKey("grinder.duration") && !properties.containsKey("grinder.runs")) {
						properties.setInt("grinder.runs", 0);
					}

					if (!properties.getBoolean("grinder.debug.singleprocess", false)) {
						// Fix to provide empty system classpath to speed up
						final WorkerProcessCommandLine workerCommandLine = new WorkerProcessCommandLine(properties, filterSystemClassPath(
								System.getProperties(), m_logger), jvmArguments, script.getDirectory());

						m_logger.info("Worker process command line: {}", workerCommandLine);

						workerFactory = new ProcessWorkerFactory(workerCommandLine, m_agentIdentity, m_fanOutStreamSender,
								consoleCommunication != null, script, properties);
					} else {
						m_logger.info("DEBUG MODE: Spawning threads rather than processes");

						if (jvmArguments != null) {
							m_logger.warn("grinder.jvm.arguments ({}) ignored in single process mode", jvmArguments);
						}

						workerFactory = new DebugThreadWorkerFactory(m_agentIdentity, m_fanOutStreamSender, consoleCommunication != null,
								script, properties);
					}
					m_logger.debug("worker launcher is prepared.");
					final WorkerLauncher workerLauncher = new WorkerLauncher(properties.getInt("grinder.processes", 1), workerFactory,
							m_eventSynchronisation, m_logger);
					final int increment = properties.getInt("grinder.processIncrement", 0);
					m_logger.debug("rampup mode by {}.", increment);
					if (increment > 0) {
						final boolean moreProcessesToStart = workerLauncher.startSomeWorkers(properties.getInt("grinder.initialProcesses",
								increment));

						if (moreProcessesToStart) {
							final int incrementInterval = properties.getInt("grinder.processIncrementInterval", 60000);

							final RampUpTimerTask rampUpTimerTask = new RampUpTimerTask(workerLauncher, increment);

							m_timer.scheduleAtFixedRate(rampUpTimerTask, incrementInterval, incrementInterval);
						}
					} else {
						m_logger.debug("start all workers");
						workerLauncher.startAllWorkers();
					}
					
					
					// Wait for a termination event.
					synchronized (m_eventSynchronisation) {
						final long maximumShutdownTime = 5000;
						long consoleSignalTime = -1;
						while (!workerLauncher.allFinished()) {
							m_logger.debug("waiting until all workers are finished");
							if (consoleSignalTime == -1 && m_consoleListener.checkForMessage(ConsoleListener.ANY ^ ConsoleListener.START)) {
								m_logger.info("dont start anymore by message from controller.");
								workerLauncher.dontStartAnyMore();
								consoleSignalTime = System.currentTimeMillis();
							}
							if (consoleSignalTime >= 0 && System.currentTimeMillis() - consoleSignalTime > maximumShutdownTime) {

								m_logger.info("forcibly terminating unresponsive processes");

								// destroyAllWorkers() prevents further workers
								// from starting.
								workerLauncher.destroyAllWorkers();
							}

							m_eventSynchronisation.waitNoInterrruptException(maximumShutdownTime);
						}
						m_logger.info("all workers are finished");
					}
					m_logger.debug("normal shutdown");
					workerLauncher.shutdown();
					break;
				}

				if (consoleCommunication == null) {
					m_logger.debug("console communication death");
					break;
				} else {
					// Ignore any pending start messages.
					m_consoleListener.discardMessages(ConsoleListener.START);

					if (!m_consoleListener.received(ConsoleListener.ANY)) {
						// We've got here naturally, without a console signal.
						m_logger.debug("test is finished, waiting for console signal");
						m_consoleListener.waitForMessage();
					}

					if (m_consoleListener.received(ConsoleListener.START)) {
						startMessage = m_consoleListener.getLastStartGrinderMessage();

					} else if (m_consoleListener.received(ConsoleListener.STOP | ConsoleListener.SHUTDOWN)) {
						m_logger.debug("get shutdown message");
						break;
					} else {
						m_logger.debug("natural death");
						// ConsoleListener.RESET or natural death.
						startMessage = null;
					}
				}
			}
		} catch (Exception e) {
			m_logger.error("Exception occurs in the agent message loop", e);
		} finally {
			if (m_timer != null) {
				m_timer.cancel();
				m_timer = null;
			}
			shutdownConsoleCommunication(consoleCommunication);
			if (m_fanOutStreamSender != null) {
				m_fanOutStreamSender.shutdown();
				m_fanOutStreamSender = null;
			}
			m_consoleListener.shutdown();
			m_logger.info("finished");
		}
	}

	private void rebaseCustomClassPath(GrinderProperties properties, Directory directory) {
		String property = properties.getProperty("grinder.jvm.classpath","");
		StringBuilder newClassPath = new StringBuilder();
		boolean isFirst = true;
		for (String each : property.split(File.pathSeparator)) {
			File file = new File(directory.getFile(), each);
			if (!isFirst) {
				newClassPath.append(File.pathSeparator);
			}
			isFirst = false;
			newClassPath.append(FilenameUtils.normalize(file.getAbsolutePath()));
		}
		properties.setProperty("grinder.jvm.classpath", newClassPath.toString());
	}

	private String addCurrentAgentPath(String jvmArguments) {
		StringBuilder sb = new StringBuilder();
		sb.append(jvmArguments).append(" -Dngrinder.exec.path=").append(new File(".").getAbsolutePath()).append(' ');
		return sb.toString();
	}

	private String addConsoleIP(String jvmArguments) {
		StringBuilder sb = new StringBuilder();
		sb.append(jvmArguments).append(" -Dngrinder.console.ip=").append(m_agentConfig.getProperty("agent.console.ip", "127.0.0.1"));
		return sb.toString();
	}

	private String addDNSIP(String jvmArguments) {
		@SuppressWarnings("restriction")
		List<?> nameservers = sun.net.dns.ResolverConfiguration.open().nameservers();
		StringBuilder sb = new StringBuilder();
		for (Object dns : nameservers) {
			sb.append(dns).append(',');
		}
		String tmpString = sb.toString();
		if (sb.length() > 0) {
			tmpString = sb.substring(0, sb.length() - 1);
		}
		return jvmArguments + " -Dngrinder.dns.ip=" + tmpString;
	}

	private String addCustomDns(GrinderProperties properties, String jvmArguments) {
		String etcHost = properties.getProperty("ngrinder.etc.hosts", "");
		if (StringUtils.isNotEmpty(etcHost)) {
			jvmArguments = jvmArguments + " -Dngrinder.etc.hosts=" + etcHost + "," + getHostName() + ":127.0.0.1,localhost:127.0.0.1"
					+ " -Dsun.net.spi.nameservice.provider.1=dns,LocalManagedDn ";
		}
		return jvmArguments;
	}

	/**
	 * Filter classpath to prevent too many instrumentation.
	 * 
	 * @param properties
	 *            system properties
	 * @return filtered properties
	 */
	public static Properties filterSystemClassPath(Properties properties, Logger m_logger) {
		String property = properties.getProperty("java.class.path", "");
		m_logger.debug("Total System Class Path in total is " + property);

		String newClassPath = GrinderClassPathUtils.filterClassPath(property, m_logger);

		properties.setProperty("java.class.path", newClassPath);
		m_logger.debug("Filtered System Class Path is " + newClassPath);
		return properties;
	}

	public static final String GRINDER_PROP_TEST_ID = "grinder.test.id";

	private GrinderProperties createAndMergeProperties(GrinderProperties properties, GrinderProperties startMessageProperties)
			throws PersistenceException {

		if (startMessageProperties != null) {
			properties.putAll(startMessageProperties);
		}
		properties.setFile(GrinderProperties.LOG_DIRECTORY,
				new File(m_agentConfig.getHome().getLogDirectory(), properties.getProperty(GRINDER_PROP_TEST_ID, "default")));
		return properties;
	}

	private void shutdownConsoleCommunication(ConsoleCommunication consoleCommunication) {
		if (consoleCommunication != null) {
			consoleCommunication.shutdown();
		}
		m_consoleListener.discardMessages(ConsoleListener.ANY);
	}

	/**
	 * Clean up resources.
	 */
	public void shutdown() {
		if (m_timer != null) {
			m_timer.cancel();
			m_timer = null;
		}
		if (m_fanOutStreamSender != null) {
			m_fanOutStreamSender.shutdown();
		}
		m_consoleListener.shutdown();
		m_logger.info("agent is forcely terminated");
	}

	private static String getHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return "UNNAMED HOST";
		}
	}

	private static class RampUpTimerTask extends TimerTask {

		private final WorkerLauncher m_processLauncher;
		private final int m_processIncrement;

		public RampUpTimerTask(WorkerLauncher processLauncher, int processIncrement) {
			m_processLauncher = processLauncher;
			m_processIncrement = processIncrement;
		}

		public void run() {
			try {
				final boolean moreProcessesToStart = m_processLauncher.startSomeWorkers(m_processIncrement);

				if (!moreProcessesToStart) {
					super.cancel();
				}
			} catch (EngineException e) {
				// Really an assertion. Can't use logger because its not
				// thread-safe.
				System.err.println("Failed to start processes");
				e.printStackTrace();
			}
		}
	}

	private final class ConsoleCommunication {
		private final ClientSender m_sender;
		private final Connector m_connector;
		private final TimerTask m_reportRunningTask;
		private final MessagePump m_messagePump;

		public ConsoleCommunication(Connector connector) throws CommunicationException, FileStore.FileStoreException {

			final ClientReceiver receiver = ClientReceiver.connect(connector, new AgentAddress(m_agentIdentity));
			m_sender = ClientSender.connect(receiver);
			m_connector = connector;

			if (m_fileStore == null) {
				// Only create the file store if we connected.
				m_fileStore = new FileStore(new File(m_agentConfig.getHome().getDirectory(), "file-store"), m_logger);
			}

			m_sender.send(new AgentProcessReportMessage(ProcessReport.STATE_STARTED, m_fileStore.getCacheHighWaterMark()));

			final MessageDispatchSender fileStoreMessageDispatcher = new MessageDispatchSender();
			m_fileStore.registerMessageHandlers(fileStoreMessageDispatcher);

			final MessageDispatchSender messageDispatcher = new MessageDispatchSender();
			m_consoleListener.registerMessageHandlers(messageDispatcher);

			// Everything that the file store doesn't handle is tee'd to the
			// worker processes and our message handlers.
			fileStoreMessageDispatcher.addFallback(new TeeSender(messageDispatcher, new IgnoreShutdownSender(m_fanOutStreamSender)));

			m_messagePump = new MessagePump(receiver, fileStoreMessageDispatcher, 1);

			m_reportRunningTask = new TimerTask() {
				public void run() {
					try {
						m_sender.send(new AgentProcessReportMessage(ProcessReport.STATE_RUNNING, m_fileStore.getCacheHighWaterMark()));
					} catch (CommunicationException e) {
						cancel();
						m_logger.error("Error while pumping up the AgentPrcessReportMessage", e.getMessage());
						m_logger.debug("Stack trace is : ", e);
					}

				}
			};
		}

		public void start() {
			m_messagePump.start();
			m_timer.schedule(m_reportRunningTask, GrinderConstants.AGENT_HEARTBEAT_DELAY, GrinderConstants.AGENT_HEARTBEAT_INTERVAL);
		}

		public Connector getConnector() {
			return m_connector;
		}

		public void shutdown() {
			m_reportRunningTask.cancel();

			try {
				m_sender.send(new AgentProcessReportMessage(ProcessReport.STATE_FINISHED, m_fileStore.getCacheHighWaterMark()));
				m_logger.debug("shut down message is sent");
			} catch (CommunicationException e) {
				// Ignore - peer has probably shut down.
			} finally {
				m_messagePump.shutdown();
			}
		}
	}
}
