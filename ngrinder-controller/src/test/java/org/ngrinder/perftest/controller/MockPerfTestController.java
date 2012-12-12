package org.ngrinder.perftest.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.mutable.MutableInt;
import org.ngrinder.agent.service.AgentManagerService;
import org.ngrinder.infra.annotation.TestOnlyController;
import org.ngrinder.infra.config.Config;
import org.ngrinder.user.service.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

@TestOnlyController
public class MockPerfTestController extends PerfTestController {

	@Autowired
	private UserContext userContext;

	@Autowired
	private Config config;

	@PostConstruct
	public void init() {
		AgentManagerService agentManagerService = mock(AgentManagerService.class);
		Map<String, MutableInt> countMap = new HashMap<String, MutableInt>(1);
		countMap.put(config.getRegion(), new MutableInt(3));
		when(agentManagerService.getUserAvailableAgentCountMap(userContext.getCurrentUser())).thenReturn(countMap);
		ReflectionTestUtils.setField(this, "agentManagerService", agentManagerService);
	}
}
