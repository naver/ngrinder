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

import org.ngrinder.agent.service.AgentPackageService;
import org.ngrinder.common.util.FileDownloadUtils;
import org.ngrinder.infra.config.Config;
import org.ngrinder.region.model.RegionInfo;
import org.ngrinder.region.service.RegionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.Preconditions.checkNotEmpty;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

/**
 * Agent Download Controller.
 *
 * @since 3.0
 */
@Controller
@RequestMapping("/agent/download")
@RequiredArgsConstructor
public class AgentDownloadController {

	private final AgentPackageService agentPackageService;

	private final RegionService regionService;

	private final Config config;

	/**
	 * Download agent.
	 *
	 * @param fileName file path of agent
	 * @param response response.
	 */
	@GetMapping("/{fileName:[a-zA-Z0-9.\\-_]+}")
	public void download(@PathVariable String fileName, HttpServletResponse response) {
		File home = config.getHome().getDownloadDirectory();
		File ngrinderFile = new File(home, fileName);
		FileDownloadUtils.downloadFile(response, ngrinderFile);
	}


	/**
	 * Download the latest agent.
	 *
	 * @param owner   agent owner
	 * @param region  agent region
	 * @param request request.
	 */
	@GetMapping("/{region}/{owner}")
	public String downloadDirect(@PathVariable(value = "owner") String owner,
	                             @PathVariable(value = "region") String region,
	                             ModelMap modelMap,
	                             HttpServletRequest request) {
		return downloadFile(owner, region, modelMap, request);
	}


	/**
	 * Download the latest agent.
	 *
	 * @param owner   agent owner
	 * @param region  agent region
	 * @param request request.
	 */
	@GetMapping("")
	public String download(@RequestParam(value = "owner", required = false) String owner,
	                       @RequestParam(value = "region", required = false) String region,
	                       ModelMap modelMap,
	                       HttpServletRequest request) {
		return downloadFile(owner, region, modelMap, request);
	}

	private String downloadFile(String owner, String region, ModelMap modelMap, HttpServletRequest request) {
		String connectingIP = request.getServerName();
		int port = config.getControllerPort();
		try {
			if (config.isClustered()) {
				checkNotEmpty(region, "region should be provided to download agent in cluster mode.");
				RegionInfo regionInfo = checkNotNull(regionService.getOne(region), "selecting region '" + region + "'" +
						" is not valid");
				port = regionInfo.getControllerPort();
				connectingIP = regionInfo.getIp();
			}
			final File agentPackage = agentPackageService.createAgentPackage(region, connectingIP, port, owner);
			modelMap.clear();
			return "redirect:/agent/download/" + agentPackage.getName();
		} catch (Exception e) {
			throw processException(e);
		}
	}
}
