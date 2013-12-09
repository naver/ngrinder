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
import org.ngrinder.common.constant.Constants;
import org.ngrinder.monitor.MonitorConstants;
import org.ngrinder.monitor.controller.domain.MonitorCollectionInfoDomain;
import org.ngrinder.monitor.share.domain.MBeanClient;
import org.ngrinder.monitor.share.domain.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.ngrinder.common.util.TypeConvertUtils.cast;

/**
 * Used to get monitor data directly from MBeanClient and save. For every
 * MBClient, one instance will be created. So it is not singleton.
 *
 * @author Mavlarn
 * @since 3.1
 */
@Service
@Scope(value = "prototype")
public class MonitorClientService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MonitorClientService.class);

	private MBeanClient mbeanClient;

	private MonitorCollectionInfoDomain sysInfoMBeanObj;

	private String ip;

	private Cache cache;

	private BufferedWriter bw;

	private FileWriter fileWriter;

	private long lastAccessedTime = 0;

	/**
	 * default constructor, used to debug the non-singleton of this class.
	 */
	public MonitorClientService() {
	}

	/**
	 * Initialize the mbeanClient connection.
	 *
	 * @param ip         IP address of the monitor target
	 * @param port       port of the monitor target
	 * @param reportPath report path
	 * @param cache      cache for {@link SystemInfo} data.
	 */
	public void init(String ip, int port, File reportPath, Cache cache) {
		LOGGER.debug("Init MonitorClientService for {}:{}", ip, port);
		this.ip = ip;
		this.cache = cache;
		try {
			mbeanClient = new MBeanClient(ip, port);
			String objNameStr = MonitorConstants.DEFAULT_MONITOR_DOMAIN + ":" + MonitorConstants.SYSTEM;
			ObjectName systemName = new ObjectName(objNameStr);
			sysInfoMBeanObj = new MonitorCollectionInfoDomain(systemName, "SystemInfo");

			fileWriter = new FileWriter(new File(reportPath, Constants.MONITOR_FILE_PREFIX + ip + ".data"),
					false);
			bw = new BufferedWriter(fileWriter);
			// write header info
			bw.write(SystemInfo.HEADER);
			bw.newLine();
			bw.flush();
		} catch (Exception e) {
			LOGGER.error("Init Error while {} and {} {}", new Object[]{ip, port, reportPath}, e);
		}
	}

	/**
	 * Initialize the mbeanClient connection.
	 *
	 * @param ip   IP address of the monitor target
	 * @param port port of the monitor target
	 */
	public void init(String ip, int port) {
		LOGGER.debug("Init MonitorClientService for {}:{}", ip, port);
		this.ip = ip;
		try {
			mbeanClient = new MBeanClient(ip, port);
			String objNameStr = MonitorConstants.DEFAULT_MONITOR_DOMAIN + ":" + MonitorConstants.SYSTEM;
			ObjectName systemName = new ObjectName(objNameStr);
			sysInfoMBeanObj = new MonitorCollectionInfoDomain(systemName, "SystemInfo");
		} catch (Exception e) {
			LOGGER.error("Init Error while {} and {}.", new Object[]{ip, port}, e);
		}
	}

	/**
	 * Get monitor data from MBClient and record into writer.
	 *
	 * @return {@link SystemInfo}
	 */
	public SystemInfo getMonitorData() {
		if (mbeanClient == null) {
			return null;
		}
		try {

			if (!mbeanClient.isConnected()) {
				mbeanClient.connect();
			}
			if (mbeanClient == null || !mbeanClient.isConnected()) {
				// if the monitor client can not be connected, just return, to
				// avoid error.
				return null;
			}
			SystemInfo retData = new SystemInfo();
			CompositeData cd = (CompositeData) mbeanClient.getAttribute(sysInfoMBeanObj.getObjectName(),
					sysInfoMBeanObj.getAttrName());
			retData.parse(cd);
			retData.setIp(ip);
			return retData;
		} catch (Exception e) {
			LOGGER.error("Error while MonitorExecutorWorker is running. Disconnect this MBean client.", e);
			closeMbeanClient();
			return null;
		}
	}

	/**
	 * close the MBClient.
	 */
	public void close() {
		closeMbeanClient();
		flushAndClose();
	}

	/**
	 * Only close the MBClient.
	 */
	public void closeMbeanClient() {
		if (mbeanClient != null) {
			mbeanClient.disconnect();
		}
		mbeanClient = null;
	}

	private void flushAndClose() {
		try {
			if (bw != null) {
				bw.flush();
			}
		} catch (IOException e) {
			LOGGER.error("While running flushAndClose() in MonitorClientSerivce, the error occurs.");
			LOGGER.error("Details : ", e);
		}
		IOUtils.closeQuietly(bw);
		IOUtils.closeQuietly(fileWriter);
	}

	/**
	 * Put the monitor data into Cache.
	 *
	 * @return saved Data
	 */
	public SystemInfo saveDataCache() {
		SystemInfo monitorData = getMonitorData();
		cache.put(ip, monitorData);
		return monitorData;
	}

	/**
	 * Record the data into file.
	 */
	public void record() {
		record(false);
	}

	/**
	 * Record the data into file.
	 *
	 * @param empty true if want to write empty string
	 */
	public void record(boolean empty) {
		ValueWrapper valueWrapper = cache.get(ip);
		SystemInfo systemInfo;
		if (valueWrapper == null) {
			systemInfo = new SystemInfo();
		} else {
			systemInfo = cast(valueWrapper.get());
		}
		try {
			if (empty) {
				bw.write(systemInfo.toEmptyRecordString());
			} else {
				bw.write(systemInfo.toRecordString());
			}
			bw.write("\n");
		} catch (IOException e) {
			LOGGER.error("Error while MonitorExecutorWorker is recoding, e", e);
		}
	}

	public long getLastAccessedTime() {
		return lastAccessedTime;
	}

	public void setLastAccessedTime(long lastAccessedTime) {
		this.lastAccessedTime = lastAccessedTime;
	}
}
