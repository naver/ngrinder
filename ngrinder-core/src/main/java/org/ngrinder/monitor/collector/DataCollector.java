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
package org.ngrinder.monitor.collector;

import org.ngrinder.monitor.agent.MXBeanStorage;
import org.ngrinder.monitor.mxbean.core.MXBean;

/**
 * 
 * Abstract class for data collector.
 * 
 * @author Mavlarn
 * @since 2.0
 */
public abstract class DataCollector implements Runnable {

	/**
	 * hook method to refresh before every collecting.
	 */
	public abstract void refresh();

	protected MXBean getMXBean(String key) {
		return MXBeanStorage.getInstance().getMXBean(key);
	}
}
