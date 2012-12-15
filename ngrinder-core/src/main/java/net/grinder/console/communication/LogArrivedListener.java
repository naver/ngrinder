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
package net.grinder.console.communication;

import java.util.EventListener;

import net.grinder.messages.console.AgentAddress;

/**
 * Listener which is invoked when log is arrived from agent controller.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public interface LogArrivedListener extends EventListener {
	/**
	 * callback method which will be called when log is arrived from a agent.
	 * @param testId test id
	 * @param agentAddress agent address
	 * @param logs compressed log byte array
	 */
	public void logArrived(String testId, AgentAddress agentAddress, byte[] logs);
}
