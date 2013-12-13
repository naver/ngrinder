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
package net.grinder.engine.communication;

import net.grinder.communication.Address;
import net.grinder.communication.AddressAwareMessage;
import net.grinder.messages.console.AgentAddress;

/**
 * NGrinder message to send a log to console.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public class LogReportGrinderMessage implements AddressAwareMessage {
	private static final long serialVersionUID = 1274708100107820158L;
	private AgentAddress m_processAddress;
	private final byte[] logs;
	private final String testId;

	/**
	 * Constructor.
	 * 
	 * @param testId		corresponding test id
	 * @param logs			compressed log byte array
	 * @param agentAddress	agent address
	 */
	public LogReportGrinderMessage(String testId, byte[] logs, AgentAddress agentAddress) {
		this.testId = testId;
		this.logs = logs;
		setAddress(agentAddress);
	}

	@Override
	public void setAddress(Address address) {
		m_processAddress = (AgentAddress) address;
	}

	public AgentAddress getAddress() {
		return m_processAddress;
	}

	public byte[] getLogs() {
		return logs;
	}

	public String getTestId() {
		return testId;
	}

}
