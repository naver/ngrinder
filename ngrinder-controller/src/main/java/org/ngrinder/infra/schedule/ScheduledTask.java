package org.ngrinder.infra.schedule;

import net.grinder.util.ListenerSupport;
import net.grinder.util.ListenerSupport.Informer;
import net.grinder.util.thread.InterruptibleRunnable;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Convenient class which make scheduled task.
 * 
 * @author JunHo Yoon
 * @since 3.1
 */
@Service
public class ScheduledTask {

	ListenerSupport<InterruptibleRunnable> runListenersEvery3Sec = new ListenerSupport<InterruptibleRunnable>();
	ListenerSupport<InterruptibleRunnable> runListenersEvery10Sec = new ListenerSupport<InterruptibleRunnable>();

	/**
	 * Run scheduled task with every 3 secs.
	 */
	@Scheduled(fixedDelay = 3000)
	public void doTaskOnEvery3Sec() {
		runListenersEvery3Sec.apply(new Informer<InterruptibleRunnable>() {
			@Override
			public void inform(InterruptibleRunnable listener) {
				listener.interruptibleRun();
			}
		});
	}

	/**
	 * Run scheduled task with every 10 secs.
	 */
	@Scheduled(fixedDelay = 10000)
	public void doTaskOnEvery10Sec() {
		runListenersEvery10Sec.apply(new Informer<InterruptibleRunnable>() {
			@Override
			public void inform(InterruptibleRunnable listener) {
				listener.interruptibleRun();
			}
		});
	}

	/**
	 * Add scheduled task on 3 sec scheduler.
	 * 
	 * @param runnable
	 *            runnable
	 */
	public void addScheduledTaskEvery3Sec(InterruptibleRunnable runnable) {
		runListenersEvery3Sec.add(runnable);
	}

	/**
	 * Add scheduled task on 10 sec scheduler.
	 * 
	 * @param runnable
	 *            runnable
	 */
	public void addScheduledTaskEvery5Sec(InterruptibleRunnable runnable) {
		runListenersEvery10Sec.add(runnable);
	}
}
