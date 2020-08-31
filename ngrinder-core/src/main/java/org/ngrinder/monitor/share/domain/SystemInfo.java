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

import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.util.DateUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Tolerate;

/**
 * System info object to save date collected by monitor.
 *
 * @author Mavlarn
 * @since 2.0
 */
@Getter
@Setter
@ToString(callSuper = true)
public class SystemInfo extends MonitorInfo implements Serializable {

	private static final long serialVersionUID = -2995334644975166549L;

	/**
	 * Header field of monitor status fields.
	 */
	public static final String HEADER = "ip,system,collectTime,freeMemory,"
			+ "totalMemory,cpuUsedPercentage,receivedPerSec,sentPerSec,customValues";

	public boolean isParsed() {
		return true;
	}

	/**
	 * Enum for the system type, linux or windows.
	 */
	public enum System {
		LINUX, WINDOW
	}

	private System system;

	protected BandWidth bandWidth;

	private long freeMemory;

	private long totalMemory;

	private float cpuUsedPercentage;

	private String ip;

	protected String customValues;

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
			this.freeMemory = getLong(cd, "freeMemory");
			this.totalMemory = getLong(cd, "totalMemory");
			this.cpuUsedPercentage = getFloat(cd, "cpuUsedPercentage");

			if (containsKey(cd, "bandWidth")) {
				CompositeData bandWidth = (CompositeData) getObject(cd, "bandWidth");
				this.bandWidth = new BandWidth(collectTime);
				long receivedPerSec = getLong(bandWidth, "receivedPerSec");
				long sentPerSec = getLong(bandWidth, "sentPerSec");
				this.bandWidth.setReceivedPerSec(receivedPerSec);
				this.bandWidth.setSentPerSec(sentPerSec);
			}
			if (containsKey(cd, "customValues")) {
				this.setCustomValues(getString(cd, "customValues"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Tolerate
	public void setSystem(String system) {
		this.system = System.valueOf(system);
	}

	/**
	 * Get record string.
	 *
	 * @return record string
	 */
	public String toRecordString() {
		StringBuilder sb = new StringBuilder();
		sb.append(ip).append(",").append(system).append(",");
		sb.append(DateUtils.getCollectTimeInLong(new Date(getCollectTime()))).append(",").append(freeMemory).append(",");
		sb.append(totalMemory).append(",").append(cpuUsedPercentage);
		if (bandWidth != null) {
			sb.append(",").append(bandWidth.getReceivedPerSec()).append(",").append(bandWidth.getSentPerSec());
		}
		if (customValues != null) {
			sb.append(",").append(customValues);
		}
		return sb.toString();
	}


	public static class NullSystemInfo extends SystemInfo {
		private static final NullSystemInfo instance = new NullSystemInfo();

		public static SystemInfo getNullSystemInfo() {
			return instance;
		}

		/**
		 * Return the empty record string.
		 *
		 * @return null filled record string.
		 * @see #toRecordString()
		 */
		@Override
		public String toRecordString() {
			StringBuilder sb = new StringBuilder();
			sb.append("null").append(",").append("null").append(",");
			sb.append("null").append(",").append("null").append(",");
			sb.append("null").append(",").append("null");
			if (bandWidth != null) {
				sb.append(",").append("null").append(",").append("null");
			}
			if (customValues != null) {
				int valueCount = StringUtils.countMatches(customValues, ",") + 1;
				for (int i = 0; i < valueCount; i++) {
					sb.append(",").append("null");
				}
			}
			return sb.toString();
		}

		public boolean isParsed() {
			return false;
		}
	}

}
