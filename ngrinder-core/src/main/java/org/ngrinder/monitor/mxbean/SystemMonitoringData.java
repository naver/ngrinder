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
package org.ngrinder.monitor.mxbean;

import org.ngrinder.monitor.collector.DataCollector;
import org.ngrinder.monitor.collector.SystemDataCollector;
import org.ngrinder.monitor.mxbean.core.MXBean;
import org.ngrinder.monitor.mxbean.core.SystemMXBean;
import org.ngrinder.monitor.share.domain.SystemInfo;

import java.io.File;

/**
 * 
 * MXBean implementation for monitoring system data.
 * 
 * @author Mavlarn
 * @since 2.0
 */
public class SystemMonitoringData extends MXBean implements SystemMXBean {

	private static final long serialVersionUID = -3568524400212296536L;
	private SystemInfo systemInfo;

	private SystemDataCollector agentSystemDataCollector = new SystemDataCollector();

	@Override
	public DataCollector gainDataCollector(File agentHome) {
		agentSystemDataCollector.refresh();
		agentSystemDataCollector.setAgentHome(agentHome);
		return agentSystemDataCollector;
	}

	public void setSystemInfo(SystemInfo systemInfo) {
		this.systemInfo = systemInfo;
	}

	@Override
	public SystemInfo getSystemInfo() {
		return systemInfo;
	}

}
