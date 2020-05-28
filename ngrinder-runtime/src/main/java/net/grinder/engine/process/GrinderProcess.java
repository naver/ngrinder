// Copyright (C) 2000 Paco Gomez
// Copyright (C) 2000 - 2012 Philip Aston
// Copyright (C) 2003 Kalyanaraman Venkatasubramaniy
// Copyright (C) 2004 Slavik Gnatenko
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

package net.grinder.engine.process;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.JoranException;
import net.grinder.common.*;
import net.grinder.common.processidentity.ProcessReport;
import net.grinder.common.processidentity.WorkerIdentity;
import net.grinder.communication.*;
import net.grinder.engine.common.ConnectorFactory;
import net.grinder.engine.common.EngineException;
import net.grinder.engine.communication.ConsoleListener;
import net.grinder.engine.messages.InitialiseGrinderMessage;
import net.grinder.engine.process.dcr.DCRContextImplementation;
import net.grinder.messages.console.RegisterTestsMessage;
import net.grinder.messages.console.ReportStatisticsMessage;
import net.grinder.messages.console.WorkerAddress;
import net.grinder.messages.console.WorkerProcessReportMessage;
import net.grinder.script.Grinder;
import net.grinder.script.InternalScriptContext;
import net.grinder.script.InvalidContextException;
import net.grinder.script.Statistics;
import net.grinder.scriptengine.Instrumenter;
import net.grinder.scriptengine.ScriptEngineService.ScriptEngine;
import net.grinder.scriptengine.ScriptEngineService.WorkerRunnable;
import net.grinder.scriptengine.ScriptExecutionException;
import net.grinder.statistics.*;
import net.grinder.synchronisation.BarrierGroups;
import net.grinder.synchronisation.BarrierIdentityGenerator;
import net.grinder.synchronisation.ClientBarrierGroups;
import net.grinder.synchronisation.LocalBarrierGroups;
import net.grinder.util.*;
import net.grinder.util.ListenerSupport.Informer;
import net.grinder.util.thread.BooleanCondition;
import net.grinder.util.thread.Condition;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * The controller for a worker process.
 * <p/>
 * <p>
 * Package scope.
 * </p>
 *
 * @author Paco Gomez
 * @author Philip Aston
 * @author JunHo Yoon (modifed for nGrinder)
 * @see GrinderThread
 */
final class GrinderProcess {

	private final Logger m_terminalLogger;
	private Logger m_logger = null;
	private final Logger m_dataLogger;
	private final LoggerContext m_logbackLoggerContext;
	private final boolean m_reportTimesToConsole;
	private final QueuedSender m_consoleSender;
	private final Sleeper m_sleeper;
	private final InitialiseGrinderMessage m_initialisationMessage;
	private final ConsoleListener m_consoleListener;
	private final StatisticsServices m_statisticsServices;
	private final TestStatisticsMap m_accumulatedStatistics;
	private final TestStatisticsHelperImplementation m_testStatisticsHelper;
	private final TestRegistryImplementation m_testRegistryImplementation;
	private final Condition m_eventSynchronisation = new Condition();
	private final MessagePump m_messagePump;

	private final ThreadStarter m_invalidThreadStarter = new InvalidThreadStarter();

	private final Times m_times = new Times();

	private final ThreadContexts m_threadContexts = new ThreadContexts();

	private final ListenerSupport<ProcessLifeCycleListener> m_processLifeCycleListeners = new ListenerSupport<ProcessLifeCycleListener>();

	// Guarded by m_eventSynchronisation.
	private ThreadStarter m_threadStarter = m_invalidThreadStarter;

	private boolean m_shutdownTriggered;
	private boolean m_communicationShutdown;

