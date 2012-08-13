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

import static org.ngrinder.perftest.model.Status.CANCELED;
import static org.ngrinder.perftest.model.Status.DISTRIBUTE_FILES;
import static org.ngrinder.perftest.model.Status.DISTRIBUTE_FILES_FINISHED;
import static org.ngrinder.perftest.model.Status.START_AGENTS;
import static org.ngrinder.perftest.model.Status.START_AGENTS_FINISHED;
import static org.ngrinder.perftest.model.Status.START_CONSOLE;
import static org.ngrinder.perftest.model.Status.START_CONSOLE_FINISHED;
import static org.ngrinder.perftest.model.Status.START_TESTING;
import static org.ngrinder.perftest.model.Status.TESTING;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.grinder.SingleConsole;
import net.grinder.common.GrinderProperties;
import net.grinder.console.model.ConsoleProperties;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ngrinder.agent.model.AgentInfo;
import org.ngrinder.chart.service.MonitorDataService;
import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.perftest.model.PerfTest;
import org.ngrinder.perftest.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * {@link PerfTest} test running run scheduler.
 * 
 * This class is responsible to execute the performance test which is ready to execute. Mostly this class is started
 * from {@link #startTest()} method. This method is scheduled by Spring Task.
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
	private MonitorDataService monitorDataService;

	// wait 30 seconds until agents start the test running.
	private static final int WAIT_TEST_START_SECOND = 30000;

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

		// In case of too many trial, cancel running.
		if (runCandidate.getTestTrialCount() > PERFTEST_MAXIMUM_TRIAL_COUNT) {
			LOG.error("The {} test is canceled because it has too many test execution errors",
					runCandidate.getTestName());
			runCandidate.setTestErrorCause(Status.READY);
			runCandidate.setTestErrorStackTrace("The test is canceled because it has too many test execution errors");
			perfTestService.savePerfTest(runCandidate, CANCELED);
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
			markPerfTestError(perfTest, singleConsole, e);
		}
	}

	/**
	 * Mark test error on {@link PerfTest} instance
	 * 
	 * @param perfTest
	 *            {@link PerfTest}
	 * @param singleConsole
	 *            console in use
	 * @param e
	 *            exception occurs.
	 */
	void markPerfTestError(PerfTest perfTest, SingleConsole singleConsole, Exception e) {
		// Leave last status as test error cause
		perfTest.setTestErrorCause(perfTest.getStatus());
		perfTest.setTestErrorStackTrace(ExceptionUtils.getFullStackTrace(e));
		perfTestService.savePerfTest(perfTest, Status.STOP_ON_ERROR);
	}

	void runTestOn(PerfTest perfTest, GrinderProperties grinderProperties, SingleConsole singleConsole) {
		// start target monitor
		Set<AgentInfo> agents = new HashSet<AgentInfo>();
		AgentInfo agent = new AgentInfo();
		agent.setIp(perfTest.getTargetHosts());
		monitorDataService.addMonitorAgents(perfTest.getTargetHosts(), agents);

		// Run test
		perfTestService.savePerfTest(perfTest, START_TESTING);
		grinderProperties.setProperty(GRINDER_PROP_TEST_ID, "test_" + perfTest.getId());
		long startTime = singleConsole.startTest(grinderProperties);
		perfTest.setStartTime(new Date(startTime));
		perfTestService.savePerfTest(perfTest, TESTING);
	}

	void distributeFileOn(PerfTest perfTest, GrinderProperties grinderProperties, SingleConsole singleConsole) {
		// Distribute files
		perfTestService.savePerfTest(perfTest, DISTRIBUTE_FILES);
		singleConsole.distributeFiles(perfTestService.prepareDistribution(perfTest));
		perfTestService.savePerfTest(perfTest, DISTRIBUTE_FILES_FINISHED);
	}

	void startAgentsOn(PerfTest perfTest, GrinderProperties grinderProperties, SingleConsole singleConsole) {
		perfTestService.savePerfTest(perfTest, START_AGENTS);
		agentManager.runAgent(singleConsole, grinderProperties, perfTest.getAgentCount());
		singleConsole.waitUntilAgentConnected(perfTest.getAgentCount());
		perfTestService.savePerfTest(perfTest, START_AGENTS_FINISHED);
	}

	SingleConsole startConsole(PerfTest perfTest) {
		perfTestService.savePerfTest(perfTest, START_CONSOLE);
		// get available consoles.
		ConsoleProperties consoleProperty = perfTestService.createConsoleProperties(perfTest);
		SingleConsole singleConsole = consoleManager.getAvailableConsole(consoleProperty);
		// increase trial count
		perfTest.setTestTrialCount(perfTest.getTestTrialCount() + 1);
		perfTest.setPort(singleConsole.getConsolePort());
		singleConsole.start();
		perfTestService.savePerfTest(perfTest, START_CONSOLE_FINISHED);
		return singleConsole;
	}

	/**
	 * Scheduled method for test finish.
	 */
	@Scheduled(fixedDelay = PERFTEST_RUN_FREQUENCY_MILLISECONDS)
	public void finishTest() {
		List<PerfTest> finishCandiate = perfTestService.getTestingPerfTest();
		for (PerfTest each : finishCandiate) {
			SingleConsole consoleUsingPort = consoleManager.getConsoleUsingPort(each.getPort());
			doFinish(each, consoleUsingPort);
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
			perfTestService.savePerfTest(perfTest, Status.STOP_ON_ERROR);
			return;
		}
		long startLastingTime = System.currentTimeMillis() - singleConsoleInUse.getStartTime();
		// because It will take some seconds to start testing sometimes , if the
		// test is not started
		// after some seconds, will set it as finished.
		if (singleConsoleInUse.isAllTestFinished() && startLastingTime > WAIT_TEST_START_SECOND) {
			// stop target host monitor
			monitorDataService.removeMonitorAgents(perfTest.getTargetHosts());
			PerfTest resultTest = perfTestService.updatePerfTestAfterTestFinish(perfTest);
			consoleManager.returnBackConsole(singleConsoleInUse);
			perfTestService.savePerfTest(resultTest, Status.FINISHED);
		}
	}
}
