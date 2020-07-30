package org.ngrinder.agent.store;

import lombok.RequiredArgsConstructor;
import org.ngrinder.infra.hazelcast.HazelcastService;
import org.ngrinder.model.AgentInfo;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.ngrinder.common.constant.CacheConstants.DIST_MAP_NAME_AGENT;
import static org.ngrinder.common.util.TypeConvertUtils.cast;

@Component
@RequiredArgsConstructor
public class AgentInfoStore {

	private final HazelcastService hazelcastService;

	public AgentInfo getAgentInfo(Object key) {
		return hazelcastService.get(DIST_MAP_NAME_AGENT, key);
	}

	public void deleteAgentInfo(Object key) {
		hazelcastService.delete(DIST_MAP_NAME_AGENT, key);
	}

	public void updateAgentInfo(Object key, AgentInfo agentInfo) {
		hazelcastService.put(DIST_MAP_NAME_AGENT, key, agentInfo);
	}

	public List<AgentInfo> getAllAgentInfo() {
		return cast(hazelcastService.getValuesAsList(DIST_MAP_NAME_AGENT));
	}
}
