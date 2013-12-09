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

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.infra.schedule.ScheduledTaskService;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.monitor.MonitorConstants;
import org.ngrinder.perftest.service.AbstractAgentReadyTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Monitor Task Test
 */
public class MonitorScheduledTaskTest extends AbstractAgentReadyTest {
	private File tempReport;

	@Autowired
	ScheduledTaskService scheduledTaskService;

	@Autowired
	CacheManager cacheManager;

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
		AgentInfo monitorAgt = new AgentInfo();
		monitorAgt.setIp("127.0.0.1");
		monitorAgt.setPort(MonitorConstants.DEFAULT_MONITOR_PORT);
		Set<AgentInfo> agents = new HashSet<AgentInfo>(2);
		agents.add(monitorAgt);
		MonitorScheduledTask monitorScheduledTask = new MonitorScheduledTask(cacheManager, perfTestService);
		monitorScheduledTask.add(agents, tempReport);
		monitorScheduledTask.run();
		monitorScheduledTask.saveData(false);
		// test to add again
		monitorScheduledTask.add(agents, tempReport);
		monitorScheduledTask.close();
	}

	@Test
	public void testAddMonitorInvalid() throws IOException {
		AgentInfo monitorAgt = new AgentInfo();
		monitorAgt.setIp("10.10.10.10");
		monitorAgt.setPort(MonitorConstants.DEFAULT_MONITOR_PORT);
		AgentInfo monitorAgt2 = new AgentInfo();
		monitorAgt2.setIp("localhost");
		monitorAgt2.setPort(MonitorConstants.DEFAULT_MONITOR_PORT);
		Set<AgentInfo> agents = new HashSet<AgentInfo>(2);
		agents.add(monitorAgt);
		agents.add(monitorAgt2);
		MonitorScheduledTask monitorScheduledTask = new MonitorScheduledTask(cacheManager, perfTestService);
		monitorScheduledTask.add(agents, tempReport);
		monitorScheduledTask.run();
		monitorScheduledTask.saveData(false);
		monitorScheduledTask.close();
	}
}
