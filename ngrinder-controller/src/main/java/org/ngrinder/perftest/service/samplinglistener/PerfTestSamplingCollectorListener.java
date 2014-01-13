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
import org.ngrinder.perftest.service.PerfTestService;

import java.io.File;

/**
 * Perf Test Sampling collection class.
 *
 * @author JunHo Yoon
 * @since 3.1.1
 */
public class PerfTestSamplingCollectorListener implements SamplingLifeCycleListener {
	private final ScheduledTaskService scheduledTaskService;
	private Runnable runnable;

	/**
	 * Constructor.
	 *
	 * @param singleConsole        singleConsole to monitor
	 * @param perfTestId           perfTest id which this sampling start
	 * @param perfTestService      perfTestService
	 * @param scheduledTaskService scheduledTaskService
	 */
	public PerfTestSamplingCollectorListener(final SingleConsole singleConsole, final Long perfTestId,
	                                         final PerfTestService perfTestService,
	                                         ScheduledTaskService scheduledTaskService) {
		this.scheduledTaskService = scheduledTaskService;
		// Make it separate asyc call to remove the delay on the sampling.
		this.runnable = new Runnable() {
			@Override
			public void run() {
				perfTestService.saveStatistics(singleConsole, perfTestId);
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
