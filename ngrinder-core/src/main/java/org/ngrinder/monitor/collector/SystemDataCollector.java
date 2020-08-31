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
package org.ngrinder.monitor.collector;

import org.apache.commons.io.IOUtils;
import org.ngrinder.common.constants.MonitorConstants;
import org.ngrinder.monitor.mxbean.SystemMonitoringData;
import org.ngrinder.monitor.share.domain.BandWidth;
import org.ngrinder.monitor.share.domain.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static com.sun.jna.Platform.isWindows;
import static org.ngrinder.common.util.SystemInfoUtils.*;

/**
 * System data collector class.
 *
 * @author Mavlarn
 * @since 2.0
 */
public class SystemDataCollector extends DataCollector implements MonitorConstants {
	private static final Logger LOGGER = LoggerFactory.getLogger(SystemDataCollector.class);

	private SystemInfo prev = null;

	private File customDataFile = null;

	/**
	 * Set Agent Home.
	 *
	 * @param agentHomeFile agentHomeFile
	 */
	public void setAgentHome(File agentHomeFile) {
		if (customDataFile == null) {
			customDataFile = new File(agentHomeFile, "monitor" + File.separator + "custom.data");
		}
	}

	@Override
	public synchronized void refresh() {
		initializeSystemInfo();
	}

	private void initializeSystemInfo() {
		if (prev == null) {
			prev = new SystemInfo();
			prev.setBandWidth(getNetworkUsage());
		}
	}

	@Override
	public void run() {
		initializeSystemInfo();
		SystemMonitoringData systemMonitoringData = (SystemMonitoringData) getMXBean(SYSTEM);
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
			systemInfo.setCpuUsedPercentage(getCpuUsedPercentage());
			systemInfo.setTotalMemory(getTotalMemory() / 1024L);
			systemInfo.setFreeMemory(getAvailableMemory() / 1024L);
			systemInfo.setSystem(isWindows() ? SystemInfo.System.WINDOW : SystemInfo.System.LINUX);
			systemInfo.setCustomValues(getCustomMonitorData());
		} catch (Throwable e) {
			LOGGER.error("Error while getting system perf data: {}", e.getMessage());
			LOGGER.debug("Error trace is ", e);
		}
		prev = systemInfo;
		return systemInfo;
	}

	private String getCustomMonitorData() {
		if (customDataFile != null && customDataFile.exists()) {
			BufferedReader customDataFileReader = null;
			try {
				customDataFileReader = new BufferedReader(new FileReader(customDataFile));
				return customDataFileReader.readLine(); // these data will be parsed at
				// monitor client side.
			} catch (IOException e) {
				// Error here is very natural
				LOGGER.debug("Error to read custom monitor data", e);
			} finally {
				IOUtils.closeQuietly(customDataFileReader);
			}
		}
		return prev.getCustomValues();
	}

}
