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
package org.ngrinder.monitor.agent.collector;

import java.util.concurrent.Callable;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.ngrinder.monitor.MonitorConstants;
import org.ngrinder.monitor.agent.collector.process.LinuxSystemMonitorProcessor;
import org.ngrinder.monitor.agent.collector.process.WindowSystemMonitorProcessor;
import org.ngrinder.monitor.agent.mxbean.SystemMonitoringData;
import org.ngrinder.monitor.share.domain.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentSystemDataCollector extends AgentDataCollector {
	private static final Logger LOG = LoggerFactory.getLogger(AgentSystemDataCollector.class);

	private Callable<SystemInfo> processor = null;
	private long count = 0;

	@Override
	public synchronized void refresh() {

		final String osSystem = System.getProperty("os.name");
		// ExecutorService executor = Executors.newSingleThreadExecutor();
		if (osSystem.toLowerCase().indexOf("windows") > -1) {
			// windows
			processor = new WindowSystemMonitorProcessor();
		} else {
			// linux
			processor = new LinuxSystemMonitorProcessor();
		}

	}

	@Override
	public void run() {

		SystemMonitoringData systemMonitoringData = (SystemMonitoringData) getMXBean(MonitorConstants.SYSTEM);
		SystemInfo systemInfo = execute();

		LOG.debug("systemInfo ifno: {}", ToStringBuilder.reflectionToString(systemInfo));
		systemMonitoringData.addNotification(systemInfo);
		systemMonitoringData.setSystemInfo(systemInfo);
	}

	public synchronized SystemInfo execute() {

		SystemInfo systemInfo = new SystemInfo();
		try {
			systemInfo = processor.call();
		} catch (Exception e) {
			if ((count++) % 60 == 0) {
				if (SystemUtils.IS_OS_WINDOWS) {
					LOG.error("Error while getting system perf data");
					LOG.error("You should run agent in administrator permission");
				} else {
					LOG.error("Error while getting system perf data", e);
				}
			}
		}
		systemInfo.setCollectTime(System.currentTimeMillis());

		return systemInfo;
	}
}
