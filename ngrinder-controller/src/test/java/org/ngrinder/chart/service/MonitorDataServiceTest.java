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
package org.ngrinder.chart.service;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.agent.model.AgentInfo;
import org.ngrinder.common.util.ThreadUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Class description.
 * 
 * @author Tobi
 * @since
 * @date 2012-7-23
 */
public class MonitorDataServiceTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private MonitorDataService monitorDataService;

	@Test
	public void testAddRemoveMonitorAgents() {
		Set<AgentInfo> agents = new HashSet<AgentInfo>();
		AgentInfo agt = new AgentInfo();
		agt.setIp("127.0.0.1");
		agents.add(agt);

		monitorDataService.addMonitorAgents("127.0.0.1_test", agents);

		ThreadUtil.sleep(3000);
		monitorDataService.removeMonitorAgents("127.0.0.1_test");
	}

}
