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

import lombok.Getter;
import lombok.Setter;
import net.grinder.common.processidentity.AgentIdentity;

import java.io.Serializable;
import java.util.Set;

/**
 * Region info to be shared b/w controllers.
 *
 * @since 3.1
 */
@Getter
@SuppressWarnings("UnusedDeclaration")
public class RegionInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	@Setter
	private String ip;
	private Integer controllerPort;
	@Setter
	private boolean visible = true;
	@Setter
	private Set<AgentIdentity> agentIdentities;
	private String regionName;


	/**
	 * Constructor with true visibility.
	 *
	 * @param ip              ip
	 * @param controllerPort  controllerPort
	 * @param agentIdentities agentIdentity Set
	 */
	public RegionInfo(String ip, int controllerPort, Set<AgentIdentity> agentIdentities) {
		this(ip, controllerPort, agentIdentities, true);
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

	public RegionInfo(String regionName , String ip, Integer controllerPort, Set<AgentIdentity> agentIdentities) {
		this.ip = ip;
		this.controllerPort = controllerPort;
		this.agentIdentities = agentIdentities;
		this.regionName = regionName;
	}

}
