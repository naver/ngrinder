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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Index;
import org.ngrinder.common.util.DateUtil;
import org.ngrinder.model.BaseEntity;
import org.ngrinder.monitor.share.domain.SystemInfo;

/**
 * 
 * System data model class, to store the system monitor data.
 *
 * @author Mavlarn
 * @since 2.0
 */
@Entity
@Table(name = "system_monitor")
public class SystemDataModel extends BaseEntity<SystemDataModel> {

	private static final long serialVersionUID = 790334226137464982L;

	@Column(name = "monitor_key")
	@Index(name = "monitor_key_index")
	private String key;
	
	@Index(name = "ip_index")
	private String ip;
	
	private int port;
	
	private String system;
	
	@Column(name = "collect_time")
	@Index(name = "collect_time_index")
	private long collectTime;
	
	@Column(name = "free_memory")
	private long freeMemory;
	
	@Column(name = "total_memory")
	private long totalMemory;
	
	@Column(name = "cpu_used_percentage")
	private float cpuUsedPercentage;
	
	/**
	 * Default constructor to create empty SystemDataModel.
	 */
	public SystemDataModel() {
	}
	
	/**
	 * Constructor to create SystemDataModel with SystemInfo.
	 * @param systemInfo is the data collected by monitor
	 */
	public SystemDataModel(SystemInfo systemInfo) {
		system = String.valueOf(systemInfo.getSystem());
		collectTime = DateUtil.getCollectTimeInLong(new Date(systemInfo.getCollectTime()));
		freeMemory = systemInfo.getFreeMemory();
		totalMemory = systemInfo.getTotalMemory();
		cpuUsedPercentage = systemInfo.getCPUUsedPercentage();
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
}