	/**
	 * Creates a new <code>GrinderProcess</code> instance.
	 *
	 * @param agentReceiver Receiver used to listen to the agent.
	 * @throws net.grinder.common.GrinderException
	 *          If the process could not be created.
	 */
	public GrinderProcess(final Receiver agentReceiver) throws GrinderException {
		try {
			m_initialisationMessage = (InitialiseGrinderMessage) agentReceiver.waitForMessage();

			if (m_initialisationMessage == null) {
				throw new EngineException("No control stream from agent");
			}

			final GrinderProperties properties = m_initialisationMessage.getProperties();

			final WorkerIdentity workerIdentity = m_initialisationMessage.getWorkerIdentity();

			final String workerName = workerIdentity.getName();
			final String logDirectory = properties.getProperty(GrinderProperties.LOG_DIRECTORY, ".");

			m_terminalLogger = LoggerFactory.getLogger(workerName);

			m_reportTimesToConsole = properties.getBoolean("grinder.reportTimesToConsole", true);

			m_logbackLoggerContext = configureLogging(workerName, logDirectory);
			m_logger = LoggerFactory.getLogger("worker." + workerName);
			m_dataLogger = LoggerFactory.getLogger("data");

			m_logger.info("The Grinder version {}", GrinderBuild.getVersionString());
			m_logger.info(JVM.getInstance().toString());
			m_logger.info("time zone is {}", new SimpleDateFormat("z (Z)").format(new Date()));

			final MessageDispatchSender messageDispatcher = new MessageDispatchSender();

			final BarrierGroups barrierGroups;

			if (m_initialisationMessage.getReportToConsole()) {
				m_consoleSender = new QueuedSenderDecorator(ClientSender.connect(new ConnectorFactory(
						ConnectionType.WORKER).create(properties), new WorkerAddress(workerIdentity)));

				barrierGroups = new ClientBarrierGroups(m_consoleSender, messageDispatcher);
			} else {
				m_consoleSender = new NullQueuedSender();
				barrierGroups = new LocalBarrierGroups();
			}

			final BarrierIdentityGenerator barrierIdentityGenerator = new BarrierIdentityGenerator(
					m_initialisationMessage.getWorkerIdentity());

			final ThreadStarter delegatingThreadStarter = new ThreadStarter() {
				@Override
				public int startThread(final Object testRunner) throws EngineException, InvalidContextException {

					final ThreadStarter threadStarter;

					synchronized (m_eventSynchronisation) {
						threadStarter = m_threadStarter;
					}

					return threadStarter.startThread(testRunner);
				}
			};

			m_statisticsServices = StatisticsServicesImplementation.getInstance();

			m_accumulatedStatistics = new TestStatisticsMap(m_statisticsServices.getStatisticsSetFactory());
			m_testStatisticsHelper = new TestStatisticsHelperImplementation(
					m_statisticsServices.getStatisticsIndexMap());

			m_testRegistryImplementation = new TestRegistryImplementation(m_threadContexts,
					m_statisticsServices.getStatisticsSetFactory(), m_testStatisticsHelper,
					m_times.getTimeAuthority());

			final Logger externalLogger = new ExternalLogger(m_logger, m_threadContexts);

			m_sleeper = new SleeperImplementation(m_times.getTimeAuthority(), externalLogger, properties.getDouble(
					"grinder.sleepTimeFactor", 1.0d), properties.getDouble("grinder.sleepTimeVariation", 0.2d));

			final Statistics scriptStatistics = new ScriptStatisticsImplementation(m_threadContexts,
					m_statisticsServices, m_consoleSender);

			final ThreadStopper threadStopper = new ThreadStopper() {
				@Override
				public boolean stopThread(final int threadNumber) {
					return m_threadContexts.shutdown(threadNumber);
				}
			};

			final InternalScriptContext scriptContext = new ScriptContextImplementation(workerIdentity,
					m_initialisationMessage.getFirstWorkerIdentity(), m_threadContexts, properties,
					externalLogger, m_sleeper, new SSLControlImplementation(m_threadContexts),
					scriptStatistics, m_testRegistryImplementation, delegatingThreadStarter, threadStopper,
					barrierGroups, barrierIdentityGenerator);

			Grinder.grinder = scriptContext;

			final PluginRegistryImplementation pluginRegistry = new PluginRegistryImplementation(externalLogger,
					scriptContext, m_threadContexts, m_statisticsServices, m_times.getTimeAuthority());

			m_processLifeCycleListeners.add(pluginRegistry);

			m_processLifeCycleListeners.add(m_threadContexts);

			// If we don't call getLocalHost() before spawning our
			// ConsoleListener thread, any attempt to call it afterwards will
			// silently crash the JVM. Reproduced with both J2SE 1.3.1-b02 and
			// J2SE 1.4.1_03-b02 on W2K. Do not ask me why, I've stopped
			// caring.
			try {
				//noinspection ResultOfMethodCallIgnored
				java.net.InetAddress.getLocalHost();
			} catch (final UnknownHostException e) { /* Ignore */
			}

			m_consoleListener = new ConsoleListener(m_eventSynchronisation, m_logger);

			m_consoleListener.registerMessageHandlers(messageDispatcher);
			m_messagePump = new MessagePump(agentReceiver, messageDispatcher, 1);
		} catch (GrinderException e) {
			if (m_logger != null) {
				m_logger.error("Error running worker process", e);
			}
			throw e;
		}
	}

