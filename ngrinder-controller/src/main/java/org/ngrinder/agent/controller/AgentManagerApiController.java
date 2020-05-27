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

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.agent.service.AgentService;
import org.ngrinder.agent.service.AgentPackageService;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.model.User;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.region.model.RegionInfo;
import org.ngrinder.region.service.RegionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.ngrinder.common.util.CollectionUtils.*;
import static org.ngrinder.common.util.SpringSecurityUtils.containsAuthority;
import static org.ngrinder.common.util.SpringSecurityUtils.getCurrentAuthorities;

/**
 * @since 3.5.0
 */
@RestController
@RequestMapping("/agent/api")
@PreAuthorize("hasAnyRole('A', 'S')")
@RequiredArgsConstructor
public class AgentManagerApiController {

	private final AgentService agentService;

	private final RegionService regionService;

	private final AgentPackageService agentPackageService;

	private final Config config;

	@GetMapping("/regions")
	@PreAuthorize("hasAnyRole('A', 'S', 'U')")
	public List<String> getAvailableRegions(final User user) {
		return regionService.getAllVisibleRegionNames();
	}

	@GetMapping("/download_link")
	@PreAuthorize("hasAnyRole('A', 'S', 'U')")
	public String getDownloadLink(final User user,  @RequestParam(value = "region", required = false) final String region) {
		String downloadLink = "";
		File agentPackage = null;
		if (config.isClustered()) {
			if (StringUtils.isNotBlank(region)) {
				final RegionInfo regionInfo = regionService.getOne(region);
				agentPackage = agentPackageService.createAgentPackage(region, regionInfo.getIp(), regionInfo.getControllerPort(), null);
			}
		} else {
			agentPackage = agentPackageService.createAgentPackage("", "", config.getControllerPort(), null);
		}
		if (agentPackage != null) {
			downloadLink = "/agent/download/" + agentPackage.getName();
		}
		return downloadLink;
	}

	/**
	 * Get the agents.
	 */
	@GetMapping({"", "/", "/list"})
	@PreAuthorize("hasAnyRole('A', 'S', 'U')")
	public List<AgentInfo> getAll(final User user, @RequestParam(value = "region", required = false) final String region) {
		final Collection<? extends GrantedAuthority> authorities = getCurrentAuthorities();
		return agentService.getAllActive()
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
	 * Get the current performance of the given agent.
	 *
	 * @param ip agent ip
	 * @param name agent name
	 * @param region agent region
	 *
	 * @return json message
	 */
	@GetMapping("/state")
	@PreAuthorize("hasAnyRole('A', 'S', 'U')")
	public SystemDataModel getState(@RequestParam String ip, @RequestParam String name, @RequestParam String region) {
		return agentService.getSystemDataModel(ip, name, region);
	}

	/**
	 * Get the current all agents state.
	 *
	 * @return json message
	 */
	@PreAuthorize("hasAnyRole('A', 'S', 'U')")
	@GetMapping(value = {"/states/", "/states"})
	public List<Map<String, Object>> getStates() {
		List<AgentInfo> agents = agentService.getAllActive();
		List<Map<String, Object>> statuses = newArrayList(agents.size());

		for (AgentInfo each : agents) {
			Map<String, Object> result = newHashMap();
			result.put("id", each.getId());
			result.put("port", each.getPort());
			result.put("icon", each.getState().getCategory().getIconName());
			result.put("state", each.getState());
			statuses.add(result);
		}

		return statuses;
	}

	/**
	 * Get all agents from database.
	 *
	 * @return agentInfoList
	 */
	@PreAuthorize("hasAnyRole('A')")
	@GetMapping(value = {"/", ""})
	public List<AgentInfo> getAll() {
		return agentService.getAllActive();
	}

	/**
	 * Get the agent for the given agent ip and name.
	 *
	 * @return agentInfo
	 */
	@PreAuthorize("hasAnyRole('A')")
	@GetMapping(value = "/{ip}/{name}")
	public AgentInfo getOneByIpAndName(@PathVariable String ip, @PathVariable String name) {
		return agentService.getAgent(ip, name);
	}

	/**
	 * Approve an agent.
	 *
	 * @param ip   agent ip
	 * @param name agent name
	 */
	@PreAuthorize("hasAnyRole('A')")
	@PutMapping(value = "/{ip}/{name}", params = "action=approve")
	public void approve(@PathVariable String ip, @PathVariable String name) {
		agentService.approve(ip, name, true);
	}

	/**
	 * Disapprove an agent.
	 *
	 * @param ip   agent ip
	 * @param name agent name
	 */
	@PreAuthorize("hasAnyRole('A')")
	@PutMapping(value = "/{ip}/{name}", params = "action=disapprove")
	public void disapprove(@PathVariable String ip, @PathVariable String name) {
		agentService.approve(ip, name, false);
	}

	/**
	 * Stop the given agent.
	 *
	 * @param ip   agent ip
	 * @param name agent name
	 */
	@PreAuthorize("hasAnyRole('A')")
	@PutMapping(value = "/{ip}/{name}", params = "action=stop")
	public void stop(@PathVariable String ip, @PathVariable String name) {
		agentService.stop(ip, name);
	}

	/**
	 * Stop the given agent.
	 *
	 * @param agentInfos agent information list
	 */
	@PreAuthorize("hasAnyRole('A')")
	@PutMapping(value = "", params = "action=stop")
	public void stop(@RequestBody List<AgentInfo> agentInfos) {
		for (AgentInfo agentInfo : agentInfos) {
			agentService.stop(agentInfo.getIp(), agentInfo.getName());
		}
	}

	/**
	 * Update the given agent.
	 *
	 * @param ip   agent ip
	 * @param name agent name
	 */
	@PreAuthorize("hasAnyRole('A')")
	@PutMapping(value = "/{ip}/{name}", params = "action=update")
	public void update(@PathVariable String ip, @PathVariable String name) {
		agentService.update(ip, name);
	}

	/**
	 * Send update message to agent side
	 *
	 * @param agentInfos agent information list
	 */
	@PreAuthorize("hasAnyRole('A')")
	@PutMapping(value = "", params = "action=update")
	public void update(@RequestBody List<AgentInfo> agentInfos) {
		for (AgentInfo agentInfo : agentInfos) {
			agentService.update(agentInfo.getIp(), agentInfo.getName());
		}
	}

	/**
	 * Get the number of available agents.
	 *
	 * @param user         The login user
	 * @param targetRegion The name of target region
	 * @return availableAgentCount Available agent count
	 */
	@GetMapping("/availableAgentCount")
	@PreAuthorize("permitAll")
	public Map<String, Integer> getAvailableAgentCount(User user, @RequestParam String targetRegion) {
		return buildMap("availableAgentCount", agentService.getReadyAgentCount(user.getUserId(), targetRegion));
	}

	/**
	 * Add an external agent.
	 * @param ip	agent ip
	 * @param port	agent port
	 */
	@PostMapping("/connect/{ip}/{port}")
	@PreAuthorize("permitAll")
	public void addConnectionAgent(@PathVariable String ip, @PathVariable int port, @RequestParam String region) {
		agentService.addConnectionAgent(ip, port, region);
	}
}
