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
package org.ngrinder.agent.controller;

import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.controller.RestAPI;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.model.User;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.ngrinder.common.util.CollectionUtils.buildMap;

/**
 * Agent management api controller.
 *
 * @since 3.5.0
 */
@RestController
@RequestMapping("/agent")
@PreAuthorize("hasAnyRole('A', 'S')")
public class AgentManagerApiController extends AgentManagerBaseController {

	/**
	 * Clean up the agents in the inactive region
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = "/api", params = "action=cleanup", method = RequestMethod.POST)
	public String cleanUpAgentsInInactiveRegion() {
		agentManagerService.cleanup();
		return returnSuccess();
	}

	/**
	 * Get the current performance of the given agent.
	 *
	 * @param ip agent ip
	 * @param name agent name
	 * @param region agent region
	 *
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@GetMapping("/api/state")
	public SystemDataModel getState(@RequestParam String ip, @RequestParam String name, @RequestParam String region) {
		return agentManagerService.getSystemDataModel(ip, name, region);
	}

	/**
	 * Get the current all agents state.
	 *
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A', 'S', 'U')")
	@RequestMapping({"/api/states/", "/api/states"})
	public List<Map<String, Object>> getStates() {
		List<AgentInfo> agents = agentManagerService.getAllVisible();
		return getAgentStatus(agents);
	}

	/**
	 * Get all agents from database.
	 *
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping({"/api/", "/api"})
	public List<AgentInfo> getAll() {
		return agentManagerService.getAllVisible();
	}

	/**
	 * Get the agent for the given agent id.
	 *
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = "/api/{id}", method = RequestMethod.GET)
	public AgentInfo getOne(@PathVariable("id") Long id) {
		return agentManagerService.getOne(id);
	}

	/**
	 * Approve an agent.
	 *
	 * @param id agent id
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = "/api/{id}", params = "action=approve", method = RequestMethod.PUT)
	public String approve(@PathVariable("id") Long id) {
		agentManagerService.approve(id, true);
		return returnSuccess();
	}

	/**
	 * Disapprove an agent.
	 *
	 * @param id agent id
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = "/api/{id}", params = "action=disapprove", method = RequestMethod.PUT)
	public String disapprove(@PathVariable("id") Long id) {
		agentManagerService.approve(id, false);
		return returnSuccess();
	}

	/**
	 * Stop the given agent.
	 *
	 * @param id agent id
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = "/api/{id}", params = "action=stop", method = RequestMethod.PUT)
	public String stop(@PathVariable("id") Long id) {
		agentManagerService.stopAgent(id);
		return returnSuccess();
	}

	/**
	 * Stop the given agent.
	 *
	 * @param ids comma separated agent id list
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = "/api", params = "action=stop", method = RequestMethod.PUT)
	public String stop(@RequestParam("ids") String ids) {
		String[] split = StringUtils.split(ids, ",");
		for (String each : split) {
			stop(Long.parseLong(each));
		}
		return returnSuccess();
	}

	/**
	 * Update the given agent.
	 *
	 * @param id agent id
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = "/api/{id}", params = "action=update", method = RequestMethod.PUT)
	public String update(@PathVariable("id") Long id) {
		agentManagerService.update(id);
		return returnSuccess();
	}

	/**
	 * Send update message to agent side
	 *
	 * @param ids comma separated agent id list
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = "/api", params = "action=update", method = RequestMethod.PUT)
	public String update(@RequestParam("ids") String ids) {
		String[] split = StringUtils.split(ids, ",");
		for (String each : split) {
			update(Long.parseLong(each));
		}
		return returnSuccess();
	}

	/**
	 * Get the number of available agents.
	 * 
	 * @param user The login user
	 * @param targetRegion The name of target region
	 * @return availableAgentCount Available agent count
	 */
	@RestAPI
	@RequestMapping("/api/availableAgentCount")
	@PreAuthorize("permitAll")
	public Map<String, Object> getAvailableAgentCount(User user, @RequestParam(value = "targetRegion", required = true) String targetRegion) {
		int availableAgentCount = agentManagerService.getReadyAgentCount(user, targetRegion);
		return buildMap("availableAgentCount", availableAgentCount);
	}
}
