package org.ngrinder.perftest.model;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
	private static final Map<String, Object> EMPTY_RESULT = new HashMap<String, Object>(1);
	
	static {
		EMPTY_RESULT.put("test_time", 0);
	}

	/**
	 * Constructor.
	 */
	public NullSingleConsole() {
		super(0);
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

	@Override
	public Map<String, Object> getStatictisData() {
		if (getCurrentRunningTime() > 0) {
			return super.getStatictisData();
		} else {
			return getNullStatictisData();
		}
	}
	
	private Map<String, Object> getNullStatictisData() {
		return EMPTY_RESULT;
	}
}
