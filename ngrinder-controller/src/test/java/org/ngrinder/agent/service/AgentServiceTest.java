package org.ngrinder.agent.service;

import net.grinder.engine.controller.AgentControllerIdentityImplementation;
import org.apache.commons.lang.StringUtils;
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
	 * 1. dedicated agent of recently used.
	 * 2. dedicated agent.
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

		List<AgentInfo> selectedDedicatedAgents = selectedAgents
			.stream()
			.filter(agentInfo -> StringUtils.isNotEmpty(agentInfo.getOwner()))
			.collect(toList());
		assertThat(selectedDedicatedAgents.size(), is(2));

		// Check if recently used agents are selected.
		assertTrue(selectedAgents.contains(createAgentInfo("test-agent-1")));
		assertTrue(selectedAgents.contains(createAgentInfo("test-agent-3")));
		assertTrue(selectedAgents.contains(createAgentInfo("test-agent-5")));

		// Add recently used agents.
		recentlyUsedAgents.add(createAgentInfo("test-agent-11", "test-region", TEST_USER_ID));
		recentlyUsedAgents.add(createAgentInfo("test-agent-14", "test-region", TEST_USER_ID));
		hazelcastService.put(DIST_MAP_NAME_RECENTLY_USED_AGENTS, TEST_USER_ID, recentlyUsedAgents);

		// Add owned agents for another test.
		allFreeAgents.add(createAgentInfo("test-agent-8", "test-region", "another-user"));
		allFreeAgents.add(createAgentInfo("test-agent-9", "test-region", "another-user"));
		allFreeAgents.add(createAgentInfo("test-agent-10", "test-region", "another-user"));
		allFreeAgents.add(createAgentInfo("test-agent-11", "test-region", TEST_USER_ID));
		allFreeAgents.add(createAgentInfo("test-agent-12", "test-region", TEST_USER_ID));
		allFreeAgents.add(createAgentInfo("test-agent-13", "test-region", TEST_USER_ID));
		allFreeAgents.add(createAgentInfo("test-agent-14", "test-region", TEST_USER_ID));
		allFreeAgents.add(createAgentInfo("test-agent-15", "test-region", TEST_USER_ID));

		selectedAgents = agentService.selectAgent(testUser, allFreeAgents, 3);

		assertThat(selectedAgents.size(), is(3));
		// Check if recently used owned agents are selected.
		assertTrue(selectedAgents.contains(createAgentInfo("test-agent-6")));
		assertTrue(selectedAgents.contains(createAgentInfo("test-agent-11")));
		assertTrue(selectedAgents.contains(createAgentInfo("test-agent-14")));

		hazelcastService.delete(DIST_MAP_NAME_RECENTLY_USED_AGENTS, TEST_USER_ID);
	}

	private Set<AgentInfo> getRecentlyUsedAgents() {
		Set<AgentInfo> cachedAgents = newHashSet();
		cachedAgents.add(createAgentInfo("test-agent-1", "test-region"));
		cachedAgents.add(createAgentInfo("test-agent-3", "test-region"));
		cachedAgents.add(createAgentInfo("test-agent-5", "test-region"));
		cachedAgents.add(createAgentInfo("test-agent-6", "test-region", TEST_USER_ID));
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
		allFreeAgents.add(createAgentInfo("test-agent-6", "test-region", TEST_USER_ID));
		allFreeAgents.add(createAgentInfo("test-agent-7", "test-region", TEST_USER_ID));
		allFreeAgents.add(createAgentInfo("test-agent-8", "test-region", "another-user"));
		allFreeAgents.add(createAgentInfo("test-agent-9", "test-region", "another-user"));
		allFreeAgents.add(createAgentInfo("test-agent-10", "test-region", "another-user"));
		return allFreeAgents;
	}

	private AgentInfo createAgentInfo(String name) {
		AgentInfo agentInfo = new AgentInfo();
		agentInfo.setIp("1.1.1.1");
		agentInfo.setName(name);
		return agentInfo;
	}

	private AgentInfo createAgentInfo(String name, String region) {
		AgentInfo agentInfo = new AgentInfo();
		agentInfo.setIp("1.1.1.1");
		agentInfo.setName(name);
		agentInfo.setRegion(region);
		return agentInfo;
	}

	private AgentInfo createAgentInfo(String name, String region, String owner) {
		AgentInfo agentInfo = createAgentInfo(name, region);
		AgentControllerIdentityImplementation identityImplementation = new AgentControllerIdentityImplementation("", "");
		identityImplementation.setOwner(owner);
		agentInfo.setAgentIdentity(identityImplementation);
		return agentInfo;
	}
}
