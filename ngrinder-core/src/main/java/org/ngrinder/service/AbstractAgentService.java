package org.ngrinder.service;

import org.apache.commons.lang.mutable.MutableInt;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.model.User;

import java.util.List;
import java.util.Map;

/**
 * This class is created to guarantee the backward compatibility for IAgentManagerService.
 *
 * @since 3.5.0
 */
public abstract class AbstractAgentService implements IAgentManagerService {

	@Override
	@Deprecated
	public List<AgentInfo> getAllVisible() {
		return getAllActive();
	}

	@Override
	@Deprecated
	public AgentInfo getOne(String ip, String name) {
		return getAgent(ip, name);
	}

	@Override
	@Deprecated
	public Map<String, MutableInt> getAvailableAgentCountMap(User user) {
		return getAvailableAgentCountMap(user.getUserId());
	}

	@Override
	@Deprecated
	public int getReadyAgentCount(User user, String targetRegion) {
		return getReadyAgentCount(user.getUserId(), targetRegion);
	}

	@Override
	@Deprecated
	public String createKey(AgentInfo agentInfo) {
		return agentInfo.getAgentKey();
	}
}
