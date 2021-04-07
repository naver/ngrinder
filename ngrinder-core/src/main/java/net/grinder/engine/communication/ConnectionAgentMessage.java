package net.grinder.engine.communication;

import net.grinder.communication.Message;

public class ConnectionAgentMessage implements Message {
	private final String ip;
	private final String name;
	private final String subregion;
	private final int port;

	public ConnectionAgentMessage(String ip, String name, String subregion, int port) {
		this.ip = ip;
		this.name = name;
		this.port = port;
		this.subregion = subregion;
	}

	public String getIp() {
		return ip;
	}

	public String getName() {
		return name;
	}

	public String getSubregion() {
		return subregion;
	}

	public int getPort() {
		return port;
	}
}
