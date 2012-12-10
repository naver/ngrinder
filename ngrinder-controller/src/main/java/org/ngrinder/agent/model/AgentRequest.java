package org.ngrinder.agent.model;

import java.io.Serializable;

import net.grinder.engine.controller.AgentControllerIdentityImplementation;

import org.ngrinder.agent.service.AgentManagerService;
import org.ngrinder.perftest.service.AgentManager;

public class AgentRequest implements Serializable {
	/**
	 * UUID
	 */
	private static final long serialVersionUID = 1L;

	private RequestType requestType;

	private final Long agentId;

	public enum RequestType {
		STOP_AGENT {
			@Override
			public void request(AgentManager agentManager, AgentManagerService agentManagerService,
							AgentControllerIdentityImplementation agentIdentity) {
				agentManager.stopAgent(agentIdentity);
			}
		};

		RequestType() {
		}

		public abstract void request(AgentManager agentManager, AgentManagerService agentManagerService,
						AgentControllerIdentityImplementation agentIdentity);
	}

	public AgentRequest(Long agentId, RequestType requestType) {
		this.agentId = agentId;
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
}
