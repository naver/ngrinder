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
import org.ngrinder.agent.service.AgentManagerService;
import org.ngrinder.infra.config.Config;
import org.ngrinder.user.service.UserContext;
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
		when(agentManagerService.getAvailableAgentCountMap(userContext.getCurrentUser())).thenReturn(countMap);
		ReflectionTestUtils.setField(this, "agentManagerService", agentManagerService);
	}
}
