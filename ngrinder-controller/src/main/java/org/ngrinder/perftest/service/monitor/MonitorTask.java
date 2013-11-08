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

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimerTask;

import org.ngrinder.model.AgentInfo;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.monitor.share.domain.SystemInfo;
import org.ngrinder.perftest.service.PerfTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;

/**
 * Scheduler to monitor and save the monitoring data.
 * 
 * @author Mavlarn
 * @author JunHo Yoon
 * @since 3.1
 */
@Service
@Scope(value = "prototype")
public class MonitorTask extends TimerTask {
	private Map<String, MonitorClientService> monitorClientsMap = Maps.newConcurrentMap();

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private PerfTestService perfTestService;

	private Long perfTestId;

	/**
	 * Add MBean monitors on the given monitorTargets.
	 * 
	 * @param monitorTargets
	 *            a set of monitor targets
	 * @param reportPath
	 *            report path
	 */
	public void add(Set<AgentInfo> monitorTargets, File reportPath) {
		for (AgentInfo target : monitorTargets) {
			String targetKey = createTargetKey(target);
			if (!monitorClientsMap.containsKey(targetKey)) {
				MonitorClientService bean = applicationContext.getBean(MonitorClientService.class);
				bean.init(target.getIp(), target.getPort(), reportPath, cacheManager.getCache("monitor_data"));
				monitorClientsMap.put(targetKey, bean);
			}
		}
	}

	/**
	 * Delete All MBean monitors.
	 */
	public void destroy() {
		for (Entry<String, MonitorClientService> target : monitorClientsMap.entrySet()) {
			target.getValue().close();
		}
		monitorClientsMap.clear();
	}

	private String createTargetKey(AgentInfo target) {
		return target.getIp();
	}

	/**
	 * Save the {@link org.ngrinder.monitor.share.domain.SystemInfo} into the report path.
	 */
	public void saveData() {
		saveData(false);
	}

	/**
	 * Save the {@link org.ngrinder.monitor.share.domain.SystemInfo} into the report path.
	 * 
	 * @param empty
	 *            true if empty data should be saved.
	 */
	public void saveData(boolean empty) {
		for (Entry<String, MonitorClientService> target : monitorClientsMap.entrySet()) {
			MonitorClientService monitorClientService = target.getValue();
			monitorClientService.record(empty);
		}
	}

	@Override
	public void run() {
		Map<String, SystemDataModel> systemInfoMap = Maps.newHashMap();
		for (Entry<String, MonitorClientService> target : monitorClientsMap.entrySet()) {
			MonitorClientService monitorClientService = target.getValue();
			SystemInfo saveDataCache = monitorClientService.saveDataCache();
			if (saveDataCache != null) {
				systemInfoMap.put(target.getKey(), new SystemDataModel(saveDataCache, "UNKNOWN"));
			}
		}
		perfTestService.updateMonitorStat(perfTestId, systemInfoMap);
	}

	public void setPerfTestService(PerfTestService perfTestService) {
		this.perfTestService = perfTestService;
	}

	public void setCorrespondingPerfTestId(Long perfTestId) {
		this.perfTestId = perfTestId;
	}

}
