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
package org.ngrinder.monitor.controller;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.SigarTestBase;
import org.ngrinder.common.util.ThreadUtil;
import org.ngrinder.monitor.MonitorConstants;
import org.ngrinder.monitor.agent.AgentMonitorServer;
import org.ngrinder.monitor.controller.domain.MonitorAgentInfo;

/**
 * Class description.
 * 
 * @author Tobi
 * @since 3.0
 * @date 2012-7-20
 */
public class MonitorExecuteManagerTest extends SigarTestBase {

	@Before
	public void startMonitorServer() throws IOException {
		AgentMonitorServer.getInstance().init();
		AgentMonitorServer.getInstance().start();
	}

	@After
	public void stopMonitorServer() throws IOException {
		AgentMonitorServer.getInstance().stop();
	}

	@Test
	public void getMonitorData() {

		int port = AgentMonitorServer.getInstance().getPort();
		assertTrue(MonitorConstants.DEFAULT_MONITOR_PORT == port);
		assertTrue(AgentMonitorServer.getInstance().isRunning());

		MonitorRecoderDemo mrd = new MonitorRecoderDemo();
		MonitorAgentInfo monitorAgentInfo = MonitorAgentInfo.getSystemMonitor("127.0.0.1",
						MonitorConstants.DEFAULT_MONITOR_PORT, mrd);

		MonitorExecuteManager.getInstance().addAgentMonitor("127.0.0.1", monitorAgentInfo);

		assertTrue(mrd.isRunning());
		ThreadUtil.sleep(3000);
		int recordCount = mrd.getData().size();
		assertTrue(recordCount > 0);

		MonitorExecuteManager.getInstance().removeAgentMonitor("127.0.0.1");
		recordCount = mrd.getData().size(); // get latest record count
		ThreadUtil.sleep(3000);
		assertTrue(!mrd.isRunning());

		// make sure no monitoring record saved anymore
		assertThat(mrd.getData().size(), is(recordCount));

		// test adding 2 monitoring job on same target
		MonitorExecuteManager.getInstance().addAgentMonitor("127.0.0.1", monitorAgentInfo);
		ThreadUtil.sleep(2000);
		MonitorExecuteManager.getInstance().addAgentMonitor("127.0.0.1", monitorAgentInfo);
		ThreadUtil.sleep(2000);

		MonitorExecuteManager.getInstance().removeAgentMonitor("127.0.0.1");
		assertTrue(mrd.isRunning()); // monitoring job is still running
		MonitorExecuteManager.getInstance().removeAgentMonitor("127.0.0.1");
		assertTrue(!mrd.isRunning());
	}
}
