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
package net.grinder.communication;

import net.grinder.util.NetworkUtils;

/**
 * Default constant value for agent controller.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public abstract class AgentControllerCommunicationDefaults {

	/**
	 * Default agent controller daemon host. Bind to all interfaces by default.
	 */
	public static final String DEFAULT_AGENT_CONTROLLER_SERVER_HOST = NetworkUtils.DEFAULT_LOCAL_HOST_ADDRESS;

	/**
	 * Default agent controller daemon port.
	 */
	public static final int DEFAULT_AGENT_CONTROLLER_SERVER_PORT = 16001;

}
