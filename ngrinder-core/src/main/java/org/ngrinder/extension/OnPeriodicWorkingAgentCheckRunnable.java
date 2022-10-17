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

import net.grinder.console.communication.AgentProcessControlImplementation.AgentStatus;
import org.pf4j.ExtensionPoint;

import java.util.Set;

/**
 * Plugin extension point which is executed whenever the controller periodically check currently
 * working agent status.
 *
 *
 * @author JunHo Yoon
 * @since 3.1.2.
 */
public interface OnPeriodicWorkingAgentCheckRunnable extends ExtensionPoint {

	/**
	 * Check currently working agent status.
	 *
	 * @param workingAgents working agent set
	 */
	void checkWorkingAgent(Set<AgentStatus> workingAgents);
}
