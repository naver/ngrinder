package org.ngrinder.listener;

import org.junit.Test;
import org.ngrinder.listener.AgentInitDataListener;
import org.ngrinder.listener.NGrinderHanitorAgent;

public class AgentListenerTest {
	private static final AgentInitDataListener agentInitDataListener = new AgentInitDataListener();
	private static final NGrinderHanitorAgent ngrinderHanitorAgent = new NGrinderHanitorAgent();

	@Test
	public void testAgentInitDataListener() {
		agentInitDataListener.contextInitialized(null);
		agentInitDataListener.contextDestroyed(null);
	}

	@Test
	public void testNGrinderHanitorAgent() {
		ngrinderHanitorAgent.contextInitialized(null);
		ngrinderHanitorAgent.contextDestroyed(null);
	}
}
