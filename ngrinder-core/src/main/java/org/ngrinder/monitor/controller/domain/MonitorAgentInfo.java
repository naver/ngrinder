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
package org.ngrinder.monitor.controller.domain;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 
 * This class is used to store the IP/port of target monitor JMX server, and the recorder.
 *
 * @author Mavlarn
 * @since 2.0
 */
public final class MonitorAgentInfo {
	private String ip;
	private int port;
	private MonitorCollection collection;
	private MonitorRecorder recoder;

	private MonitorAgentInfo(String ip, int port, MonitorCollection collection, MonitorRecorder recoder) {
		this.ip = ip;
		this.port = port;
		this.collection = collection;
		this.recoder = recoder;
	}

	/**
	 * create and return the {@link MonitorAgentInfo}.
	 * @param ip is the IP address of monitor target
	 * @param port is the port number of JMX monitor listener on target
	 * @param recoder is the recorder for the collected data
	 * @return monitorAgentInfo is {@link MonitorAgentInfo}
	 */
	public static MonitorAgentInfo getSystemMonitor(String ip, int port, MonitorRecorder recoder) {
		return new MonitorAgentInfo(ip, port, MonitorCollection.getSystemMonitorCollection(), recoder);
	}

	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}

	public MonitorCollection getCollection() {
		return collection;
	}

	public MonitorRecorder getRecoder() {
		return recoder;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}
