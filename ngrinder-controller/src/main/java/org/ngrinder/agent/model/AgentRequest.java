package org.ngrinder.agent.model;

import java.io.Serializable;

import net.grinder.engine.controller.AgentControllerIdentityImplementation;

import org.ngrinder.agent.service.AgentManagerService;
import org.ngrinder.perftest.service.AgentManager;

/**
 * Agent related Request.
 * 
 * @author JunHo Yoon
 * @since 3.1
 */
public class AgentRequest implements Serializable {
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
							AgentManagerService agentManagerService, AgentControllerIdentityImplementation agentIdentity) {
				agentManager.stopAgent(agentIdentity);
			}
		},
		SHARE_AGENT_SYSTEM_DATA_MODEL {
			@Override
			public void process(Long agentId, String agentIp, AgentManager agentManager,
							AgentManagerService agentManagerService, AgentControllerIdentityImplementation agentIdentity) {
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
						AgentManagerService agentManagerService, AgentControllerIdentityImplementation agentIdentity);
	}

	/**
	 * Constructor.
	 * 
	 * @param agentId
	 *            agent id
	 * @param requestType
	 *            request type
	 */
	public AgentRequest(Long agentId, String agentIp, String agentName, RequestType requestType) {
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
