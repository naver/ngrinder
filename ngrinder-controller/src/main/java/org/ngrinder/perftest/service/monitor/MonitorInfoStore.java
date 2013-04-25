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

import org.ngrinder.monitor.share.domain.SystemInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Used to save JMX connect for every request that want to observe monitor real-time system
 * information.
 */
@Service
@Scope(value = "singleton")
public class MonitorInfoStore {

	@Autowired
	private ApplicationContext applicationContext;

	private Map<String, MonitorClientSerivce> monitorInfoMap = Collections
					.synchronizedMap(new HashMap<String, MonitorClientSerivce>());

	private void add(String ip, MonitorClientSerivce monitorClient) {
		synchronized (this) {
			monitorInfoMap.put(ip, monitorClient);
		}
	}

	/**
	 * Get monitor data from MBClient.
	 * 
	 * @param ip
	 *            ip
	 * @param port
	 *            port
	 * @return {@link SystemInfo}
	 */
	public SystemInfo getSystemInfo(String ip, int port) {
		MonitorClientSerivce monitorClient = monitorInfoMap.get(ip);
		if (monitorClient == null) {
			monitorClient = applicationContext.getBean(MonitorClientSerivce.class);
			monitorClient.init(ip, port);
			add(ip, monitorClient);
		}
		return monitorClient.getMonitorData();
	}

	/**
	 * Used to close MBClient connect.
	 * 
	 * @param ip
	 *            ip
	 */
	public void remove(String ip) {
		synchronized (this) {
			MonitorClientSerivce monitorClient = monitorInfoMap.get(ip);
			if (monitorClient == null) {
				return;
			}
			monitorClient.closeMBClient();
			monitorInfoMap.remove(ip);
		}
	}

}
