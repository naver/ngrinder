package net.grinder.console.communication;

import java.util.EventListener;

public interface ConnectionAgentCommunicationListener extends EventListener {
	void onConnectionAgentCommunication(int usingPort, String ip, int port);
}
