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
package org.ngrinder.agent.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.message.console.AgentControllerState;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Type;
import org.ngrinder.model.BaseEntity;

/**
 * Agent model.
 * 
 * @author Tobi
 * @author JunHo Yoon
 * @since 3.0
 */
@Entity
@Table(name = "AGENT")
public class AgentInfo extends BaseEntity<AgentInfo> {

	/**
	 * UUID.
	 */
	private static final long serialVersionUID = 677610999461391813L;

	/** Agent IP. */
	private String ip;

	/**
	 * agent application port. It's only available when the connection is re-established.
	 */
	private Integer port;

	@Transient
	private AgentIdentity agentIdentity;

	/**
	 * agent application name.
	 */
	private String hostName = "";

	@Enumerated(EnumType.STRING)
	private AgentControllerState status;

	private String region;

	@Transient
	private int number;

	@Type(type = "true_false")
	@Column(columnDefinition = "char(1)")
	private boolean approved = false;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public AgentControllerState getStatus() {
		return status;
	}

	public void setStatus(AgentControllerState status) {
		this.status = status;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + port;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AgentInfo other = (AgentInfo) obj;
		if (ip == null) {
			if (other.ip != null) {
				return false;
			}
		} else if (!ip.equals(other.ip)) {
			return false;
		}
		return true;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	/**
	 * Get host name.
	 * 
	 * @deprecated use {@link #getName()} instead.
	 * @return host name
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * Set host name.
	 * 
	 * @param hostName
	 *            host name
	 * @deprecated use {@link #setName(String)} instead
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getName() {
		return getHostName();
	}

	/**
	 * Set name.
	 * 
	 * @param name
	 *            name
	 */
	public void setName(String name) {
		setHostName(name);
	}

	public AgentIdentity getAgentIdentity() {
		return agentIdentity;
	}

	public void setAgentIdentity(AgentIdentity agentIdentity) {
		this.agentIdentity = agentIdentity;
	}

	public boolean isApproved() {
		return approved;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}

	/**
	 * @deprecated unused now.
	 * @return the number
	 */
	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
