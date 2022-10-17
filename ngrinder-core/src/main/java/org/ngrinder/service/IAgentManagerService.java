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
	 * @param userId current user id
	 * @return user available agent count map
	 */
	Map<String, MutableInt> getAvailableAgentCountMap(String userId);

	/**
	 * Get all attached agents. The agent list is obtained from IMap.
	 *
	 * @return agent list
	 */
	List<AgentInfo> getAllAttached();

	/**
	 * Get all attached agents in specific controller.
	 *
	 * @return agent list
	 */
	List<AgentInfo> getLocalAgents();

	/**
	 * Create the agent key from the given agent identity.
	 *
	 * @param agentIdentity agent identity
	 * @return agent key
	 */
	String createKey(AgentControllerIdentityImplementation agentIdentity);

	/**
	 * Get the agent identity by IP and host name.
	 *
	 * @param ip   ip
	 * @param name host name
	 * @return {@link AgentControllerIdentityImplementation} instance.
	 */
	AgentControllerIdentityImplementation getAgentIdentityByIpAndName(String ip, String name);

	/**
	 * Get all active agents from IMap.
	 *
	 * @return agent list
	 */
	List<AgentInfo> getAllActive();

	/**
	 * Get the agent for the given id without agent identity info. If it's called from the other controller, only
	 * limited info available in db will be return.
	 *
	 * @param ip   agent ip
	 * @param name agent name
	 * @return agent
	 */
	AgentInfo getAgent(String ip, String name);

	/**
	 * Get the agent system data model for the given ip. This method is cluster
	 * aware.
	 *
	 * @param ip   agent ip
	 * @param name agent name
	 * @param region region name
	 * @return {@link SystemDataModel} instance.
	 */
	SystemDataModel getSystemDataModel(String ip, String name, String region);

	/**
	 * Update agent
	 *
	 * @param ip   agent ip
	 * @param name agent name
	 */
	void update(String ip, String name) throws IOException;

	/**
	 * Stop agent
	 *
	 * @param ip   agent ip
	 * @param name agent name
	 */
	void stop(String ip, String name) throws IOException;

	/**
	 * Ready status agent count return
	 *
	 * @param userId          the login user id
	 * @param targetRegion    the name of target region
	 *
	 * @return ready status agent count
	 */
	int getReadyAgentCount(String userId, String targetRegion);

	/**
	 * Ready status agent count return
	 *
	 * @param userId          the login user id
	 * @param targetRegion    the name of target region
	 * @param targetSubregion the name of target subregion
	 *
	 * @return ready status agent count
	 */
	int getReadyAgentCount(String userId, String targetRegion, String targetSubregion);

	@Deprecated
	int getReadyAgentCount(User user, String targetRegion);

	@Deprecated
	Map<String, MutableInt> getAvailableAgentCountMap(User user);

	@Deprecated
	List<AgentInfo> getAllVisible();

	@Deprecated
	AgentInfo getOne(String ip, String name);

	@Deprecated
	String createKey(AgentInfo agentInfo);
}
