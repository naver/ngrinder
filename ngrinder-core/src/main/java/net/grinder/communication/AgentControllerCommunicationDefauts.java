package net.grinder.communication;

public class AgentControllerCommunicationDefauts {
	private AgentControllerCommunicationDefauts() {
	}

	/** Default console host. Bind to all interfaces by default. */
	public static final String DEFAULT_AGENT_CONTROLLER_SERVER_HOST = "";

	/** Default console port. */
	public static final int DEFAULT_AGENT_CONTROLLER_SERVER_PORT = 6370;

	/** Minimum value for ports. */
	public static final int MIN_PORT = 1;

	/** Maximum value for ports. */
	public static final int MAX_PORT = 0xFFFF;
}
