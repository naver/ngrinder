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

import net.grinder.SingleConsole;
import net.grinder.SingleConsole.ConsoleShutdownListener;
import net.grinder.StopReason;
import net.grinder.common.GrinderProperties;
import net.grinder.console.model.ConsoleProperties;
import net.grinder.util.ListenerHelper;
import net.grinder.util.ListenerSupport;
import net.grinder.util.UnitUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.DateUtils;
import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.extension.OnTestLifeCycleRunnable;
import org.ngrinder.extension.OnTestSamplingRunnable;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.plugin.PluginManager;
import org.ngrinder.infra.schedule.ScheduledTaskService;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Status;
import org.ngrinder.perftest.model.NullSingleConsole;
import org.ngrinder.perftest.service.samplinglistener.*;
import org.ngrinder.script.handler.ScriptHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.apache.commons.lang.ObjectUtils.defaultIfNull;
import static org.ngrinder.common.constant.ClusterConstants.PROP_CLUSTER_SAFE_DIST;
import static org.ngrinder.common.util.AccessUtils.getSafe;
import static org.ngrinder.model.Status.*;

/**
 * {@link PerfTest} run scheduler.
 * <p/>
 * This class is responsible to execute/finish the performance test. The job is
 * started from {@link #doStart()}  and {@link #doFinish()} method. These
 * methods are scheduled by Spring Task.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
@Profile("production")
@Component
public class PerfTestRunnable implements ControllerConstants {

	private static final Logger LOG = LoggerFactory.getLogger(PerfTestRunnable.class);

	@SuppressWarnings("SpringJavaAutowiringInspection")
	@Autowired
	private PerfTestService perfTestService;

	@Autowired
	private ConsoleManager consoleManager;

	@Autowired
	private AgentManager agentManager;

	@Autowired
	private PluginManager pluginManager;

	@Autowired
	private Config config;

	@Autowired
	private ScheduledTaskService scheduledTaskService;

	private Runnable startRunnable;

	private Runnable finishRunnable;

	@PostConstruct
	public void init() {
		// Clean up db first.
		doFinish(true);

		this.startRunnable = new Runnable() {
			@Override
			public void run() {
				startPeriodically();
			}
		};
		scheduledTaskService.addFixedDelayedScheduledTask(startRunnable, PERFTEST_RUN_FREQUENCY_MILLISECONDS);
		this.finishRunnable = new Runnable() {
			@Override
			public void run() {
				finishPeriodically();
			}
		};
		scheduledTaskService.addFixedDelayedScheduledTask(finishRunnable, PERFTEST_RUN_FREQUENCY_MILLISECONDS);

	}

	@PreDestroy
	public void destroy() {
		scheduledTaskService.removeScheduledJob(this.startRunnable);
		scheduledTaskService.removeScheduledJob(this.finishRunnable);
	}

	/**
	 * Scheduled method for test execution. This method dispatches the test
	 * candidates and run one of them. This method is responsible until a test
	 * is executed.
	 */
	public void startPeriodically() {
		doStart();
	}

	void doStart() {
		if (config.hasNoMoreTestLock()) {
			return;
		}
		// Block if the count of testing exceed the limit
		if (!canExecuteMore()) {
			// LOG MORE
			List<PerfTest> currentlyRunningTests = perfTestService.getCurrentlyRunningTest();
			LOG.debug("Currently running test is {}. No more tests can not run.", currentlyRunningTests.size());
			return;
		}
		// Find out next ready perftest
		PerfTest runCandidate = getRunnablePerfTest();
		if (runCandidate == null) {
			return;
		}

		if (!isScheduledNow(runCandidate)) {
			// this test project is reserved,but it isn't yet going to run test
			// right now.
			return;
		}


		if (!hasEnoughFreeAgents(runCandidate)) {
			return;
		}

		doTest(runCandidate);
	}

	private PerfTest getRunnablePerfTest() {
		return perfTestService.getNextRunnablePerfTestPerfTestCandidate();
	}

	private boolean canExecuteMore() {
		return consoleManager.getConsoleInUse().size() < perfTestService.getMaximumConcurrentTestCount();
	}

	private boolean isScheduledNow(PerfTest test) {
		Date current = new Date();
		Date scheduledDate = DateUtils
				.truncate((Date) defaultIfNull(test.getScheduledTime(), current), Calendar.MINUTE);
		return current.after(scheduledDate);
	}


	/**
	 * Check the free agent availability for the given {@link PerfTest}.
	 *
	 * @param test {@link PerfTest}
	 * @return true if enough agents
	 */
	protected boolean hasEnoughFreeAgents(PerfTest test) {
		int size = agentManager.getAllFreeApprovedAgentsForUser(test.getCreatedUser()).size();
		if (test.getAgentCount() != null && test.getAgentCount() > size) {
			perfTestService.markProgress(test, "The test is tried to execute but there is not enough free agents."
					+ "\n- Current free agent count : " + size + "  / Requested : " + test.getAgentCount() + "\n");
			return false;
		}
		return true;
	}

	/**
	 * Run the given test.
	 * <p/>
	 * If fails, it marks STOP_BY_ERROR in the given {@link PerfTest} status
	 *
	 * @param perfTest perftest instance;
	 */
	public void doTest(final PerfTest perfTest) {
		SingleConsole singleConsole = null;
		try {
			singleConsole = startConsole(perfTest);
			ScriptHandler prepareDistribution = perfTestService.prepareDistribution(perfTest);
			GrinderProperties grinderProperties = perfTestService.getGrinderProperties(perfTest, prepareDistribution);
			startAgentsOn(perfTest, grinderProperties, checkCancellation(singleConsole));
			distributeFileOn(perfTest, checkCancellation(singleConsole));

			singleConsole.setReportPath(perfTestService.getReportFileDirectory(perfTest));
			runTestOn(perfTest, grinderProperties, checkCancellation(singleConsole));
		} catch (SingleConsoleCancellationException ex) {
			// In case of error, mark the occurs error on perftest.
			doCancel(perfTest, singleConsole);
			notifyFinish(perfTest, StopReason.CANCEL_BY_USER);
		} catch (Exception e) {
			// In case of error, mark the occurs error on perftest.
			LOG.error("Error while executing test: {} - {} ", perfTest.getTestIdentifier(), e.getMessage());
			LOG.debug("Stack Trace is : ", e);
			doTerminate(perfTest, singleConsole);
			notifyFinish(perfTest, StopReason.ERROR_WHILE_PREPARE);
		}
	}

	/**
	 * Check the cancellation status on console.
	 *
	 * @param singleConsole console
	 * @return true if cancellation is requested.
	 */
	SingleConsole checkCancellation(SingleConsole singleConsole) {
		if (singleConsole.isCanceled()) {
			throw new SingleConsoleCancellationException("Single Console " + singleConsole.getConsolePort()
					+ " is canceled");
		}
		return singleConsole;
	}

	/**
	 * Start a console for given {@link PerfTest}.
	 *
	 * @param perfTest perftest
	 * @return started console
	 */
	SingleConsole startConsole(PerfTest perfTest) {
		perfTestService.markStatusAndProgress(perfTest, START_CONSOLE, "Console is being prepared.");
		// get available consoles.
		ConsoleProperties consoleProperty = perfTestService.createConsoleProperties(perfTest);
		SingleConsole singleConsole = consoleManager.getAvailableConsole(consoleProperty);
		singleConsole.start();
		perfTestService.markPerfTestConsoleStart(perfTest, singleConsole.getConsolePort());
		return singleConsole;
	}

	/**
	 * Distribute files to agents.
	 *
	 * @param perfTest      perftest
	 * @param singleConsole console to be used.
	 */
	void distributeFileOn(final PerfTest perfTest, SingleConsole singleConsole) {
		// Distribute files
		perfTestService.markStatusAndProgress(perfTest, DISTRIBUTE_FILES, "All necessary files are being distributed.");
		ListenerSupport<SingleConsole.FileDistributionListener> listener = ListenerHelper.create();
		final long safeThreadHold = getSafeTransmissionThreshold();

		listener.add(new SingleConsole.FileDistributionListener() {
			@Override
			public void distributed(String fileName) {
				perfTestService.markProgress(perfTest, " - " + fileName);
			}

			@Override
			public boolean start(File dir, boolean safe) {
				if (safe) {
					perfTestService.markProgress(perfTest, "Safe file distribution mode is enabled.");
					return safe;
				}
				long sizeOfDirectory = FileUtils.sizeOfDirectory(dir);
				if (sizeOfDirectory > safeThreadHold) {
					perfTestService.markProgress(perfTest, "The total size of distributed files is over "
							+ UnitUtils.byteCountToDisplaySize(safeThreadHold) + "B.\n- Safe file distribution mode is enabled by force.");
					return true;
				}
				return safe;
			}

		});

		// the files have prepared before
		singleConsole.distributeFiles(perfTestService.getDistributionPath(perfTest), listener,
				isSafeDistPerfTest(perfTest));
		perfTestService.markStatusAndProgress(perfTest, DISTRIBUTE_FILES_FINISHED,
				"All necessary files are distributed.");
	}

	protected long getSafeTransmissionThreshold() {
		return config.getControllerProperties().getPropertyLong(PROP_CONTROLLER_SAFE_DIST_THRESHOLD);
	}

	private boolean isSafeDistPerfTest(final PerfTest perfTest) {
		boolean safeDist = getSafe(perfTest.getSafeDistribution());
		if (config.isClustered()) {
			safeDist = config.getClusterProperties().getPropertyBoolean(PROP_CLUSTER_SAFE_DIST);
		}
		return safeDist;
	}

	/**
	 * Start agents for the given {@link PerfTest}.
	 *
	 * @param perfTest          perftest
	 * @param grinderProperties grinder properties
	 * @param singleConsole     console to be used.
	 */
	void startAgentsOn(PerfTest perfTest, GrinderProperties grinderProperties, SingleConsole singleConsole) {
		perfTestService.markStatusAndProgress(perfTest, START_AGENTS, getSafe(perfTest.getAgentCount())
				+ " agents are starting.");
		agentManager.runAgent(perfTest.getCreatedUser(), singleConsole, grinderProperties,
				getSafe(perfTest.getAgentCount()));
		singleConsole.waitUntilAgentConnected(perfTest.getAgentCount());
		perfTestService.markStatusAndProgress(perfTest, START_AGENTS_FINISHED, getSafe(perfTest.getAgentCount())
				+ " agents are ready.");
	}

	/**
	 * Run a given {@link PerfTest} with the given {@link GrinderProperties} and
	 * the {@link SingleConsole} .
	 *
	 * @param perfTest          perftest
	 * @param grinderProperties grinder properties
	 * @param singleConsole     console to be used.
	 */
	void runTestOn(final PerfTest perfTest, GrinderProperties grinderProperties, final SingleConsole singleConsole) {
		// start target monitor
		for (OnTestLifeCycleRunnable run : pluginManager.getEnabledModulesByClass(OnTestLifeCycleRunnable.class)) {
			run.start(perfTest, perfTestService, config.getVersion());
		}

		// Run test
		perfTestService.markStatusAndProgress(perfTest, START_TESTING, "The test is ready to start.");
		// Add listener to detect abnormal condition and mark the perfTest
		singleConsole.addListener(new ConsoleShutdownListener() {
			@Override
			public void readyToStop(StopReason stopReason) {
				perfTestService.markAbnormalTermination(perfTest, stopReason);
				LOG.error("Abnormal test {} due to {}", perfTest.getId(), stopReason.name());
			}
		});
		long startTime = singleConsole.startTest(grinderProperties);
		perfTest.setStartTime(new Date(startTime));
		addSamplingListeners(perfTest, singleConsole);
		perfTestService.markStatusAndProgress(perfTest, TESTING, "The test is started.");
		singleConsole.startSampling();

	}

	protected void addSamplingListeners(final PerfTest perfTest, final SingleConsole singleConsole) {
		// Add SamplingLifeCycleListener
		singleConsole.addSamplingLifeCyleListener(new PerfTestSamplingCollectorListener(singleConsole,
				perfTest.getId(), perfTestService, scheduledTaskService));
		singleConsole.addSamplingLifeCyleListener(new AgentLostDetectionListener(singleConsole, perfTest,
				perfTestService, scheduledTaskService));
		List<OnTestSamplingRunnable> testSamplingPlugins = pluginManager.getEnabledModulesByClass
				(OnTestSamplingRunnable.class, new MonitorCollectorPlugin(config, scheduledTaskService,
						perfTestService, perfTest.getId()));
		singleConsole.addSamplingLifeCyleListener(new PluginRunListener(testSamplingPlugins, singleConsole,
				perfTest, perfTestService));
		singleConsole.addSamplingLifeCyleListener(new AgentDieHardListener(singleConsole, perfTest, perfTestService,
				agentManager, scheduledTaskService));
	}


	/**
	 * Notify test finish to plugins.
	 *
	 * @param perfTest PerfTest
	 * @param reason   the reason of test finish..
	 * @see OnTestLifeCycleRunnable
	 */
	public void notifyFinish(PerfTest perfTest, StopReason reason) {
		for (OnTestLifeCycleRunnable run : pluginManager.getEnabledModulesByClass(OnTestLifeCycleRunnable.class)) {
			run.finish(perfTest, reason.name(), perfTestService, config.getVersion());
		}
	}

	/**
	 * Finish the tests.(Scheduled by SpringTask)
	 * <p/>
	 * There are three types of test finish.
	 * <p/>
	 * <ul>
	 * <li>Abnormal test finish : when TPS is too low or too many errors occur</li>
	 * <li>User requested test finish : when user requested to finish the test</li>
	 * <li>Normal test finish : when the test reaches the planned duration and run
	 * count.</li>
	 * </ul>
	 */
	public void finishPeriodically() {
		doFinish(false);
	}

	protected void doFinish(boolean initial) {
		if (!initial && consoleManager.getConsoleInUse().isEmpty()) {
			return;
		}
		doFinish();
	}

	void doFinish() {
		for (PerfTest each : perfTestService.getAllAbnormalTesting()) {
			LOG.info("Terminate {}", each.getId());
			SingleConsole consoleUsingPort = consoleManager.getConsoleUsingPort(each.getPort());
			doTerminate(each, consoleUsingPort);
			cleanUp(each);
			notifyFinish(each, StopReason.TOO_MANY_ERRORS);
		}

		for (PerfTest each : perfTestService.getAllStopRequested()) {
			LOG.info("Stop test {}", each.getId());
			SingleConsole consoleUsingPort = consoleManager.getConsoleUsingPort(each.getPort());
			doCancel(each, consoleUsingPort);
			cleanUp(each);
			notifyFinish(each, StopReason.CANCEL_BY_USER);
		}

		for (PerfTest each : perfTestService.getAllTesting()) {
			SingleConsole consoleUsingPort = consoleManager.getConsoleUsingPort(each.getPort());
			if (isTestFinishCandidate(each, consoleUsingPort)) {
				doNormalFinish(each, consoleUsingPort);
				cleanUp(each);
				notifyFinish(each, StopReason.NORMAL);
			}
		}
	}

	/**
	 * Clean up distribution directory for the given perfTest.
	 *
	 * @param perfTest perfTest
	 */
	private void cleanUp(PerfTest perfTest) {
		perfTestService.cleanUpDistFolder(perfTest);
		perfTestService.cleanUpRuntimeOnlyData(perfTest);
	}

	/**
	 * Check if the given {@link PerfTest} is ready to finish.
	 *
	 * @param perfTest           perf test
	 * @param singleConsoleInUse singleConsole
	 * @return true if it's a finish candidate.
	 */
	private boolean isTestFinishCandidate(PerfTest perfTest, SingleConsole singleConsoleInUse) {
		// Give 5 seconds to be finished
		if (perfTest.isThresholdDuration()
				&& singleConsoleInUse.isCurrentRunningTimeOverDuration(perfTest.getDuration())) {
			LOG.debug(
					"Test {} is ready to finish. Current : {}, Planned : {}",
					new Object[]{perfTest.getTestIdentifier(), singleConsoleInUse.getCurrentRunningTime(),
							perfTest.getDuration()});
			return true;
		} else if (perfTest.isThresholdRunCount()
				&& singleConsoleInUse.getCurrentExecutionCount() >= perfTest.getTotalRunCount()) {
			LOG.debug("Test {} is ready to finish. Current : {}, Planned : {}",
					new Object[]{perfTest.getTestIdentifier(), singleConsoleInUse.getCurrentExecutionCount(),
							perfTest.getTotalRunCount()});
			return true;
		} else if (singleConsoleInUse instanceof NullSingleConsole) {
			LOG.debug("Test {} is ready to finish. Current : {}, Planned : {}",
					new Object[]{perfTest.getTestIdentifier(), singleConsoleInUse.getCurrentExecutionCount(),
							perfTest.getTotalRunCount()});
			return true;
		}

		return false;
	}

	/**
	 * Cancel the given {@link PerfTest}.
	 *
	 * @param perfTest           {@link PerfTest} to be canceled.
	 * @param singleConsoleInUse {@link SingleConsole} which is being used for the given
	 *                           {@link PerfTest}
	 */
	public void doCancel(PerfTest perfTest, SingleConsole singleConsoleInUse) {
		LOG.info("Cancel test {} by user request.", perfTest.getId());
		singleConsoleInUse.unregisterSampling();
		try {
			perfTestService.markProgressAndStatusAndFinishTimeAndStatistics(perfTest, CANCELED,
					"Stop requested by user");
		} catch (Exception e) {
			LOG.error("Error while canceling test {} : {}", perfTest.getId(), e.getMessage());
			LOG.debug("Details : ", e);
		}
		consoleManager.returnBackConsole(perfTest.getTestIdentifier(), singleConsoleInUse);
	}

	/**
	 * Terminate the given {@link PerfTest}.
	 *
	 * @param perfTest           {@link PerfTest} to be finished
	 * @param singleConsoleInUse {@link SingleConsole} which is being used for the given
	 *                           {@link PerfTest}
	 */
	public void doTerminate(PerfTest perfTest, SingleConsole singleConsoleInUse) {
		singleConsoleInUse.unregisterSampling();
		try {
			perfTestService.markProgressAndStatusAndFinishTimeAndStatistics(perfTest, Status.STOP_BY_ERROR,
					"Stopped by error");
		} catch (Exception e) {
			LOG.error("Error while terminating {} : {}", perfTest.getTestIdentifier(), e.getMessage());
			LOG.debug("Details : ", e);
		}
		consoleManager.returnBackConsole(perfTest.getTestIdentifier(), singleConsoleInUse);
	}

	/**
	 * Finish the given {@link PerfTest}.
	 *
	 * @param perfTest           {@link PerfTest} to be finished
	 * @param singleConsoleInUse {@link SingleConsole} which is being used for the given
	 *                           {@link PerfTest}
	 */
	public void doNormalFinish(PerfTest perfTest, SingleConsole singleConsoleInUse) {
		LOG.debug("PerfTest {} status - currentRunningTime {} ", perfTest.getId(),
				singleConsoleInUse.getCurrentRunningTime());
		singleConsoleInUse.unregisterSampling();
		try {
			// stop target host monitor
			if (perfTestService.hasTooManyError(perfTest)) {
				perfTestService.markProgressAndStatusAndFinishTimeAndStatistics(perfTest, Status.STOP_BY_ERROR,
						"[WARNING] The test is finished but contains too much errors(over 30% of total runs).");
			} else if (singleConsoleInUse.hasNoPerformedTest()) {
				perfTestService.markProgressAndStatusAndFinishTimeAndStatistics(perfTest, Status.STOP_BY_ERROR,
						"[WARNING] The test is finished but has no TPS.");
			} else {
				perfTestService.markProgressAndStatusAndFinishTimeAndStatistics(perfTest, Status.FINISHED,
						"The test is successfully finished.");
			}
		} catch (Exception e) {
			perfTestService.markStatusAndProgress(perfTest, Status.STOP_BY_ERROR, e.getMessage());
			LOG.error("Error while finishing {} : {}", perfTest.getTestIdentifier(), e.getMessage());
			LOG.debug("Details : ", e);
		}
		consoleManager.returnBackConsole(perfTest.getTestIdentifier(), singleConsoleInUse);
	}

	public PerfTestService getPerfTestService() {
		return perfTestService;
	}

	public AgentManager getAgentManager() {
		return agentManager;
	}

}
