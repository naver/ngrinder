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

import net.grinder.console.communication.AgentProcessControlImplementation;
import net.grinder.console.communication.AgentProcessControlImplementation.AgentStatus;

import org.ngrinder.extension.OnPeriodicWorkingAgentCheckRunnable;
import org.ngrinder.infra.plugin.PluginManager;
import org.ngrinder.infra.schedule.ScheduledTaskService;
import org.ngrinder.perftest.service.AgentManager;
import org.python.google.common.base.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

/**
 * Agent periodic check service.
 * <p/>
 * This class runs the plugins implementing
 * {@link OnPeriodicWorkingAgentCheckRunnable}.
 * <p/>
 * It's separated from {@link AgentManagerService} to get rid of cyclic
 * injection.
 *
 * @author JunHo Yoon
 * @since 3.1.2
 */
@Service
public class PeriodicWorkingAgentCheckService implements Runnable {
	@Autowired
	private PluginManager pluginManager;

	@Autowired
	private AgentManager agentManager;

	@Autowired
	private ScheduledTaskService scheduledTaskService;

	@PostConstruct
	public void init() {
		scheduledTaskService.addFixedDelayedScheduledTaskInTransactionContext(this, 2000);
	}

	@Override
	public void run() {
		Set<AgentStatus> workingAgents = agentManager
				.getAgentStatusSet(new Predicate<AgentProcessControlImplementation.AgentStatus>() {
					@Override
					public boolean apply(AgentStatus agentStatus) {
						return agentStatus.getConnectingPort() != 0;
					}
				});
		for (OnPeriodicWorkingAgentCheckRunnable runnable : pluginManager
				.getEnabledModulesByClass(OnPeriodicWorkingAgentCheckRunnable.class)) {
			runnable.checkWorkingAgent(workingAgents);
		}
	}
}
