package org.ngrinder.perftest.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.mutable.MutableInt;
import org.ngrinder.agent.service.AgentManagerService;
import org.ngrinder.infra.annotation.TestOnlyController;
import org.ngrinder.infra.config.Config;
import org.ngrinder.region.service.RegionService;
import org.ngrinder.user.service.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

@TestOnlyController
public class MockPerfTestController extends PerfTestController {

	
	@Autowired
	private RegionService regionService;

	@Autowired
	private UserContext userContext;

	@Autowired
	private Config config;

	@PostConstruct
	public void init() {
		AgentManagerService agentManagerService = mock(AgentManagerService.class);
		List<String> regionList = regionService.getRegionList();
		Map<String, MutableInt> countMap = new HashMap<String, MutableInt>(1);
		countMap.put(config.getRegion(), new MutableInt(3));
		when(agentManagerService.getUserAvailableAgentCountMap(regionList, userContext.getCurrentUser())).thenReturn(countMap);
		ReflectionTestUtils.setField(this, "agentManagerService", agentManagerService);
	}
}
