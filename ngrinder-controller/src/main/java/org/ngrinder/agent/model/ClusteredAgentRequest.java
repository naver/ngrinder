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

import net.grinder.engine.controller.AgentControllerIdentityImplementation;
import org.ngrinder.agent.service.ClusteredAgentManagerService;

import java.io.Serializable;

/**
 * Agent control request b/w controllers in the clustered nGrinder installation.
 *
 * @author JunHo Yoon
 * @since 3.1
 */
public class ClusteredAgentRequest implements Serializable {
	/**
	 * UUID.
	 */
	private static final long serialVersionUID = 1L;

	private RequestType requestType;

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
			public void process(ClusteredAgentManagerService agentManagerService,
			                    AgentControllerIdentityImplementation agentIdentity) {
				agentManagerService.stopAgent(agentIdentity);
			}
		},
		EXPIRE_LOCAL_CACHE {
			@Override
			public void process(ClusteredAgentManagerService agentManagerService,
			                    AgentControllerIdentityImplementation agentIdentity) {
			}

		},
		UPDATE_AGENT {
			@Override
			public void process(ClusteredAgentManagerService agentManagerService,
								AgentControllerIdentityImplementation agentIdentity) {
				agentManagerService.updateAgent(agentIdentity);
			}
		},
		SHARE_AGENT_SYSTEM_DATA_MODEL {
			@Override
			public void process(ClusteredAgentManagerService agentManagerService,
								AgentControllerIdentityImplementation agentIdentity) {
				agentManagerService.addAgentMonitoringTarget(agentIdentity);
			}
		};

		RequestType() {
		}

		/**
		 * Process the request.
		 *
		 * @param agentManagerService agentManagerService
		 * @param agentIdentity       agentIdentity
		 */
		public abstract void process(ClusteredAgentManagerService agentManagerService,
		                             AgentControllerIdentityImplementation agentIdentity);
	}

	/**
	 * Constructor.
	 *
	 * @param agentIp     agent ip
	 * @param agentName   agent name
	 * @param requestType request type
	 */
	public ClusteredAgentRequest(String agentIp, String agentName, RequestType requestType) {
		this.agentIp = agentIp;
		this.agentName = agentName;
		this.requestType = requestType;
	}

	public RequestType getRequestType() {
		return requestType;
	}

	@SuppressWarnings("UnusedDeclaration")
	public void setRequestType(RequestType requestType) {
		this.requestType = requestType;
	}

	public String getAgentIp() {
		return agentIp;
	}

	public String getAgentName() {
		return agentName;
	}
}
