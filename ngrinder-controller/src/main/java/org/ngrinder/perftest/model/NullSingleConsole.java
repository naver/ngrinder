package org.ngrinder.perftest.model;

import java.io.File;

import net.grinder.SingleConsole;

/**
 * Null Object for {@link SingleConsole}.<br/>
 * 
 * This class is for invalidating default {@link SingleConsole} behavior.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public class NullSingleConsole extends SingleConsole {
	
	public static final NullSingleConsole NUll_CONSOLE = new NullSingleConsole();

	/**
	 * Constructor.
	 */
	public NullSingleConsole() {
		super(1);
	}

	@Override
	public long getCurrentRunningTime() {
		return Long.MAX_VALUE;
	}

	@Override
	public long getCurrentExecutionCount() {
		return Long.MAX_VALUE;
	}

	@Override
	public boolean hasTooManyError() {
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
