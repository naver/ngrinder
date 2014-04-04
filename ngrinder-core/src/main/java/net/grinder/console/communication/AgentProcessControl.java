// Copyright (C) 2000 - 2012 Philip Aston
// All rights reserved.
//
// This file is part of The Grinder software distribution. Refer to
// the file LICENSE which is part of The Grinder distribution for
// licensing details. The Grinder distribution is available on the
// Internet at http://grinder.sourceforge.net/
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
// FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
// COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
// OF THE POSSIBILITY OF SUCH DAMAGE.
package net.grinder.console.communication;

import net.grinder.common.GrinderProperties;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.console.communication.AgentProcessControlImplementation.Listener;
import net.grinder.message.console.AgentControllerState;
import org.ngrinder.monitor.controller.model.SystemDataModel;

import java.util.Set;

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
	 * @param agents		agent list to be started.
	 * @param properties	Properties that override the agent's local properties.
	 *
	 */
	public void startAgent(Set<AgentIdentity> agents, GrinderProperties properties);

	/**
	 * Stop agent.
	 *
	 * @param agentIdentity	agent to be stopped
	 *
	 */
	public void stopAgent(AgentIdentity agentIdentity);

	/**
	 * Get the number of agents.
	 *
	 * @return available agent's count
	 */
	public int getNumberOfLiveAgents();

	/**
	 * Get the agents for given state and count.
	 *
	 * @param state	current state of agent
	 * @param count	the count of agent to be get
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
	 * @param agentIdentity	agent identity
	 * @return agent version
	 */
	String getAgentVersion(AgentIdentity agentIdentity);


	/**
	 * Get {@link SystemDataModel} for given {@link AgentIdentity}.
	 *
	 * @param agentIdentity	agent identity
	 * @return {@link SystemDataModel} instance if available. null otherwise
	 */
	SystemDataModel getSystemDataModel(AgentIdentity agentIdentity);

	/**
	 * Get agent controller state for the given agent identity.
	 *
	 * @param agentIdentity	agent identity
	 * @return {@link AgentControllerState} member
	 */
	AgentControllerState getAgentControllerState(AgentIdentity agentIdentity);

	/**
	 * Get agent connecting port for the given agent identity.
	 *
	 * @param agentIdentity	agent identity
	 * @return {@link AgentControllerState} member
	 */
	int getAgentConnectingPort(AgentIdentity agentIdentity);

	/**
	 * Add agent status update listener.
	 *
	 * @param listener	listener.
	 */
	public void addListener(Listener listener);
}