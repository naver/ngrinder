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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.ngrinder.monitor.share.domain.SystemInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Used to save JMX connect for every request that want to observe monitor real-time system information.
 */
@Service
@Scope(value = "singleton")
public class MonitorInfoStore {

	@Autowired
	private ApplicationContext applicationContext;

	private Map<String, MonitorClientService> monitorInfoMap = Collections
			.synchronizedMap(new HashMap<String, MonitorClientService>());

	/**
	 * Get monitor data from mbean client.
	 * 
	 * @param ip
	 *            ip
	 * @param port
	 *            port
	 * @return {@link SystemInfo}
	 */
	public SystemInfo getSystemInfo(String ip, int port) {
		MonitorClientService monitorClient = monitorInfoMap.get(ip);
		if (monitorClient == null) {
			monitorClient = applicationContext.getBean(MonitorClientService.class);
			monitorClient.init(ip, port);
			monitorClient.setLastAccessedTime(System.currentTimeMillis());
			add(ip, monitorClient);
		}
		monitorClient.setLastAccessedTime(System.currentTimeMillis());
		return monitorClient.getMonitorData();
	}

	private void add(String ip, MonitorClientService monitorClient) {
		synchronized (this) {
			monitorInfoMap.put(ip, monitorClient);
		}
	}

	/**
	 * Delete the unused monitor clients periodically.
	 */
	@Scheduled(fixedDelay = 30000)
	public void closeUnusedMonitorClient() {
		for (Entry<String, MonitorClientService> each : monitorInfoMap.entrySet()) {
			if ((System.currentTimeMillis() - each.getValue().getLastAccessedTime()) > 30000) {
				close(each.getKey());
			}
		}
	}

	/**
	 * Close mbean client connected to the given ip.
	 * 
	 * @param ip
	 *            ip
	 */
	public void close(String ip) {
		synchronized (this) {
			MonitorClientService monitorClient = monitorInfoMap.get(ip);
			if (monitorClient == null) {
				return;
			}
			monitorClient.closeMbeanClient();
			monitorInfoMap.remove(ip);
		}
	}

}
