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

import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.communication.Address;
import net.grinder.communication.AddressAwareMessage;
import net.grinder.communication.CommunicationException;
import net.grinder.messages.console.AgentAddress;
import org.ngrinder.monitor.controller.model.SystemDataModel;

/**
 * Message for informing the console of agent process status.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public class AgentControllerProcessReportMessage implements AddressAwareMessage {

	private static final long serialVersionUID = 4L;

	private final AgentControllerState m_state;

	private transient AgentAddress m_processAddress;

	private final SystemDataModel systemDataModel;

	private int connectingPort = 0;

	private String version;

	/**
	 * Creates a new <code>AgentProcessReportMessage</code> instance.
	 * 
	 * @param state	The process state. See {@link net.grinder.common.processidentity.ProcessReport}.
	 * @param systemDataModel system performance data model
	 * @param connectingPort agent connecting console port
	 * @param version agent version
	 */
	public AgentControllerProcessReportMessage(AgentControllerState state,
					SystemDataModel systemDataModel, int connectingPort, String version) {
		this.m_state = state;
		this.systemDataModel = systemDataModel;
		this.connectingPort = connectingPort;
		this.version = version;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAddress(Address address) throws CommunicationException {

		try {
			m_processAddress = (AgentAddress) address;
		} catch (ClassCastException e) {
			throw new CommunicationException("Not an agent process address", e);
		}
	}

	/**
	 * Accessor for the process identity.
	 * 
	 * @return The process identity.
	 */
	@SuppressWarnings("UnusedDeclaration")
	public AgentAddress getProcessAddress() {
		return m_processAddress;
	}

	/**
	 * Accessor for the process identity.
	 * 
	 * @return The process identity.
	 */
	public AgentIdentity getAgentIdentity() {
		return m_processAddress.getIdentity();
	}

	/**
	 * Accessor for the process state.
	 * 
	 * @return The process state.
	 */
	public AgentControllerState getState() {
		return m_state;
	}

	public SystemDataModel getSystemDataModel() {
		return systemDataModel;
	}

	public int getConnectingPort() {
		return connectingPort;
	}

	public String getVersion() {
		return version;
	}
}
