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

import org.ngrinder.agent.service.AgentPackageService;
import org.ngrinder.common.controller.NGrinderBaseController;
import org.ngrinder.common.util.FileDownloadUtil;
import org.ngrinder.infra.config.Config;
import org.ngrinder.region.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

import static org.ngrinder.common.util.Preconditions.checkNotEmpty;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

/**
 * Agent Download Controller.
 *
 * @author Tobi
 * @since 3.0
 */
@Controller
@RequestMapping("/agent")
public class AgentDownloadController extends NGrinderBaseController {

	@Autowired
	private Config config;

	@Autowired
	private AgentPackageService agentPackageService;

	@Autowired
	private RegionService regionService;

	/**
	 * Download agent.
	 *
	 * @param fileName file path of agent
	 * @param response response.
	 */
	@RequestMapping(value = "/download/{fileName:[a-zA-Z0-9\\.\\-]+}")
	public void downloadAgent(@PathVariable String fileName, HttpServletResponse response) {
		File ngrinderFile = new File(config.getHome().getDownloadDirectory(), fileName);
		FileDownloadUtil.downloadFile(response, ngrinderFile);
	}

	/**
	 * Download the latest agent.
	 *
	 * @param response response.
	 */
	@RequestMapping(value = "/download")
	public void downloadLatestAgent(@RequestParam(value = "owner", required = false) String owner,
	                                @RequestParam(value = "region", required = false) String region, HttpServletRequest request,
	                                HttpServletResponse response) {
		String connectingIP = request.getServerName();
		if (config.isCluster()) {
			checkNotEmpty(region, "region should be provided to download agent.");
			connectingIP = checkNotNull(regionService.getRegion(region), "selecting region '" + region + "' is not " +
					"available").getIp();
		}
		FileDownloadUtil.downloadFile(response, agentPackageService.createAgentPackage(connectingIP, region, owner));
	}

}
