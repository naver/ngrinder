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
import net.grinder.statistics.StatisticsSet;
import org.ngrinder.infra.schedule.ScheduledTaskService;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Status;
import org.ngrinder.perftest.service.PerfTestService;

import java.io.File;

/**
 * Agent Lost Detector.
 *
 * @author JunHo Yoon
 * @since 3.1.2
 */
public class AgentLostDetectionListener implements SamplingLifeCycleListener {
	private final Runnable runnable;
	private final ScheduledTaskService scheduledTaskService;
	private int lostAgentDetectionTrial;

	/**
	 * Constructor.
	 *
	 * @param singleConsole   singleConsole to monitor
	 * @param perfTest        perfTest which this sampling start
	 * @param perfTestService service
	 * @param scheduledTaskService scheduledTaskService
	 */
	public AgentLostDetectionListener(final SingleConsole singleConsole, final PerfTest perfTest,
	                                  final PerfTestService perfTestService, ScheduledTaskService scheduledTaskService) {
		this.scheduledTaskService = scheduledTaskService;
		this.runnable = new Runnable() {
			@Override
			public void run() {
				if (singleConsole.getAllAttachedAgentsCount() == 0) {
					if (lostAgentDetectionTrial++ > 10) {
						perfTestService.markStatusAndProgress(perfTest, Status.ABNORMAL_TESTING,
								"[ERROR] All agents are unexpectedly lost.");
					}
				} else {
					lostAgentDetectionTrial = 0;
				}
			}
		};
	}

	@Override
	public void onSamplingStarted() {
	}

	@Override
	public void onSampling(File file, StatisticsSet intervalStatistics, StatisticsSet cumulativeStatistics) {
		scheduledTaskService.runAsync(this.runnable);
	}

	@Override
	public void onSamplingEnded() {
	}

}
