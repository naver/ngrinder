package net.grinder.engine.process;

import net.grinder.engine.process.GrinderProcess.ThreadContexts;

/**
 * ThreadContext updater in JUnit context.
 * 
 * This class is responsible to update the Grinder thread context values when it's not executed in
 * the Grinder agent.
 * 
 * @author JunHo Yoon
 * @since 3.2.1
 * 
 */
public class JUnitThreadContextUpdater {
	private ThreadContexts m_threadContexts;

	/**
	 * Constructor.
	 * 
	 * @param threadContexts	threadContexts
	 */
	public JUnitThreadContextUpdater(ThreadContexts threadContexts) {
		this.m_threadContexts = threadContexts;
	}

	/**
	 * Set run count in thread context.
	 * 
	 * @param count	count
	 */
	public void setRunCount(int count) {
		m_threadContexts.get().setCurrentRunNumber(count);
	}
	
}
