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

import org.apache.commons.io.IOUtils;
import org.ngrinder.infra.schedule.ScheduledTaskService;
import org.ngrinder.monitor.share.domain.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Used to save JMX connect for every request that want to observe monitor real-time system information.
 */
@Service
public class MonitorInfoStore implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(MonitorInfoStore.class);

	@Autowired
	private ScheduledTaskService scheduledTaskService;
	private Map<String, MonitorClientService> monitorClientMap = new ConcurrentHashMap<String, MonitorClientService>();


	@PostConstruct
	public void init() {
		scheduledTaskService.addFixedDelayedScheduledTask(this, 30000);
	}

	/**
	 * Get monitor data from mbean client.
	 *
	 * @param ip   ip
	 * @param port port
	 * @return {@link org.ngrinder.monitor.share.domain.SystemInfo}
	 */
	public SystemInfo getSystemInfo(String ip, int port) {
		MonitorClientService monitorClient = monitorClientMap.get(ip);
		if (monitorClient == null) {
			monitorClient = new MonitorClientService(ip, port);
			monitorClient.init();
			IOUtils.closeQuietly(monitorClientMap.put(ip, monitorClient));
		}
		monitorClient.update();
		monitorClient.setLastAccessedTime(System.currentTimeMillis());
		return monitorClient.getSystemInfo();
	}

	/**
	 * Delete the unused monitor clients periodically.
	 */
	public void run() {
		for (Entry<String, MonitorClientService> each : monitorClientMap.entrySet()) {
			if ((System.currentTimeMillis() - each.getValue().getLastAccessedTime()) > 30000) {
				close(each.getKey());
				LOGGER.info("Remove unused monitor {}", each.getKey());
			}
		}
	}

	/**
	 * Close mbean client connected to the given ip.
	 *
	 * @param ip ip
	 */
	public void close(String ip) {
		IOUtils.closeQuietly(monitorClientMap.remove(ip));
	}
}
