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
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.common.util.FileDownloadUtils;
import org.ngrinder.region.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;

import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.Preconditions.checkNotEmpty;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

/**
 * Monitor Download Controller.
 *
 * @author Tobi
 * @since 3.0
 */
@Controller
@RequestMapping("/monitor")
public class MonitorDownloadController extends BaseController {

	@Autowired
	private AgentPackageService agentPackageService;

	@Autowired
	private RegionService regionService;

	/**
	 * Download monitor.
	 *
	 * @param response response.
	 */
	@RequestMapping(value = "/download/last")
	public void download(HttpServletResponse response) {
		try {
			FileDownloadUtils.downloadFile(response, agentPackageService.createMonitorPackage());
		} catch (Exception e) {
			throw processException(e);
		}
	}

}
