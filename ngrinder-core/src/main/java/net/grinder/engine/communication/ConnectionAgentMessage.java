package net.grinder.engine.communication;

import net.grinder.communication.Message;

public class ConnectionAgentMessage implements Message {
	private final String ip;
	private final String name;
	private final int port;

	public ConnectionAgentMessage(String ip, String name, int port) {
		this.ip = ip;
		this.name = name;
		this.port = port;
	}

	public String getIp() {
		return ip;
	}

	public String getName() {
		return name;
	}

	public int getPort() {
		return port;
	}
}
