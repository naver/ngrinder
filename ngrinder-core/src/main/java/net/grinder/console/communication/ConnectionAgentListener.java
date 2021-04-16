package net.grinder.console.communication;

import java.util.EventListener;

public interface ConnectionAgentListener extends EventListener {
	void onConnectionAgentMessage(String ip, String name, String region, int port);
}
