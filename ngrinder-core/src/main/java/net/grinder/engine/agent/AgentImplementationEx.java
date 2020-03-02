// Copyright (C) 2000 - 2012 Philip Aston
// All rights reserved.
//
// This file is part of The Grinder software distribution. Refer to
// the file LICENSE which is part of The Grinder distribution for
// licensing details. The Grinder distribution is available on the
// Internet at http://grinder.sourceforge.net/
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
// FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
// COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
// OF THE POSSIBILITY OF SUCH DAMAGE.
package net.grinder.engine.agent;

import net.grinder.GrinderConstants;
import net.grinder.common.GrinderBuild;
import net.grinder.common.GrinderException;
import net.grinder.common.GrinderProperties;
import net.grinder.common.GrinderProperties.PersistenceException;
import net.grinder.common.processidentity.ProcessReport;
import net.grinder.communication.*;
import net.grinder.engine.common.ConnectorFactory;
import net.grinder.engine.common.EngineException;
import net.grinder.engine.common.ScriptLocation;
import net.grinder.engine.communication.ConsoleListener;
import net.grinder.lang.AbstractLanguageHandler;
import net.grinder.lang.Lang;
import net.grinder.messages.agent.StartGrinderMessage;
import net.grinder.messages.console.AgentAddress;
import net.grinder.messages.console.AgentProcessReportMessage;
import net.grinder.util.AbstractGrinderClassPathProcessor;
import net.grinder.util.Directory;
import net.grinder.util.NetworkUtils;
import net.grinder.util.thread.Condition;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.constants.AgentConstants;
import org.ngrinder.common.util.NoOp;
import org.ngrinder.infra.AgentConfig;
import org.slf4j.Logger;

import java.io.File;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import static org.ngrinder.common.constants.GrinderConstants.*;

/**
 * This is the entry point of The Grinder agent process.
 *
 * @author Grinder Developers.
 * @author JunHo Yoon (modified for nGrinder)
 * @since 3.0
 */
public class AgentImplementationEx implements Agent, AgentConstants {

	private final Logger m_logger;
	private final boolean m_proceedWithoutConsole;

	private Timer m_timer;
	private final Condition m_eventSynchronisation = new Condition();
	private final AgentIdentityImplementation m_agentIdentity;
	private final ConsoleListener m_consoleListener;
	private FanOutStreamSender m_fanOutStreamSender;
	private final ConnectorFactory m_connectorFactory = new ConnectorFactory(ConnectionType.AGENT);
	private WorkerLauncher m_workerLauncherForShutdown = null;
	/**
	 * We use an most one file store throughout an agent's life, but can't Initialize it until we've
	 * read the properties and connected to the console.
	 */
	private volatile FileStore m_fileStore;

	private final AgentConfig m_agentConfig;

	/**
	 * Constructor.
	 *
	 * @param logger                Logger.
	 * @param agentConfig           which contains basic agent configuration
	 * @param proceedWithoutConsole <code>true</code> => proceed if a console connection could not be made.
	 */
	public AgentImplementationEx(Logger logger, AgentConfig agentConfig, boolean proceedWithoutConsole) {

		m_logger = logger;
		m_agentConfig = agentConfig;
		m_proceedWithoutConsole = proceedWithoutConsole;

		m_consoleListener = new ConsoleListener(m_eventSynchronisation, m_logger);
		m_agentIdentity = new AgentIdentityImplementation(NetworkUtils.getLocalHostName());

	}

	/**
	 * Constructor with connection to console.
	 *
	 * @param logger      logger
	 * @param agentConfig agent configuration
	 */
	public AgentImplementationEx(Logger logger, AgentConfig agentConfig) {
		this(logger, agentConfig, false);
	}

	/**
	 * Run grinder with empty {@link GrinderProperties}.
	 *
	 * @throws GrinderException occurs when initialization is failed.
	 */
	public void run() throws GrinderException {
		run(new GrinderProperties());
	}

