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
package org.ngrinder.extension;

import net.grinder.statistics.ImmutableStatisticsSet;

import org.ngrinder.model.PerfTest;
import org.ngrinder.service.IPerfTestService;
import org.ngrinder.service.ISingleConsole;

import ro.fortsoft.pf4j.ExtensionPoint;

/**
 * Plugin extension point for {@link PerfTest} control by examining each sampling statistics.
 * 
 * Ex) You can finish the test if the response byte is over than 1G bps.
 * 
 * @author JunHo Yoon
 * @since 3.0.3
 */
public interface OnTestSamplingRunnable extends ExtensionPoint {

	/**
	 * Callback method which will be invoked when the given {@link PerfTest} test sampling is
	 * started.
	 * 
	 * @param singleConsole		{@link ISingleConsole}
	 * @param perfTest			{@link PerfTest}
	 * @param perfTestService	{@link IPerfTestService}
	 */
	public void startSampling(ISingleConsole singleConsole, PerfTest perfTest, IPerfTestService perfTestService);

	/**
	 * Callback method which will be invoked whenever test sampling is performed.
	 * 
	 * @param singleConsole			{@link ISingleConsole}
	 * @param perfTest				{@link PerfTest}
	 * @param perfTestService		{@link IPerfTestService}
	 * @param intervalStatistics	sampling statistics on this time
	 * @param cumulativeStatistics	cumulative sampling statistics.
	 */
	public void sampling(ISingleConsole singleConsole, PerfTest perfTest, IPerfTestService perfTestService,
					ImmutableStatisticsSet intervalStatistics, ImmutableStatisticsSet cumulativeStatistics);

	/**
	 * Callback method which will be invoked when the given {@link PerfTest} test sampling is
	 * finished.
	 * 
	 * @param singleConsole		{@link ISingleConsole}
	 * @param perfTest			{@link PerfTest}
	 * @param perfTestService	{@link IPerfTestService}
	 */
	public void endSampling(ISingleConsole singleConsole, PerfTest perfTest, IPerfTestService perfTestService);

}
