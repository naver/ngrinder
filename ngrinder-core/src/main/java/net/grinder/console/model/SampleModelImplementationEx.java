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
package net.grinder.console.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import net.grinder.common.GrinderException;
import net.grinder.common.Test;
import net.grinder.console.common.ErrorHandler;
import net.grinder.console.common.Resources;
import net.grinder.statistics.PeakStatisticExpression;
import net.grinder.statistics.StatisticExpression;
import net.grinder.statistics.StatisticExpressionFactory;
import net.grinder.statistics.StatisticsIndexMap;
import net.grinder.statistics.StatisticsServices;
import net.grinder.statistics.StatisticsSet;
import net.grinder.statistics.TestStatisticsMap;
import net.grinder.util.ListenerSupport;

/**
 * Collate test reports into samples and distribute to listeners.
 * <p>
 * NHN Customized version
 * 
 * When notifying listeners of changes to the number of tests we send copies of the new index
 * arrays. This helps because most listeners are Swing dispatched and so can't guarantee the model
 * is in a reasonable state when they call back.
 * </p>
 * 
 * @author JunHo Yoon
 * @author Mavlarn
 * @since 3.0
 */
public class SampleModelImplementationEx implements SampleModel {

	private final ConsoleProperties m_properties;
	private final StatisticsServices m_statisticsServices;
	private final Timer m_timer;
	private final ErrorHandler m_errorHandler;

	private final String m_stateIgnoringString;
	private final String m_stateWaitingString;
	private final String m_stateStoppedString;
	private final String m_stateCapturingString;
	private final String m_unknownTestString;

	/**
	 * The current test set. A TreeSet is used to maintain the test order. Guarded by itself.
	 */
	private final Set<Test> m_tests = new TreeSet<Test>();

	private final ListenerSupport<Listener> m_listeners = new ListenerSupport<Listener>();

	private final StatisticsIndexMap.LongIndex m_periodIndex;
	private final StatisticExpression m_tpsExpression;
	private final PeakStatisticExpression m_peakTPSExpression;

	private final SampleAccumulatorEx m_totalSampleAccumulator;

	private ModelTestIndex modelTestIndex;

	/**
	 * A {@link SampleAccumulator} for each test. Guarded by itself.
	 */
	private final Map<Test, SampleAccumulator> m_accumulators = Collections
					.synchronizedMap(new HashMap<Test, SampleAccumulator>());

	// Guarded by this.
	private InternalState m_state;

	/**
	 * Creates a new <code>SampleModelImplementation</code> instance.
	 * 
	 * @param properties
	 *            The console properties.
	 * @param statisticsServices
	 *            Statistics services.
	 * @param timer
	 *            A timer.
	 * @param resources
	 *            Console resources.
	 * @param errorHandler
	 *            Error handler
	 * 
	 * @exception GrinderException
	 *                if an error occurs
	 */
	public SampleModelImplementationEx(ConsoleProperties properties, StatisticsServices statisticsServices,
					Timer timer, Resources resources, ErrorHandler errorHandler) throws GrinderException {

		m_properties = properties;
		m_statisticsServices = statisticsServices;
		m_timer = timer;
		m_errorHandler = errorHandler;

		m_stateIgnoringString = resources.getString("state.ignoring.label") + ' ';
		m_stateWaitingString = resources.getString("state.waiting.label");
		m_stateStoppedString = resources.getString("state.stopped.label");
		m_stateCapturingString = resources.getString("state.capturing.label") + ' ';
		m_unknownTestString = resources.getString("ignoringUnknownTest.text");

		final StatisticsIndexMap indexMap = statisticsServices.getStatisticsIndexMap();

		m_periodIndex = indexMap.getLongIndex("period");

		final StatisticExpressionFactory statisticExpressionFactory = m_statisticsServices
						.getStatisticExpressionFactory();

		m_tpsExpression = statisticsServices.getTPSExpression();

		m_peakTPSExpression = statisticExpressionFactory
						.createPeak(indexMap.getDoubleIndex("peakTPS"), m_tpsExpression);

		m_totalSampleAccumulator = new SampleAccumulatorEx(m_peakTPSExpression, m_periodIndex,
						m_statisticsServices.getStatisticsSetFactory());

		setInternalState(new WaitingForTriggerState());
	}

	/**
	 * Get the expression for TPS.
	 * 
	 * @return The TPS expression for this model.
	 */
	public StatisticExpression getTPSExpression() {
		return m_tpsExpression;
	}

	/**
	 * Get the expression for peak TPS.
	 * 
	 * @return The peak TPS expression for this model.
	 */
	public StatisticExpression getPeakTPSExpression() {
		return m_peakTPSExpression;
	}