	/**
	 * Run the Grinder agent process.
	 *
	 * @param grinderProperties {@link GrinderProperties} which contains grinder agent base configuration.
	 * @throws GrinderException If an error occurs.
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
					properties = createAndMergeProperties(grinderProperties,
							startMessage != null ? startMessage.getProperties() : null);
					properties.setProperty(GrinderProperties.CONSOLE_HOST, m_agentConfig.getControllerIP());
					m_agentIdentity.setName(m_agentConfig.getAgentHostID());
					final Connector connector = m_connectorFactory.create(properties);
					// We only reconnect if the connection details have changed.
					if (consoleCommunication != null && !consoleCommunication.getConnector().equals(connector)) {
						shutdownConsoleCommunication(consoleCommunication);
						consoleCommunication = null;
						// Accept any startMessage from previous console - see
						// bug 2092881.
					}

					if (consoleCommunication == null && connector != null) {
						try {
							consoleCommunication = new ConsoleCommunication(connector, grinderProperties.getProperty(
									"grinder.user", "_default"));
							consoleCommunication.start();
							m_logger.info("Connect to console at {}", connector.getEndpointAsString());
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
						m_logger.info("Waiting for console signal");
						m_consoleListener.waitForMessage();

						if (m_consoleListener.received(ConsoleListener.START)) {
							startMessage = m_consoleListener.getLastStartGrinderMessage();
							continue; // Loop to handle new properties.
						} else {
							break; // Another message, check at end of outer while loop.
						}
					}

					if (startMessage != null) {

						final GrinderProperties messageProperties = startMessage.getProperties();
						final Directory fileStoreDirectory = m_fileStore.getDirectory();

						// Convert relative path to absolute path.
						messageProperties.setAssociatedFile(fileStoreDirectory.getFile(messageProperties
								.getAssociatedFile()));

						final File consoleScript = messageProperties.resolveRelativeFile(messageProperties.getFile(
								GrinderProperties.SCRIPT, GrinderProperties.DEFAULT_SCRIPT));

						// We only fall back to the agent properties if the start message
						// doesn't specify a script and there is no default script.
						if (messageProperties.containsKey(GrinderProperties.SCRIPT) || consoleScript.canRead()) {
							// The script directory may not be the file's direct parent.
							script = new ScriptLocation(fileStoreDirectory, consoleScript);
						}
						m_agentIdentity.setNumber(startMessage.getAgentNumber());
					} else {
						m_agentIdentity.setNumber(-1);
					}

					if (script == null) {
						final File scriptFile = properties.resolveRelativeFile(properties.getFile(
								GrinderProperties.SCRIPT, GrinderProperties.DEFAULT_SCRIPT));
						script = new ScriptLocation(scriptFile);
					}
					m_logger.debug("The script location is {}", script.getFile().getAbsolutePath());
					if (!script.getFile().canRead()) {
						m_logger.error("The script file '{}' does not exist or is not readable.", script);
						script = null;
						break;
					}
				} while (script == null);

				if (script != null) {
					// Set up log directory.
					if (!properties.containsKey(GrinderProperties.LOG_DIRECTORY)) {
						properties.setFile(GrinderProperties.LOG_DIRECTORY, new File(m_agentConfig.getHome()
								.getLogDirectory(), properties.getProperty(GRINDER_PROP_TEST_ID, "default")));
					}
					File logFile = new File(properties.getFile(GrinderProperties.LOG_DIRECTORY, new File(".")),
							m_agentIdentity.getName() + "-" + m_agentIdentity.getNumber() + ".log");
					m_logger.info("log file : {}", logFile);
					AbstractLanguageHandler handler = Lang.getByFileName(script.getFile()).getHandler();
					final WorkerFactory workerFactory;
					Properties rebasedSystemProperty = rebaseSystemClassPath(System.getProperties(), m_agentConfig.getCurrentDirectory());

					String jvmArguments = buildTestRunProperties(script, handler, rebasedSystemProperty, properties);

					if (!properties.getBoolean("grinder.debug.singleprocess", false)) {
						// Fix to provide empty system classpath to speed up
						final WorkerProcessCommandLine workerCommandLine = new WorkerProcessCommandLine(properties,
								filterSystemClassPath(rebasedSystemProperty, handler, m_logger), jvmArguments,
								script.getDirectory());

						m_logger.info("Worker process command line: {}", workerCommandLine);
						FileUtils.writeStringToFile(logFile, workerCommandLine.toString() + "\n\n");
						workerFactory = new ProcessWorkerFactory(workerCommandLine, m_agentIdentity,
								m_fanOutStreamSender, consoleCommunication != null, script, properties);
					} else {
						m_logger.info("DEBUG MODE. Spawning threads rather than processes");
						m_logger.warn("grinder.jvm.arguments ({}) ignored in single process mode", jvmArguments);

						workerFactory = new DebugThreadWorkerFactory(m_agentIdentity, m_fanOutStreamSender,
								consoleCommunication != null, script, properties);
					}
					m_logger.debug("Worker launcher is prepared.");
					final WorkerLauncher workerLauncher = new WorkerLauncher(properties.getInt("grinder.processes", 1),
							workerFactory, m_eventSynchronisation, m_logger);
					m_workerLauncherForShutdown = workerLauncher;
					final boolean threadRampUp = properties.getBoolean("grinder.threadRampUp", false);
					final int increment = properties.getInt("grinder.processIncrement", 0);
					if (!threadRampUp) {
						m_logger.debug("'Ramp Up' mode by {}.", increment);
					}
					if (!threadRampUp && increment > 0) {
						final boolean moreProcessesToStart = workerLauncher.startSomeWorkers(properties.getInt(
								"grinder.initialProcesses", increment));

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
							m_logger.debug("Waiting until all workers are finished");
							if (consoleSignalTime == -1
									&& m_consoleListener.checkForMessage(ConsoleListener.ANY
									^ ConsoleListener.START)) {
								m_logger.info("Don't start anymore by message from controller.");
								workerLauncher.dontStartAnyMore();
								consoleSignalTime = System.currentTimeMillis();
							}
							if (consoleSignalTime >= 0
									&& System.currentTimeMillis() - consoleSignalTime > maximumShutdownTime) {

								m_logger.info("Terminating unresponsive processes by force");

								// destroyAllWorkers() prevents further workers
								// from starting.
								workerLauncher.destroyAllWorkers();
							}
							m_eventSynchronisation.waitNoInterrruptException(maximumShutdownTime);
						}
						m_logger.info("All workers are finished");
					}
					m_logger.debug("Normal shutdown");
					workerLauncher.shutdown();
					break;
				}

				if (consoleCommunication == null) {
					m_logger.debug("Console communication death");
					break;
				} else {
					// Ignore any pending start messages.
					m_consoleListener.discardMessages(ConsoleListener.START);

					if (!m_consoleListener.received(ConsoleListener.ANY)) {
						// We've got here naturally, without a console signal.
						m_logger.debug("Test is finished, wait for console signal");
						m_consoleListener.waitForMessage();
					}

					if (m_consoleListener.received(ConsoleListener.START)) {
						startMessage = m_consoleListener.getLastStartGrinderMessage();

					} else if (m_consoleListener.received(ConsoleListener.STOP | ConsoleListener.SHUTDOWN)) {
						m_logger.debug("Got shutdown message");
						break;
					} else {
						m_logger.debug("Natural death");
						// ConsoleListener.RESET or natural death.
						startMessage = null;
					}
				}
			}
		} catch (Exception e) {
			m_logger.error("Exception occurred in the agent message loop", e);
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
			m_logger.info("Test shuts down.");
		}
	}

	private Properties rebaseSystemClassPath(Properties properties, File curDir) {
		Properties newProperties = new Properties();
		newProperties.putAll(properties);
		StringBuilder newClassPath = new StringBuilder();
		boolean isFirst = true;
		for (String each : StringUtils.split(properties.getProperty("java.class.path"), File.pathSeparator)) {
			File file = new File(each);
			if (!file.isAbsolute()) {
				file = new File(curDir, each);
			}
			if (!isFirst) {
				newClassPath.append(File.pathSeparator);
			}
			isFirst = false;
			newClassPath.append(FilenameUtils.normalize(file.getAbsolutePath()));
		}
		newProperties.put("java.class.path", newClassPath.toString());
		return newProperties;
	}

	private boolean isSecurityEnabled(GrinderProperties properties) {
		return m_agentConfig.isSecurityEnabled() && properties.getBoolean(GRINDER_PROP_SECURITY, false);
	}

	private String buildTestRunProperties(ScriptLocation script, AbstractLanguageHandler handler, Properties systemProperty,
	                                      GrinderProperties properties) {
		PropertyBuilder builder = new PropertyBuilder(properties, script.getDirectory(), isSecurityEnabled(properties), properties.getProperty(GRINDER_PROP_SECURITY_LEVEL, GRINDER_SECURITY_LEVEL_NORMAL), properties.getProperty(GRINDER_PROP_ETC_HOSTS),
				NetworkUtils.getLocalHostName(), m_agentConfig.getAgentProperties().getPropertyBoolean(PROP_AGENT_SERVER_MODE),
				m_agentConfig.getAgentProperties().getPropertyBoolean(PROP_AGENT_LIMIT_XMX),
				m_agentConfig.getAgentProperties().getPropertyBoolean(PROP_AGENT_ENABLE_LOCAL_DNS),
				m_agentConfig.getAgentProperties().getProperty(PROP_AGENT_JAVA_OPT));
		String jvmArguments = builder.buildJVMArgument();
		String rebaseCustomClassPath = getForeMostClassPath(systemProperty, handler, m_logger)
				+ File.pathSeparator
				+ builder.rebaseCustomClassPath(properties.getProperty("grinder.jvm.classpath", ""));
		properties.setProperty("grinder.jvm.classpath", rebaseCustomClassPath);

		m_logger.info("grinder properties {}", properties);
		m_logger.info("jvm arguments {}", jvmArguments);

		// To be safe...
		if (properties.containsKey("grinder.duration") && !properties.containsKey("grinder.runs")) {
			properties.setInt("grinder.runs", 0);
		}
		return jvmArguments;
	}

	/**
	 * Get classpath which should be located in the head of classpath.
	 *
	 * @param properties system properties
	 * @param handler    language specific handler
	 * @param logger     logger
	 * @return foremost classpath
	 */
	private String getForeMostClassPath(Properties properties, AbstractLanguageHandler handler, Logger logger) {
		String systemClassPath = properties.getProperty("java.class.path");
		AbstractGrinderClassPathProcessor classPathProcessor = handler.getClassPathProcessor();
		return classPathProcessor.filterForeMostClassPath(systemClassPath, logger) + File.pathSeparator
				+ classPathProcessor.filterPatchClassPath(systemClassPath, logger);
	}

