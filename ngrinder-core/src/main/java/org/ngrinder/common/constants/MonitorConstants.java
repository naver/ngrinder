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
package org.ngrinder.common.constants;

/**
 * Monitor related constants
 *
 * @since 3.3
 */
public interface MonitorConstants {
	public static final String PROP_MONITOR_BINDING_IP = "monitor.binding_ip";
	public static final String PROP_MONITOR_BINDING_PORT = "monitor.binding_port";

	/**
	 * Monitor Constant
	 */
	public static final int DEFAULT_MONITOR_COLLECTOR_INTERVAL = 1;
	public static final String DEFAULT_MONITOR_DOMAIN = "org.ngrinder.monitor";
	public static final String SYSTEM = "name=System";
	public static final String MONITOR_FILE_PREFIX = "monitor_system_";
}
