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
package org.ngrinder.monitor.agent.collector;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ngrinder.monitor.MonitorConstants;
import org.ngrinder.monitor.agent.AgentMXBeanStorage;
import org.ngrinder.monitor.agent.mxbean.core.MXBean;

/**
 * 
 * Agent data collect manager class, to start/stop the data collector jobs.
 * 
 * @author Mavlarn
 * @since 2.0
 */
public final class AgentDataCollectManager {
	private static final Logger LOG = LoggerFactory.getLogger(AgentDataCollectManager.class);

	private boolean isRunning = false;
	private ScheduledExecutorService scheduler;
	private static final AgentDataCollectManager INSTANCE = new AgentDataCollectManager();

	private AgentDataCollectManager() {
	}

	public static AgentDataCollectManager getInstance() {
		return INSTANCE;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public int getInterval() {
		return MonitorConstants.DEFAULT_MONITOR_COLLECTOR_INTERVAL;
	}

	/**
	 * start a scheduler for the data collector jobs.
	 */
	public void start() {
		int collectorCount = AgentMXBeanStorage.getInstance().getSize();
		scheduler = Executors.newScheduledThreadPool(collectorCount);
		if (!isRunning()) {
			Collection<MXBean> mxBeans = AgentMXBeanStorage.getInstance().getMXBeans();
			for (MXBean mxBean : mxBeans) {
				scheduler.scheduleWithFixedDelay(mxBean.gainAgentDataCollector(), 0L, getInterval(), TimeUnit.SECONDS);
				LOG.info("Agent collector: {} started.", mxBean.gainAgentDataCollector().getClass().getSimpleName());
			}
			LOG.info("Agent collector start (interval :{} s).", getInterval());

			isRunning = true;
		}
	}

	/**
	 * stop the scheduler for data collecting jobs.
	 */
	public void stop() {
		if (isRunning()) {
			scheduler.shutdown();
			isRunning = false;
			LOG.info("Agent collector End");
		}
	}
}
