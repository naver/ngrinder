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
package org.ngrinder.perftest.controller;

import lombok.RequiredArgsConstructor;

import net.grinder.util.LogCompressUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.ngrinder.common.controller.annotation.GlobalControllerModel;
import org.ngrinder.common.util.FileDownloadUtils;
import org.ngrinder.infra.logger.CoreLogger;
import org.ngrinder.infra.spring.RemainedPath;
import org.ngrinder.model.*;
import org.ngrinder.perftest.service.PerfTestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;

import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.Preconditions.*;

/**
 * Performance Test Controller.
 */
@SuppressWarnings({"SpringMVCViewInspection", "unused"})
@Controller
@RequestMapping("/perftest")
@GlobalControllerModel
@RequiredArgsConstructor
public class PerfTestController {

	private final PerfTestService perfTestService;

	/**
	 * Get the perf test lists.
	 */
	@GetMapping({"/list", "/", ""})
	public String getAll(User user) {
		return "app";
	}

	/**
	 * Open the new perf test creation form.
	 */
	@GetMapping("/new")
	public String openForm(User user) {
		return "app";
	}

	/**
	 * Open the new perf test quickstart.
	 */
	@GetMapping("/quickstart")
	public String quickstart(User user) {
		return "app";
	}

	/**
	 * perf test detail on the given perf test id.
	 *
	 * @param id perf test id
	 */
	@GetMapping("/{id}")
	public String detail(User user, @PathVariable Long id) {
		return "app";
	}

	/**
	 * Download the CSV report for the given perf test id.
	 *
	 * @param user     user
	 * @param id       test id
	 * @param response response
	 */
	@GetMapping("/{id}/download_csv")
	public void downloadCSV(User user, @PathVariable long id, HttpServletResponse response) {
		PerfTest test = getOneWithPermissionCheck(user, id, false);
		File targetFile = perfTestService.getCsvReportFile(test);
		checkState(targetFile.exists(), "File %s doesn't exist!", targetFile.getName());
		FileDownloadUtils.downloadFile(response, targetFile);
	}

	/**
	 * Download logs for the perf test having the given id.
	 *
	 * @param user     user
	 * @param id       test id
	 * @param path     path in the log folder
	 * @param response response
	 */
	@GetMapping("/{id}/download_log/**")
	public void downloadLog(User user, @PathVariable("id") long id, @RemainedPath String path,
	                        HttpServletResponse response) {
		getOneWithPermissionCheck(user, id, false);
		File targetFile = perfTestService.getLogFile(id, path);
		FileDownloadUtils.downloadFile(response, targetFile);
	}

	/**
	 * Show the given log for the perf test having the given id.
	 *
	 * @param user     user
	 * @param id       test id
	 * @param path     path in the log folder
	 * @param response response
	 */
	@GetMapping("/{id}/show_log/**")
	public void showLog(User user, @PathVariable("id") long id, @RemainedPath String path, HttpServletResponse response) {
		getOneWithPermissionCheck(user, id, false);
		File targetFile = perfTestService.getLogFile(id, path);
		response.reset();
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");

		try (FileInputStream fileInputStream = new FileInputStream(targetFile)) {
			ServletOutputStream outputStream = response.getOutputStream();
			if (FilenameUtils.isExtension(targetFile.getName(), "zip")) {
				// Limit log view to 1MB
				outputStream.println(" Only the last 1MB of a log shows.\n");
				outputStream.println("==========================================================================\n\n");
				LogCompressUtils.decompress(fileInputStream, outputStream, 1024 * 1024);
			} else {
				IOUtils.copy(fileInputStream, outputStream);
			}
		} catch (Exception e) {
			CoreLogger.LOGGER.error("Error while processing log. {}", targetFile, e);
		}
	}

	/**
	 * Get the detailed perf test report.
	 *
	 * @return perftest/detail_report
	 */
	@GetMapping({"/{id}/detail_report", /* for backward compatibility */"/{id}/report"})
	public String getReport(@PathVariable long id) {
		return "app";
	}

	/**
	 * Get the detailed perf test report.
	 *
	 * @param id test id
	 * @return perftest/detail_report/perf
	 */
	@SuppressWarnings("UnusedParameters")
	@GetMapping("/{id}/detail_report/perf")
	public String getDetailPerfReport(@PathVariable("id") long id) {
		return "perftest/detail_report/perf";
	}

	/**
	 * Get the detailed perf test monitor report.
	 *
	 * @param id       test id
	 * @param targetIP target ip
	 * @param modelMap model map
	 * @return perftest/detail_report/monitor
	 */
	@SuppressWarnings("UnusedParameters")
	@GetMapping("/{id}/detail_report/monitor")
	public String getDetailMonitorReport(@PathVariable("id") long id, @RequestParam("targetIP") String targetIP,
	                                     ModelMap modelMap) {
		modelMap.addAttribute("targetIP", targetIP);
		return "perftest/detail_report/monitor";
	}

	/**
	 * Get the detailed perf test report.
	 *
	 * @param id       test id
	 * @param plugin   test report plugin category
	 * @param modelMap model map
	 * @return perftest/detail_report/plugin
	 */
	@SuppressWarnings("UnusedParameters")
	@GetMapping("/{id}/detail_report/plugin/{plugin}")
	public String getDetailPluginReport(@PathVariable("id") long id,
	                                    @PathVariable("plugin") String plugin, @RequestParam("kind") String kind, ModelMap modelMap) {
		modelMap.addAttribute("plugin", plugin);
		modelMap.addAttribute("kind", kind);
		return "perftest/detail_report/plugin";
	}


	@SuppressWarnings("SameParameterValue")
	private PerfTest getOneWithPermissionCheck(User user, Long id, boolean withTag) {
		PerfTest perfTest = withTag ? perfTestService.getOneWithTag(id) : perfTestService.getOne(id);
		if (user.getRole().equals(Role.ADMIN) || user.getRole().equals(Role.SUPER_USER)) {
			return perfTest;
		}
		if (perfTest != null && !user.equals(perfTest.getCreatedBy())) {
			throw processException("User " + user.getUserId() + " has no right on PerfTest " + id);
		}
		return perfTest;
	}
}