	private LoggerContext configureLogging(final String workerName, final String logDirectory) throws EngineException {

		final ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();

		if (iLoggerFactory instanceof Context) {
			final Context context = (Context) iLoggerFactory;
			final LoggerContext result = (LoggerContext) iLoggerFactory;

			final JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(context);
			context.putProperty("WORKER_NAME", workerName);
			context.putProperty("LOG_DIRECTORY", logDirectory);

			try {
				configurator.doConfigure(GrinderProcess.class.getResource("/logback-worker.xml"));
			} catch (final JoranException e) {
				throw new EngineException("Could not initialise logger", e);
			}

			return result;
		} else {
			m_terminalLogger.warn("Logback not found; grinder log configuration will be ignored.\n"
					+ "Consider adding logback-classic to the start of the CLASSPATH.");

			return null;
		}
	}

	/**
	 * The application's main loop. This is split from the constructor as theoretically it might be
	 * called multiple times. The constructor sets up the static configuration, this does a single
	 * execution.
	 * <p/>
	 * <p>
	 * This method is interruptible, in the same sense as
	 * {@link net.grinder.util.thread.InterruptibleRunnable#interruptibleRun()}. We don't implement
	 * that method because we want to be able to throw exceptions.
	 * </p>
	 *
	 * @throws net.grinder.common.GrinderException
	 *          If something went wrong.
	 */
	public void run() throws GrinderException {
		try {
			final GrinderProperties properties = m_initialisationMessage.getProperties();

			final ScriptEngineContainer scriptEngineContainer = new ScriptEngineContainer(properties, m_logger,
					DCRContextImplementation.create(m_logger), m_initialisationMessage.getScript());

			final WorkerIdentity workerIdentity = m_initialisationMessage.getWorkerIdentity();

			final StringBuilder numbers = new StringBuilder("worker process ");

			numbers.append(workerIdentity.getNumber());

			final int agentNumber = workerIdentity.getAgentIdentity().getNumber();

			if (agentNumber >= 0) {
				numbers.append(" of agent number ");
				numbers.append(agentNumber);
			}

			m_logger.info(numbers.toString());

			final short numberOfThreads = properties.getShort("grinder.threads", (short) 1);
			final int reportToConsoleInterval = properties.getInt("grinder.reportToConsole.interval", 500);
			final int duration = properties.getInt("grinder.duration", 0);

			final Instrumenter instrumenter = scriptEngineContainer.createInstrumenter();

			m_testRegistryImplementation.setInstrumenter(instrumenter);

			m_logger.info("Instrumentation agents: {}", instrumenter.getDescription());

			// Force initialisation of the script engine before we start the
			// message
			// pump. Jython 2.5+ tests to see whether the stdin stream is a tty,
			// and
			// on some versions of Windows, this synchronises on the stream
			// object's
			// monitor. This clashes with the message pump which starts a thread
			// to
			// call StreamRecevier.waitForMessage(), and so also synchronises on
			// that
			// monitor. See bug 2936167.

			final ScriptEngine scriptEngine = scriptEngineContainer
					.getScriptEngine(m_initialisationMessage.getScript());

			m_logger.info("Running \"{}\" using {}", m_initialisationMessage.getScript(),
					scriptEngine.getDescription());

			m_messagePump.start();

			// Don't write out the data log header until now as the script may
			// declare new statistics.

			final StringBuilder dataLogHeader = new StringBuilder("Thread, Run, Test, Start time (ms since Epoch)");

			final ExpressionView[] detailExpressionViews = m_statisticsServices.getDetailStatisticsView()
					.getExpressionViews();

			for (final ExpressionView detailExpressionView : detailExpressionViews) {
				dataLogHeader.append(", ");
				dataLogHeader.append(detailExpressionView.getDisplayName());
			}

			m_dataLogger.info(dataLogHeader.toString());

			sendStatusMessage(ProcessReport.STATE_STARTED, (short) 0, numberOfThreads);
			boolean threadRampUp = properties.getBoolean("grinder.threadRampUp", false);
			final ThreadSynchronisation threadSynchronisation = threadRampUp ?
					new ThreadRampUpEnabledThreadSynchronisation(m_eventSynchronisation, m_sleeper) :
					new ThreadSynchronisation(m_eventSynchronisation);

			m_terminalLogger.info("Starting threads");

			synchronized (m_eventSynchronisation) {
				m_threadStarter = new ThreadStarterImplementation(threadSynchronisation, scriptEngine);

				for (int i = 0; i < numberOfThreads; i++) {
					m_threadStarter.startThread(null);
				}
			}

			threadSynchronisation.startThreads();

			m_times.setExecutionStartTime();

			m_logger.info("Start time is {} ms since Epoch", m_times.getExecutionStartTime());

			final TimerTask reportTimerTask = new ReportToConsoleTimerTask(threadSynchronisation);
			final TimerTask shutdownTimerTask = new ShutdownTimerTask();

			// Schedule a regular statistics report to the console. We don't
			// need to schedule this at a fixed rate. Each report contains the
			// work done since the last report.

			// First (empty) report to console to start it recording if its
			// not already.
			reportTimerTask.run();

			final Timer timer = new Timer(true);

			timer.schedule(reportTimerTask, reportToConsoleInterval, reportToConsoleInterval);

			try {
				if (duration > 0) {
					m_terminalLogger.info("This test will shut down after {} ms", duration);

					timer.schedule(shutdownTimerTask, duration);
				}

				// Wait for a termination event.
				synchronized (m_eventSynchronisation) {
					while (!threadSynchronisation.isFinished()) {

						if (m_consoleListener.checkForMessage(ConsoleListener.ANY ^ ConsoleListener.START)) {
							break;
						}

						if (m_shutdownTriggered) {
							m_terminalLogger.info("Specified duration exceeded, Test is shut down");
							break;
						}

						m_eventSynchronisation.waitNoInterrruptException();
					}
				}

				synchronized (m_eventSynchronisation) {
					if (!threadSynchronisation.isFinished()) {

						m_terminalLogger.info("Waiting for threads to terminate");

						m_threadStarter = m_invalidThreadStarter;
						m_threadContexts.shutdownAll();

						// Interrupt any sleepers.
						SleeperImplementation.shutdownAllCurrentSleepers();

						final long time = System.currentTimeMillis();
						final long maximumShutdownTime = 10000;

						while (!threadSynchronisation.isFinished()) {
							if (System.currentTimeMillis() - time > maximumShutdownTime) {
								m_terminalLogger.info("Ignoring unresponsive threads");
								break;
							}

							m_eventSynchronisation.waitNoInterrruptException(maximumShutdownTime);
						}
					}
				}
			} finally {
				reportTimerTask.cancel();
				shutdownTimerTask.cancel();
			}

			scriptEngine.shutdown();

			// Final report to the console.
			reportTimerTask.run();

			if (!m_communicationShutdown) {
				sendStatusMessage(ProcessReport.STATE_FINISHED, (short) 0, (short) 0);
			}

			m_consoleSender.shutdown();

			final long elapsedTime = m_times.getElapsedTime();
			m_logger.info("elapsed time is {} ms", elapsedTime);

			m_logger.info("Final statistics for this process:");

			final StatisticsTable statisticsTable = new StatisticsTable(
					m_statisticsServices.getSummaryStatisticsView(),
					m_statisticsServices.getStatisticsIndexMap(), m_accumulatedStatistics);

			final StringWriter statistics = new StringWriter();
			statistics.write("\n");
			statisticsTable.print(new PrintWriter(statistics), elapsedTime);
			m_logger.info(statistics.toString());

			timer.cancel();

			m_terminalLogger.info("Finished");

		} catch (final ScriptExecutionException e) {
			m_logger.error("Aborting process - {}", e.getShortMessage(), e);
			m_terminalLogger.error("aborting process - {}", e.getShortMessage(), e);
		} catch (EngineException e) {
			m_logger.error("Script error - {}", e.getMessage(), e);
			throw e;
		}
	}

