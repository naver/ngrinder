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
