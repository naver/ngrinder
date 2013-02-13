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
package org.ngrinder.monitor.agent.mxbean.core;

import java.io.Serializable;

import org.ngrinder.monitor.agent.collector.AgentDataCollector;

/**
 * 
 * Abstract MXBean class for data monitoring.
 * 
 * @author Mavlarn
 * @since 2.0
 */
public abstract class MXBean implements Serializable {
	private static final long serialVersionUID = 1L;

	// protected MonitorInfo monitorInfo;

	/**
	 * get {@link AgentDataCollector} which is provided by its implementation.
	 * 
	 * @return AgentDataCollector of this MXBean
	 */
	public abstract AgentDataCollector gainAgentDataCollector();

}
