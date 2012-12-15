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

import java.io.BufferedWriter;
import java.io.IOException;

import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.ngrinder.monitor.MonitorConstants;
import org.ngrinder.monitor.controller.domain.MonitorCollectionInfoDomain;
import org.ngrinder.monitor.share.CachedMBeanClient;
import org.ngrinder.monitor.share.domain.MBeanClient;
import org.ngrinder.monitor.share.domain.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Used to get monitor data directly from MBeanClient and save.
 * For every MBClient, one instance will be created. So it is not singleton.
 *
 * @author Mavlarn
 * @since 3.1
 */
@Service
@Scope(value = "prototype")
public class MonitorClientSerivce {
	
	private static final Logger LOG = LoggerFactory.getLogger(MonitorClientSerivce.class);

	private MBeanClient mbeanClient;
	
	private MonitorCollectionInfoDomain sysInfoMBeanObj;
	
	private String ip;
	
	/**
	 * default constructor, used to debug the non-singleton of this class.
	 */
	public MonitorClientSerivce() {
		LOG.debug("Created MonitorClientSerivce.");
	}
	
	/**
	 * Initialize the mbeanClient connection.
	 * @param ip
	 * 			IP address of the monitor target
	 * @param port
	 * 			port of the monitor target
	 */
	public void init(String ip, int port) {
		LOG.debug("Init MonitorClientSerivce for {}:{}", ip, port);
		this.ip = ip;
		try {
			mbeanClient = CachedMBeanClient.getMBeanClient(ip, port);
			
			String objNameStr = MonitorConstants.DEFAULT_MONITOR_DOMAIN + ":" + MonitorConstants.SYSTEM;
			ObjectName systemName = new ObjectName(objNameStr);
			sysInfoMBeanObj = new MonitorCollectionInfoDomain(systemName, "SystemInfo", SystemInfo.class);
			
		} catch (IOException e) {
			LOG.error("Connect to remote mbean error:{}", e.getMessage());
			LOG.debug(e.getMessage(), e);
		} catch (Exception e) {
			LOG.error("Error to create MBean object:{}", e.getMessage());
			LOG.debug(e.getMessage(), e);
		}
	}
	
	/**
	 * get monitor data from MBClient and record into writer.
	 * @param bw buffered writer of file
	 */
	@Async
	public void recordMonitorData(BufferedWriter bw) {
		try {
			if (!mbeanClient.isConnected()) {
				mbeanClient.connect();
			}
			if (!mbeanClient.isConnected()) {
				// if the monitor client can not be connected, just return, to avoid error.
				return;
			}
			CompositeData cd = (CompositeData) mbeanClient.getAttribute(sysInfoMBeanObj.getObjectName(),
					sysInfoMBeanObj.getAttrName());
			SystemInfo retData = new SystemInfo();
			retData.parse(cd);
			retData.setIp(ip);
			bw.write(retData.getRecordString());
			bw.newLine();
			bw.flush();
		} catch (Exception e) {
			LOG.error("Error while MonitorExecutorWorker is running. Disconnect this MBean client.", e);
			mbeanClient.disconnect();
			return;
		}
	}
	
	/**
	 * close the MBClient.
	 */
	public void close() {
		mbeanClient.disconnect();
	}

}
