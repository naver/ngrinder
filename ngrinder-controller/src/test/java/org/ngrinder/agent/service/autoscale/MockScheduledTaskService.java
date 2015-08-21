package org.ngrinder.agent.service.autoscale;

import org.ngrinder.infra.schedule.ScheduledTaskService;

import java.util.concurrent.ScheduledFuture;

/**
 * Created by junoyoon on 15. 8. 18.
 */
public class MockScheduledTaskService extends ScheduledTaskService {
	@Override
	public void runAsync(Runnable runnable) {
		runnable.run();
	}


	public void addFixedDelayedScheduledTask(Runnable runnable, int delay) {
		runnable.run();
	}

}
