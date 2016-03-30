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
package org.ngrinder.extension;

import ro.fortsoft.pf4j.ExtensionPoint;

/**
 * Plugin extension point which is executable when controller start and finish.
 * 
 * This plugin is necessary if you want to notify the controller start and end.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public interface OnControllerLifeCycleRunnable extends ExtensionPoint {

	/**
	 * Callback method which will be invoked whenever Controller is started.
	 * 
	 * 
	 * @param ip		ip
	 * @param version	version
	 */
	public void start(String ip, String version);

	/**
	 * Callback method which will be invoked whenever Controller is stopped.
	 * 
	 * 
	 * @param ip		ip
	 * @param version	version
	 */
	public void finish(String ip, String version);
}
