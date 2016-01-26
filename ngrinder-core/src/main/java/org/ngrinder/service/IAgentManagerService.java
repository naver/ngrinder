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

import net.grinder.engine.controller.AgentControllerIdentityImplementation;
import org.apache.commons.lang.mutable.MutableInt;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.model.User;
import org.ngrinder.monitor.controller.model.SystemDataModel;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Agent manager service interface which is used to retrieve the agent info
 * attaching the current controller.
 *
 * @author JunHo Yoon
 * @since 3.1.2
 */
public interface IAgentManagerService {

	/**
	 * Get the available agent count map across all users including the free
	 * agents and user's private agents.
	 *
	 * @param user current user
	 * @return user available agent count map
	 */
	public abstract Map<String, MutableInt> getAvailableAgentCountMap(User user);

	/**
	 * Get all local agents. The agent list is obtained from db and cached.
	 *
	 * @return agent list
	 */
	public abstract List<AgentInfo> getAllLocal();

	/**
	 * Get all local agent agents. The agent list is obtained by combining the data from
	 * DB and {@link AgentManager}
	 *
	 * @return agent list
	 */
	@SuppressWarnings("UnusedDeclaration")
	public abstract List<AgentInfo> getAllLocalWithFullInfo();

	/**
	 * Create the agent key from the given agent info.
	 *
	 * @param agentInfo agent information
	 * @return agent key
	 */
	public abstract String createKey(AgentInfo agentInfo);

	/**
	 * Create the agent key from the given agent identity.
	 *
	 * @param agentIdentity agent identity
	 * @return agent key
	 */
	public abstract String createKey(AgentControllerIdentityImplementation agentIdentity);

	/**
	 * Get the agent identity by IP and host name.
	 *
	 * @param ip   ip
	 * @param name host name
	 * @return {@link AgentControllerIdentityImplementation} instance.
	 */
	public abstract AgentControllerIdentityImplementation getAgentIdentityByIpAndName(String ip, String name);

	/**
	 * Get all active agents from DB.
	 *
	 * @return agent list
	 */
	public abstract List<AgentInfo> getAllActive();


	/**
	 * Get local agents. This is only for backward compatibility.
	 *
	 * @return local agents
	 * @deprecated Use IAgentManagerService#getAllLocal
	 */
	@SuppressWarnings("UnusedDeclaration")
	public abstract List<AgentInfo> getLocalAgents();

	/**
	 * Get all visible agents from DB.
	 *
	 * @return agent list
	 */
	public abstract List<AgentInfo> getAllVisible();

	/**
	 * Get the agent for the given id without agent identity info. If it's called from the other controller, only
	 * limited info available in db will be return.
	 *
	 * @param id agent id
	 * @return agent
	 */
	public abstract AgentInfo getOne(Long id);

	/**
	 * Get the agent for the given id. If it's called from the other controller, only
	 * limited info available in db will be return.
	 *
	 * @param id                   agent id
	 * @param includeAgentIdentity include agent identity field.
	 * @return agent
	 */
	public abstract AgentInfo getOne(Long id, boolean includeAgentIdentity);

	/**
	 * Get the agent system data model for the given ip. This method is cluster
	 * aware.
	 *
	 * @param ip   agent ip.
	 * @param name agent name
	 * @return {@link SystemDataModel} instance.
	 */
	public abstract SystemDataModel getSystemDataModel(String ip, String name);

	/**
	 * Update agent
	 *
	 * @param id ids.
	 */
	public abstract void update(Long id) throws IOException;
	
	/**
	 * Get Ready agent state count return
	 *
	 * @param user current user
	 * @return int readyAgentCnt
	 */
	public abstract int getReadyAgentCount(User user, String targetRegion);

}