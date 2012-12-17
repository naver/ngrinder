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
package org.ngrinder.monitor.service;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ngrinder.agent.model.AgentInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;

/**
 * Used to get monitor data directly from MBeanClient and save. For every MBClient, one instance
 * will be created. So it is not singleton.
 * 
 * @author Mavlarn
 * @since 3.1
 */
@Service
public class MonitorClientScheduler {
	private Map<String, MonitorClientSerivce> monitorClientsMap = Maps.newConcurrentMap();

	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private CacheManager cacheManager;

	public void add(Set<AgentInfo> agents, File reportPath) {
		for (AgentInfo target : agents) {
			String targetKey = createTargetKey(target, reportPath);
			MonitorClientSerivce bean = applicationContext.getBean(MonitorClientSerivce.class);
			bean.init(target.getIp(), target.getPort(), reportPath, cacheManager.getCache("monitor_data"));
			monitorClientsMap.put(targetKey, bean);
		}
	}

	public void remove(Set<AgentInfo> agents, File reportPath) {
		for (AgentInfo target : agents) {
			String targetIP = createTargetKey(target, reportPath);
			MonitorClientSerivce monitorClient = monitorClientsMap.remove(targetIP);
			monitorClient.close();
		}
	}

	private String createTargetKey(AgentInfo target, File reportPath) {
		return target.getIp() + "_" + reportPath;
	}

	@Scheduled(fixedDelay = 500)
	public void retriveData() {
		for (Entry<String, MonitorClientSerivce> target : monitorClientsMap.entrySet()) {
			MonitorClientSerivce monitorClientSerivce = target.getValue();
			monitorClientSerivce.putCache();
		}
	}

	@Scheduled(fixedRate = 1000)
	public void saveData() {
		for (Entry<String, MonitorClientSerivce> target : monitorClientsMap.entrySet()) {
			MonitorClientSerivce monitorClientSerivce = target.getValue();
			monitorClientSerivce.record();
		}
	}

}
