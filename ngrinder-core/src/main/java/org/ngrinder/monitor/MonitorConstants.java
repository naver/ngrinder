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
package org.ngrinder.monitor;

import java.util.HashSet;
import java.util.Set;

import org.ngrinder.infra.AgentConfig;

/**
 * Static Monitoring Constants.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public final class MonitorConstants {

	public static final int DEFAULT_MONITOR_PORT = 13243;
	public static final int DEFAULT_MONITOR_COLLECTOR_INTERVAL = 1;
	public static final String DEFAULT_MONITOR_DOMAIN = "org.ngrinder.monitor";
	public static final String DEFALUT_MONITOR_DISPLAY_NAME = "nGrinder monitoring agent";
	public static final String RECODER_METHOD_PREFIX = "recoder";
	public static final String SYSTEM = "name=System";
	public static final int DEFAULT_CONTROLLER_CACHE_SIZE = 128;
	public static final int DEFAULT_CONTROLLER_INTERVAL = 1;

	public static final Set<String> SYSTEM_DATA_COLLECTOR = new HashSet<String>();
	static {
		SYSTEM_DATA_COLLECTOR.add(SYSTEM);
	}

	private MonitorConstants() {
	}

	// default collector will only collect system data.
	public static final Set<String> DEFAULT_DATA_COLLECTOR = SYSTEM_DATA_COLLECTOR;

	/**
	 * empty means all processes.
	 */
	public static final Set<Integer> DEFAULT_JVM_PID = new HashSet<Integer>();

	public static final String P_COMMA = ",";

	/**
	 * Initialize the Monitor configuration. The configuration in agent.cnf file is not used now.
	 * 
	 * @param agentConfig
	 *            {@link AgentConfig} from which the property is loaded.
	 */
	public static void init(AgentConfig agentConfig) {
		// TODO: in version 3.1, we will make the monitor listener port configurable.
		// PropertiesWrapper agentProperties = agentConfig.getAgentProperties();
		// DEFAULT_MONITOR_PORT = agentProperties.getPropertyInt("monitor.listen.port",
		// DEFAULT_MONITOR_PORT);
	}
}
