package net.grinder.engine.communication;

import net.grinder.communication.Message;

/**
 * Message for update agent
 * 
 * @author JunHo Yoon
 * 
 */
public class UpdateAgentGrinderMessage implements Message {

	private static final long serialVersionUID = 3218379141994562444L;
	private final String version;
	private final byte[] binary;

	public UpdateAgentGrinderMessage(String version, byte[] binary) {
		this.version = version;
		this.binary = binary;
	}

	public String getVersion() {
		return version;
	}

	public byte[] getBinary() {
		return binary;
	}

}
