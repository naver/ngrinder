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
package org.ngrinder.infra.schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Convenient class which makes scheduled task.
 *
 * @author JunHo Yoon
 * @since 3.1
 */
@Service
public class ScheduledTaskService {
	@Autowired
	private TaskScheduler taskScheduler;

	private Map<Runnable, ScheduledFuture> scheduledRunnable = new ConcurrentHashMap<Runnable, ScheduledFuture>();


	public void addFixedDelayedScheduledTask(Runnable runnable, int delay) {
		final ScheduledFuture scheduledFuture = taskScheduler.scheduleWithFixedDelay(runnable, delay);
		scheduledRunnable.put(runnable, scheduledFuture);

	}


	public void removeScheduledJob(Runnable runnable) {
		final ScheduledFuture scheduledTaskInfo = scheduledRunnable.remove(runnable);
		if (scheduledTaskInfo != null) {
			scheduledTaskInfo.cancel(false);
		}
	}

	/**
	 * Run async job.
	 *
	 * @param runnable job to run
	 */
	@Async
	public void runAsync(Runnable runnable) {
		runnable.run();
	}

}
