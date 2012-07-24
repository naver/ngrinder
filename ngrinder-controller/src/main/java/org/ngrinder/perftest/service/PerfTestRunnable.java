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

import java.util.List;

import net.grinder.SingleConsole;

import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.perftest.model.PerfTest;
import org.ngrinder.perftest.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * perf test run scheduler.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@Component
@Transactional
public class PerfTestRunnable implements NGrinderConstants {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(PerfTestRunnable.class);

	@Autowired
	private PerfTestService perfTestService;

	@Autowired
	private ConsoleManager consoleManager;

	@Autowired
	private AgentManager agentManager;

	/**
	 * Scheduled method for test execution.
	 */
	@Scheduled(fixedDelay = PERFTEST_RUN_FREQUENCY_MILLISECONDS)
	@Transactional
	public void startTest() {
		PerfTest runCandidate = perfTestService.getPerfTestCandiate();
		if (runCandidate == null) {
			return;
		}
		// In case of too many trial, cancel running.
		if (runCandidate.getTestTrialCount() > PERFTEST_MAXIMUM_TRIAL_COUNT) {
			perfTestService.savePerfTest(runCandidate, Status.CANCELED);
			return;
		}
		doTest(runCandidate);
	}

	/**
	 * Run given test.
	 * 
	 * @param perfTest
	 *            perftest instance;
	 */
	public void doTest(PerfTest perfTest) {
		SingleConsole singleConsole = consoleManager.getAvailableConsole();
		singleConsole.start();
		// increase trial count
		perfTest.setTestTrialCount(perfTest.getTestTrialCount() + 1);

		// Start agents
		perfTestService.savePerfTest(perfTest, Status.WAITING_AGENT);
		agentManager.runAgent(singleConsole, null, perfTest.getAgentCount());
		singleConsole.waitUntilAgentConnected(perfTest.getAgentCount());

		// Distribute files
		perfTestService.savePerfTest(perfTest, Status.DISTRIBUTE_FILES);
		singleConsole.distributeFiles(perfTestService.prepareDistribution(perfTest));

		// Run test
		singleConsole.startTest(perfTestService.getGrinderProperties(perfTest));
		perfTest.setStatus(Status.TESTING);
		perfTestService.savePerfTest(perfTest);

	}

	/**
	 * Scheduled method for test finish.
	 */
	@Scheduled(fixedDelay = PERFTEST_RUN_FREQUENCY_MILLISECONDS)
	@Transactional
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
		if (singleConsoleInUse.isAllTestFinished()) {
			perfTestService.savePerfTest(perfTest, Status.FINISHED);
		}
	}
	


}
