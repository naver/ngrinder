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
import org.ngrinder.infra.config.Config;
import org.ngrinder.packages.MonitorPackageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URLClassLoader;

import static org.ngrinder.common.util.ExceptionUtils.processException;

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
	private Config config;

	@Autowired
	@Qualifier("monitorPackageHandler")
	private MonitorPackageHandler monitorPackageHandler;

	/**
	 * Download monitor.
	 *
	 * @param fileName monitor file name.
	 * @param response response.
	 */

	@RequestMapping(value = "/download/{fileName:[a-zA-Z0-9\\.\\-_]+}")
	public void download(@PathVariable String fileName, HttpServletResponse response) {
		File home = getConfig().getHome().getDownloadDirectory();
		File monitorFile = new File(home, fileName);
		FileDownloadUtils.downloadFile(response, monitorFile);
	}

	/**
	 * Download monitor.
	 */
	@RequestMapping(value = "/download")
	public String download(ModelMap model) {
		try {
			final File monitorPackage = agentPackageService.createPackage(monitorPackageHandler, (URLClassLoader) getClass().getClassLoader(),
				"", null, config.getMonitorPort(), "");
			model.clear();
			return "redirect:/monitor/download/" + monitorPackage.getName();
		} catch (Exception e) {
			throw processException(e);
		}
	}

}
