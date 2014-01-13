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
package org.ngrinder.service;

import org.ngrinder.model.AgentInfo;

import java.util.List;

/**
 * Abstract Agent manager service class which is used to retrieve the agent info
 * attaching the current controller.
 *
 * This class is created to guarantee the backward compatibility for IAgentManagerService.
 *
 * @author JunHo Yoon
 * @since 3.3
 */
public abstract class AbstractAgentManagerService implements IAgentManagerService {


	@SuppressWarnings("UnusedDeclaration")
	public List<AgentInfo> getLocalAgents() {
		return getAllLocal();
	}
}