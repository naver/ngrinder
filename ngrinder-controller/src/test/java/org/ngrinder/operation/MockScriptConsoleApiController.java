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
package org.ngrinder.operation;

import org.ngrinder.agent.service.AgentService;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.hazelcast.HazelcastService;
import org.ngrinder.infra.plugin.PluginManager;
import org.ngrinder.operation.cotroller.ScriptConsoleApiController;
import org.ngrinder.perftest.service.AgentManager;
import org.ngrinder.perftest.service.ConsoleManager;
import org.ngrinder.perftest.service.PerfTestService;
import org.ngrinder.perftest.service.TagService;
import org.ngrinder.region.service.RegionService;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.user.service.UserContext;
import org.ngrinder.user.service.UserService;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("unit-test")
@Component
public class MockScriptConsoleApiController extends ScriptConsoleApiController {

	public MockScriptConsoleApiController(ApplicationContext applicationContext, AgentManager agentManager, AgentService agentService,
										  ConsoleManager consoleManager, PerfTestService perfTestService, FileEntryService fileEntryService,
										  UserService userService, RegionService regionService, PluginManager pluginManager,
										  TagService tagService, CacheManager cacheManager, UserContext userContext,
										  Config config, HazelcastService hazelcastService) {
		super(applicationContext, agentManager, agentService, consoleManager, perfTestService, fileEntryService,
			userService, regionService, pluginManager, tagService, cacheManager, userContext, config, hazelcastService);
	}
}
