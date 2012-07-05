package net.grinder.console.communication;

import java.util.Set;

import net.grinder.common.GrinderProperties;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.message.console.AgentControllerState;

public interface AgentProcessControl {

	/**
	 * Signal the worker processes to start.
	 * 
	 * @param properties
	 *            Properties that override the agent's local properties.
	 */
	public abstract void startAgent(Set<AgentIdentity> agents, GrinderProperties properties);

	public abstract void stopAgent(AgentIdentity agentIdentity);

	public abstract int getNumberOfLiveAgents();

	public abstract Set<AgentIdentity> getAgents(AgentControllerState state, int count);

	public abstract Set<AgentIdentity> getAllAgents();

}