	/**
	 * Filter classpath to prevent too many instrumentation.
	 *
	 * @param properties system properties
	 * @param handler    Language specific handler
	 * @param logger     logger
	 * @return new filtered properties
	 */
	private Properties filterSystemClassPath(Properties properties, AbstractLanguageHandler handler, Logger logger) {
		String property = properties.getProperty("java.class.path", "");
		logger.debug("Total system class path in total is " + property);

		String newClassPath = handler.getClassPathProcessor().filterClassPath(property, logger);
		Properties returnProperties = new Properties(properties);
		returnProperties.setProperty("java.class.path", newClassPath);
		logger.debug("Filtered system class path is {}", newClassPath);
		return returnProperties;
	}

	public static final String GRINDER_PROP_TEST_ID = "grinder.test.id";

	private GrinderProperties createAndMergeProperties(GrinderProperties properties,
	                                                   GrinderProperties startMessageProperties) throws PersistenceException {

		if (startMessageProperties != null) {
			properties.putAll(startMessageProperties);
		}
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

		if (m_workerLauncherForShutdown != null && !m_workerLauncherForShutdown.allFinished()) {
			m_workerLauncherForShutdown.destroyAllWorkers();
		}
		m_logger.info("Agent is terminated by force");
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
			}
		}
	}

	private final class ConsoleCommunication {
		private final ClientSender m_sender;
		private final Connector m_connector;
		private final TimerTask m_reportRunningTask;
		private final MessagePump m_messagePump;

		public ConsoleCommunication(Connector connector, String user) throws CommunicationException,
				FileStore.FileStoreException {

			final ClientReceiver receiver = ClientReceiver.connect(connector, new AgentAddress(m_agentIdentity));
			m_sender = ClientSender.connect(receiver);
			m_connector = connector;

			if (m_fileStore == null) {
				// Only create the file store if we connected.
				File base = m_agentConfig.getHome().getDirectory();
				File directory = new File(new File(base, "file-store"), user);
				m_fileStore = new FileStore(directory, m_logger);
			}

			m_sender.send(new AgentProcessReportMessage(ProcessReport.STATE_STARTED, m_fileStore
					.getCacheHighWaterMark()));

			final MessageDispatchSender fileStoreMessageDispatcher = new MessageDispatchSender();
			m_fileStore.registerMessageHandlers(fileStoreMessageDispatcher);

			final MessageDispatchSender messageDispatcher = new MessageDispatchSender();
			m_consoleListener.registerMessageHandlers(messageDispatcher);

			// Everything that the file store doesn't handle is tee'd to the
			// worker processes and our message handlers.
			fileStoreMessageDispatcher.addFallback(new TeeSender(messageDispatcher, new IgnoreShutdownSender(
					m_fanOutStreamSender)));

			m_messagePump = new MessagePump(receiver, fileStoreMessageDispatcher, 1);

			m_reportRunningTask = new TimerTask() {
				public void run() {
					try {
						m_sender.send(new AgentProcessReportMessage(ProcessReport.STATE_RUNNING, m_fileStore
								.getCacheHighWaterMark()));
					} catch (CommunicationException e) {
						cancel();
						m_logger.error("Error while pumping up the AgentProcessReportMessage", e.getMessage());
						m_logger.debug("The error detail is ", e);
					}

				}
			};
		}

		public void start() {
			m_messagePump.start();
			m_timer.schedule(m_reportRunningTask, GrinderConstants.AGENT_HEARTBEAT_DELAY,
					GrinderConstants.AGENT_HEARTBEAT_INTERVAL);
		}

		public Connector getConnector() {
			return m_connector;
		}

		public void shutdown() {
			m_reportRunningTask.cancel();

			try {
				m_sender.send(new AgentProcessReportMessage(ProcessReport.STATE_FINISHED, m_fileStore
						.getCacheHighWaterMark()));
				m_logger.debug("Shut down message was sent");
			} catch (CommunicationException e) {
				NoOp.noOp();
			} finally {
				m_messagePump.shutdown();
			}
		}
	}
}
