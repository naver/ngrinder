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
package org.ngrinder.monitor.collector;

import org.ngrinder.common.constants.MonitorConstants;
import org.ngrinder.infra.AgentConfig;
import org.ngrinder.monitor.agent.MXBeanStorage;
import org.ngrinder.monitor.mxbean.core.MXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Data collect manager class, to start/stop the data collector jobs.
 *
 * @author Mavlarn
 * @since 2.0
 */
public final class DataCollectManager implements MonitorConstants {
	private static final Logger LOG = LoggerFactory.getLogger(DataCollectManager.class);

	private boolean isRunning = false;
	private ScheduledExecutorService scheduler;
	private static final DataCollectManager INSTANCE = new DataCollectManager();

	private AgentConfig agentConfig;

	private DataCollectManager() {
	}

	public static DataCollectManager getInstance() {
		return INSTANCE;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public int getInterval() {
		return DEFAULT_MONITOR_COLLECTOR_INTERVAL;
	}

	/**
	 * Initialize the manager. Need to set the agent home directory for the
	 * collector.
	 *
	 * @param agentConfig agentHome
	 */
	public void init(AgentConfig agentConfig) {
		this.agentConfig = agentConfig;
	}

	/**
	 * start a scheduler for the data collector jobs.
	 */
	public void start() {
		int collectorCount = MXBeanStorage.getInstance().getSize();
		scheduler = Executors.newScheduledThreadPool(collectorCount);
		if (!isRunning()) {
			Collection<MXBean> mxBeans = MXBeanStorage.getInstance().getMXBeans();
			for (MXBean mxBean : mxBeans) {
				DataCollector collector = mxBean.gainDataCollector(agentConfig.getHome().getDirectory());
				scheduler.scheduleWithFixedDelay(collector, 0L, getInterval(), TimeUnit.SECONDS);
				LOG.info("{} started.", collector.getClass().getSimpleName());
			}
			LOG.info("Collection interval : {}s).", getInterval());
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
			LOG.info("Collector shutdown");
		}
	}
}
