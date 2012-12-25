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


/**
 * 
 * Monitor recorder interface.
 *
 * @author Mavlarn
 * @since 2.0
 */
public interface MonitorRecorder {
	
	/**
	 * It is used to initialize before storing monitor data, and executed during
	 * MonitorExecuteWorker is initializing.
	 */
	void before();

	/**
	 * function to record the SystemInfo.
	 * 
	 * @param key is a key to mark the monitoring worker
	 * @param systemInfo is current collected system data
	 * @param agentInfo is the target of this monitoring
	 */
	//void recoderSystemInfo(final String key, final SystemInfo systemInfo, final MonitorAgentInfo agentInfo);

	/**
	 * Used to finalize after recording all data. It is called when MonitorExecuteWorker is closed.
	 */
	void after();
}