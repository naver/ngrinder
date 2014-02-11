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

import org.ngrinder.infra.transaction.TransactionService;
import org.ngrinder.service.IScheduledTaskService;
import org.springframework.beans.factory.BeanCreationNotAllowedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import static net.grinder.util.NoOp.noOp;

/**
 * Convenient class which makes scheduled task.
 *
 * @author JunHo Yoon
 * @since 3.3
 */
@Service
public class ScheduledTaskService implements IScheduledTaskService {

	@Autowired
	private TaskScheduler taskScheduler;

	@Autowired
	private InternalAsyncTaskService internalAsyncTaskService;

	private Map<Runnable, ScheduledFuture> scheduledRunnable = new ConcurrentHashMap<Runnable, ScheduledFuture>();

	@Autowired
	private TransactionService transactionService;

	public void addFixedDelayedScheduledTask(Runnable runnable, int delay) {
		final ScheduledFuture scheduledFuture = taskScheduler.scheduleWithFixedDelay(runnable, delay);
		scheduledRunnable.put(runnable, scheduledFuture);
	}


	public void addFixedDelayedScheduledTaskInTransactionContext(final Runnable runnable, int delay) {
		final Runnable transactionalRunnable = new Runnable() {
			@Override
			public void run() {
				try {
					transactionService.runInTransaction(runnable);
				} catch (IllegalStateException e) {
					noOp();
				} catch (BeanCreationNotAllowedException e) {
					noOp();
				}
			}
		};
		final ScheduledFuture scheduledFuture = taskScheduler.scheduleWithFixedDelay(transactionalRunnable, delay);
		scheduledRunnable.put(runnable, scheduledFuture);
	}

	public void removeScheduledJob(Runnable runnable) {
		final ScheduledFuture scheduledTaskInfo = scheduledRunnable.remove(runnable);
		if (scheduledTaskInfo != null) {
			scheduledTaskInfo.cancel(false);
		}
	}

	public void runAsync(Runnable runnable) {
		internalAsyncTaskService.runAsync(runnable);
	}

}
