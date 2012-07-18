/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.grinder.console.communication;

import java.util.Set;

import net.grinder.common.GrinderProperties;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.message.console.AgentControllerState;

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
	 * Get the all available agents.
	 * 
	 * @return agent list
	 */
	public Set<AgentIdentity> getAllAgents();

}