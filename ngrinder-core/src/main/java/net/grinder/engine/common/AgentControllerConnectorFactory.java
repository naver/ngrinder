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
package net.grinder.engine.common;

import net.grinder.common.GrinderProperties;
import net.grinder.communication.AgentControllerCommunicationDefauts;
import net.grinder.communication.ConnectionType;
import net.grinder.communication.Connector;

import org.ngrinder.infra.AgentConfig;

/**
 * ConnectorFactory.
 * 
 * @author Philip Aston
 */
public class AgentControllerConnectorFactory {

	private final ConnectionType m_connectionType;

	/**
	 * Constructor.
	 * 
	 * @param connectionType
	 *            The connection type.
	 */
	public AgentControllerConnectorFactory(ConnectionType connectionType) {
		m_connectionType = connectionType;
	}

	/**
	 * Factory method.
	 * 
	 * @param properties
	 *            Properties.
	 * @return A connector which can be used to contact the console.
	 */
	public Connector create(GrinderProperties properties) {
		String host = properties.getProperty(AgentConfig.AGENT_CONTROLER_SERVER_HOST,
				AgentControllerCommunicationDefauts.DEFAULT_AGENT_CONTROLLER_SERVER_HOST);
		int port = properties.getInt(
				AgentConfig.AGENT_CONTROLER_SERVER_PORT,
				AgentControllerCommunicationDefauts.DEFAULT_AGENT_CONTROLLER_SERVER_PORT);
		return new Connector(host, port, m_connectionType);
	}
}
