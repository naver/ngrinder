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

import net.grinder.SingleConsole.SamplingLifeCycleFollowUpListener;
import net.grinder.statistics.StatisticsSet;
import org.apache.commons.io.IOUtils;
import org.ngrinder.infra.schedule.ScheduledTaskService;
import org.ngrinder.perftest.service.monitor.MonitorScheduledTask;

import java.io.File;

/**
 * Monitor data collector.
 *
 * @author JunHo Yoon
 * @since 3.1.1
 */
public class MonitorCollectorListener implements SamplingLifeCycleFollowUpListener {
	private final ScheduledTaskService scheduledTaskService;
	private int interval;
	private MonitorScheduledTask monitorScheduledTask;

	/**
	 * Constructor.
	 *
	 * @param monitorScheduledTask monitoring task which will be periodically run
	 * @param scheduledTaskService scheduling service to run monitor task
	 */
	public MonitorCollectorListener(MonitorScheduledTask monitorScheduledTask,
	                                ScheduledTaskService scheduledTaskService, int interval) {
		this.monitorScheduledTask = monitorScheduledTask;
		this.scheduledTaskService = scheduledTaskService;
		this.interval = interval;
	}

	@Override
	public void onSamplingStarted() {
		scheduledTaskService.addFixedDelayedScheduledTask(monitorScheduledTask, interval);
	}

	@Override
	public void onSampling(File file, StatisticsSet intervalStatistics, StatisticsSet cumulativeStatistics,
	                       boolean lastCall) {
		monitorScheduledTask.saveData(!lastCall);
	}

	@Override
	public void onSamplingEnded() {
		scheduledTaskService.removeScheduledJob(this.monitorScheduledTask);
		IOUtils.closeQuietly(this.monitorScheduledTask);
	}
}
