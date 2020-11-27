/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ngrinder.perftest.controller;

import org.apache.commons.lang.mutable.MutableInt;
import org.ngrinder.agent.service.AgentService;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.hazelcast.HazelcastService;
import org.ngrinder.perftest.service.AgentManager;
import org.ngrinder.perftest.service.PerfTestService;
import org.ngrinder.perftest.service.TagService;
import org.ngrinder.region.service.RegionService;
import org.ngrinder.script.handler.ScriptHandlerFactory;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.user.service.UserContext;
import org.ngrinder.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Profile("unit-test")
@Component
public class MockPerfTestApiController extends PerfTestApiController {

	@Autowired
	private UserContext userContext;

	@Autowired
	private Config config;

	public MockPerfTestApiController(PerfTestService perfTestService, TagService tagService, AgentManager agentManager,
									 RegionService regionService, AgentService agentService, FileEntryService fileEntryService,
									 UserService userService, HazelcastService hazelcastService, ScriptHandlerFactory scriptHandlerFactory,
									 UserContext userContext, Config config) {
		super(perfTestService, tagService, agentManager, regionService, agentService, fileEntryService,
			userService, hazelcastService, scriptHandlerFactory, userContext, config);
	}

	@PostConstruct
	public void init() {
		AgentService agentService = mock(AgentService.class);
		Map<String, MutableInt> countMap = new HashMap<>(1);
		countMap.put(config.getRegion(), new MutableInt(3));
		when(agentService.getAvailableAgentCountMap(userContext.getCurrentUser().getUserId())).thenReturn(countMap);
		ReflectionTestUtils.setField(this, "agentService", agentService);
	}
}
