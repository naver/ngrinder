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
package org.ngrinder.monitor.controller;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;

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
public class MonitorExecuteManagerTest extends SigarTestBase{

	@Before
	public void startMonitorServer() throws MalformedObjectNameException, InstanceAlreadyExistsException,
			MBeanRegistrationException, NotCompliantMBeanException, IOException {
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
		ThreadUtil.sleep(3000);
		assertTrue(!mrd.isRunning());
		
		//make sure no monitoring record saved anymore
		assertTrue(mrd.getData().size() == recordCount);
		
		//test adding 2 monitoring job on same target
		MonitorExecuteManager.getInstance().addAgentMonitor("127.0.0.1", monitorAgentInfo);
		ThreadUtil.sleep(2000);
		MonitorExecuteManager.getInstance().addAgentMonitor("127.0.0.1", monitorAgentInfo);
		ThreadUtil.sleep(2000);

		MonitorExecuteManager.getInstance().removeAgentMonitor("127.0.0.1");
		assertTrue(mrd.isRunning()); //monitoring job is still running
		MonitorExecuteManager.getInstance().removeAgentMonitor("127.0.0.1");
		assertTrue(!mrd.isRunning());
	}
}
