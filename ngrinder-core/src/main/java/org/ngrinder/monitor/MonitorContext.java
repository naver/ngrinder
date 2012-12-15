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
 * Monitor context class, used to store the collectors of monitor. Currently, there is only system
 * data collector.
 * 
 * @author Mavlarn
 * @since 3.0
 */
public final class MonitorContext {

	private static final MonitorContext INSTANCE = new MonitorContext();

	private MonitorContext() {
	}

	public static MonitorContext getInstance() {
		return INSTANCE;
	}

	private Set<String> dataCollectors = new HashSet<String>();

	public void setDataCollectors(Set<String> dataCollectors) {
		this.dataCollectors = dataCollectors;
	}

	public Set<String> getDataCollectors() {
		return dataCollectors;
	}

}
