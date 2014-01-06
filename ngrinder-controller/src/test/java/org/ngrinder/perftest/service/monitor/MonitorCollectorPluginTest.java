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
package org.ngrinder.perftest.service.monitor;

import com.google.common.collect.Lists;
import net.grinder.SingleConsole;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.common.constants.AgentConstants;
import org.ngrinder.common.util.ThreadUtils;
import org.ngrinder.infra.schedule.ScheduledTaskService;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.model.PerfTest;
import org.ngrinder.perftest.service.AbstractAgentReadyTest;
import org.ngrinder.perftest.service.PerfTestService;
import org.ngrinder.perftest.service.samplinglistener.MonitorCollectorPlugin;
import org.ngrinder.service.IConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Monitor Task Test
 */
public class MonitorCollectorPluginTest extends AbstractAgentReadyTest implements AgentConstants {
	public static final int DEFAULT_MONITOR_PORT = 13243;
	private File tempReport;

	@Autowired
	ScheduledTaskService scheduledTaskService;

	@Autowired
	CacheManager cacheManager;

	@Autowired
	PerfTestService perfTestService;

	@Before
	public void before() {
		tempReport = new File(System.getProperty("java.io.tmpdir"), "tmp-report");
		FileUtils.deleteQuietly(tempReport);
		tempReport.mkdir();
	}


	@After
	public void after() {
		tempReport.deleteOnExit();
	}

	@Test
	public void testAddMonitor() throws IOException {
		PerfTest perfTest = mock(PerfTest.class);
		when(perfTest.getTargetHostIP()).thenReturn(Lists.newArrayList("127.0.0.1"));
		when(perfTest.getSamplingInterval()).thenReturn(2);
		SingleConsole singleConsole = mock(SingleConsole.class);
		when(singleConsole.getReportPath()).thenReturn(tempReport);
		MonitorCollectorPlugin monitorCollectorPlugin = new MonitorCollectorPlugin(config, scheduledTaskService,
				perfTestService, 1L) {
			@Override
			protected int getPort(IConfig config) {
				return 13243;
			}
		};
		monitorCollectorPlugin.startSampling(singleConsole, perfTest, perfTestService);
		ThreadUtils.sleep(2400);
		monitorCollectorPlugin.sampling(null, null, null, null, null);
		monitorCollectorPlugin.endSampling(null, null, null);
	}

}
