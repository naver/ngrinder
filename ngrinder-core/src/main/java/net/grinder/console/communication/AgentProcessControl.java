/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package net.grinder.console.communication;

import java.util.Set;

import net.grinder.common.GrinderProperties;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.console.communication.AgentProcessControlImplementation.Listener;
import net.grinder.message.console.AgentControllerState;

import org.ngrinder.monitor.controller.model.SystemDataModel;

/**
 * AgentProcessControl interface.
 *
 * This class is core of handling agent action.
 *
 * @author JunHo Yoon
 * @since 3.0
 *
 */
public interface AgentProcessControl {

	/**
	 * Signal the worker processes to start.
	 *
	 * @param agents
	 *            agent list to be started.
	 * @param properties
	 *            Properties that override the agent's local properties.
	 *
	 */
	public void startAgent(Set<AgentIdentity> agents, GrinderProperties properties);

	/**
	 * Stop agent.
	 *
	 * @param agentIdentity
	 *            agent to be stopped
	 *
	 */
	public void stopAgent(AgentIdentity agentIdentity);

	/**
	 * Get the number of agents.
	 *
	 * @return avalable agent's count
	 */
	public int getNumberOfLiveAgents();

	/**
	 * Get the agents for given state and count.
	 *
	 * @param state
	 *            current state of agent
	 * @param count
	 *            the count of agent to be get
	 * @return agent list
	 */
	public Set<AgentIdentity> getAgents(AgentControllerState state, int count);

	/**
	 * Get all available agents.
	 *
	 * @return agent list
	 */
	public Set<AgentIdentity> getAllAgents();

	/**
	 * Get agent version for given {@link AgentIdentity}.
	 *
	 * @param agentIdentity
	 *            agent identity
	 * @return agent version
	 */
	String getAgentVersion(AgentIdentity agentIdentity);


	/**
	 * Get {@link JavaDataModel} for given {@link AgentIdentity}.
	 *
	 * @param agentIdentity
	 *            agent identity
	 * @return {@link JavaDataModel} instance if available. null otherwise
	 */
	SystemDataModel getSystemDataModel(AgentIdentity agentIdentity);

	/**
	 * Get agent controller state for the given agent identity.
	 *
	 * @param agentIdentity
	 *            agent identity
	 * @return {@link AgentControllerState} member
	 */
	AgentControllerState getAgentControllerState(AgentIdentity agentIdentity);

	/**
	 * Get agent connecting port for the given agent identity.
	 *
	 * @param agentIdentity
	 *            agent identity
	 * @return {@link AgentControllerState} member
	 */
	int getAgentConnectingPort(AgentIdentity agentIdentity);

	/**
	 * Add agent status update listener.
	 *
	 * @param listener
	 *            listener.
	 */
	public void addListener(Listener listener);
}