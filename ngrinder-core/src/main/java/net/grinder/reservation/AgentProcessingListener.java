package net.grinder.reservation;

import net.grinder.common.processidentity.AgentIdentity;

public interface AgentProcessingListener {
	public boolean added(AgentIdentity agentIndentity);
}
