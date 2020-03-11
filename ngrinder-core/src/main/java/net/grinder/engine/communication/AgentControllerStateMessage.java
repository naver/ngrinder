package net.grinder.engine.communication;

import lombok.Getter;
import net.grinder.communication.Address;
import net.grinder.communication.AddressAwareMessage;
import net.grinder.communication.CommunicationException;
import net.grinder.message.console.AgentControllerState;
import net.grinder.messages.console.AgentAddress;

public class AgentControllerStateMessage implements AddressAwareMessage {
	public static final AgentControllerStateMessage EMPTY = new AgentControllerStateMessage(null);

	private static final long serialVersionUID = 1293801923713L;

	@Getter
	private final AgentControllerState state;
	@Getter
	private transient AgentAddress address;

	public AgentControllerStateMessage(AgentControllerState state) {
		this.state = state;
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
