/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.grinder.message.console;

import org.ngrinder.monitor.controller.model.JavaDataModel;
import org.ngrinder.monitor.controller.model.SystemDataModel;

import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.communication.Address;
import net.grinder.communication.AddressAwareMessage;
import net.grinder.communication.CommunicationException;
import net.grinder.messages.console.AgentAddress;

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

	private final JavaDataModel javaDataModel;

	private final SystemDataModel systemDataModel;

	private int connectingPort = 0;

	/**
	 * Creates a new <code>AgentProcessReportMessage</code> instance.
	 * 
	 * @param state
	 *            The process state. See {@link net.grinder.common.processidentity.ProcessReport}.
	 */
	public AgentControllerProcessReportMessage(AgentControllerState state, JavaDataModel javaDataModel,
					SystemDataModel systemDataModel, int connectingPort) {
		m_state = state;
		this.javaDataModel = javaDataModel;
		this.systemDataModel = systemDataModel;
		this.connectingPort = connectingPort;
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

	public JavaDataModel getJavaDataModel() {
		return javaDataModel;
	}

	public SystemDataModel getSystemDataModel() {
		return systemDataModel;
	}

	public int getConnectingPort() {
		return connectingPort;
	}

}
