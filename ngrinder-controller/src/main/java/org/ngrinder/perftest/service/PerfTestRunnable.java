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
package org.ngrinder.perftest.service;

import static org.ngrinder.model.Status.CANCELED;
import static org.ngrinder.model.Status.DISTRIBUTE_FILES;
import static org.ngrinder.model.Status.DISTRIBUTE_FILES_FINISHED;
import static org.ngrinder.model.Status.START_AGENTS;
import static org.ngrinder.model.Status.START_AGENTS_FINISHED;
import static org.ngrinder.model.Status.START_CONSOLE;
import static org.ngrinder.model.Status.START_TESTING;
import static org.ngrinder.model.Status.TESTING;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.grinder.SingleConsole;
import net.grinder.SingleConsole.ConsoleShutdownListener;
import net.grinder.StopReason;
import net.grinder.common.GrinderProperties;
import net.grinder.console.model.ConsoleProperties;

import org.ngrinder.agent.model.AgentInfo;
import org.ngrinder.chart.service.MonitorAgentService;
import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.common.util.DateUtil;
import org.ngrinder.extension.OnTestStartRunnable;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.plugin.PluginManager;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Status;
import org.ngrinder.monitor.MonitorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * {@link PerfTest} test running run scheduler.
 * 
 * This class is responsible to execute the performance test which is ready to execute. Mostly this
 * class is started from {@link #startTest()} method. This method is scheduled by Spring Task.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@Component
public class PerfTestRunnable implements NGrinderConstants {

	private static final Logger LOG = LoggerFactory.getLogger(PerfTestRunnable.class);

	@Autowired
	private PerfTestService perfTestService;

	@Autowired
	private ConsoleManager consoleManager;

	@Autowired
	private AgentManager agentManager;

	@Autowired
	private MonitorAgentService monitorDataService;

	@Autowired
	private PluginManager pluginManager;

	@Autowired
	private Config config;
	// wait 10 seconds until agents start the test running.
	private static final int WAIT_TEST_START_SECOND = 10000;

	/**
	 * Scheduled method for test execution.
	 */
	@Scheduled(fixedDelay = PERFTEST_RUN_FREQUENCY_MILLISECONDS)
	public void startTest() {
		// Block if the count of testing exceed the limit
		if (!perfTestService.canExecuteTestMore()) {
			// LOG MORE
			List<PerfTest> currentlyRunningTests = perfTestService.getCurrentlyRunningTest();
			LOG.debug("current running test is {}. so no tests start to run", currentlyRunningTests.size());
			for (PerfTest perfTest : currentlyRunningTests) {
				LOG.trace("- " + perfTest);
			}
			return;
		}
		// Find out next ready perftest
		PerfTest runCandidate = perfTestService.getPerfTestCandiate();
		if (runCandidate == null) {
			return;
		}

		// schedule test
		Date schedule = runCandidate.getScheduledTime();
		if (schedule != null
						&& !DateUtil.compareDateEndWithMinute(schedule, new Date(System.currentTimeMillis()))) {
			// this test project is reserved,but it isn't yet going to run test
			// right now.
			return;
		}

		// If agent is not enough...
		int size = agentManager.getAllFreeApprovedAgents().size();
		if (runCandidate.getAgentCount() > size) {
			perfTestService.markProgress(runCandidate,
							"The test is tried to execute but there is not enough free agents.\n- Current free agent size : "
											+ size + "  / Requested : " + runCandidate.getAgentCount() + "\n");
			return;
		}

		// In case of too many trial, cancel running.
		if (runCandidate.getTestTrialCount() > PERFTEST_MAXIMUM_TRIAL_COUNT) {
			perfTestService.markPerfTestError(runCandidate,
							"The test is tried to execute but there is not enough free agents.\n- Current free agent size : "
											+ size + "  / Requested : " + runCandidate.getAgentCount() + "\n");
			return;
		}
		doTest(runCandidate);
	}

	/**
	 * Run given test.
	 * 
	 * If fails, it marks STOP_ON_ERROR in the given {@link PerfTest} status
	 * 
	 * @param perfTest
	 *            perftest instance;
	 */
	public void doTest(PerfTest perfTest) {
		SingleConsole singleConsole = null;
		try {
			singleConsole = startConsole(perfTest);
			GrinderProperties grinderProperties = perfTestService.getGrinderProperties(perfTest);
			startAgentsOn(perfTest, grinderProperties, singleConsole);
			distributeFileOn(perfTest, grinderProperties, singleConsole);
			singleConsole.setReportPath(perfTestService.getReportFileDirectory(perfTest.getId()));
			runTestOn(perfTest, grinderProperties, singleConsole);
		} catch (Exception e) {
			// In case of error, mark the occurs error on perftest.
			LOG.error("Error while excuting test", e);
			perfTestService.markPerfTestError(perfTest, e.getMessage());
		}
	}

	void runTestOn(final PerfTest perfTest, GrinderProperties grinderProperties,
					final SingleConsole singleConsole) {
		// start target monitor
		for (OnTestStartRunnable run : pluginManager.getEnabledModulesByClass(OnTestStartRunnable.class)) {
			run.start(perfTest, perfTestService, config.getVesion());
		}

		Set<AgentInfo> agents = new HashSet<AgentInfo>();
		List<String> targetIPList = perfTest.getTargetHostIP();
		for (String targetIP : targetIPList) {
			AgentInfo targetServer = new AgentInfo();
			targetServer.setIp(targetIP);
			targetServer.setPort(MonitorConstants.DEFAULT_AGENT_PORT);
			agents.add(targetServer);
		}
		// use perf test id as key for the set of target server.
		monitorDataService.addMonitorTarget("PerfTest-" + perfTest.getId(), agents);

		// Run test
		perfTestService.changePerfTestStatus(perfTest, START_TESTING);
		singleConsole.addListener(new ConsoleShutdownListener() {
			@Override
			public void readyToStop(StopReason stopReason) {
				perfTestService.markAbromalTermination(perfTest, stopReason);
			}
		});
		long startTime = singleConsole.startTest(grinderProperties);
		perfTestService.setRecodingStarting(perfTest, startTime);
		perfTestService.changePerfTestStatus(perfTest, TESTING);
	}

	void distributeFileOn(PerfTest perfTest, GrinderProperties grinderProperties, SingleConsole singleConsole) {
		// Distribute files
		perfTestService.changePerfTestStatus(perfTest, DISTRIBUTE_FILES);
		singleConsole.distributeFiles(perfTestService.prepareDistribution(perfTest));
		perfTestService.changePerfTestStatus(perfTest, DISTRIBUTE_FILES_FINISHED);
	}

	void startAgentsOn(PerfTest perfTest, GrinderProperties grinderProperties, SingleConsole singleConsole) {
		perfTestService.changePerfTestStatus(perfTest, START_AGENTS);
		agentManager.runAgent(singleConsole, grinderProperties, perfTest.getAgentCount());
		singleConsole.waitUntilAgentConnected(perfTest.getAgentCount());
		perfTestService.changePerfTestStatus(perfTest, START_AGENTS_FINISHED);
	}

	SingleConsole startConsole(PerfTest perfTest) {
		perfTestService.changePerfTestStatus(perfTest, START_CONSOLE);
		// get available consoles.
		ConsoleProperties consoleProperty = perfTestService.createConsoleProperties(perfTest);
		SingleConsole singleConsole = consoleManager.getAvailableConsole(consoleProperty);
		// increase trial count
		singleConsole.start();
		perfTestService.markPerfTestConsoleStart(perfTest, singleConsole.getConsolePort(),
						perfTest.getTestTrialCount());
		return singleConsole;
	}

	public void notifyFinsish(PerfTest perfTest, StopReason reason) {
		for (OnTestStartRunnable run : pluginManager.getEnabledModulesByClass(OnTestStartRunnable.class)) {
			run.finish(perfTest, reason.name(), perfTestService, config.getVesion());
		}
	}

	/**
	 * Scheduled method for test finish.
	 */
	@Scheduled(fixedDelay = PERFTEST_RUN_FREQUENCY_MILLISECONDS)
	public void finishTest() {

		for (PerfTest each : perfTestService.getAbnoramlTestingPerfTest()) {
			SingleConsole consoleUsingPort = consoleManager.getConsoleUsingPort(each.getPort());
			doTerminate(each, consoleUsingPort);
			notifyFinsish(each, StopReason.TOO_MANY_ERRORS);
		}

		for (PerfTest each : perfTestService.getStopRequestedPerfTest()) {
			SingleConsole consoleUsingPort = consoleManager.getConsoleUsingPort(each.getPort());
			doStop(each, consoleUsingPort);
			notifyFinsish(each, StopReason.STOP_BY_USER);
		}

		for (PerfTest each : perfTestService.getTestingPerfTest()) {
			SingleConsole consoleUsingPort = consoleManager.getConsoleUsingPort(each.getPort());
			doFinish(each, consoleUsingPort);
			notifyFinsish(each, StopReason.NORMAL);
		}
	}

	/**
	 * Terminate test.
	 * 
	 * @param perfTest
	 *            {@link PerfTest} to be finished
	 * @param singleConsoleInUse
	 *            {@link SingleConsole} which is being using for {@link PerfTest}
	 */
	public void doStop(PerfTest perfTest, SingleConsole singleConsoleInUse) {
		perfTestService.markProgressAndStatusAndFinishTimeAndStatistics(perfTest, CANCELED,
						"Stop requested by user");
		monitorDataService.removeMonitorAgents("PerfTest-" + perfTest.getId());
		if (singleConsoleInUse != null) {
			consoleManager.returnBackConsole(singleConsoleInUse);
			return;
		}
	}

	/**
	 * Terminate test.
	 * 
	 * @param perfTest
	 *            {@link PerfTest} to be finished
	 * @param singleConsoleInUse
	 *            {@link SingleConsole} which is being using for {@link PerfTest}
	 */
	public void doTerminate(PerfTest perfTest, SingleConsole singleConsoleInUse) {
		perfTestService.markProgressAndStatus(perfTest, Status.STOP_ON_ERROR, "Stoped by error");
		monitorDataService.removeMonitorAgents("PerfTest-" + perfTest.getId());
		if (singleConsoleInUse != null) {
			// need to finish test as error
			consoleManager.returnBackConsole(singleConsoleInUse);
			return;
		}
	}

	/**
	 * Finish test.
	 * 
	 * @param perfTest
	 *            {@link PerfTest} to be finished
	 * @param singleConsoleInUse
	 *            {@link SingleConsole} which is being using for {@link PerfTest}
	 */
	public void doFinish(PerfTest perfTest, SingleConsole singleConsoleInUse) {
		// FIXME... it should found abnormal test status..
		if (singleConsoleInUse == null) {
			LOG.error("There is no console found for test:{}", perfTest);
			// need to finish test as error
			perfTestService.changePerfTestStatus(perfTest, Status.STOP_ON_ERROR);
			monitorDataService.removeMonitorAgents("PerfTest-" + perfTest.getId());
			return;
		}
		long startLastingTime = System.currentTimeMillis() - singleConsoleInUse.getStartTime();
		// because It will take some seconds to start testing sometimes , if the
		// test is not started
		// after some seconds, will set it as finished.
		if (singleConsoleInUse.isAllTestFinished() && startLastingTime > WAIT_TEST_START_SECOND) {
			// stop target host monitor
			perfTestService.markProgressAndStatusAndFinishTimeAndStatistics(perfTest,
							((isAbormalFinishing(perfTest)) ? Status.STOP_ON_ERROR : Status.FINISHED), "");
			monitorDataService.removeMonitorAgents("PerfTest-" + perfTest.getId());
			consoleManager.returnBackConsole(singleConsoleInUse);
		}
	}

	public boolean isAbormalFinishing(PerfTest perfTest) {
		if ("D".equals(perfTest.getThreshold())) {
			if ((new Date().getTime() - perfTest.getStartTime().getTime()) < perfTest.getDuration()) {
				return true;
			}
		}
		return false;
	}
}