	public void shutdown(final boolean inputStreamIsStdin) {
		if (!inputStreamIsStdin) {
			// Sadly it appears its impossible to interrupt a read() on a
			// process
			// input stream (at least under W2K), so we can't shut down the
			// message
			// pump cleanly. It runs in a daemon thread, so this isn't a big
			// deal.
			m_messagePump.shutdown();
		}

		// Logback doesn't stop its loggers on exit (see LBCORE-202). We do
		// so explicitly to flush our BufferedEchoMessageEncoder.
		if (m_logbackLoggerContext != null) {
			m_logbackLoggerContext.stop();
		}
	}

	private class ReportToConsoleTimerTask extends TimerTask {
		private final ThreadSynchronisation m_threads;

		public ReportToConsoleTimerTask(final ThreadSynchronisation threads) {
			m_threads = threads;
		}

		@Override
		public void run() {
			if (!m_communicationShutdown) {
				try {
					final TestStatisticsMap sample = m_testRegistryImplementation.getTestStatisticsMap().reset();
					m_accumulatedStatistics.add(sample);

					// We look up the new tests after we've taken the sample to
					// avoid a race condition when new tests are being added.
					final Collection<Test> newTests = m_testRegistryImplementation.getNewTests();

					if (newTests != null) {
						m_consoleSender.send(new RegisterTestsMessage(newTests));
					}

					if (sample.size() > 0) {
						if (!m_reportTimesToConsole) {
							m_testStatisticsHelper.removeTestTimeFromSample(sample);
						}

						m_consoleSender.send(new ReportStatisticsMessage(sample));
					}

					sendStatusMessage(ProcessReport.STATE_RUNNING, m_threads.getNumberOfRunningThreads(),
							m_threads.getTotalNumberOfThreads());
				} catch (final CommunicationException e) {
					m_terminalLogger.info("Report to console failed", e);

					m_communicationShutdown = true;
				}
			}
		}
	}

