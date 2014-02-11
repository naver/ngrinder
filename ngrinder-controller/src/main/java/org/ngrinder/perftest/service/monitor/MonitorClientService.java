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

import org.ngrinder.common.constants.MonitorConstants;
import org.ngrinder.monitor.share.domain.MBeanClient;
import org.ngrinder.monitor.share.domain.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import java.io.Closeable;
import java.io.IOException;

import static org.ngrinder.common.util.TypeConvertUtils.cast;

/**
 * Used to get monitor data directly from MBeanClient and save. For every
 * MBClient, one instance will be created. So it is not singleton.
 *
 * @author Mavlarn
 * @since 3.1
 */
public class MonitorClientService implements Closeable, MonitorConstants {
	private static final Logger LOGGER = LoggerFactory.getLogger(MonitorClientService.class);

	private MBeanClient mBeanClient;

	private static ObjectName objectName;

	static {
		try {
			objectName = new ObjectName(DEFAULT_MONITOR_DOMAIN + ":" + SYSTEM);
		} catch (MalformedObjectNameException e) {
			LOGGER.error("Error while creating ObjectName", e);
		}
	}

	private String ip;

	private int port;
	private SystemInfo systemInfo = SystemInfo.NullSystemInfo.getNullSystemInfo();
	private long lastAccessedTime;

	public MonitorClientService(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public boolean isConnected() {
		return mBeanClient.isConnected();
	}


	/**
	 * Initialize the mBeanClient connection.
	 */
	public void init() {
		LOGGER.debug("Init MonitorClientService for {}:{}", ip, port);
		try {
			mBeanClient = new MBeanClient(ip, port);
			mBeanClient.connect();
			LOGGER.debug("Connection finished, isConnected :{}", mBeanClient.isConnected());
		} catch (IOException e) {
			LOGGER.info("Monitor Connection Error to {} by {}", ip + ":" + port, e.getMessage());
		}


	}

	/**
	 * Get monitor data from MBClient and record into writer.
	 */
	public void update() {
		try {
			if (mBeanClient.isConnected()) {
				CompositeData cd = cast(mBeanClient.getAttribute(objectName, "SystemInfo"));
				SystemInfo systemInfo = new SystemInfo();
				systemInfo.parse(cd);
				systemInfo.setIp(ip);
				this.systemInfo = systemInfo;
			}
		} catch (Exception e) {
			LOGGER.error("Error while MonitorExecutorWorker is running. Disconnect this MBean client.", e);
		}
	}

	/**
	 * close the MBClient.
	 */

	public void close() {
		mBeanClient.disconnect();
	}


	public SystemInfo getSystemInfo() {
		return systemInfo;
	}

	public String getIp() {
		return this.ip;
	}

	public void setLastAccessedTime(long lastAccessedTime) {
		this.lastAccessedTime = lastAccessedTime;
	}

	public long getLastAccessedTime() {
		return lastAccessedTime;
	}
}
