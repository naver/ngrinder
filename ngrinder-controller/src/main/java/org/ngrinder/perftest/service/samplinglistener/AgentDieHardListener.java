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
package org.ngrinder.perftest.service.samplinglistener;

import net.grinder.SingleConsole;
import net.grinder.SingleConsole.SamplingLifeCycleListener;
import net.grinder.console.communication.AgentProcessControlImplementation.AgentStatus;
import net.grinder.statistics.StatisticsSet;
import org.ngrinder.infra.schedule.ScheduledTaskService;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Status;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.perftest.service.AgentManager;
import org.ngrinder.perftest.service.PerfTestService;

import java.io.File;

/**
 * Agent crash detector.
 *
 * @author JunHo Yoon
 * @since 3.1.2
 */
public class AgentDieHardListener implements SamplingLifeCycleListener, Runnable {
	private final SingleConsole singleConsole;
	private final PerfTest perfTest;
	private final PerfTestService perfTestService;
	private final AgentManager agentManager;
	private final ScheduledTaskService scheduledTaskService;

	/**
	 * Constructor.
	 *
	 * @param singleConsole   singleConsole to monitor
	 * @param perfTest        perfTest which this sampling start
	 * @param perfTestService perfTestService
	 * @param agentManager    agent manager
	 */
	public AgentDieHardListener(final SingleConsole singleConsole, final PerfTest perfTest,
	                            final PerfTestService perfTestService,
	                            final AgentManager agentManager, final ScheduledTaskService scheduledTaskService) {
		this.singleConsole = singleConsole;
		this.perfTest = perfTest;
		this.perfTestService = perfTestService;
		this.agentManager = agentManager;
		this.scheduledTaskService = scheduledTaskService;
		this.scheduledTaskService.addFixedDelayedScheduledTask(this, 2);
	}

	@Override
	public void run() {
		for (AgentStatus agentStates : agentManager.getAgentStatusSetConnectingToPort(singleConsole.getConsolePort())) {
			SystemDataModel systemDataModel = agentStates.getSystemDataModel();
			if (systemDataModel != null) {
				// If the memory is available less than 2%.
				double freeMemoryRatio = ((double) systemDataModel.getFreeMemory()) / systemDataModel.getTotalMemory();
				if (freeMemoryRatio < 0.02) {
					if (perfTest.getStatus() != Status.ABNORMAL_TESTING) {
						perfTestService.markStatusAndProgress(perfTest, Status.ABNORMAL_TESTING, //
								String.format("[ERROR] %s agent is about to die due to lack of free memory.\n"
										+ "Shutdown PerfTest %s by force for safety\n" + "Please decrease the vuser count.", //
										agentStates.getAgentName(), perfTest.getId()));
					}
				}
			}
		}
	}

	@Override
	public void onSamplingStarted() {
	}

	@Override
	public void onSampling(File file, StatisticsSet intervalStatistics, StatisticsSet cumulativeStatistics) {


	}

	@Override
	public void onSamplingEnded() {
		this.scheduledTaskService.removeScheduledJob(this);
	}
}
