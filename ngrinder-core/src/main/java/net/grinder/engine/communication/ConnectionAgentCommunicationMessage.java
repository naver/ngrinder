package net.grinder.engine.communication;

import net.grinder.communication.Message;

public class ConnectionAgentCommunicationMessage implements Message {
	private final int usingPort;
	private final String ip;
	private final int port;

	public ConnectionAgentCommunicationMessage(int usingPort, String ip, int port) {
		this.usingPort = usingPort;
		this.ip = ip;
		this.port = port;
	}

	public int getUsingPort() {
		return usingPort;
	}

	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}
}
