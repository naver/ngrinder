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
package org.ngrinder.agent.service;

import java.util.Set;

import lombok.RequiredArgsConstructor;

import net.grinder.console.communication.AgentProcessControlImplementation.AgentStatus;

import org.ngrinder.extension.OnPeriodicWorkingAgentCheckRunnable;
import org.ngrinder.infra.plugin.PluginManager;
import org.ngrinder.infra.schedule.ScheduledTaskService;
import org.ngrinder.perftest.service.AgentManager;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Agent periodic check service.
 * <p/>
 * This class runs the plugins implementing
 * {@link OnPeriodicWorkingAgentCheckRunnable}.
 * <p/>
 * It's separated from {@link AgentService} to get rid of cyclic
 * injection.
 *
 * @since 3.1.2
 */
@Service
@RequiredArgsConstructor
public class PeriodicWorkingAgentCheckService implements Runnable {

	private final PluginManager pluginManager;

	private final AgentManager agentManager;

	private final ScheduledTaskService scheduledTaskService;

	@PostConstruct
	public void init() {
		scheduledTaskService.addFixedDelayedScheduledTaskInTransactionContext(this, 2000);
	}

	@Override
	public void run() {
		Set<AgentStatus> workingAgents = agentManager.getAttachedAgentStatusSet(agentStatus -> agentStatus.getConnectingPort() != 0);
		for (OnPeriodicWorkingAgentCheckRunnable runnable : pluginManager
				.getEnabledModulesByClass(OnPeriodicWorkingAgentCheckRunnable.class)) {
			runnable.checkWorkingAgent(workingAgents);
		}
	}
}
