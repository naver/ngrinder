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

import net.grinder.util.ListenerHelper;
import net.grinder.util.ListenerSupport;
import net.grinder.util.ListenerSupport.Informer;
import net.grinder.util.thread.InterruptibleRunnable;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Convenient class which makes scheduled task.
 * 
 * @author JunHo Yoon
 * @since 3.1
 */
@Service
public class ScheduledTask {

	private ListenerSupport<InterruptibleRunnable> runListenersEvery3Sec = ListenerHelper.create();
	private ListenerSupport<InterruptibleRunnable> runListenersEvery10Sec = ListenerHelper.create();

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
