package org.ngrinder.perftest.service;

import org.ngrinder.infra.annotation.TestOnlyComponent;

@TestOnlyComponent
public class MockConsoleManager extends ConsoleManager {
	@Override
	protected int getConsolePortBase() {
		return 8000;
	}

	@Override
	public long getMaxWaitingMiliSecond() {
		return 3000;
	}
}
