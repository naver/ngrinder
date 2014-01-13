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
package org.ngrinder.model;

import javax.persistence.*;

import com.google.gson.annotations.Expose;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.message.console.AgentControllerState;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Type;

import static org.ngrinder.common.util.AccessUtils.getSafe;

/**
 * Agent model.
 *
 * @author Tobi
 * @author JunHo Yoon
 * @since 3.0
 */
@SuppressWarnings({"deprecation", "UnusedDeclaration", "JpaDataSourceORMInspection"})
@Entity
@Table(name = "AGENT")
public class AgentInfo extends BaseEntity<AgentInfo> {

	/**
	 * UUID.
	 */
	private static final long serialVersionUID = 677610999461391813L;

	/**
	 * Agent IP.
	 */
	@Expose
	private String ip;

	/**
	 * agent application port. It's only available when the connection is
	 * re-established.
	 */

	@Expose
	private Integer port;

	@Transient
	private AgentIdentity agentIdentity;

	/**
	 * Host name of the agent machine.
	 */

	@Expose
	private String hostName;

	@Expose
	@Enumerated(EnumType.STRING)
	private AgentControllerState state;


	@Expose
	@Column(name = "system_stat", length = 2000)
	private String systemStat;


	@Expose
	private String region;

	@Transient
	private Integer number;


	@Expose
	@Type(type = "true_false")
	@Column(columnDefinition = "char(1) default 'F'")
	private Boolean approved;

	@Expose
	private String version;

	@PrePersist
	public void init() {
		this.approved = getSafe(this.approved, false);
		this.version = getSafe(this.version, "");
		this.region = getSafe(this.region, "");
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public AgentControllerState getState() {
		return state;
	}

	public void setState(AgentControllerState status) {
		this.state = status;
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
	 * @return host name
	 * @deprecated use {@link #getName()} instead.
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * Set host name.
	 *
	 * @param hostName host name
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
	 * @param name name
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
		return approved == null ? false : approved;
	}

	public Boolean getApproved() {
		return approved;
	}

	public void setApproved(Boolean approved) {
		this.approved = approved;
	}

	/**
	 * @return the number
	 * @deprecated unused now.
	 */
	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public String getSystemStat() {
		return systemStat;
	}

	public void setSystemStat(String systemStat) {
		this.systemStat = systemStat;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getVersion() {
		return this.version;
	}
}
