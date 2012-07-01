package net.grinder.reservation;

import java.util.Set;

import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.util.Precondition;

public class ConsoleReservationRequest implements AgentProcessingListener {
	private final Integer port;
	private final Set<AgentReservationRequest> agentReserveRequests;

	public ConsoleReservationRequest(int port,
			Set<AgentReservationRequest> agentReserveRequests) {

		Precondition.isTrue(port >= 6000 && port <= 7000,
				"port should be b/w 6000~7000. But it is " + port);
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
	 * @return true if the agentIdentity is requested by this console
	 *         reservation request
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
