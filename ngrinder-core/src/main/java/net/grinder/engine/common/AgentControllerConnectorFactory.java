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
package net.grinder.engine.common;

import net.grinder.communication.ConnectionType;
import net.grinder.communication.Connector;

/**
 * ConnectorFactory.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public class AgentControllerConnectorFactory {

	private final ConnectionType m_connectionType;

	/**
	 * Constructor.
	 *
	 * @param connectionType The connection type.
	 */
	public AgentControllerConnectorFactory(ConnectionType connectionType) {
		m_connectionType = connectionType;
	}

	/**
	 * Factory method.
	 *
	 * @param host host
	 * @param port port
	 * @return A connector which can be used to contact the console.
	 */
	public Connector create(String host, int port) {
		return new Connector(host, port, m_connectionType);
	}
}
