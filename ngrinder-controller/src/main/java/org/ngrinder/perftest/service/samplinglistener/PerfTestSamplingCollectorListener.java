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

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.grinder.SingleConsole;
import net.grinder.SingleConsole.SamplingLifeCycleListener;
import net.grinder.statistics.StatisticsSet;

import org.ngrinder.infra.logger.CoreLogger;
import org.ngrinder.perftest.service.PerfTestService;

/**
 * Perf Test Sampling collection class.
 * 
 * @author JunHo Yoon
 * @since 3.1.1
 */
public class PerfTestSamplingCollectorListener implements SamplingLifeCycleListener {
	private final PerfTestService perfTestService;
	private ExecutorService newSingleThreadExecutor;
	private PerfTestStatisticCollectionRunnable perfTestStatisticCollectionRunnable;

	/**
	 * Constructor.
	 * 
	 * @param singleConsole
	 *            singleConsole to monitor
	 * @param perfTestId
	 *            perfTest id which this sampling start
	 * @param perfTestService
	 *            service
	 */
	public PerfTestSamplingCollectorListener(SingleConsole singleConsole, Long perfTestId,
			PerfTestService perfTestService) {
		this.perfTestService = perfTestService;
		this.newSingleThreadExecutor = Executors.newSingleThreadExecutor();
		this.perfTestStatisticCollectionRunnable = new PerfTestStatisticCollectionRunnable(singleConsole, perfTestId);
	}

	class PerfTestStatisticCollectionRunnable implements Runnable {
		private final SingleConsole singleConsole;
		private final Long perfTestId;

		PerfTestStatisticCollectionRunnable(SingleConsole singleConsole, Long perfTestId) {
			this.singleConsole = singleConsole;
			this.perfTestId = perfTestId;
		}

		@Override
		public void run() {
			perfTestService.saveStatistics(singleConsole, perfTestId);
		}

	}

	@Override
	public void onSamplingStarted() {
	}

	@Override
	public void onSampling(File file, StatisticsSet intervalStatistics, StatisticsSet cumulativeStatistics) {
		CoreLogger.LOGGER.debug("Sampling is performed");
		newSingleThreadExecutor.execute(this.perfTestStatisticCollectionRunnable);
	}

	@Override
	public void onSamplingEnded() {
		newSingleThreadExecutor.shutdown();
	}

}
