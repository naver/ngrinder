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
	public static final String SYSTEM = "name=System";
	public static final Set<String> SYSTEM_DATA_COLLECTOR = new HashSet<String>();

	static {
		SYSTEM_DATA_COLLECTOR.add(SYSTEM);
	}
}
