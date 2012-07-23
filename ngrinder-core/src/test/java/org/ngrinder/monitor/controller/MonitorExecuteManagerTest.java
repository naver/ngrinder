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

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.ngrinder.common.util.ThreadUtil;
import org.ngrinder.monitor.controller.domain.MonitorAgentInfo;

/**
 * Class description.
 * 
 * @author Tobi
 * @since
 * @date 2012-7-20
 */
public class MonitorExecuteManagerTest {

	@Test
	public void getMonitorData() {
		Set<MonitorAgentInfo> agentInfo = new HashSet<MonitorAgentInfo>();
		MonitorAgentInfo monitorAgentInfo = MonitorAgentInfo.getAgentMonitor("127.0.0.1", 4096,
				new MonitorRecoderDemo());
		agentInfo.add(monitorAgentInfo);
		MonitorExecuteManager monitorExecuteManager = new MonitorExecuteManager("127.0.0.1", 2, 1, agentInfo);
		monitorExecuteManager.start();
		ThreadUtil.sleep(10000);
	}
}
