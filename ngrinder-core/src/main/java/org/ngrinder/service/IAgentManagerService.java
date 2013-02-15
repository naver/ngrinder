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

import java.util.List;
import java.util.Map;

import net.grinder.engine.controller.AgentControllerIdentityImplementation;

import org.apache.commons.lang.mutable.MutableInt;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.model.User;
import org.ngrinder.monitor.controller.model.SystemDataModel;

/**
 * Agent manager service interface which is used to retrieve the agent infos from current
 * controller.
 * 
 * @author JunHo Yoon
 * @since 3.1.2
 */
public interface IAgentManagerService {

	/**
	 * get the available agent count map in all regions of the user, including the free agents and
	 * user specified agents.
	 * 
	 * @param user
	 *            current user
	 * @return user available agent count map
	 */
	public abstract Map<String, MutableInt> getUserAvailableAgentCountMap(User user);

	/**
	 * Get all agent agents. agent list is obtained from DB and {@link AgentManager}
	 * 
	 * This includes not persisted agent as well.
	 * 
	 * @return agent list
	 */
	public abstract List<AgentInfo> getLocalAgents();

	/**
	 * Create agent key.
	 * 
	 * @param agentInfo
	 *            agent information
	 * 
	 * @return agent key
	 */
	public abstract String createAgentKey(AgentInfo agentInfo);

	/**
	 * Create agent key.
	 * 
	 * @param agentIdentity
	 *            agent identity
	 * 
	 * @return agent key
	 */
	public abstract String createAgentKey(AgentControllerIdentityImplementation agentIdentity);

	/**
	 * Get agent identity by ip and name.
	 * 
	 * @param ip
	 *            ip
	 * @param name
	 *            name
	 * @return {@link AgentControllerIdentityImplementation} instance.
	 */
	public abstract AgentControllerIdentityImplementation getLocalAgentIdentityByIpAndName(String ip, String name);

	/**
	 * Get all agents attached of this region from DB.
	 * 
	 * This method is cluster aware. If it's cluster mode it return all agents attached in this
	 * region.
	 * 
	 * @return agent list
	 */
	public abstract List<AgentInfo> getLocalAgentListFromDB();

	/**
	 * Get all active agents from DB.
	 * 
	 * @return agent list
	 */
	public abstract List<AgentInfo> getAllActiveAgentInfoFromDB();

	/**
	 * Get all visible agents from DB.
	 * 
	 * @return agent list
	 */
	public abstract List<AgentInfo> getAllVisibleAgentInfoFromDB();

	/**
	 * Get a agent on given id. If it's called from the other controller, only limited info
	 * available in db will be return.
	 * 
	 * @param id
	 *            agent id
	 * @param includeAgentIndentity
	 *            include agent identity
	 * @return agent
	 */
	public abstract AgentInfo getAgent(long id, boolean includeAgentIndentity);

	/**
	 * Get agent system data model for the given ip. This method is cluster aware.
	 * 
	 * @param ip
	 *            agent ip.
	 * @param name
	 *            agent name
	 * @return {@link SystemDataModel} instance.
	 */
	public abstract SystemDataModel getAgentSystemDataModel(String ip, String name);

}