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
import org.ngrinder.extension.OnTestSamplingRunnable;
import org.ngrinder.model.PerfTest;
import org.ngrinder.perftest.service.PerfTestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Sampling LifeCycle listener to run the pluings implementing {@link OnTestSamplingRunnable}.
 *
 * @author JunHo Yoon
 * @since 3.1
 */
public class PluginRunListener implements SamplingLifeCycleListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(MonitorCollectorPlugin.class);

	private final List<OnTestSamplingRunnable> plugins;
	private final SingleConsole singleConsole;
	private final PerfTest perfTest;
	private final PerfTestService perfTestService;

	/**
	 * Constructor.
	 *
	 * @param plugins         plugins
	 * @param singleConsole   singleConsole
	 * @param perfTest        perfTest
	 * @param perfTestService perfTestService
	 */
	public PluginRunListener(List<OnTestSamplingRunnable> plugins, SingleConsole singleConsole, PerfTest perfTest,
	                         PerfTestService perfTestService) {
		this.plugins = plugins;
		this.singleConsole = singleConsole;
		this.perfTest = perfTest;
		this.perfTestService = perfTestService;
	}

	@Override
	public void onSamplingStarted() {
		for (OnTestSamplingRunnable each : plugins) {
			try {
				each.startSampling(singleConsole, perfTest, perfTestService);
			} catch (Exception e) {
				LOGGER.error("While running plugins, the error occurred : {}", e.getMessage());
				LOGGER.debug("Details : ", e);
			}
		}
	}

	@Override
	public void onSampling(File file, StatisticsSet intervalStatistics, StatisticsSet cumulativeStatistics) {
		for (OnTestSamplingRunnable each : plugins) {
			try {
				each.sampling(singleConsole, perfTest, perfTestService, intervalStatistics, cumulativeStatistics);
			} catch (Exception e) {
				LOGGER.error("While running plugin the following error occurred : {}", e.getMessage());
				LOGGER.debug("Details : ", e);
			}
		}

	}

	@Override
	public void onSamplingEnded() {
		for (OnTestSamplingRunnable each : plugins) {
			try {
				each.endSampling(singleConsole, perfTest, perfTestService);
			} catch (Exception e) {
				LOGGER.error("While running plugin the following error occurs : {}", e.getMessage());
				LOGGER.debug("Details : ", e);
			}
		}

	}
}
