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

import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.message.console.AgentControllerState;

import org.hibernate.annotations.Type;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static org.ngrinder.common.util.AccessUtils.getSafe;

/**
 * Agent model.
 *
 * @author Tobi
 * @author JunHo Yoon
 * @since 3.0
 */
@SuppressWarnings({"deprecation", "UnusedDeclaration", "JpaDataSourceORMInspection"})
@Getter
@Setter
@ToString
@Entity
@Table(name = "AGENT")
public class AgentInfo extends BaseEntity<AgentInfo> {

	private static final long serialVersionUID = 677610999461391813L;

	private String ip;

	/**
	 * agent application port. It's only available when the connection is
	 * re-established.
	 */
	private Integer port;

	@Transient
	private AgentIdentity agentIdentity;

	/**
	 * Host name of the agent machine.
	 */
	@Column(name = "hostName")
	private String name;

	@Transient
	private AgentControllerState state;

	@Column(name = "system_stat", length = 2000)
	private String systemStat;

	private String region;

	@Type(type = "true_false")
	@Column(columnDefinition = "char(1) default 'F'")
	private Boolean approved;

	private String version;

	@PrePersist
	public void init() {
		this.approved = getSafe(this.approved, false);
		this.version = getSafe(this.version, "");
		this.region = getSafe(this.region, "");
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

	@Deprecated
	public String getHostName() {
		return getName();
	}

	@Deprecated
	public void setHostName(String name) {
		setName(name);
	}

	public boolean isApproved() {
		return approved == null ? false : approved;
	}

}
