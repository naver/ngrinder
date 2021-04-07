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

import org.ngrinder.agent.model.PackageDownloadInfo;
import org.ngrinder.agent.service.AgentPackageService;
import org.ngrinder.common.util.FileDownloadUtils;
import org.ngrinder.infra.config.Config;
import org.ngrinder.packages.MonitorPackageHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.File;

import lombok.RequiredArgsConstructor;

import static org.ngrinder.common.util.ExceptionUtils.processException;

/**
 * Monitor Download Controller.
 *
 * @since 3.0
 */
@Controller
@RequestMapping("/monitor/download")
@RequiredArgsConstructor
public class MonitorDownloadController {

	private final AgentPackageService agentPackageService;

	private final Config config;

	private final MonitorPackageHandler monitorPackageHandler;

	/**
	 * Download monitor.
	 *
	 * @param fileName monitor file name.
	 * @param response response.
	 */
	@GetMapping("/{fileName:[a-zA-Z0-9.\\-_]+}")
	public void download(@PathVariable String fileName, HttpServletResponse response) {
		File home = config.getHome().getDownloadDirectory();
		File monitorFile = new File(home, fileName);
		FileDownloadUtils.downloadFile(response, monitorFile);
	}

	/**
	 * Download monitor.
	 */
	@SuppressWarnings("SpringMVCViewInspection")
	@GetMapping("")
	public String download(ModelMap model) {
		try {
			PackageDownloadInfo packageDownloadInfo = PackageDownloadInfo.builder().connectionPort(config.getMonitorPort()).build();
			final File monitorPackage = agentPackageService.createPackage(monitorPackageHandler, packageDownloadInfo);
			model.clear();
			return "redirect:/monitor/download/" + monitorPackage.getName();
		} catch (Exception e) {
			throw processException(e);
		}
	}

}
