package org.ngrinder.perftest.service.samplinglistener;

import lombok.extern.slf4j.Slf4j;
import net.grinder.SingleConsole;
import net.grinder.statistics.ImmutableStatisticsSet;
import net.grinder.statistics.StatisticsIndexMap;
import org.ngrinder.extension.OnTestSamplingRunnable;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Status;
import org.ngrinder.service.IPerfTestService;
import org.ngrinder.service.ISingleConsole;

import static net.grinder.util.NoOp.noOp;
import static org.ngrinder.common.util.LoggingUtils.format;

@Slf4j
public class TooManyErrorCheckPlugin implements OnTestSamplingRunnable {

	private static final int TOO_MANY_ERROR_TIME = 10000;

	private long lastMomentWhenErrorsMoreThanHalfOfTotalTPSValue;

	@Override
	public void startSampling(ISingleConsole singleConsole,
							  PerfTest perfTest,
							  IPerfTestService perfTestService) {
		noOp();
	}

	@Override
	public void sampling(ISingleConsole singleConsole,
						 PerfTest perfTest,
						 IPerfTestService perfTestService,
						 ImmutableStatisticsSet intervalStatistics,
						 ImmutableStatisticsSet cumulativeStatistics) {

		if (!perfTest.getIgnoreTooManyError()) {
			checkTooManyError(singleConsole, perfTest, perfTestService, cumulativeStatistics);
		}
	}

	@Override
	public void endSampling(ISingleConsole singleConsole,
							PerfTest perfTest,
							IPerfTestService perfTestService) {
		noOp();
	}

	/**
	 * Check if too many error has been occurred. If the half of total
	 * transaction is error for the last 10 secs. It notifies the
	 * {@link SingleConsole.ConsoleShutdownListener}
	 *
	 * @param cumulativeStatistics accumulated Statistics
	 */
	private void checkTooManyError(ISingleConsole singleConsole,
								   PerfTest perfTest,
								   IPerfTestService perfTestService,
								   ImmutableStatisticsSet cumulativeStatistics) {

		StatisticsIndexMap statisticsIndexMap = singleConsole.getStatisticsIndexMap();
		long testSum = cumulativeStatistics.getCount(statisticsIndexMap.getLongSampleIndex("timedTests"));
		long errors = cumulativeStatistics.getValue(statisticsIndexMap.getLongIndex("errors"));
		if (((double) (testSum + errors)) / 2 < errors) {
			if (lastMomentWhenErrorsMoreThanHalfOfTotalTPSValue == 0) {
				lastMomentWhenErrorsMoreThanHalfOfTotalTPSValue = System.currentTimeMillis();
			} else if (isOverLowTpsThreshold()) {
				log.warn(format(perfTest, "Stop the test because the count of test error is more than"
					+ " half of total tps for last {} seconds.", TOO_MANY_ERROR_TIME / 1000));

				if (perfTest.getStatus() != Status.ABNORMAL_TESTING) {
					perfTestService.markStatusAndProgress(perfTest, Status.ABNORMAL_TESTING, "Too many errors");
				}
				lastMomentWhenErrorsMoreThanHalfOfTotalTPSValue = 0;
			}
		}
	}

	private boolean isOverLowTpsThreshold() {
		return (System.currentTimeMillis() - lastMomentWhenErrorsMoreThanHalfOfTotalTPSValue) >= TOO_MANY_ERROR_TIME;
	}
}
