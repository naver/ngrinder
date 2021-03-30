package net.grinder.engine.communication;

import net.grinder.communication.Message;

public class ConnectionAgentMessage implements Message {
	private final String ip;
	private final String name;
	private final String region;
	private final int port;

	public ConnectionAgentMessage(String ip, String name, String region, int port) {
		this.ip = ip;
		this.name = name;
		this.port = port;
		this.region = region;
	}

	public String getIp() {
		return ip;
	}

	public String getName() {
		return name;
	}

	public String getRegion() {
		return region;
	}

	public int getPort() {
		return port;
	}
}
