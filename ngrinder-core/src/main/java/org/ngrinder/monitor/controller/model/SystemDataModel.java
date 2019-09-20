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

import org.ngrinder.model.BaseEntity;
import org.ngrinder.monitor.share.domain.BandWidth;
import org.ngrinder.monitor.share.domain.SystemInfo;

import lombok.Getter;
import lombok.Setter;

/**
 * System data model class, to store the system monitor data.
 *
 * @since 2.0
 */
@SuppressWarnings("UnusedDeclaration")
@Getter
@Setter
public class SystemDataModel extends BaseEntity<SystemDataModel> {
	private static final long serialVersionUID = 790334226137464982L;

	private String key;

	private String ip;

	private int port;

	private String system;

	private long collectTime;

	private long freeMemory;

	private long totalMemory;

	private float cpuUsedPercentage;

	private long receivedPerSec;

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
		this.cpuUsedPercentage = systemInfo.getCpuUsedPercentage();
		BandWidth bandWidth = systemInfo.getBandWidth();
		if (bandWidth != null) {
			this.receivedPerSec = bandWidth.getReceivedPerSec();
			this.sentPerSec = bandWidth.getSentPerSec();
		}
		this.customValues = systemInfo.getCustomValues();
		this.version = version;
	}

}
