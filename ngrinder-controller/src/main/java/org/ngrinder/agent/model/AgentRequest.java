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

import lombok.Getter;
import lombok.Setter;
import net.grinder.engine.controller.AgentControllerIdentityImplementation;
import org.ngrinder.agent.service.ClusteredAgentManagerService;

import java.io.Serializable;

import static org.ngrinder.common.util.NoOp.noOp;

/**
 * Agent control request b/w controllers in the clustered nGrinder installation.
 *
 * @since 3.1
 */
@Getter
public class AgentRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String agentIp;

	private final String agentName;

	@Setter
	private RequestType requestType;

	public enum RequestType {
		STOP_AGENT {
			@Override
			public void process(ClusteredAgentManagerService agentManagerService,
			                    AgentControllerIdentityImplementation agentIdentity) {
				agentManagerService.stop(agentIdentity);
			}
		},
		UPDATE_AGENT {
			@Override
			public void process(ClusteredAgentManagerService agentManagerService,
								AgentControllerIdentityImplementation agentIdentity) {
				agentManagerService.updateAgent(agentIdentity);
			}
		};

		RequestType() {
		}

		public abstract void process(ClusteredAgentManagerService agentManagerService,
		                             AgentControllerIdentityImplementation agentIdentity);
	}

	public AgentRequest(String agentIp, String agentName, RequestType requestType) {
		this.agentIp = agentIp;
		this.agentName = agentName;
		this.requestType = requestType;
	}

}