	private void sendStatusMessage(final short state, final short numberOfThreads, final short totalNumberOfThreads)
			throws CommunicationException {

		m_consoleSender.send(new WorkerProcessReportMessage(state, numberOfThreads, totalNumberOfThreads));

		m_consoleSender.flush();
	}

	private class ShutdownTimerTask extends TimerTask {
		@Override
		public void run() {
			synchronized (m_eventSynchronisation) {
				m_shutdownTriggered = true;
				m_eventSynchronisation.notifyAll();
			}
		}
	}

	/**
	 * Implement {@link net.grinder.engine.process.WorkerThreadSynchronisation}. I looked hard at JSR 166's
	 * <code>CountDownLatch</code> and <code>CyclicBarrier</code>, but neither of them allow for the
	 * waiting thread to be interrupted by other events.
	 * <p/>
	 * <p>
	 * Package scope for unit tests.
	 * </p>
	 */
	static class ThreadSynchronisation implements WorkerThreadSynchronisation {
		final BooleanCondition m_started = new BooleanCondition();
		final Condition m_threadEventCondition;

		short m_numberCreated = 0;
		short m_numberAwaitingStart = 0;
		short m_numberFinished = 0;
		short m_numberRunning = 0;

		ThreadSynchronisation(final Condition condition) {
			m_threadEventCondition = condition;
		}


		/**
		 * The number of worker threads that have been created but not run to completion.
		 */
		public short getNumberOfRunningThreads() {
			synchronized (m_threadEventCondition) {
				return (short) (m_numberCreated - m_numberFinished);
			}
		}

		public boolean isReadyToStart() {
			synchronized (m_threadEventCondition) {
				return m_numberAwaitingStart >= getNumberOfRunningThreads();
			}
		}

