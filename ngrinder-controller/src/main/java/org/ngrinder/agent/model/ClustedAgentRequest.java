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

import java.io.Serializable;

import net.grinder.engine.controller.AgentControllerIdentityImplementation;

import org.ngrinder.agent.service.ClusteredAgentManagerService;
import org.ngrinder.perftest.service.AgentManager;

/**
 * Agent related Request.
 * 
 * @author JunHo Yoon
 * @since 3.1
 */
public class ClustedAgentRequest implements Serializable {
	/**
	 * UUID.
	 */
	private static final long serialVersionUID = 1L;

	private RequestType requestType;

	private final Long agentId;

	private final String agentIp;

	private final String agentName;

	/**
	 * Request Type.
	 * 
	 * @author JunHo Yoon
	 */
	public enum RequestType {
		STOP_AGENT {
			@Override
			public void process(Long agentId, String agentIp, AgentManager agentManager,
							ClusteredAgentManagerService agentManagerService,
							AgentControllerIdentityImplementation agentIdentity) {
				agentManager.stopAgent(agentIdentity);
			}
		},
		SHARE_AGENT_SYSTEM_DATA_MODEL {
			@Override
			public void process(Long agentId, String agentIp, AgentManager agentManager,
							ClusteredAgentManagerService agentManagerService,
							AgentControllerIdentityImplementation agentIdentity) {
				agentManagerService.addAgentMonitoringTarget(agentId, agentIp, agentIdentity);
			}
		};

		RequestType() {
		}

		/**
		 * Process the request.
		 * 
		 * @param agentManager
		 *            agentManager
		 * @param agentManagerService
		 *            agentManagerService
		 * @param agentIdentity
		 *            agentIdentity
		 */
		public abstract void process(Long agentId, String agentIp, AgentManager agentManager,
						ClusteredAgentManagerService agentManagerService,
						AgentControllerIdentityImplementation agentIdentity);
	}

	/**
	 * Constructor.
	 * 
	 * @param agentId
	 *            agent id
	 * @param requestType
	 *            request type
	 */
	public ClustedAgentRequest(Long agentId, String agentIp, String agentName, RequestType requestType) {
		this.agentId = agentId;
		this.agentIp = agentIp;
		this.agentName = agentName;
		this.requestType = requestType;
	}

	public RequestType getRequestType() {
		return requestType;
	}

	public void setRequestType(RequestType requestType) {
		this.requestType = requestType;
	}

	public Long getAgentId() {
		return agentId;
	}

	public String getAgentIp() {
		return agentIp;
	}

	public String getAgentName() {
		return agentName;
	}
}