	/**
	 * Register new tests.
	 * 
	 * @param tests
	 *            The new tests.
	 */
	public void registerTests(Collection<Test> tests) {
		// Need to copy collection, might be immutable.
		final Set<Test> newTests = new HashSet<Test>(tests);

		final Test[] testArray;

		synchronized (m_tests) {
			newTests.removeAll(m_tests);

			if (newTests.size() == 0) {
				// No new tests.
				return;
			}

			m_tests.addAll(newTests);

			// Create an index of m_tests sorted by test number.
			testArray = m_tests.toArray(new Test[m_tests.size()]);
		}

		final SampleAccumulator[] accumulatorArray = new SampleAccumulator[testArray.length];

		synchronized (m_accumulators) {
			for (Test test : newTests) {
				m_accumulators.put(
								test,
								new SampleAccumulator(m_peakTPSExpression, m_periodIndex, m_statisticsServices
												.getStatisticsSetFactory()));
			}

			for (int i = 0; i < accumulatorArray.length; i++) {
				accumulatorArray[i] = m_accumulators.get(testArray[i]);
			}
		}

		final ModelTestIndex modelTestIndex = new ModelTestIndex(testArray, accumulatorArray);
		this.modelTestIndex = modelTestIndex;
		m_listeners.apply(new ListenerSupport.Informer<Listener>() {
			public void inform(Listener l) {
				l.newTests(newTests, modelTestIndex);
			}
		});
	}

	/**
	 * Get the cumulative statistics for this model.
	 * 
	 * @return The cumulative statistics.
	 */
	public StatisticsSet getTotalCumulativeStatistics() {
		return m_totalSampleAccumulator.getCumulativeStatistics();
	}

	/**
	 * Add a new model listener.
	 * 
	 * @param listener
	 *            The listener.
	 */
	public void addModelListener(Listener listener) {
		m_listeners.add(listener);
	}

	/**
	 * Add a new sample listener for the specific test.
	 * 
	 * @param test
	 *            The test to add the sample listener for.
	 * @param listener
	 *            The sample listener.
	 */
	public void addSampleListener(Test test, SampleListener listener) {
		final SampleAccumulator sampleAccumulator = m_accumulators.get(test);

		if (sampleAccumulator != null) {
			sampleAccumulator.addSampleListener(listener);
		}
	}

	/**
	 * Add a new total sample listener.
	 * 
	 * @param listener
	 *            The sample listener.
	 */
	public void addTotalSampleListener(SampleListener listener) {
		m_totalSampleAccumulator.addSampleListener(listener);
	}

	/**
	 * Reset the model.
	 * 
	 * <p>
	 * This doesn't affect our internal state, just the statistics and the listeners.
	 * </p>
	 */
	public void reset() {

		synchronized (m_tests) {
			m_tests.clear();
		}

		m_accumulators.clear();
		m_totalSampleAccumulator.zero();

		m_listeners.apply(new ListenerSupport.Informer<Listener>() {
			public void inform(Listener l) {
				l.resetTests();
			}
		});
	}

	/**
	 * Start the model.
	 */
	public void start() {
		getInternalState().start();
	}

	/**
	 * Stop the model.
	 */
	public void stop() {
		getInternalState().stop();
	}

	/**
	 * Add a new test report.
	 * 
	 * @param testStatisticsMap
	 *            The new test statistics.
	 */
	public void addTestReport(TestStatisticsMap testStatisticsMap) {
		getInternalState().newTestReport(testStatisticsMap);
	}

	/**
	 * Get the current model state.
	 * 
	 * @return The model state.
	 */
	public State getState() {
		return getInternalState().toExternalState();
	}

	/**
	 * Zero the accumulators.
	 */
	public void zero() {
		synchronized (m_accumulators) {
			for (SampleAccumulator sampleAccumulator : m_accumulators.values()) {
				sampleAccumulator.zero();
			}
		}
		m_totalSampleAccumulator.zero();
	}

	private InternalState getInternalState() {
		synchronized (this) {
			return m_state;
		}
	}

	private void setInternalState(InternalState newState) {
		synchronized (this) {
			m_state = newState;
		}

		m_listeners.apply(new ListenerSupport.Informer<Listener>() {
			public void inform(Listener l) {
				l.stateChanged();
			}
		});
	}

	private interface InternalState {
		State toExternalState();

		void start();

		void stop();

		void newTestReport(TestStatisticsMap testStatisticsMap);
	}

	private abstract class AbstractInternalState implements InternalState, State {

		protected final boolean isActiveState() {
			return getInternalState() == this;
		}

		public State toExternalState() {
			// We don't bother cloning the state, only the description varies.
			return this;
		}

		public void start() {
			// Valid transition for all states.
			setInternalState(new WaitingForTriggerState());
		}

		public void stop() {
			// Valid transition for all states.
			setInternalState(new StoppedState());
		}
	}

	private final class WaitingForTriggerState extends AbstractInternalState {
		public WaitingForTriggerState() {
			zero();
		}

		public void newTestReport(TestStatisticsMap testStatisticsMap) {
			if (m_properties.getIgnoreSampleCount() == 0) {
				setInternalState(new CapturingState());
			} else {
				setInternalState(new TriggeredState());
			}

			// Ensure the the first sample is recorded.
			getInternalState().newTestReport(testStatisticsMap);
		}

