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
package net.grinder;

import net.grinder.common.processidentity.WorkerProcessReport;
import net.grinder.console.communication.ProcessControl.ProcessReports;
import net.grinder.console.model.SampleModelImplementationEx;
import net.grinder.statistics.StatisticExpression;
import net.grinder.statistics.StatisticsSet;
import org.junit.Test;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.util.ThreadUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SingleConsoleTest {
	double errorCount = 0;

	/**
	 * Returns a free port number on localhost, or -1 if unable to find a free port.
	 *
	 * @return a free port number on localhost, or -1 if unable to find a free port
	 */
	public int getFreePort() {
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(0);
			return socket.getLocalPort();
		} catch (IOException e) {
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
		return -1;
	}

	@Test
	public void testSingleConsoleTooManyError() {
		SingleConsole singleConsole = new SingleConsole(getFreePort()) {
			@Override
			public long getCurrentExecutionCount() {
				return 10;
			}

			@Override
			public Map<String, Object> getStatisticsData() {
				Map<String, Object> newMap = new HashMap<String, Object>();
				Map<Object, Object> errorMap = new HashMap<Object, Object>();

				errorMap.put("Errors", errorCount);
				newMap.put("totalStatistics", errorMap);
				return newMap;
			}
		};
		errorCount = 1;
		assertThat(singleConsole.hasTooManyError(), is(false));
		errorCount = 3;
		assertThat(singleConsole.hasTooManyError(), is(true));
	}

	public Date current = new Date();

	@Test
	public void testCurrentRunningTimeOverDuration() {
		SingleConsole singleConsole = new SingleConsole(getFreePort()) {

			public long getCurrentRunningTime() {
				return new Date().getTime() - current.getTime();
			}
		};
		ThreadUtils.sleep(200);
		assertThat(singleConsole.isCurrentRunningTimeOverDuration(1000), is(false));
		ThreadUtils.sleep(800);
		assertThat(singleConsole.isCurrentRunningTimeOverDuration(1000), is(true));
	}

	public boolean canceled = false;

	@Test
	public void testWaitUnitAgentConnected() {
		SingleConsole singleConsole = new SingleConsole(getFreePort()) {
			@Override
			public boolean isCanceled() {
				return canceled;
			}
		};
		ProcessReports report = mock(ProcessReports.class);
		WorkerProcessReport workerProcessReport = mock(WorkerProcessReport.class);
		WorkerProcessReport workerProcessReport2 = mock(WorkerProcessReport.class);
		WorkerProcessReport[] workerProcessReports = new WorkerProcessReport[]{workerProcessReport,
				workerProcessReport2};
		when(workerProcessReport.getNumberOfRunningThreads()).thenReturn((short) 3);
		when(workerProcessReport2.getNumberOfRunningThreads()).thenReturn((short) 2);

		when(report.getWorkerProcessReports()).thenReturn(workerProcessReports);

		ProcessReports[] processReports = new ProcessReports[]{report};
		singleConsole.update(processReports);
		assertThat(singleConsole.getRunningProcess(), is(2));
		assertThat(singleConsole.getRunningThread(), is(5));

		processReports = new ProcessReports[]{};
		singleConsole.waitUntilAgentConnected(1);
		singleConsole.update(processReports);
		try {
			singleConsole.waitUntilAgentConnected(1);
			fail("Should throw Exception");
		} catch (NGrinderRuntimeException e) {
			//
		}

		singleConsole.waitUntilAllAgentDisconnected();

		processReports = new ProcessReports[]{report};
		singleConsole.update(processReports);
		try {
			singleConsole.waitUntilAllAgentDisconnected();
			fail("Should throw Exception");
		} catch (NGrinderRuntimeException e) {
			//
		}
	}

	@Test
	public void testTpsValue() {
		SingleConsole singleConsole = new SingleConsole(getFreePort());
		singleConsole.setTpsValue(10);
		assertThat(singleConsole.getTpsValues(), is(10D));
		singleConsole.setTpsValue(12);
		assertThat(singleConsole.getTpsValues(), is(12D));
		singleConsole.setTpsValue(8);
		assertThat(singleConsole.getPeakTpsForGraph(), is(12D));
	}

	public double testCount = 10D;

	@Test
	public void testCurrentExecutionCount() {
		SingleConsole singleConsole = new SingleConsole(getFreePort()) {
			@Override
			public long getCurrentExecutionCount() {
				return 10;
			}

			@Override
			public Map<String, Object> getStatisticsData() {
				Map<String, Object> newMap = new HashMap<String, Object>();
				Map<Object, Object> errorMap = new HashMap<Object, Object>();
				errorMap.put("Tests", testCount);
				errorMap.put("Errors", errorCount);
				newMap.put("totalStatistics", errorMap);
				return newMap;
			}
		};
		assertThat(singleConsole.getCurrentExecutionCount(), is((long) (testCount + errorCount)));
	}

	@Test
	public void testUpdate() {
		SingleConsole singleConsole = new SingleConsole(getFreePort()) {
			@Override
			public long getCurrentRunningTime() {
				return 2000;
			}

			@Override
			public Map<String, Object> getStatisticsData() {
				Map<String, Object> newMap = new HashMap<String, Object>();
				Map<Object, Object> errorMap = new HashMap<Object, Object>();
				errorMap.put("Tests", testCount);
				errorMap.put("Errors", errorCount);
				newMap.put("totalStatistics", errorMap);
				return newMap;
			}

			@Override
			protected void updateStatistics(StatisticsSet intervalStatisticsSnapshot,
			                                StatisticsSet accumulatedStatisticsSnapshot) {
			}

		};

		singleConsole.update(null, null);
		singleConsole.startSampling();

		SampleModelImplementationEx sampleModelMock = mock(SampleModelImplementationEx.class);

		singleConsole.setSampleModel(sampleModelMock);
		StatisticExpression exp = mock(StatisticExpression.class);
		StatisticsSet statisticMock = mock(StatisticsSet.class);
		StatisticsSet statisticAccumulatedMock = mock(StatisticsSet.class);
		when(statisticMock.snapshot()).thenReturn(statisticMock);
		when(statisticAccumulatedMock.snapshot()).thenReturn(statisticAccumulatedMock);
		when(exp.getDoubleValue(any(StatisticsSet.class))).thenReturn(3D);
		when(sampleModelMock.getTPSExpression()).thenReturn(exp);

		singleConsole.update(statisticMock, statisticAccumulatedMock);
		singleConsole.update(statisticMock, statisticAccumulatedMock);

	}
}
