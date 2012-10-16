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


import org.hyperic.sigar.OperatingSystem;
import org.hyperic.sigar.Sigar;
import org.ngrinder.monitor.MonitorConstants;
import org.ngrinder.monitor.agent.mxbean.SystemMonitoringData;
import org.ngrinder.monitor.share.domain.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * System data collector class.
 *
 * @author Mavlarn
 * @since 2.0
 */
public class AgentSystemDataCollector extends AgentDataCollector {
	private static final Logger LOG = LoggerFactory.getLogger(AgentSystemDataCollector.class);

	private Sigar sigar = null;

	@Override
	public synchronized long refresh() {
		if (sigar == null) {
			sigar = new Sigar();
		}
		return sigar.getPid();
	}


	@Override
	public void run() {
		if (sigar == null) {
			sigar = new Sigar();
		}
		SystemMonitoringData systemMonitoringData = (SystemMonitoringData) getMXBean(MonitorConstants.SYSTEM);
		SystemInfo systemInfo = execute();
		//systemMonitoringData.addNotification(systemInfo);
		systemMonitoringData.setSystemInfo(systemInfo);
	}

	/**
	 * Execute the collector to get the system info model.
	 * @return SystemInfo in current time
	 */
	public synchronized SystemInfo execute() {
		SystemInfo systemInfo = new SystemInfo();
		try {
			systemInfo.setCPUUsedPercentage((float) sigar.getCpuPerc().getCombined() * 100);
			systemInfo.setTotalCpuValue(sigar.getCpu().getTotal());
			systemInfo.setIdleCpuValue(sigar.getCpu().getIdle());
			systemInfo.setTotalMemory(sigar.getMem().getTotal() / 1024L);
			systemInfo.setFreeMemory(sigar.getMem().getFree() / 1024L);
			if (OperatingSystem.IS_WIN32) {
				systemInfo.setSystem(SystemInfo.System.WINDOW);
			} else {
				systemInfo.setLoadAvgs(sigar.getLoadAverage());
				systemInfo.setSystem(SystemInfo.System.LINUX);
			}
		} catch (Throwable e) {
			LOG.error("Error while getting system perf data:{}", e.getMessage());
			LOG.debug("Error trace is ", e);
		}
		systemInfo.setCollectTime(System.currentTimeMillis());
		return systemInfo;
	}
}
