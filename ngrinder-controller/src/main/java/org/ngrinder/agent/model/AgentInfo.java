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
package org.ngrinder.agent.model;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.message.console.AgentControllerState;

import org.ngrinder.model.BaseEntity;

/**
 * Agent model.
 * 
 * @author Tobi
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
	@Transient
	private Integer port;

	@Transient
	private AgentIdentity agentIdentity;

	/**
	 * agent application name.
	 */
	private String hostName = "";

	@Transient
	private AgentControllerState status;

	private String region;

	@org.hibernate.annotations.Type(type="true_false")
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
		if (getPort() != other.getPort()) {
			return false;
		}
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

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
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

}