		public boolean isFinished() {
			return getNumberOfRunningThreads() <= 0;
		}

		/**
		 * The number of worker threads that have been created.
		 */
		public short getTotalNumberOfThreads() {
			synchronized (m_threadEventCondition) {
				return m_numberCreated;
			}
		}

		@Override
		public void threadCreated() {
			synchronized (m_threadEventCondition) {
				++m_numberCreated;
			}
		}

		public void startThreads() {
			synchronized (m_threadEventCondition) {
				while (!isReadyToStart()) {
					m_threadEventCondition.waitNoInterrruptException();
				}

				m_numberAwaitingStart = 0;
			}

			m_started.set(true);
		}

		@Override
		public void awaitStart() {
			synchronized (m_threadEventCondition) {
				++m_numberAwaitingStart;

				if (isReadyToStart()) {
					m_threadEventCondition.notifyAll();
				}
			}

			m_started.await(true);
		}

		@Override
		public void threadFinished() {
			synchronized (m_threadEventCondition) {
				++m_numberFinished;

				if (isReadyToStart() || isFinished()) {
					m_threadEventCondition.notifyAll();
				}
			}
		}


	}

	static class ThreadRampUpEnabledThreadSynchronisation extends ThreadSynchronisation {
		private final Sleeper sleeper;

		ThreadRampUpEnabledThreadSynchronisation(Condition condition, Sleeper sleeper) {
			super(condition);
			this.sleeper = sleeper;
		}

		public void startThreads() {
			synchronized (m_threadEventCondition) {
				while (!isReadyToStart()) {
					m_threadEventCondition.waitNoInterrruptException();
				}
				m_numberAwaitingStart = 0;
			}
//			m_started.set(true);
		}

		@Override
		public void awaitStart() {
			int waitingTime = doRampUp();
			int threadNumber = 0;
			if (Grinder.grinder != null) {
				threadNumber = Math.max(Grinder.grinder.getThreadNumber(), 0);
			}
			synchronized (m_threadEventCondition) {
				m_numberAwaitingStart++;
				m_numberRunning++;
				m_threadEventCondition.notifyAll();
			}
			if (Grinder.grinder != null) {
				Grinder.grinder.getLogger().info("thread-{} is invoked after {} ms sleep", threadNumber,
						waitingTime);
			}

//			m_started.await(true);
		}

		@Override
		public short getNumberOfRunningThreads() {
			synchronized (m_threadEventCondition) {
				return m_numberRunning;
			}
		}


		@Override
		public boolean isReadyToStart() {
			return true;
		}


		@Override
		public void threadFinished() {
			synchronized (m_threadEventCondition) {
				++m_numberFinished;

				if (isFinished()) {
					m_threadEventCondition.notifyAll();
				}
			}
		}

		public boolean isFinished() {
			return getNumberOfNotFinishedThreads() <= 0;
		}

		/**
		 * The number of worker threads that have been created but not run to completion.
		 */
		public short getNumberOfNotFinishedThreads() {
			synchronized (m_threadEventCondition) {
				return (short) (m_numberCreated - m_numberFinished);
			}
		}

		public static final String GRINDER_PROP_THREAD_INCREMENT = "grinder.processIncrement";
		public static final String GRINDER_PROP_THREAD_INCREMENT_INTERVAL = "grinder.processIncrementInterval";
		public static final String GRINDER_PROP_INITIAL_PROCESS = "grinder.initialProcesses";
		public static final String GRINDER_PROP_INITIAL_THREAD_SLEEP_TIME = "grinder.initialThreadSleepTime";

		protected int doRampUp() {
			InternalScriptContext grinder = Grinder.grinder;
			if (grinder != null) {
				GrinderProperties properties = grinder.getProperties();
				int rampUpInterval = properties.getInt(GRINDER_PROP_THREAD_INCREMENT_INTERVAL, 0);
				int rampUpStep = properties.getInt(GRINDER_PROP_THREAD_INCREMENT, 0);
				int rampUpInitialThread = properties.getInt(GRINDER_PROP_INITIAL_PROCESS, 0);
				int rampUpInitialSleep = properties.getInt(GRINDER_PROP_INITIAL_THREAD_SLEEP_TIME, 0);
				return doRampUp(rampUpInterval, rampUpStep, rampUpInitialThread, rampUpInitialSleep);
			}
			return 0;
		}

