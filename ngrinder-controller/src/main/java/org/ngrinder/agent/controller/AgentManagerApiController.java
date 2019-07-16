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

import static com.google.common.collect.ImmutableMap.of;
import static java.util.stream.Collectors.toList;
import static org.ngrinder.common.util.SpringSecurityUtils.containsAuthority;
import static org.ngrinder.common.util.SpringSecurityUtils.getCurrentAuthorities;

import org.apache.commons.lang.StringUtils;
import org.ngrinder.agent.service.AgentManagerService;
import org.ngrinder.agent.service.AgentPackageService;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.common.controller.RestAPI;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.model.User;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.region.model.RegionInfo;
import org.ngrinder.region.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @since 3.5.0
 */
@RestController
@RequestMapping("/agent/api")
@PreAuthorize("hasAnyRole('A', 'S')")
public class AgentManagerApiController extends BaseController {

	@Autowired
	private AgentManagerService agentManagerService;

	@Autowired
	private RegionService regionService;

	@Autowired
	private AgentPackageService agentPackageService;

	@RestAPI
	@GetMapping("/regions")
	@PreAuthorize("hasAnyRole('A', 'S', 'U')")
	public List<String> getAvailableRegions(final User user) {
		return availRegions();
	}

	@RestAPI
	@GetMapping("/download_link")
	@PreAuthorize("hasAnyRole('A', 'S', 'U')")
	public String getDownloadLink(final User user,  @RequestParam(value = "region", required = false) final String region) {
		String downloadLink = "";
		File agentPackage = null;
		if (isClustered()) {
			if (StringUtils.isNotBlank(region)) {
				final RegionInfo regionInfo = regionService.getOne(region);
				agentPackage = agentPackageService.createAgentPackage(region, regionInfo.getIp(), regionInfo.getControllerPort(), null);
			}
		} else {
			agentPackage = agentPackageService.createAgentPackage("", "", getConfig().getControllerPort(), null);
		}
		if (agentPackage != null) {
			downloadLink = "/agent/download/" + agentPackage.getName();
		}
		return downloadLink;
	}

	/**
	 * Get the agents.
	 */
	@RestAPI
	@GetMapping({"", "/", "/list"})
	@PreAuthorize("hasAnyRole('A', 'S', 'U')")
	public List<AgentInfo> getAll(final User user, @RequestParam(value = "region", required = false) final String region) {
		final Collection<? extends GrantedAuthority> authorities = getCurrentAuthorities();
		return agentManagerService.getAllVisible()
			.stream()
			.filter(agent -> filterAgentByCluster(region, agent.getRegion()))
			.filter(agent -> filterAgentByUserAuthorityAndUserId(authorities, user.getUserId(), region, agent.getRegion()))
			.collect(toList());
	}

	/**
	 * Filter agent list by referring to cluster
	 */
	private boolean filterAgentByCluster(String region, String agentRegion) {
		//noinspection SimplifiableIfStatement
		if (StringUtils.isEmpty(region)) {
			return true;
		} else {
			return agentRegion.startsWith(region + "_owned") || region.equals(agentRegion);
		}
	}

	/**
	 * Filter agent list using user authority and user id
	 */
	private boolean filterAgentByUserAuthorityAndUserId(Collection<? extends GrantedAuthority> authorities, String userId, String region, String agentRegion) {
		if (isAdminOrSuperUser(authorities)) {
			return true;
		}

		if (StringUtils.isEmpty(region)) {
			return !agentRegion.contains("_owned_") || agentRegion.endsWith("_owned_" + userId);
		} else {
			return agentRegion.startsWith(region + "_owned_" + userId) || region.equals(agentRegion);
		}
	}

	/**
	 * Check if the current user is admin or super user
	 */
	private boolean isAdminOrSuperUser(Collection<? extends GrantedAuthority> authorities) {
		return containsAuthority(authorities, "A") || containsAuthority(authorities, "S");
	}

	/**
	 * Clean up the agents in the inactive region
	 */
	@PreAuthorize("hasAnyRole('A')")
	@PostMapping(value = "", params = "action=cleanup")
	public Map<String, Object> cleanUpAgentsInInactiveRegion() {
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
	@PreAuthorize("hasAnyRole('A')")
	@GetMapping("/state")
	public SystemDataModel getState(@RequestParam String ip, @RequestParam String name, @RequestParam String region) {
		return agentManagerService.getSystemDataModel(ip, name, region);
	}

	/**
	 * Get all agents from database.
	 *
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@GetMapping(value = {"/", ""})
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
	@GetMapping(value = "/{id}")
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
	@PutMapping(value = "/{id}", params = "action=approve")
	public Map<String, Object> approve(@PathVariable("id") Long id) {
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
	@PutMapping(value = "/{id}", params = "action=disapprove")
	public Map<String, Object> disapprove(@PathVariable("id") Long id) {
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
	@PutMapping(value = "/{id}", params = "action=stop")
	public Map<String, Object> stop(@PathVariable("id") Long id) {
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
	@PutMapping(value = "", params = "action=stop")
	public Map<String, Object> stop(@RequestParam("ids") String ids) {
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
	@PutMapping(value = "/{id}", params = "action=update")
	public Map<String, Object> update(@PathVariable("id") Long id) {
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
	@PutMapping(value = "", params = "action=update")
	public Map<String, Object> update(@RequestParam("ids") String ids) {
		String[] split = StringUtils.split(ids, ",");
		for (String each : split) {
			update(Long.parseLong(each));
		}
		return returnSuccess();
	}

	/**
	 * Get the number of available agents.
	 *
	 * @param user         The login user
	 * @param targetRegion The name of target region
	 * @return availableAgentCount Available agent count
	 */
	@RestAPI
	@GetMapping("/availableAgentCount")
	@PreAuthorize("permitAll")
	public Map<String, Integer> getAvailableAgentCount(User user, @RequestParam String targetRegion) {
		return of("availableAgentCount", agentManagerService.getReadyAgentCount(user, targetRegion));
	}
}
