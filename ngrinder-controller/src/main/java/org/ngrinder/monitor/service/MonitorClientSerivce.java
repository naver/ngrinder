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

import static org.ngrinder.common.util.TypeConvertUtil.convert;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.apache.commons.io.IOUtils;
import org.ngrinder.infra.config.Config;
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

/**
 * Used to get monitor data directly from MBeanClient and save. For every MBClient, one instance
 * will be created. So it is not singleton.
 * 
 * @author Mavlarn
 * @since 3.1
 */
@Service
@Scope(value = "prototype")
public class MonitorClientSerivce {

	private static final Logger LOGGER = LoggerFactory.getLogger(MonitorClientSerivce.class);

	private MBeanClient mbeanClient;

	private MonitorCollectionInfoDomain sysInfoMBeanObj;

	private String ip;

	private Cache cache;

	private BufferedWriter bw;

	/**
	 * default constructor, used to debug the non-singleton of this class.
	 */
	public MonitorClientSerivce() {
		LOGGER.debug("Created MonitorClientSerivce.");
	}

	/**
	 * Initialize the mbeanClient connection.
	 * 
	 * @param ip
	 *            IP address of the monitor target
	 * @param port
	 *            port of the monitor target
	 * @param reportPath
	 *            report path
	 * @param cache
	 *            cache for {@link SystemInfo} data.
	 */
	public void init(String ip, int port, File reportPath, Cache cache) {
		LOGGER.debug("Init MonitorClientSerivce for {}:{}", ip, port);
		this.ip = ip;
		this.cache = cache;
		try {
			mbeanClient = new MBeanClient(ip, port);
			String objNameStr = MonitorConstants.DEFAULT_MONITOR_DOMAIN + ":" + MonitorConstants.SYSTEM;
			ObjectName systemName = new ObjectName(objNameStr);
			sysInfoMBeanObj = new MonitorCollectionInfoDomain(systemName, "SystemInfo", SystemInfo.class);

			bw = new BufferedWriter(new FileWriter(new File(reportPath, Config.MONITOR_FILE_PREFIX + ip + ".data"),
							false));
			// write header info
			bw.write(SystemInfo.HEADER);
			bw.newLine();
			bw.flush();

		} catch (Exception e) {
			LOGGER.error("Init Error while {} and {} {}", new Object[] { ip, port, reportPath }, e);
		}

	}

	/**
	 * Get monitor data from MBClient and record into writer.
	 * 
	 */
	public SystemInfo getMonitorData() {
		SystemInfo retData = new SystemInfo();
		try {
			if (!mbeanClient.isConnected()) {
				mbeanClient.connect();
			}
			if (!mbeanClient.isConnected()) {
				// if the monitor client can not be connected, just return, to avoid error.
				return null;
			}
			CompositeData cd = (CompositeData) mbeanClient.getAttribute(sysInfoMBeanObj.getObjectName(),
							sysInfoMBeanObj.getAttrName());
			retData.parse(cd);
			retData.setIp(ip);
			return retData;
		} catch (Exception e) {
			LOGGER.error("Error while MonitorExecutorWorker is running. Disconnect this MBean client.", e);
			mbeanClient.disconnect();
			return null;
		}
	}

	/**
	 * close the MBClient.
	 */
	public void close() {
		mbeanClient.disconnect();
		flushAndClose(bw);

	}

	private void flushAndClose(BufferedWriter bw) {
		try {
			bw.flush();
		} catch (IOException e) {
			LOGGER.error("While running flushAndClose() in MonitorClientSerivce, the error occurs.");
			LOGGER.error("Details : ", e);
		}
		IOUtils.closeQuietly(bw);
	}

	/**
	 * Put the monitor data into Cache.
	 */
	public void saveDataCache() {
		cache.put(ip, getMonitorData());
	}

	/**
	 * Record the data into file.
	 */
	public void record() {
		ValueWrapper valueWrapper = cache.get(ip);
		SystemInfo systemInfo = null;
		if (valueWrapper == null || valueWrapper.get() == null) {
			systemInfo = new SystemInfo();
		} else {
			systemInfo = convert(valueWrapper.get());
		}
		try {
			bw.write(systemInfo.getRecordString() + "\n");
		} catch (IOException e) {
			LOGGER.error("Error while MonitorExecutorWorker is recoding, e", e);
		}

	}
}
