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

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.common.util.ThreadUtil;
import org.ngrinder.monitor.agent.MockAgentServer;
import org.ngrinder.monitor.controller.domain.MonitorAgentInfo;

/**
 * Class description.
 * 
 * @author Tobi
 * @since 3.0
 * @date 2012-7-20
 */
public class MonitorExecuteManagerTest {

	@Before
	public void start() throws MalformedObjectNameException, InstanceAlreadyExistsException,
			MBeanRegistrationException, NotCompliantMBeanException, IOException {
		MockAgentServer.startServer();
	}

	@Test
	public void getMonitorData() {
		Set<MonitorAgentInfo> agentInfo = new HashSet<MonitorAgentInfo>();
		MonitorRecoderDemo mrd = new MonitorRecoderDemo();
		MonitorAgentInfo monitorAgentInfo = MonitorAgentInfo.getSystemMonitor("127.0.0.1",
				MockAgentServer.MOCK_MONITOR_AGENT_PORT, mrd);
		agentInfo.add(monitorAgentInfo);
		MonitorExecuteManager monitorExecuteManager = new MonitorExecuteManager("127.0.0.1", 1, 1, agentInfo);
		monitorExecuteManager.start();
		Assert.assertTrue(mrd.isRunning());
		ThreadUtil.sleep(5000);
		Assert.assertTrue(!mrd.getData().isEmpty());
		monitorExecuteManager.stop();
		Assert.assertFalse(mrd.isRunning());
		fail("NEVER NEVER Write this kind of test code please");
	}
}
