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
package org.ngrinder.perftest.service;

import java.io.File;
import java.util.Set;
import java.util.Timer;

import net.grinder.SingleConsole.SamplingLifeCycleListener;
import net.grinder.statistics.StatisticsSet;

import org.ngrinder.agent.model.AgentInfo;
import org.ngrinder.monitor.service.MontorClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * Monitor data collector.
 * 
 * @author JunHo Yoon
 * @since 3.1.1
 */
public class MonitorCollectorListener implements SamplingLifeCycleListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(MonitorCollectorListener.class);
	private final ApplicationContext applicationContext;
	private final Set<AgentInfo> agents;
	private MontorClientManager monitorClientTask;
	private final File reportPath;
	private final Timer timer;

	/**
	 * Constructor.
	 * 
	 * @param applicationContext
	 *            application context
	 * @param agents
	 *            set of monitors to be collected
	 * @param reportPath
	 *            where does it report.
	 */
	public MonitorCollectorListener(ApplicationContext applicationContext, Set<AgentInfo> agents, File reportPath) {
		this.applicationContext = applicationContext;
		this.agents = agents;
		this.timer = new Timer(true);
		this.reportPath = reportPath;
	}

	@Override
	public void onSamplingStarted() {
		monitorClientTask = applicationContext.getBean(MontorClientManager.class);
		monitorClientTask.add(agents, reportPath);
		timer.schedule(monitorClientTask, 800, 800);
	}

	@Override
	public void onSampling(File file, StatisticsSet intervalStatistics, StatisticsSet cumulativeStatistics) {
		if (monitorClientTask != null) {
			monitorClientTask.saveData();
		}
	}

	@Override
	public void onSamplingEnded() {
		LOGGER.info("remove monitors on {}", agents);
		if (timer != null) {
			timer.cancel();
		}
		if (monitorClientTask != null) {
			monitorClientTask.destroy();
		}
	}
}
