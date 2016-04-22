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
package org.ngrinder.perftest.service.samplinglistener;

import net.grinder.statistics.ImmutableStatisticsSet;
import org.apache.commons.io.IOUtils;
import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.common.constants.MonitorConstants;
import org.ngrinder.extension.OnTestSamplingRunnable;
import org.ngrinder.model.PerfTest;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.monitor.share.domain.SystemInfo;
import org.ngrinder.perftest.service.PerfTestService;
import org.ngrinder.perftest.service.monitor.MonitorClientService;
import org.ngrinder.service.IConfig;
import org.ngrinder.service.IPerfTestService;
import org.ngrinder.service.IScheduledTaskService;
import org.ngrinder.service.ISingleConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.ngrinder.common.util.CollectionUtils.newHashMap;

/**
 * Monitor data collector plugin.
 *
 * @author JunHo Yoon
 * @since 3.3
 */
public class MonitorCollectorPlugin implements OnTestSamplingRunnable, Runnable, MonitorConstants {
	private static final Logger LOGGER = LoggerFactory.getLogger(MonitorCollectorPlugin.class);
	private final int port;
	private Map<MonitorClientService, BufferedWriter> clientMap = new ConcurrentHashMap<MonitorClientService, BufferedWriter>();

	private final IScheduledTaskService scheduledTaskService;
	private final PerfTestService perfTestService;
	private Long perfTestId;

	/**
	 * Constructor.
	 *
	 * @param config               config
	 * @param scheduledTaskService scheduling service to run monitor task
	 */
	public MonitorCollectorPlugin(IConfig config, IScheduledTaskService scheduledTaskService,
	                              PerfTestService perfTestService, Long perfTestId) {
		this.scheduledTaskService = scheduledTaskService;
		this.perfTestService = perfTestService;
		this.perfTestId = perfTestId;
		this.port = getPort(config);
	}

	protected int getPort(IConfig config) {
		return config.getControllerProperties().getPropertyInt(ControllerConstants.PROP_CONTROLLER_MONITOR_PORT);
	}

	@Override
	public void startSampling(final ISingleConsole singleConsole, PerfTest perfTest,
	                          IPerfTestService perfTestService) {
		final List<String> targetHostIP = perfTest.getTargetHostIP();
		final Integer samplingInterval = perfTest.getSamplingInterval();
		for (final String target : targetHostIP) {
			scheduledTaskService.runAsync(new Runnable() {
				@Override
				public void run() {
					LOGGER.info("Start JVM monitoring for IP:{}", target);
					MonitorClientService client = new MonitorClientService(target, MonitorCollectorPlugin.this.port);
					client.init();
					if (client.isConnected()) {
						File testReportDir = singleConsole.getReportPath();
						File dataFile = null;
						FileWriter fw = null;
						BufferedWriter bw = null;
						try {
							dataFile = new File(testReportDir, MONITOR_FILE_PREFIX + target + ".data");
							fw = new FileWriter(dataFile, false);
							bw = new BufferedWriter(fw);
							// write header info
							bw.write(SystemInfo.HEADER);
							bw.newLine();
							bw.flush();
							clientMap.put(client, bw);
						} catch (IOException e) {
							LOGGER.error("Error to write to file:{}, Error:{}", dataFile.getPath(), e.getMessage());
						}
					}
				}
			});
		}
		assignScheduledTask(samplingInterval);
	}

	protected void assignScheduledTask(Integer samplingInterval) {
		scheduledTaskService.addFixedDelayedScheduledTask(this, samplingInterval * 1000);
	}

	@Override
	public void sampling(ISingleConsole singleConsole, PerfTest perfTest, IPerfTestService perfTestService, ImmutableStatisticsSet intervalStatistics, ImmutableStatisticsSet cumulativeStatistics) {
		for (Map.Entry<MonitorClientService, BufferedWriter> each : clientMap.entrySet()) {
			try {
				SystemInfo currentInfo = each.getKey().getSystemInfo();
				BufferedWriter bw = each.getValue();
				bw.write(currentInfo.toRecordString());
				bw.newLine();
			} catch (IOException e) {
				LOGGER.error("Error while saving file :" + e.getMessage());
			}
		}
	}

	@Override
	public void endSampling(ISingleConsole singleConsole, PerfTest perfTest, IPerfTestService perfTestService) {
		scheduledTaskService.removeScheduledJob(this);
		for (Map.Entry<MonitorClientService, BufferedWriter> each : clientMap.entrySet()) {
			closeQuietly(each.getKey());
			closeQuietly(each.getValue());
		}
		clientMap.clear();
	}

	@Override
	public void run() {
		if (!this.clientMap.isEmpty()) {
			Map<String, SystemDataModel> systemInfoMap = newHashMap();
			for (MonitorClientService each : this.clientMap.keySet()) {
				each.update();
				final SystemInfo systemInfo = each.getSystemInfo();
				if (systemInfo.isParsed()) {
					systemInfoMap.put(each.getIp(), new SystemDataModel(systemInfo, "UNKNOWN"));
				}
			}
			perfTestService.updateMonitorStat(perfTestId, systemInfoMap);
		}
	}
}
