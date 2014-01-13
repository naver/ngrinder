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
package org.ngrinder.monitor.controller.model;

import com.google.gson.annotations.Expose;
import org.ngrinder.model.BaseEntity;
import org.ngrinder.monitor.share.domain.BandWidth;
import org.ngrinder.monitor.share.domain.SystemInfo;

/**
 * System data model class, to store the system monitor data.
 *
 * @author Mavlarn
 * @author JunHo Yoon
 * @since 2.0
 */
@SuppressWarnings("UnusedDeclaration")
public class SystemDataModel extends BaseEntity<SystemDataModel> {
	private static final long serialVersionUID = 790334226137464982L;

	private String key;

	private String ip;

	private int port;

	private String system;

	private long collectTime;

	@Expose
	private long freeMemory;

	@Expose
	private long totalMemory;

	@Expose
	private float cpuUsedPercentage;

	@Expose
	private long receivedPerSec;

	@Expose
	private long sentPerSec;

	private String version;

	private String customValues;

	/**
	 * Default constructor to create empty SystemDataModel.
	 */
	public SystemDataModel() {
	}

	/**
	 * Constructor to create SystemDataModel with SystemInfo.
	 *
	 * @param systemInfo is the data collected by monitor
	 */
	public SystemDataModel(SystemInfo systemInfo) {
		this(systemInfo, null);
	}

	/**
	 * Constructor to create SystemDataModel with SystemInfo.
	 *
	 * @param systemInfo is the data collected by monitor
	 * @param version    version of model.
	 */
	public SystemDataModel(SystemInfo systemInfo, String version) {
		this.system = String.valueOf(systemInfo.getSystem());
		this.collectTime = systemInfo.getCollectTime();
		this.freeMemory = systemInfo.getFreeMemory();
		this.totalMemory = systemInfo.getTotalMemory();
		this.cpuUsedPercentage = systemInfo.getCPUUsedPercentage();
		BandWidth bandWidth = systemInfo.getBandWidth();
		if (bandWidth != null) {
			this.receivedPerSec = bandWidth.getReceivedPerSec();
			this.sentPerSec = bandWidth.getSentPerSec();
		}
		this.customValues = systemInfo.getCustomValues();
		this.version = version;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getSystem() {
		return system;
	}

	public void setSystem(String system) {
		this.system = system;
	}

	public long getCollectTime() {
		return collectTime;
	}

	public void setCollectTime(long collectTime) {
		this.collectTime = collectTime;
	}

	public long getFreeMemory() {
		return freeMemory;
	}

	public void setFreeMemory(long freeMemory) {
		this.freeMemory = freeMemory;
	}

	public long getTotalMemory() {
		return totalMemory;
	}

	public void setTotalMemory(long totalMemory) {
		this.totalMemory = totalMemory;
	}

	public float getCpuUsedPercentage() {
		return cpuUsedPercentage;
	}

	public void setCpuUsedPercentage(float cpuUsedPercentage) {
		this.cpuUsedPercentage = cpuUsedPercentage;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public long getReceivedPerSec() {
		return receivedPerSec;
	}

	public void setReceivedPerSec(long receivedPerSec) {
		this.receivedPerSec = receivedPerSec;
	}

	public long getSentPerSec() {
		return sentPerSec;
	}

	public void setSentPerSec(long sentPerSec) {
		this.sentPerSec = sentPerSec;
	}

	public String getCustomValues() {
		return customValues;
	}

	public void setCustomValues(String customValues) {
		this.customValues = customValues;
	}

}