		private int doRampUp(int rampUpInterval, int rampUpStep, int rampUpInitialThread, int rampUpInitialSleep) {
			int threadNumber = 0;
			int waitingTime;
			if (Grinder.grinder != null) {
				threadNumber = Math.max(Grinder.grinder.getThreadNumber(), 0);
			}
			try {
				waitingTime = getWaitingTime(rampUpInterval, rampUpStep,
						rampUpInitialThread, rampUpInitialSleep,
						threadNumber);
				if (waitingTime != 0) {
					if (Grinder.grinder != null) {
						Grinder.grinder.getLogger().info("thread-{} is sleeping {} ms for ramp-up", threadNumber,
								waitingTime);
					}
					sleeper.sleepNormal(waitingTime, 0);
				}

				return waitingTime;
			} catch (Sleeper.ShutdownException e) {
				throw new RuntimeException(e);
			}
		}

		public int getWaitingTime(int rampUpInterval, int rampUpStep,
		                          int rampUpInitialThread, int rampUpInitialSleep, int threadNumber) {
			// 100 2 1 0 3   ==> 100
			if (threadNumber < rampUpInitialThread) {
				return 0;
			}
			int remained = (threadNumber - rampUpInitialThread);
			int threadStep = (remained / rampUpStep) + 1;
			return Math.max(rampUpInitialSleep + (threadStep * rampUpInterval), 0);
		}

	}

	private final class ThreadStarterImplementation implements ThreadStarter {
		private final ThreadSynchronisation m_threadSynchronisation;
		private final ScriptEngine m_scriptEngine;
		private final WorkerRunnableFactory m_defaultWorkerRunnableFactory;

		private final ProcessLifeCycleListener m_threadLifeCycleCallbacks = new ProcessLifeCycleListener() {
			@Override
			public void threadCreated(final ThreadContext threadContext) {
				m_processLifeCycleListeners.apply(new Informer<ProcessLifeCycleListener>() {
					@Override
					public void inform(final ProcessLifeCycleListener listener) {
						listener.threadCreated(threadContext);
					}
				});
			}

			@Override
			public void threadStarted(final ThreadContext threadContext) {
				m_processLifeCycleListeners.apply(new Informer<ProcessLifeCycleListener>() {
					@Override
					public void inform(final ProcessLifeCycleListener listener) {
						listener.threadStarted(threadContext);
					}
				});
			}
		};

		private int m_i = -1;

		private ThreadStarterImplementation(final ThreadSynchronisation threadSynchronisation,
		                                    final ScriptEngine scriptEngine) {
			m_threadSynchronisation = threadSynchronisation;
			m_scriptEngine = scriptEngine;

			m_defaultWorkerRunnableFactory = new WorkerRunnableFactory() {
				@Override
				public WorkerRunnable create() throws EngineException {
					return m_scriptEngine.createWorkerRunnable();
				}
			};
		}

		@Override
		public int startThread(final Object testRunner) throws EngineException {
			final int threadNumber;
			synchronized (this) {
				threadNumber = ++m_i;
			}

			final ThreadContext threadContext = new ThreadContextImplementation(
					m_initialisationMessage.getProperties(), m_statisticsServices, threadNumber, m_dataLogger);

			final WorkerRunnableFactory workerRunnableFactory;

			if (testRunner != null) {
				workerRunnableFactory = new WorkerRunnableFactory() {
					@Override
					public WorkerRunnable create() throws EngineException {
						return m_scriptEngine.createWorkerRunnable(testRunner);
					}
				};
			} else {
				workerRunnableFactory = m_defaultWorkerRunnableFactory;
			}

			final GrinderThread runnable = new GrinderThread(m_logger, threadContext, m_threadSynchronisation,
					m_threadLifeCycleCallbacks, m_initialisationMessage.getProperties(), m_sleeper,
					workerRunnableFactory);

			final Thread t = new Thread(runnable, "thread " + threadNumber);
			t.setDaemon(true);
			t.start();

			return threadNumber;
		}
	}

