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

import java.io.File;

import net.grinder.SingleConsole;
import net.grinder.SingleConsole.SamplingLifeCycleListener;
import net.grinder.console.communication.AgentProcessControlImplementation.AgentStatus;
import net.grinder.statistics.StatisticsSet;

import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Status;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.perftest.service.AgentManager;
import org.ngrinder.perftest.service.PerfTestService;

/**
 * Agent Crash Preventer. .
 * 
 * @author JunHo Yoon
 * @since 3.1.2
 */
public class AgentDieHardListener implements SamplingLifeCycleListener {
	private final PerfTest perfTest;
	private final SingleConsole singleConsole;
	private final PerfTestService perfTestService;
	private final AgentManager agentManager;

	/**
	 * Constructor.
	 * 
	 * @param singleConsole
	 *            singleConsole to monitor
	 * @param perfTest
	 *            perfTest which this sampling start
	 * @param perfTestService
	 *            service
	 */
	public AgentDieHardListener(SingleConsole singleConsole, PerfTest perfTest, PerfTestService perfTestService,
					AgentManager agentManager) {
		this.singleConsole = singleConsole;
		this.perfTest = perfTest;
		this.perfTestService = perfTestService;
		this.agentManager = agentManager;
	}

	@Override
	public void onSamplingStarted() {
	}

	@Override
	public void onSampling(File file, StatisticsSet intervalStatistics, StatisticsSet cumulativeStatistics) {
		for (AgentStatus agentStates : agentManager.getAgentStatusSetConnectingToPort(singleConsole.getConsolePort())) {
			SystemDataModel systemDataModel = agentStates.getSystemDataModel();
			// If the memory is available less than 3%.
			double freeMemoryRatio = ((double) systemDataModel.getFreeMemory()) / systemDataModel.getTotalMemory();
			if (freeMemoryRatio < 0.03) {
				perfTestService.markStatusAndProgress(
								perfTest,
								Status.ABNORMAL_TESTING,
								String.format("DANGER!! %s agent is about to die due to lack of free memory.\nShutdown PerfTest %s by force for safety\nPlease decrease the vuser count.",
												agentStates.getAgentName(), perfTest.getId()));
			}
		}

	}

	@Override
	public void onSamplingEnded() {
	}

}
