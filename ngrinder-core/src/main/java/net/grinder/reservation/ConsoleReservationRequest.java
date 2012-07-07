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
package net.grinder.reservation;

import static org.ngrinder.common.util.Preconditions.checkTrue;

import java.util.Set;

import net.grinder.common.processidentity.AgentIdentity;

public class ConsoleReservationRequest implements AgentProcessingListener {
	private final Integer port;
	private final Set<AgentReservationRequest> agentReserveRequests;

	public ConsoleReservationRequest(int port, Set<AgentReservationRequest> agentReserveRequests) {
		checkTrue(port >= 6000 && port <= 7000, "port should be b/w 6000~7000. But it is " + port);
		this.port = port;
		this.agentReserveRequests = agentReserveRequests;
	}

	public int getAllAssignedAgentCount() {
		int count = 0;
		for (AgentReservationRequest agentReserveRequest : agentReserveRequests) {
			count += agentReserveRequest.getAssigendAgentCount();
		}
		return count;
	}

	public int getAllRequiredAgentCount() {
		int count = 0;
		for (AgentReservationRequest agentReserveRequest : agentReserveRequests) {
			count += agentReserveRequest.getRequiredAgentCount();
		}
		return count;
	}

	public boolean isAllAgentAssigned() {
		for (AgentReservationRequest agentReserveRequest : agentReserveRequests) {
			if (!agentReserveRequest.isAllAgentAssigned())
				return false;
		}
		return true;
	}

	private AgentReservationRequest getAgentReserveRequestByCategory(String category) {
		for (AgentReservationRequest agentReserveRequest : agentReserveRequests) {
			if (agentReserveRequest.isSameCategory(category)) {
				return agentReserveRequest;
			}
		}
		return null;
	}

	public Integer getPort() {
		return port;
	}

	/**
	 * Add assigned agent.
	 * 
	 * @return true if the agentIdentity is requested by this console reservation request
	 */
	public boolean added(AgentIdentity agentIdentity) {
		String category = "default";
		String[] split = category.split("_");
		// If there is category exists.
		if (split.length >= 2) {
			category = split[0];
		}
		AgentReservationRequest agentReserveRequestByCategory = getAgentReserveRequestByCategory(category);
		// We didn't requested it before.
		if (agentReserveRequestByCategory == null) {
			return false;
		}

		agentReserveRequestByCategory.addAssignedAgent(agentIdentity);
		return true;
	}
}
