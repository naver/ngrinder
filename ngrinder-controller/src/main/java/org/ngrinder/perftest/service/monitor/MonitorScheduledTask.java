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
package org.ngrinder.perftest.service.monitor;

import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.monitor.share.domain.SystemInfo;
import org.ngrinder.perftest.service.PerfTestService;
import org.springframework.cache.CacheManager;

import java.io.Closeable;
import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Scheduler to monitor and save the monitoring data.
 *
 * @author Mavlarn
 * @author JunHo Yoon
 * @since 3.1
 */
public class MonitorScheduledTask implements Runnable, Closeable {
	private Map<String, MonitorClientService> monitorClientsMap = Maps.newConcurrentMap();

	private CacheManager cacheManager;

	private PerfTestService perfTestService;

	private Long perfTestId;

	public MonitorScheduledTask(CacheManager cacheManager, PerfTestService perfTestService) {
		this.cacheManager = cacheManager;
		this.perfTestService = perfTestService;
	}

	/**
	 * Add MBean monitors on the given monitorTargets.
	 *
	 * @param monitorTargets a set of monitor targets
	 * @param reportPath     report path
	 */
	public void add(Set<AgentInfo> monitorTargets, File reportPath) {
		for (AgentInfo target : monitorTargets) {
			String targetKey = createTargetKey(target);
			if (!monitorClientsMap.containsKey(targetKey)) {
				MonitorClientService bean = new MonitorClientService();
				bean.init(target.getIp(), target.getPort(), reportPath, cacheManager.getCache("monitor_data"));
				monitorClientsMap.put(targetKey, bean);
			}
		}
	}

	/**
	 * Delete All MBean monitors.
	 */
	public void close() {
		for (Entry<String, MonitorClientService> target : monitorClientsMap.entrySet()) {
			IOUtils.closeQuietly(target.getValue());
		}
		monitorClientsMap.clear();
	}

	private String createTargetKey(AgentInfo target) {
		return target.getIp();
	}


	/**
	 * Save the {@link org.ngrinder.monitor.share.domain.SystemInfo} into the report path.
	 *
	 * @param includeEmpty true if empty data should be saved.
	 */
	public void saveData(boolean includeEmpty) {
		for (Entry<String, MonitorClientService> target : monitorClientsMap.entrySet()) {
			MonitorClientService monitorClientService = target.getValue();
			monitorClientService.record(includeEmpty);
		}
	}


	public void setCorrespondingPerfTestId(Long perfTestId) {
		this.perfTestId = perfTestId;
	}


	@Override
	public void run() {
		Map<String, SystemDataModel> systemInfoMap = Maps.newHashMap();
		for (Entry<String, MonitorClientService> target : monitorClientsMap.entrySet()) {
			MonitorClientService monitorClientService = target.getValue();
			SystemInfo saveDataCache = monitorClientService.saveDataIntoCache();
			if (saveDataCache.isParsed()) {
				systemInfoMap.put(target.getKey(), new SystemDataModel(saveDataCache, "UNKNOWN"));
			}
		}
		perfTestService.updateMonitorStat(perfTestId, systemInfoMap);
	}

}
