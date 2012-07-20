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
package org.ngrinder.monitor.agent.mxbean;

import org.ngrinder.monitor.agent.collector.AgentDataCollector;
import org.ngrinder.monitor.agent.collector.AgentSystemDataCollector;
import org.ngrinder.monitor.agent.mxbean.core.MXBean;
import org.ngrinder.monitor.agent.mxbean.core.SystemMXBean;
import org.ngrinder.monitor.share.domain.SystemInfo;

public class SystemMonitoringData extends MXBean implements SystemMXBean {
	// private static final Log LOG =
	// LogFactory.getLog(SystemMonitoringData.class);

	private static final long serialVersionUID = -3568524400212296536L;
	private SystemInfo systemInfo;

	private AgentSystemDataCollector agentSystemDataCollector = new AgentSystemDataCollector();

	@Override
	public AgentDataCollector gainAgentDataCollector() {
		agentSystemDataCollector.refresh();
		return agentSystemDataCollector;
	}

	public void setSystemInfo(SystemInfo systemInfo) {
		this.systemInfo = systemInfo;
	}

	@Override
	public SystemInfo getSystemInfo() {
		return systemInfo;
	}

}
