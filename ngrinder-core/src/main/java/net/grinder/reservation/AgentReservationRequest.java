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

import static org.ngrinder.common.util.Preconditions.checkNotEmpty;
import static org.ngrinder.common.util.Preconditions.checkNotZero;

import java.util.HashSet;
import java.util.Set;

import net.grinder.common.processidentity.AgentIdentity;

/**
 * Agent assign request information.
 * 
 * @author JunHo Yoon
 */
public class AgentReservationRequest {
	private final String category;
	private final int requiredAgentCount;
	private final Set<AgentIdentity> assignedAgents = new HashSet<AgentIdentity>();

	public AgentReservationRequest(String category, int requiredAgentCount) {
		checkNotEmpty(category, "category should not be emtpy");
		checkNotZero(requiredAgentCount, "required count of agents should be not 0.");
		this.category = category;
		this.requiredAgentCount = requiredAgentCount;
	}

	public String getCategory() {
		return category;
	}

	public boolean isSameCategory(String category) {
		if (this.category == null || category == null) {
			return false;
		}
		return this.category.equals(category);
	}

	public void addAssignedAgent(AgentIdentity agentIdentity) {
		getAssignedAgents().add(agentIdentity);
	}

	public Set<AgentIdentity> getAssignedAgents() {
		return assignedAgents;
	}

	public int getRequiredAgentCount() {
		return requiredAgentCount;
	}

	public int getAssigendAgentCount() {
		return assignedAgents.size();
	}

	public boolean isAllAgentAssigned() {
		return requiredAgentCount <= assignedAgents.size();
	}

}
