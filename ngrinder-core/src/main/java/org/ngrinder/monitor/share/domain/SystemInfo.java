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
package org.ngrinder.monitor.share.domain;

import java.io.Serializable;
import java.util.Date;

import javax.management.openmbean.CompositeData;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.ngrinder.common.util.DateUtil;

/**
 * 
 * System info object to save date collected by monitor.
 * 
 * @author Mavlarn
 * @since 2.0
 */
public class SystemInfo extends MonitorInfo implements Serializable {

	private static final long serialVersionUID = -2995334644975166549L;

	/**
	 * Header field of monitor status fields.
	 */
	public static final String HEADER = "ip,system,collectTime,freeMemory,"
					+ "totalMemory,cpuUsedPercentage,recivedPerSec,sentPerSec";

	/**
	 * Enum for the system type, linux or windows.
	 */
	public enum System {
		LINUX, WINDOW
	}

	private System system;

	private BandWidth bandWidth;

	private long totalCpuValue;

	private long idleCpuValue;

	private long freeMemory;

	private long totalMemory;

	private float cpuUsedPercentage;

	private String ip;

	@Override
	public void parse(CompositeData cd) {
		if (cd == null) {
			return;
		}
		try {
			long collectTime = getLong(cd, "collectTime");
			setCollectTime(collectTime);
			String string = getString(cd, "system");
			this.system = System.valueOf(string);
			this.totalCpuValue = getLong(cd, "totalCpuValue");
			this.idleCpuValue = getLong(cd, "idlecpu");
			this.freeMemory = getLong(cd, "freeMemory");
			this.totalMemory = getLong(cd, "totalMemory");
			this.cpuUsedPercentage = getFloat(cd, "CPUUsedPercentage");

			if (containsKey(cd, "bandWidth")) {
				CompositeData bandWidth = (CompositeData) getObject(cd, "bandWidth");
				this.bandWidth = new BandWidth(collectTime);
				long recivedPerSec = getLong(bandWidth, "recivedPerSec");
				long sentPerSec = getLong(bandWidth, "sentPerSec");
				this.bandWidth.setRecivedPerSec(recivedPerSec);
				this.bandWidth.setSentPerSec(sentPerSec);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public System getSystem() {
		return system;
	}

	public void setSystem(System system) {
		this.system = system;
	}

	public void setSystem(String system) {
		this.system = System.valueOf(system);
	}

	public void setCPUUsedPercentage(float cpuUsedPercentage) {
		this.cpuUsedPercentage = cpuUsedPercentage;
	}

	public float getCPUUsedPercentage() {
		return cpuUsedPercentage;
	}

	public long getIdlecpu() {
		return idleCpuValue;
	}

	public long getTotalCpuValue() {
		return totalCpuValue;
	}

	public long getFreeMemory() {
		return freeMemory;
	}

	public void setTotalCpuValue(long totalCpuValue) {
		this.totalCpuValue = totalCpuValue;
	}

	public void setIdleCpuValue(long idleCpuValue) {
		this.idleCpuValue = idleCpuValue;
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

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * Get record string.
	 * 
	 * @return record string
	 */
	public String getRecordString() {
		StringBuilder sb = new StringBuilder();
		sb.append(ip).append(",").append(system).append(",");
		sb.append(DateUtil.getCollectTimeInLong(new Date(getCollectTime()))).append(",").append(freeMemory).append(",");
		sb.append(totalMemory).append(",").append(cpuUsedPercentage);
		if (bandWidth != null) {
			sb.append(",").append(bandWidth.getRecivedPerSec()).append(",").append(bandWidth.getSentPerSec());
		}
		return sb.toString();
	}

	public BandWidth getBandWidth() {
		return bandWidth;
	}

	public void setBandWidth(BandWidth bandWidth) {
		this.bandWidth = bandWidth;
	}

}
