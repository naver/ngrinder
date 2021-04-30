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

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.grinder.SingleConsole;
import net.grinder.StopReason;
import net.grinder.common.GrinderProperties;
import net.grinder.console.model.ConsoleProperties;
import net.grinder.util.ListenerHelper;
import net.grinder.util.ListenerSupport;
import net.grinder.util.UnitUtils;
import org.apache.commons.io.FileUtils;
import org.ngrinder.agent.service.AgentService;
import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.common.exception.PerfTestPrepareException;
import org.ngrinder.extension.OnTestLifeCycleRunnable;
import org.ngrinder.extension.OnTestSamplingRunnable;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.hazelcast.HazelcastService;
import org.ngrinder.infra.plugin.PluginManager;
import org.ngrinder.infra.schedule.ScheduledTaskService;
import org.ngrinder.infra.webhook.plugin.NGrinderWebhookPlugin;
import org.ngrinder.infra.webhook.service.WebhookConfigService;
import org.ngrinder.infra.webhook.service.WebhookService;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Status;
import org.ngrinder.perftest.model.NullSingleConsole;
import org.ngrinder.perftest.service.samplinglistener.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static java.time.Instant.*;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static net.grinder.util.FileUtils.*;
import static org.apache.commons.lang.ObjectUtils.defaultIfNull;
import static org.ngrinder.common.constant.CacheConstants.DIST_MAP_NAME_MONITORING;
import static org.ngrinder.common.constant.CacheConstants.DIST_MAP_NAME_SAMPLING;
import static org.ngrinder.common.constant.ClusterConstants.PROP_CLUSTER_SAFE_DIST;
import static org.ngrinder.common.util.AccessUtils.getSafe;
import static org.ngrinder.common.util.LoggingUtils.format;
import static org.ngrinder.common.util.TypeConvertUtils.cast;
import static org.ngrinder.model.Status.*;

/**
 * {@link PerfTest} run scheduler.
 * <p/>
 * This class is responsible to execute/finish the performance test. The job is
 * started from {@link #doStart()}  and {@link #doFinish()} method. These
 * methods are scheduled by Spring Task.
 *
 * @since 3.0
 */
@Profile("production")
@Component
@RequiredArgsConstructor
public class PerfTestRunnable implements ControllerConstants {

	private static final Logger LOG = LoggerFactory.getLogger(PerfTestRunnable.class);

	@Getter
	private final PerfTestService perfTestService;

	@Getter
	private final AgentManager agentManager;

	private final ConsoleManager consoleManager;

	private final PluginManager pluginManager;

	private final Config config;

	private final ScheduledTaskService scheduledTaskService;

	private final HazelcastService hazelcastService;

	@Getter
	private final AgentService agentService;

	private final WebhookService webhookService;

	private final WebhookConfigService webhookConfigService;

	private Runnable startRunnable;

	private Runnable finishRunnable;

	@PostConstruct
	public void init() {
		// Clean up db first.
		doFinish(true);

		this.startRunnable = this::startPeriodically;
		scheduledTaskService.addFixedDelayedScheduledTask(startRunnable, PERFTEST_RUN_FREQUENCY_MILLISECONDS);
		this.finishRunnable = this::finishPeriodically;
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
			// Test is reserved, but it isn't yet going to run test right now.
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
		Instant now = now();
		Instant scheduledTime = cast(defaultIfNull(test.getScheduledTime(), now));
		return (now.truncatedTo(MINUTES).getEpochSecond() - scheduledTime.truncatedTo(MINUTES).getEpochSecond()) >= 0;
	}

