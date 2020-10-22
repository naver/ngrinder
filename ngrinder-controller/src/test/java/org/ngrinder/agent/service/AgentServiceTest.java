package org.ngrinder.agent.service;

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.infra.hazelcast.HazelcastService;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.model.User;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;
import static org.ngrinder.common.constant.CacheConstants.DIST_MAP_NAME_RECENTLY_USED_AGENTS;
import static org.ngrinder.common.util.CollectionUtils.newHashSet;

public class AgentServiceTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private AgentService agentService;

	@Autowired
	private HazelcastService hazelcastService;

	private static final String TEST_USER_ID = "test-user";

	/**
	 *
	 * Priority of agent selection.
	 * 1. owned agent of recently used.
	 * 2. owned agent.
	 * 3. public agent of recently used.
	 * 4. public agent.
	 *
	 * */
	@Test
	public void selectAgentsTest() {
		Set<AgentInfo> recentlyUsedAgents = getRecentlyUsedAgents();
		hazelcastService.put(DIST_MAP_NAME_RECENTLY_USED_AGENTS, TEST_USER_ID, recentlyUsedAgents);

		User testUser = new User();
		testUser.setUserId(TEST_USER_ID);

		Set<AgentInfo> allFreeAgents = getFreeAgents();
		Set<AgentInfo> selectedAgents = agentService.selectAgent(testUser, allFreeAgents, 5);

		assertThat(selectedAgents.size(), is(5));

		List<AgentInfo> selectedOwnedAgents = selectedAgents
			.stream()
			.filter(agentInfo -> agentInfo.getRegion().contains("owned"))
			.collect(toList());

		assertThat(selectedOwnedAgents.size(), is(2));

		// Check if recently used agents are selected.
		assertTrue(selectedAgents.contains(createAgentInfo("test-agent-1", "test-region")));
		assertTrue(selectedAgents.contains(createAgentInfo("test-agent-3", "test-region")));
		assertTrue(selectedAgents.contains(createAgentInfo("test-agent-5", "test-region")));

		// Add recently used agents.

		recentlyUsedAgents.add(createAgentInfo("test-agent-11", "test-region_owned_test-user"));
		recentlyUsedAgents.add(createAgentInfo("test-agent-14", "test-region_owned_test-user"));
		hazelcastService.put(DIST_MAP_NAME_RECENTLY_USED_AGENTS, TEST_USER_ID, recentlyUsedAgents);

		// Add owned agents for another test.
		allFreeAgents.add(createAgentInfo("test-agent-8", "test-region_owned_test-user"));
		allFreeAgents.add(createAgentInfo("test-agent-9", "test-region_owned_test-user"));
		allFreeAgents.add(createAgentInfo("test-agent-10", "test-region_owned_test-user"));
		allFreeAgents.add(createAgentInfo("test-agent-11", "test-region_owned_test-user"));
		allFreeAgents.add(createAgentInfo("test-agent-12", "test-region_owned_test-user"));
		allFreeAgents.add(createAgentInfo("test-agent-13", "test-region_owned_test-user"));
		allFreeAgents.add(createAgentInfo("test-agent-14", "test-region_owned_test-user"));
		allFreeAgents.add(createAgentInfo("test-agent-15", "test-region_owned_test-user"));

		selectedAgents = agentService.selectAgent(testUser, allFreeAgents, 3);

		assertThat(selectedAgents.size(), is(3));
		// Check if recently used owned agents are selected.
		assertTrue(selectedAgents.contains(createAgentInfo("test-agent-6", "test-region_owned_test-user")));
		assertTrue(selectedAgents.contains(createAgentInfo("test-agent-11", "test-region_owned_test-user")));
		assertTrue(selectedAgents.contains(createAgentInfo("test-agent-14", "test-region_owned_test-user")));

		hazelcastService.delete(DIST_MAP_NAME_RECENTLY_USED_AGENTS, TEST_USER_ID);
	}

	private Set<AgentInfo> getRecentlyUsedAgents() {
		Set<AgentInfo> cachedAgents = newHashSet();
		cachedAgents.add(createAgentInfo("test-agent-1", "test-region"));
		cachedAgents.add(createAgentInfo("test-agent-3", "test-region"));
		cachedAgents.add(createAgentInfo("test-agent-5", "test-region"));
		cachedAgents.add(createAgentInfo("test-agent-6", "test-region_owned_test-user"));
		return cachedAgents;
	}

	private Set<AgentInfo> getFreeAgents() {
		Set<AgentInfo> allFreeAgents = newHashSet();
		// public agents
		allFreeAgents.add(createAgentInfo("test-agent-1", "test-region"));
		allFreeAgents.add(createAgentInfo("test-agent-2", "test-region"));
		allFreeAgents.add(createAgentInfo("test-agent-3", "test-region"));
		allFreeAgents.add(createAgentInfo("test-agent-4", "test-region"));
		allFreeAgents.add(createAgentInfo("test-agent-5", "test-region"));

		// owned agents
		allFreeAgents.add(createAgentInfo("test-agent-6", "test-region_owned_test-user"));
		allFreeAgents.add(createAgentInfo("test-agent-7", "test-region_owned_test-user"));
		return allFreeAgents;
	}

	private AgentInfo createAgentInfo(String name, String region) {
		AgentInfo agentInfo = new AgentInfo();
		agentInfo.setIp("1.1.1.1");
		agentInfo.setName(name);
		agentInfo.setRegion(region);
		return agentInfo;
	}
}
