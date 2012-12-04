/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ngrinder.extension;

import net.grinder.statistics.ImmutableStatisticsSet;

import org.ngrinder.model.PerfTest;
import org.ngrinder.service.IPerfTestService;
import org.ngrinder.service.ISingleConsole;

/**
 * Plugin extension for {@link PerfTest} control by examining sampling statistics.
 * 
 * Ex) You can finish the test if the response byte is over than 1G bps.
 * 
 * @author JunHo Yoon
 * @since 3.0.3
 */
public interface OnTestSamplingRunnable {

	/**
	 * Callback method which will be invoked when the given {@link PerfTest} test sampling is
	 * started.
	 * 
	 * @param singleConsole
	 *            {@link ISingleConsole}
	 * @param perfTest
	 *            {@link PerfTest}
	 * @param perfTestService
	 *            {@link IPerfTestService}
	 */
	public void startSampling(ISingleConsole singleConsole, PerfTest perfTest, IPerfTestService perfTestService);

	/**
	 * Callback method which will be invoked whenever test sampling is performed.
	 * 
	 * @param singleConsole
	 *            {@link ISingleConsole}
	 * @param perfTest
	 *            {@link PerfTest}
	 * @param perfTestService
	 *            {@link IPerfTestService}
	 * @param intervalStatistics
	 *            sampling statistics on this time
	 * @param cumulativeStatistics
	 *            cumulative sampling statistics.
	 */
	public void sampling(ISingleConsole singleConsole, PerfTest perfTest, IPerfTestService perfTestService,
					ImmutableStatisticsSet intervalStatistics, ImmutableStatisticsSet cumulativeStatistics);

	/**
	 * Callback method which will be invoked when the given {@link PerfTest} test sampling is
	 * finished.
	 * 
	 * @param singleConsole
	 *            {@link ISingleConsole}
	 * @param perfTest
	 *            {@link PerfTest}
	 * @param perfTestService
	 *            {@link IPerfTestService}
	 */
	public void endSampling(ISingleConsole singleConsole, PerfTest perfTest, IPerfTestService perfTestService);

}
