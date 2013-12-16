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
package org.ngrinder.monitor.mxbean.core;

import org.ngrinder.monitor.collector.DataCollector;

import java.io.File;
import java.io.Serializable;

/**
 * 
 * Abstract MXBean class for data monitoring.
 * 
 * @author Mavlarn
 * @since 2.0
 */
public abstract class MXBean implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Get {@link org.ngrinder.monitor.collector.DataCollector} which is provided by its implementation.
	 * 
	 * @param agentHome	agentHome
	 * @return DataCollector of this MXBean
	 */
	public abstract DataCollector gainDataCollector(File agentHome);

}
