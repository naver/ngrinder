// Copyright (C) 2000 - 2012 Philip Aston
// All rights reserved.
//
// This file is part of The Grinder software distribution. Refer to
// the file LICENSE which is part of The Grinder distribution for
// licensing details. The Grinder distribution is available on the
// Internet at http://grinder.sourceforge.net/
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
// FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
// COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
// OF THE POSSIBILITY OF SUCH DAMAGE.
package net.grinder.console.model;

import net.grinder.statistics.PeakStatisticExpression;
import net.grinder.statistics.StatisticsSet;
import net.grinder.statistics.StatisticsIndexMap;
import net.grinder.statistics.StatisticsSetFactory;
import net.grinder.util.ListenerSupport;

/**
 * Extended {@link SampleAccumulator} class to enable snapshot of existing {@link SampleAccumulator}
 * object. .
 *
 * @author Grinder Developers.
 * @author JunHo Yoon (modified for nGrinder)
 * @since 3.1.3
 */
public class SampleAccumulatorEx implements Cloneable {

	private ListenerSupport<SampleListener> m_listeners = new ListenerSupport<SampleListener>();

	private final PeakStatisticExpression m_peakTPSExpression;
	private final StatisticsIndexMap.LongIndex m_periodIndex;
	private final StatisticsSetFactory m_statisticsSetFactory;

	private final StatisticsSet m_cumulativeStatistics;
	private StatisticsSet m_intervalStatistics;
	private StatisticsSet m_lastSampleStatistics;

	/**
	 * Constructor.
	 * 
	 * @param peakTPSExpression		peakTPSExpression
	 * @param periodIndex			periodIndex
	 * @param statisticsSetFactory	statisticsSetFactory
	 */
	public SampleAccumulatorEx(PeakStatisticExpression peakTPSExpression, StatisticsIndexMap.LongIndex periodIndex,
					StatisticsSetFactory statisticsSetFactory) {

		m_peakTPSExpression = peakTPSExpression;
		m_periodIndex = periodIndex;
		m_statisticsSetFactory = statisticsSetFactory;

		m_cumulativeStatistics = m_statisticsSetFactory.create();
		m_intervalStatistics = m_statisticsSetFactory.create();
		m_lastSampleStatistics = m_statisticsSetFactory.create();
	}

	/**
	 * Constructor which copies the given {@link SampleAccumulatorEx} object.
	 * 
	 * @param original	{@link SampleAccumulatorEx} original object.
	 */
	public SampleAccumulatorEx(SampleAccumulatorEx original) {
		m_peakTPSExpression = original.m_peakTPSExpression;
		m_periodIndex = original.m_periodIndex;
		m_statisticsSetFactory = original.m_statisticsSetFactory;

		m_cumulativeStatistics = original.m_cumulativeStatistics.snapshot();
		m_intervalStatistics = original.m_intervalStatistics.snapshot();
		m_lastSampleStatistics = original.m_lastSampleStatistics.snapshot();
		m_listeners = original.m_listeners;
	}

	/**
	 * Add sampling listener.
	 * 
	 * @param listener	sample listener
	 */
	public void addSampleListener(SampleListener listener) {
		m_listeners.add(listener);
	}

	/**
	 * Add the interval statistics.
	 * 
	 * @param report	report
	 */
	public void addIntervalStatistics(StatisticsSet report) {
		m_intervalStatistics.add(report);
	}

	/**
	 * Add the cumulative statistics.
	 * 
	 * @param report	report
	 */
	public void addCumulativeStatistics(StatisticsSet report) {
		m_cumulativeStatistics.add(report);
	}

	/**
	 * Fire sampling.
	 * 
	 * @param sampleInterval	sampling interval
	 * @param period			period
	 */
	public void fireSample(long sampleInterval, long period) {
		m_intervalStatistics.setValue(m_periodIndex, sampleInterval);
		m_cumulativeStatistics.setValue(m_periodIndex, period);
		m_listeners.apply(new ListenerSupport.Informer<SampleListener>() {
			public void inform(SampleListener l) {
				l.update(m_intervalStatistics, m_cumulativeStatistics);
			}
		});
		m_lastSampleStatistics = m_intervalStatistics;

		// We create new statistics each time to ensure that
		// m_lastSampleStatistics is always valid and fixed.
	}

	/**
	 * Start new interval statistics.
	 * 
	 * @param sampleInterval	sampleInterval
	 * @param period			period
	 */
	public void refreshIntervalStatistics(long sampleInterval, long period) {
		m_intervalStatistics.setValue(m_periodIndex, sampleInterval);
		m_cumulativeStatistics.setValue(m_periodIndex, period);
		m_peakTPSExpression.update(m_intervalStatistics, m_cumulativeStatistics);
		m_intervalStatistics = m_statisticsSetFactory.create();
	}

	/**
	 * Reset all statistics.
	 */
	public void zero() {
		m_intervalStatistics.reset();
		m_lastSampleStatistics.reset();
		m_cumulativeStatistics.reset();
	}

	@SuppressWarnings("UnusedDeclaration")
	public StatisticsSet getLastSampleStatistics() {
		return m_lastSampleStatistics;
	}

	public StatisticsSet getCumulativeStatistics() {
		return m_cumulativeStatistics;
	}
}
