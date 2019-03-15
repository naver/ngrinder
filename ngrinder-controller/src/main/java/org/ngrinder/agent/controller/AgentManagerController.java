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

import com.google.common.base.Predicate;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.agent.service.AgentPackageService;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.model.User;
import org.ngrinder.region.model.RegionInfo;
import org.ngrinder.region.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.util.Collection;

import static com.google.common.collect.Collections2.filter;
import static org.ngrinder.common.util.SpringSecurityUtils.getCurrentAuthorities;

/**
 * Agent management controller.
 *
 * @author JunHo Yoon
 * @since 3.1
 */
@Controller
@RequestMapping("/agent")
@PreAuthorize("hasAnyRole('A', 'S')")
public class AgentManagerController extends AgentManagerBaseController {

	@Autowired
	private RegionService regionService;

	@Autowired
	private AgentPackageService agentPackageService;

	/**
	 * Get the agents.
	 */
	@RequestMapping({"", "/", "/list"})
	@PreAuthorize("hasAnyRole('A', 'S', 'U')")
	public String getAll(final User user, @RequestParam(value = "region", required = false) final String region, ModelMap model) {
		final Collection<? extends GrantedAuthority> authorities = getCurrentAuthorities();
		Collection<AgentInfo> agents = agentManagerService.getAllVisible();

		agents = filter(agents, new Predicate<AgentInfo>() {
			@Override
			public boolean apply(AgentInfo agentInfo) {
				return filterAgentByCluster(region, agentInfo.getRegion());
			}
		});

		agents = filter(agents, new Predicate<AgentInfo>() {
			@Override
			public boolean apply(AgentInfo agentInfo) {
				return filterAgentByUserAuthorityAndId(authorities, user.getUserId(), region, agentInfo.getRegion());
			}
		});

		model.addAttribute("agents", agents);
		model.addAttribute("region", region);
		model.addAttribute("regions", regionService.getAllVisibleRegionNames());
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
			model.addAttribute("downloadLink", "/agent/download/" + agentPackage.getName());
		}
		return "agent/list";
	}

	/**
	 * Get the agent detail info for the given agent id.
	 *
	 * @param id    agent id
	 * @param model model
	 * @return agent/agentDetail
	 */
	@RequestMapping("/{id}")
	public String getOne(@PathVariable Long id, ModelMap model) {
		model.addAttribute("agent", agentManagerService.getOne(id));
		return "agent/detail";
	}
}
