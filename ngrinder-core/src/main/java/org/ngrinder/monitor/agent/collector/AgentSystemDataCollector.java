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
	public synchronized void refresh() {
		if (sigar == null) {
			sigar = new Sigar();
		}
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
