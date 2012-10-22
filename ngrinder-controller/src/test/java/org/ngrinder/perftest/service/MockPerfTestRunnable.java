package org.ngrinder.perftest.service;

import static org.junit.Assert.fail;
import static org.ngrinder.model.Status.START_AGENTS;
import static org.ngrinder.model.Status.START_AGENTS_FINISHED;
import net.grinder.SingleConsole;
import net.grinder.common.GrinderProperties;

import org.ngrinder.infra.annotation.TestOnlyComponent;
import org.ngrinder.model.PerfTest;

/**
 * Mock PerfTest which disable spring task schedule.
 * 
 * @author JunHo Yoon
 */
@TestOnlyComponent
public class MockPerfTestRunnable extends PerfTestRunnable {

	@Override
	public void startTest() {
		super.startTest();
	}

	@Override
	void startAgentsOn(PerfTest perfTest, GrinderProperties grinderProperties, SingleConsole singleConsole) {
		perfTestService.markStatusAndProgress(perfTest, START_AGENTS, perfTest.getAgentCount()
						+ " agents are starting.");
		agentManager.runAgent(perfTest.getLastModifiedUser(), singleConsole, grinderProperties, perfTest.getAgentCount());
		perfTestService.markStatusAndProgress(perfTest, START_AGENTS_FINISHED, perfTest.getAgentCount()
						+ " agents are started.");
	}
}
