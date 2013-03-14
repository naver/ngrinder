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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.hyperic.sigar.Cpu;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.OperatingSystem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.ngrinder.common.util.NoOp;
import org.ngrinder.monitor.MonitorConstants;
import org.ngrinder.monitor.agent.mxbean.SystemMonitoringData;
import org.ngrinder.monitor.share.domain.BandWidth;
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
	private static final Logger LOGGER = LoggerFactory.getLogger(AgentSystemDataCollector.class);

	private Sigar sigar = null;

	private SystemInfo prev = null;

	private String[] netInterfaces = new String[] {};
	
	private String agentHomeDir = null;
	private String customDataFileName = null;
	private File customDataFile = null;
	
	public void setAgentHome(String agentHome) {
		agentHomeDir = agentHome;
		customDataFileName = agentHomeDir + File.separator + "monitor" + File.separator + "custom.data";
	}

	@Override
	public synchronized void refresh() {
		initSigar();
	}

	private void initSigar() {
		if (sigar == null) {
			sigar = new Sigar();
			try {
				netInterfaces = sigar.getNetInterfaceList();
				prev = new SystemInfo();
				prev.setBandWidth(getNetworkUsage());
			} catch (SigarException e) {
				LOGGER.error("Network usage data retrieval failed.", e);
			}
		}
	}

	@Override
	public void run() {
		initSigar();
		SystemMonitoringData systemMonitoringData = (SystemMonitoringData) getMXBean(MonitorConstants.SYSTEM);
		systemMonitoringData.setSystemInfo(execute());
	}

	/**
	 * Execute the collector to get the system info model.
	 * 
	 * @return SystemInfo in current time
	 */
	public synchronized SystemInfo execute() {
		SystemInfo systemInfo = new SystemInfo();
		systemInfo.setCollectTime(System.currentTimeMillis());
		try {
			BandWidth networkUsage = getNetworkUsage();
			BandWidth bandWidth = networkUsage.adjust(prev.getBandWidth());
			systemInfo.setBandWidth(bandWidth);
			systemInfo.setCPUUsedPercentage((float) sigar.getCpuPerc().getCombined() * 100);
			Cpu cpu = sigar.getCpu();
			systemInfo.setTotalCpuValue(cpu.getTotal());
			systemInfo.setIdleCpuValue(cpu.getIdle());
			Mem mem = sigar.getMem();
			systemInfo.setTotalMemory(mem.getTotal() / 1024L);
			systemInfo.setFreeMemory(mem.getFree() / 1024L);
			systemInfo.setSystem(OperatingSystem.IS_WIN32 ? SystemInfo.System.WINDOW : SystemInfo.System.LINUX);
			
			systemInfo.setCustomValues(getCustomizedMonitorData());
		} catch (Throwable e) {
			LOGGER.error("Error while getting system perf data:{}", e.getMessage());
			LOGGER.debug("Error trace is ", e);
		}
		prev = systemInfo;
		return systemInfo;
	}

	/**
	 * Get the current network usage.
	 * 
	 * @return BandWith
	 * @throws SigarException
	 *             occurs when the underlying lib is not linked
	 */
	public BandWidth getNetworkUsage() throws SigarException {
		BandWidth bandWidth = new BandWidth(System.currentTimeMillis());
		for (String each : netInterfaces) {
			try {
				NetInterfaceStat netInterfaceStat = sigar.getNetInterfaceStat(each);
				bandWidth.setRecieved(bandWidth.getRecieved() + netInterfaceStat.getRxBytes());
				bandWidth.setSent(bandWidth.getSent() + netInterfaceStat.getTxBytes());
			} catch (Exception e) {
				NoOp.noOp();
			}
		}
		return bandWidth;
	}
	
	
	private void initCustomizedMonitor() {
		// set data file for reuse.
		customDataFile = new File(customDataFileName);
		if (!customDataFile.exists()) {
			customDataFile = null;
		}
	}
	
	private String getCustomizedMonitorData() {
		if (customDataFile == null) {
			initCustomizedMonitor();
		}
		if (customDataFile != null) {
			BufferedReader customDataFileReader = null;
			try {
				customDataFileReader = new BufferedReader(new FileReader(customDataFile));
				String line = customDataFileReader.readLine();//these data will be parsed at monitor client side.
				return line;
			} catch (IOException e) {
				LOGGER.error("Error to read custom monitor data header:" + e.getMessage(), e);
			} finally {
				IOUtils.closeQuietly(customDataFileReader);
			}
		}
		return null;
	}

}