		public String getDescription() {
			return m_stateWaitingString;
		}

		public boolean isCapturing() {
			return false;
		}

		public boolean isStopped() {
			return false;
		}
	}

	private final class StoppedState extends AbstractInternalState {
		public void newTestReport(TestStatisticsMap testStatisticsMap) {
			// nothing to do
		}

		public String getDescription() {
			return m_stateStoppedString;
		}

		public boolean isStopped() {
			return true;
		}

		public boolean isCapturing() {
			return false;
		}
	}

	private abstract class AbstractSamplingState extends AbstractInternalState {
		// Guarded by this.
		private long mlastTime = 0;

		private volatile long msampleCount = 1;

		public void newTestReport(TestStatisticsMap testStatisticsMap) {
			(testStatisticsMap.new ForEach() {
				public void next(Test test, StatisticsSet statistics) {
					final SampleAccumulator sampleAccumulator = m_accumulators.get(test);
					synchronized (m_accumulators) {
						if (sampleAccumulator == null) {
							m_errorHandler.handleInformationMessage(m_unknownTestString + " " + test);
						} else {
							sampleAccumulator.addIntervalStatistics(statistics);

							if (shouldAccumulateSamples()) {
								sampleAccumulator.addCumulativeStaticstics(statistics);
							}

							if (!statistics.isComposite()) {
								m_totalSampleAccumulator.addIntervalStatistics(statistics);

								if (shouldAccumulateSamples()) {
									m_totalSampleAccumulator.addCumulativeStatistics(statistics);
								}
							}
						}
					}
				}
				// CHECKSTYLE:OFF
			}).iterate();
		}

		protected void schedule() {
			synchronized (this) {
				if (mlastTime == 0) {
					mlastTime = System.currentTimeMillis();
				}
			}

			m_timer.schedule(new TimerTask() {
				public void run() {
					sample();
				}
			}, m_properties.getSampleInterval());
		}

		public final void sample() {
			if (!isActiveState()) {
				return;
			}

			try {
				final long period;

				synchronized (this) {
					period = System.currentTimeMillis() - mlastTime;
				}

				final long sampleInterval = m_properties.getSampleInterval();
				SampleAccumulatorEx totalSampleAccumulatorSnapshot;
				synchronized (m_accumulators) {
					for (SampleAccumulator sampleAccumulator : m_accumulators.values()) {
						sampleAccumulator.fireSample(sampleInterval, period);
					}
					totalSampleAccumulatorSnapshot = new SampleAccumulatorEx(m_totalSampleAccumulator);
					m_totalSampleAccumulator.refreshIntervalStatistics(sampleInterval, period);
				}
				totalSampleAccumulatorSnapshot.fireSample(sampleInterval, period);
				++msampleCount;

				// I'm ignoring a minor race here: the model could have been
				// stopped
				// after the task was started.
				// We call setInternalState() even if the InternalState hasn't
				// changed since we've altered the sample count.
				if (getInternalState() instanceof StoppedState) {
					return;
				}

				setInternalState(nextState());

				m_listeners.apply(new ListenerSupport.Informer<Listener>() {
					public void inform(Listener l) {
						l.newSample();
					}
				});
			} finally {
				synchronized (this) {
					if (isActiveState()) {
						schedule();
					}
				}
			}
		}

		public final long getSampleCount() {
			return msampleCount;
		}

		protected abstract boolean shouldAccumulateSamples();

		protected abstract InternalState nextState();
	}

	private final class TriggeredState extends AbstractSamplingState {
		public TriggeredState() {
			schedule();
		}

		protected boolean shouldAccumulateSamples() {
			return false;
		}

		protected InternalState nextState() {
			if (getSampleCount() > m_properties.getIgnoreSampleCount()) {
				return new CapturingState();
			}

			return this;
		}

		public String getDescription() {
			return m_stateIgnoringString + getSampleCount();
		}

		public boolean isCapturing() {
			return false;
		}

		public boolean isStopped() {
			return false;
		}
	}

	private final class CapturingState extends AbstractSamplingState {
		public CapturingState() {
			zero();
			schedule();
		}

		protected boolean shouldAccumulateSamples() {
			return true;
		}

		protected InternalState nextState() {
			final int collectSampleCount = m_properties.getCollectSampleCount();

			if (collectSampleCount != 0 && getSampleCount() > collectSampleCount) {
				return new StoppedState();
			}

			return this;
		}

		public String getDescription() {
			return m_stateCapturingString + getSampleCount();
		}

		public boolean isCapturing() {
			return true;
		}

		public boolean isStopped() {
			return false;
		}
	}

	public ModelTestIndex getModelTestIndex() {
		return modelTestIndex;
	}

	public StatisticsIndexMap.LongIndex getPeriodIndex() {
		return m_periodIndex;
	}

	public int getSampleInterval() {
		return m_properties.getSampleInterval();
	}
}