	/**
	 * Check the free agent availability for the given {@link PerfTest}.
	 *
	 * @param test {@link PerfTest}
	 * @return true if enough agents
	 */
	protected boolean hasEnoughFreeAgents(PerfTest test) {
		int size = agentService.getAllAttachedFreeApprovedAgentsForUser(test.getCreatedBy().getUserId(), test.getRegion()).size();
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
			GrinderProperties grinderProperties = perfTestService.prepareTest(perfTest);
			singleConsole = startConsole(perfTest);
			startAgentsOn(perfTest, grinderProperties, checkCancellation(singleConsole));
			distributeFileOn(perfTest, checkCancellation(singleConsole));
			singleConsole.setReportPath(perfTestService.getReportFileDirectory(perfTest));
			runTestOn(perfTest, grinderProperties, checkCancellation(singleConsole));
		} catch (PerfTestPrepareException ex) {
			LOG.error(format(perfTest, "Error while preparing a test : {} ", ex.getMessage()));
			perfTestService.markProgressAndStatusAndFinishTimeAndStatistics(perfTest, Status.STOP_BY_ERROR,
				ex.getMessage());
		} catch (SingleConsoleCancellationException ex) {
			// In case of error, mark the occurs error on perftest.
			LOG.error(format(perfTest, "Error while preparing a single console : {} ", ex.getMessage()));
			doCancel(perfTest, requireNonNull(singleConsole));
			notifyFinish(perfTest, StopReason.CANCEL_BY_USER);
		} catch (Exception ex) {
			// In case of error, mark the occurs error on perftest.
			LOG.error(format(perfTest, "Error while executing a test: {}", ex.getMessage()));
			LOG.debug("Stack Trace is : ", ex);
			doTerminate(perfTest, singleConsole, ex.getMessage());
			notifyFinish(perfTest, StopReason.ERROR_WHILE_PREPARE);
		}
	}

	/**
	 * Delete cached distribution files, These are already in the agent cache directory.
	 *
	 * @param distDir						   Directory containing files to be distributed for testing.
	 * @param distFiles 					   Required files for currently running test.
	 * @param distFilesDigest				   Required file's digest for currently running test.
	 * @param agentCachedDistFilesDigestList   Digest of files in each agent cache directory.
	 *
	 * */
	private void deleteCachedDistFiles(File distDir,
									   List<File> distFiles,
									   Set<String> distFilesDigest,
									   List<Set<String>> agentCachedDistFilesDigestList) {
		Set<String> cachedDistFilesDigest = extractCachedDistFilesDigest(distFilesDigest, agentCachedDistFilesDigestList);

		distFiles
			.stream()
			.filter(file -> cachedDistFilesDigest.contains(getFileDigest(distDir, file)))
			.forEach(FileUtils::deleteQuietly);
	}

	/**
	 * Extract non cached distribution files for send to each agents.
	 *
	 * @param distFilesDigest					Required file's digest for currently running test.
	 * @param agentCachedDistFilesDigestList    Digest of files in each agent cache directory.
	 *
	 * */
	private Set<String> extractCachedDistFilesDigest(Set<String> distFilesDigest,
													 List<Set<String>> agentCachedDistFilesDigestList) {
		return distFilesDigest
			.stream()
			.filter(distFileDigest -> agentCachedDistFilesDigestList
				.stream()
				.allMatch(agentCachedDistFilesDigest -> agentCachedDistFilesDigest.contains(distFileDigest)))
			.collect(toSet());
	}

	private void prepareFileDistribution(PerfTest perfTest, SingleConsole singleConsole) throws IOException {
		File distDir = perfTestService.getDistributionPath(perfTest);
		List<File> distFiles = getAllFilesInDirectory(distDir);

		Set<String> distFilesDigest = getFilesDigest(distDir, distFiles);

		singleConsole.sendDistFilesDigestToAgents(distFilesDigest);
		LOG.info(format(perfTest, "Send digest of distribution files to agent for refresh agent's cache directory."));
		deleteCachedDistFiles(distDir, distFiles, distFilesDigest, singleConsole.getAgentCachedDistFilesDigestList());
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
	void distributeFileOn(final PerfTest perfTest, SingleConsole singleConsole) throws IOException {
		prepareFileDistribution(perfTest, singleConsole);
		// Distribute files
		perfTestService.markStatusAndProgress(perfTest, DISTRIBUTE_FILES, "All necessary files are being distributed.");

		ListenerSupport<SingleConsole.FileDistributionListener> listener = ListenerHelper.create();
		final long safeThreshold = getSafeTransmissionThreshold();

		listener.add(new SingleConsole.FileDistributionListener() {
			@Override
			public void distributed(String fileName) {
				perfTestService.markProgress(perfTest, " - " + fileName);
			}

			@SuppressWarnings("ConstantConditions")
			@Override
			public boolean start(File dir, boolean safe) {
				if (safe) {
					perfTestService.markProgress(perfTest, "Safe file distribution mode is enabled.");
					return safe;
				}
				long sizeOfDirectory = FileUtils.sizeOfDirectory(dir);
				if (sizeOfDirectory > safeThreshold) {
					perfTestService.markProgress(perfTest, "The total size of distributed files is over "
							+ UnitUtils.byteCountToDisplaySize(safeThreshold) + ".\n- Safe file distribution mode is enabled by force.");
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
		int agentCount = perfTest.getAgentCount();
		perfTestService.markStatusAndProgress(perfTest, START_AGENTS, getSafe(agentCount)
				+ " agents are starting.");
		agentService.runAgent(perfTest, singleConsole, grinderProperties, getSafe(agentCount));
		singleConsole.waitUntilAgentPrepared(agentCount);
		perfTestService.markStatusAndProgress(perfTest, START_AGENTS_FINISHED, getSafe(agentCount)
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
		for (OnTestLifeCycleRunnable run : pluginManager.getEnabledModulesByClass(OnTestLifeCycleRunnable.class, getDefaultTestLifeCyclePlugins())) {
			run.start(perfTest, perfTestService, config.getVersion());
		}

		// Run test
		perfTestService.markStatusAndProgress(perfTest, START_TESTING, "The test is ready to start.");
		// Add listener to detect abnormal condition and mark the perfTest

		singleConsole.addListener(stopReason -> {
			PerfTest fetchedPerftest = perfTestService.getOne(perfTest.getId());
			if (fetchedPerftest.getStatus().isStoppable()) {
				perfTestService.markAbnormalTermination(perfTest, stopReason);
				LOG.error(format(perfTest, "Abnormal test due to {}", stopReason.name()));
			}
		});

		long startTime = singleConsole.startTest(grinderProperties);
		perfTest.setStartTime(ofEpochMilli(startTime));
		addSamplingListeners(perfTest, singleConsole);
		perfTestService.markStatusAndProgress(perfTest, TESTING, "The test is started.");
		singleConsole.startSampling();
		LOG.info(format(perfTest, "Sampling is started"));
	}

	protected void addSamplingListeners(final PerfTest perfTest, final SingleConsole singleConsole) {
		// Add SamplingLifeCycleListener
		singleConsole.addSamplingLifeCycleListener(new PerfTestSamplingCollectorListener(singleConsole,
				perfTest.getId(), perfTestService, scheduledTaskService));
		singleConsole.addSamplingLifeCycleListener(new AgentLostDetectionListener(singleConsole, perfTest,
				perfTestService, scheduledTaskService));
		List<OnTestSamplingRunnable> testSamplingPlugins = pluginManager.getEnabledModulesByClass
				(OnTestSamplingRunnable.class, asList(new MonitorCollectorPlugin(config, scheduledTaskService,
					perfTestService, perfTest.getId()), new TooManyErrorCheckPlugin()));
		singleConsole.addSamplingLifeCycleListener(new PluginRunListener(testSamplingPlugins, singleConsole,
				perfTest, perfTestService));
		singleConsole.addSamplingLifeCycleListener(new AgentDieHardListener(singleConsole, perfTest, perfTestService,
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
		for (OnTestLifeCycleRunnable run : pluginManager.getEnabledModulesByClass(OnTestLifeCycleRunnable.class, getDefaultTestLifeCyclePlugins())) {
			run.finish(perfTest, reason.name(), perfTestService, config.getVersion());
		}
	}

	private List<OnTestLifeCycleRunnable> getDefaultTestLifeCyclePlugins() {
		return ImmutableList.of(new NGrinderWebhookPlugin(webhookService, webhookConfigService));
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

		if (initial) {
			String message = "Test is canceled by system";
			for (PerfTest each : perfTestService.getAllProgressing()) {
				SingleConsole consoleUsingPort = consoleManager.getConsoleUsingPort(each.getPort());
				doCancel(each, consoleUsingPort, message);
				LOG.info(format(each, message, each.getId()));
				cleanUp(each);
				notifyFinish(each, StopReason.CANCEL_BY_SYSTEM);
			}
		}

		doFinish();
	}

	void doFinish() {
		for (PerfTest each : perfTestService.getAllAbnormalTesting()) {
			SingleConsole consoleUsingPort = consoleManager.getConsoleUsingPort(each.getPort());
			doTerminate(each, consoleUsingPort);
			LOG.info(format(each, "Abnormal test is terminated.", each.getId()));
			cleanUp(each);
			notifyFinish(each, StopReason.TOO_MANY_ERRORS);
		}

		for (PerfTest each : perfTestService.getAllStopRequested()) {
			SingleConsole consoleUsingPort = consoleManager.getConsoleUsingPort(each.getPort());
			doCancel(each, consoleUsingPort);
			LOG.info(format(each, "Test is Stopped."));
			cleanUp(each);
			notifyFinish(each, StopReason.CANCEL_BY_USER);
		}

		for (PerfTest each : perfTestService.getAllTesting()) {
			SingleConsole consoleUsingPort = consoleManager.getConsoleUsingPort(each.getPort());
			if (isTestFinishCandidate(each, consoleUsingPort)) {
				doNormalFinish(each, consoleUsingPort);
				LOG.info(format(each, "Test is finished.", each.getId()));
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
		hazelcastService.delete(DIST_MAP_NAME_MONITORING, perfTest.getId());
		hazelcastService.delete(DIST_MAP_NAME_SAMPLING, perfTest.getId());
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
			LOG.debug(format(perfTest,
					"Test is ready to finish. Current : {}, Planned : {}",
				singleConsoleInUse.getCurrentRunningTime(), perfTest.getDuration()));
			return true;
		} else if (perfTest.isThresholdRunCount()
				&& singleConsoleInUse.getCurrentExecutionCount() >= perfTest.getTotalRunCount()) {
			LOG.debug(format(perfTest, "Test is ready to finish. Current : {}, Planned : {}",
				singleConsoleInUse.getCurrentExecutionCount(), perfTest.getTotalRunCount()));
			return true;
		} else if (singleConsoleInUse instanceof NullSingleConsole) {
			LOG.debug(format(perfTest, "Test is ready to finish. Current : {}, Planned : {}",
				singleConsoleInUse.getCurrentExecutionCount(), perfTest.getTotalRunCount()));
			return true;
		}
		return false;
	}

	public void doCancel(PerfTest perfTest, SingleConsole singleConsoleInUse) {
		doCancel(perfTest, singleConsoleInUse, "Stop requested by user");
	}

	/**
	 * Cancel the given {@link PerfTest}.
	 *
	 * @param perfTest           {@link PerfTest} to be canceled
	 * @param singleConsoleInUse {@link SingleConsole} which is being used for the given
	 * @param message            cancel message
	 */
	public void doCancel(PerfTest perfTest, SingleConsole singleConsoleInUse, String message) {
		LOG.info(format(perfTest, "Cancel test."));
		singleConsoleInUse.unregisterSampling();
		LOG.info(format(perfTest, "Sampling is stopped"));
		try {
			perfTestService.markProgressAndStatusAndFinishTimeAndStatistics(perfTest, CANCELED, message);
		} catch (Exception e) {
			LOG.error(format(perfTest,"Error while canceling test : {}", e.getMessage()));
			LOG.debug(format(perfTest, "Details : "), e);
		}
		consoleManager.returnBackConsole(perfTest.getTestIdentifier(), singleConsoleInUse);
	}

	public void doTerminate(PerfTest perfTest, SingleConsole singleConsoleInUse) {
		doTerminate(perfTest, singleConsoleInUse, "");
	}

	/**
	 * Terminate the given {@link PerfTest}.
	 *
	 * @param perfTest           {@link PerfTest} to be finished
	 * @param singleConsoleInUse {@link SingleConsole} which is being used for the given
	 *                           {@link PerfTest}
	 * @param errorMessage       error message
	 */
	public void doTerminate(PerfTest perfTest, SingleConsole singleConsoleInUse, String errorMessage) {
		if (singleConsoleInUse != null) {
			singleConsoleInUse.unregisterSampling();
			LOG.info(format(perfTest, "Sampling is stopped"));
		}
		String progressMessage = "Stopped by error";

		if (!errorMessage.isEmpty()) {
			progressMessage += "\n" + errorMessage;
		}

		try {
			perfTestService.markProgressAndStatusAndFinishTimeAndStatistics(perfTest,
				Status.STOP_BY_ERROR, progressMessage);
		} catch (Exception e) {
			LOG.error(format(perfTest, "Error while terminating : {}", e.getMessage()));
			LOG.debug(format(perfTest, "Details : "), e);
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
		LOG.debug(format(perfTest, "PerfTest status - currentRunningTime {} ", singleConsoleInUse.getCurrentRunningTime()));
		singleConsoleInUse.unregisterSampling();
		LOG.info(format(perfTest, "Sampling is stopped"));
		try {
			// stop target host monitor
			if (perfTestService.hasTooManyError(perfTest)) {
				perfTestService.markProgressAndStatusAndFinishTimeAndStatistics(perfTest, FINISHED_WITH_WARNING,
						"[WARNING] The test is finished but contains too much errors(over 30% of total runs).");
			} else if (singleConsoleInUse.hasNoPerformedTest()) {
				perfTestService.markProgressAndStatusAndFinishTimeAndStatistics(perfTest, Status.FINISHED_WITH_WARNING,
						"[WARNING] The test is finished but has no TPS.");
			} else {
				perfTestService.markProgressAndStatusAndFinishTimeAndStatistics(perfTest, Status.FINISHED,
						"The test is successfully finished.");
			}
		} catch (Exception e) {
			perfTestService.markStatusAndProgress(perfTest, Status.STOP_BY_ERROR, e.getMessage());
			LOG.error(format(perfTest, "Error while finishing : {}", e.getMessage()));
			LOG.debug(format(perfTest, "Details : "), e);
		}
		consoleManager.returnBackConsole(perfTest.getTestIdentifier(), singleConsoleInUse);
	}

}
