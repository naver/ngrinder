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
package org.ngrinder.monitor.service;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.ngrinder.agent.model.AgentInfo;
import org.ngrinder.common.util.ThreadUtil;
import org.ngrinder.monitor.MonitorConstants;
import org.ngrinder.perftest.service.AbstractPerfTestTransactionalTest;

/**
 * Class description.
 *
 * @author Mavlarn
 * @since
 */
public class MontorClientManagerTest extends AbstractPerfTestTransactionalTest {

	@Test
	public void testAddMonitor() throws IOException {
		File tempRepo = new File(System.getProperty("java.io.tmpdir"), "test-repo");
		tempRepo.mkdir();
		tempRepo.deleteOnExit();
		AgentInfo monitorAgt = new AgentInfo();
		monitorAgt.setIp("127.0.0.1");
		monitorAgt.setPort(MonitorConstants.DEFAULT_MONITOR_PORT);
		Set<AgentInfo> agents = new HashSet<AgentInfo>(2);
		agents.add(monitorAgt);
		MontorClientManager monitorMngr = applicationContext.getBean(MontorClientManager.class);
		monitorMngr.add(agents, tempRepo);
		new Thread(monitorMngr).start();
		monitorMngr.saveData();
		
		ThreadUtil.sleep(3000);
		// test to add again
		monitorMngr.add(agents, tempRepo);

		ThreadUtil.sleep(3000);
		monitorMngr.destroy();
	}

	@Test
	public void testAddMonitorInvalid() throws IOException {
		File tempRepo = new File(System.getProperty("java.io.tmpdir"), "test-repo");
		tempRepo.mkdir();
		tempRepo.deleteOnExit();
		AgentInfo monitorAgt = new AgentInfo();
		monitorAgt.setIp("10.10.10.10");
		monitorAgt.setPort(MonitorConstants.DEFAULT_MONITOR_PORT);
		AgentInfo monitorAgt2 = new AgentInfo();
		monitorAgt2.setIp("localhost");
		monitorAgt2.setPort(MonitorConstants.DEFAULT_MONITOR_PORT);
		Set<AgentInfo> agents = new HashSet<AgentInfo>(2);
		agents.add(monitorAgt);
		agents.add(monitorAgt2);
		MontorClientManager monitorMngr = applicationContext.getBean(MontorClientManager.class);
		monitorMngr.add(agents, tempRepo);
		new Thread(monitorMngr).start();
		monitorMngr.saveData();
		ThreadUtil.sleep(3000);
		monitorMngr.destroy();
	}
}
