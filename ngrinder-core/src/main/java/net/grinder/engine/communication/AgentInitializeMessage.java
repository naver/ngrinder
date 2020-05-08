package net.grinder.engine.communication;

import lombok.Getter;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.communication.Address;
import net.grinder.communication.AddressAwareMessage;
import net.grinder.communication.CommunicationException;
import net.grinder.messages.console.AgentAddress;

public class AgentInitializeMessage implements AddressAwareMessage {
	public static final AgentInitializeMessage EMPTY = new AgentInitializeMessage(null);

	private static final long serialVersionUID = 1293801923888L;

	@Getter
	private final AgentIdentity agentIdentity;
	@Getter
	private transient AgentAddress address;

	public AgentInitializeMessage(AgentIdentity agentIdentity) {
		this.agentIdentity = agentIdentity;
	}

	@Override
	public void setAddress(Address address) throws CommunicationException {
		try {
			this.address = (AgentAddress) address;
		} catch (ClassCastException e) {
			throw new CommunicationException("Not an agent process address", e);
		}
	}
}