	/**
	 * Package scope for unit tests.
	 */
	static final class InvalidThreadStarter implements ThreadStarter {
		@Override
		public int startThread(final Object testRunner) throws InvalidContextException {
			throw new InvalidContextException("You should not start worker threads until the main thread has "
					+ "initialised the script engine, or after all other threads have "
					+ "shut down. Typically, you should only call startWorkerThread() "
					+ "from another worker thread.");
		}
	}

	/**
	 * Package scope for unit tests.
	 */
	static final class Times {
		private volatile long m_executionStartTime;

		private final TimeAuthority m_timeAuthority = new StandardTimeAuthority();

		/**
		 * {@link net.grinder.engine.process.GrinderProcess} calls {@link #setExecutionStartTime} just before launching
		 * threads, after which it is never called again.
		 */
		public void setExecutionStartTime() {
			m_executionStartTime = m_timeAuthority.getTimeInMilliseconds();
		}

		/**
		 * {@link net.grinder.engine.process.GrinderProcess} calls {@link #setExecutionStartTime} just before launching
		 * threads, after which it is never called again.
		 *
		 * @return Start of execution, in milliseconds since the Epoch.
		 */
		public long getExecutionStartTime() {
			return m_executionStartTime;
		}

		/**
		 * Elapsed time since execution was started.
		 *
		 * @return The time in milliseconds.
		 * @see #getExecutionStartTime()
		 */
		public long getElapsedTime() {
			return m_timeAuthority.getTimeInMilliseconds() - getExecutionStartTime();
		}

		public TimeAuthority getTimeAuthority() {
			return m_timeAuthority;
		}
	}

	/**
	 * Package scope for unit tests.
	 */
	static final class ThreadContexts implements ProcessLifeCycleListener, ThreadContextLocator {

		private final ThreadLocal<ThreadContext> m_threadContextThreadLocal = new ThreadLocal<ThreadContext>();

		// Guarded by self.
		private final Map<Integer, ThreadContext> m_threadContextsMap = new HashMap<Integer, ThreadContext>();

		// Guarded by m_threadContextsMap.
		private boolean m_allShutdown;

		@Override
		public ThreadContext get() {
			return m_threadContextThreadLocal.get();
		}

		@Override
		public void threadCreated(final ThreadContext threadContext) {
			final Integer threadNumber = threadContext.getThreadNumber();

			final boolean shutdown;

			synchronized (m_threadContextsMap) {
				shutdown = m_allShutdown;

				if (!shutdown) {
					threadContext.registerThreadLifeCycleListener(new SkeletonThreadLifeCycleListener() {
						@Override
						public void endThread() {
							m_threadContextsMap.remove(threadNumber);
						}
					});

					// Very unlikely, harmless race here - we could store a
					// reference to
					// a thread context that is in the process of shutting down.
					m_threadContextsMap.put(threadNumber, threadContext);
				}
			}

			if (shutdown) {
				// Stop new threads in their tracks.
				threadContext.shutdown();
			}
		}

		@Override
		public void threadStarted(final ThreadContext threadContext) {
			m_threadContextThreadLocal.set(threadContext);
		}

		public boolean shutdown(final int threadNumber) {
			final ThreadContext threadContext;

			synchronized (m_threadContextsMap) {
				threadContext = m_threadContextsMap.get(threadNumber);
			}

			if (threadContext != null) {
				threadContext.shutdown();
				return true;
			}

			return false;
		}

		public void shutdownAll() {
			final ThreadContext[] threadContexts;

			synchronized (m_threadContextsMap) {
				m_allShutdown = true;

				threadContexts = m_threadContextsMap.values().toArray(new ThreadContext[m_threadContextsMap.size()]);
			}

			for (final ThreadContext threadContext : threadContexts) {
				threadContext.shutdown();
			}
		}
	}

	/**
	 * Package scope for unit tests.
	 */
	static final class NullQueuedSender implements QueuedSender {
		@Override
		public void send(final Message message) {
		}

		@Override
		public void flush() {
		}

		@Override
		public void shutdown() {
		}
	}
}
