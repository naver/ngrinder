package org.ngrinder.perftest.service;

import net.grinder.SingleConsole;
import net.grinder.common.GrinderProperties;

import org.ngrinder.common.util.ThreadUtil;
import org.ngrinder.infra.annotation.TestOnlyComponent;
import org.ngrinder.model.PerfTest;

/**
 * Mock PerfTest which disable spring task schedule.
 * 
 * @author JunHo Yoon
 */
@TestOnlyComponent
public class MockPerfTestRunnableForCanclation extends PerfTestRunnable {
	private Runnable runnable;
	private int ignoreCount;
	
	@Override
	public void startTest() {
		// No Nothing.
	};
	
	
	public void testDrive() {
		super.startTest();
	};
	
	
	@Override
	protected boolean hasEnoughFreeAgents(PerfTest test) {
		return true;
	}

	@Override
	void startAgentsOn(PerfTest perfTest, GrinderProperties grinderProperties, SingleConsole singleConsole) {
		ThreadUtil.sleep(1000);
	}

	int calledCount = 0;

	@Override
	SingleConsole checkCancellation(SingleConsole singleConsole) {
		if (++calledCount > ignoreCount) {
			if (this.runnable != null) {
				this.runnable.run();
			}
		}
		return super.checkCancellation(singleConsole);
	}

	public void setRunnable(Runnable runnable, int ignoreCount) {
		this.runnable = runnable;
		this.ignoreCount = ignoreCount;
	}

}
