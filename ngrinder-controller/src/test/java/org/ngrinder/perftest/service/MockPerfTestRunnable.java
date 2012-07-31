package org.ngrinder.perftest.service;

import org.ngrinder.infra.annotation.TestOnlyComponent;

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
}
