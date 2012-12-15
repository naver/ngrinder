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
package net.grinder.message.console;

/**
 * Agent Controller State.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public enum AgentControllerState {
	/** Just after agent controller is started. */
	STARTED,
	/** When agent controller is ready to get some message. */
	READY,
	/** When agent controller runs agents or other jobs. */
	BUSY,
	/** When agent controller is closed. */
	FINISHED,
	/** When agent controller is down. */
	INACTIVE,
	/** When agent is attached wrong region */
	WRONG_REGION,
	/** Unknown. */
	UNKNOWN
}
