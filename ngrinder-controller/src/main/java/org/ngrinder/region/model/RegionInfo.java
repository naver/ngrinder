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
package org.ngrinder.region.model;

import java.io.Serializable;
import java.util.Set;

import net.grinder.common.processidentity.AgentIdentity;

/**
 * Region info to be shared b/w controllers.
 *
 * @author JunHo Yoon
 * @since 3.1
 */
@SuppressWarnings("UnusedDeclaration")
public class RegionInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	private String ip;
	private Integer controllerPort;
	private boolean visible = true;
	private Set<AgentIdentity> agentIdentities;


	/**
	 * Constructor with true visibility.
	 *
	 * @param ip              ip
	 * @param controllerPort  controllerPort
	 * @param agentIdentities agentIdentity Set
	 */
	public RegionInfo(String ip, int port, Set<AgentIdentity> agentIdentities) {
		this(ip, port, agentIdentities, true);
	}

	/**
	 * Constructor.
	 *
	 * @param ip              ip
	 * @param agentIdentities agentIdentity Set
	 * @param visible         true if visible
	 */
	public RegionInfo(String ip, Set<AgentIdentity> agentIdentities, boolean visible) {
		this(ip, null, agentIdentities, visible);
	}

	/**
	 * Constructor.
	 *
	 * @param ip              ip
	 * @param agentIdentities agentIdentity Set
	 * @param controllerPort  controllerPort
	 * @param visible         true if visible
	 */
	public RegionInfo(String ip, Integer controllerPort, Set<AgentIdentity> agentIdentities, boolean visible) {
		this.ip = ip;
		this.controllerPort = controllerPort;
		this.visible = visible;
		this.agentIdentities = agentIdentities;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}


	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}


	public Set<AgentIdentity> getAgentIdentities() {
		return agentIdentities;
	}

	public void setAgentIdentities(Set<AgentIdentity> agentIdentities) {
		this.agentIdentities = agentIdentities;
	}

	public Integer getControllerPort() {
		return controllerPort;
	}
}
