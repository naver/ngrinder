package org.ngrinder.perftest.model;

import java.io.File;

import net.grinder.SingleConsole;

/**
 * Null Object for {@link SingleConsole}
 * 
 * @author JunHo Yoon
 * 
 */
public class NullSingleConsole extends SingleConsole {

	public NullSingleConsole() {
		super(0);
	}

	@Override
	public long getCurrentRunningTime() {
		return Long.MAX_VALUE;
	}

	@Override
	public long getCurrentTestsCount() {
		return Long.MAX_VALUE;
	}

	@Override
	public boolean isTooManyError() {
		return true;
	}

	@Override
	public void unregisterSampling() {
		// Do nothing
	}

	@Override
	public void sendStopMessageToAgents() {
		// Do nothing
	}

	@Override
	public void shutdown() {
		// Do nothing
	}

	@Override
	public void waitUntilAgentConnected(int size) {
		// Do nothing
	}

	@Override
	public void start() {
		// Do nothing
	}

	@Override
	public void distributeFiles(File filePath) {
		// Do nothing
	}
}
