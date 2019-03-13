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

import net.grinder.util.LogCompressUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.ngrinder.common.util.FileDownloadUtils;
import org.ngrinder.infra.logger.CoreLogger;
import org.ngrinder.infra.spring.RemainedPath;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Status;
import org.ngrinder.model.User;
import org.ngrinder.perftest.service.TagService;
import org.ngrinder.region.service.RegionService;
import org.ngrinder.script.handler.ScriptHandlerFactory;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.service.FileEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Map;

import static org.ngrinder.common.util.Preconditions.checkState;
import static org.ngrinder.common.util.Preconditions.checkValidURL;

/**
 * Performance Test Controller.
 *
 * @author Mavlarn
 * @author JunHo Yoon
 */
@Profile("production")
@Controller
@RequestMapping("/perftest")
public class PerfTestController extends PerfTestBaseController {

	@Autowired
	private FileEntryService fileEntryService;

	@Autowired
	private TagService tagService;

	@Autowired
	private ScriptHandlerFactory scriptHandlerFactory;

	@Autowired
	private RegionService regionService;

	/**
	 * Get the perf test lists.
	 *
	 * @param user        user
	 * @param query       query string to search the perf test
	 * @param tag         tag
	 * @param queryFilter "F" means get only finished, "S" means get only scheduled tests.
	 * @param pageable    page
	 * @param model       modelMap
	 * @return perftest/list
	 */
	@RequestMapping({"/list", "/", ""})
	public String getAll(User user, @RequestParam(required = false) String query,
	                     @RequestParam(required = false) String tag, @RequestParam(required = false) String queryFilter,
	                     @PageableDefault(page = 0, size = 10) Pageable pageable, ModelMap model) {
		pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
			pageable.getSort().isUnsorted() ? new Sort(Direction.DESC, "id") : pageable.getSort());
		Page<PerfTest> tests = perfTestService.getPagedAll(user, query, tag, queryFilter, pageable);
		if (tests.getNumberOfElements() == 0) {
			pageable = PageRequest.of(0, pageable.getPageSize(),
				pageable.getSort().isUnsorted() ? new Sort(Direction.DESC, "id") : pageable.getSort());
			tests = perfTestService.getPagedAll(user, query, tag, queryFilter, pageable);
		}
		annotateDateMarker(tests);
		model.addAttribute("tag", tag);
		model.addAttribute("availTags", tagService.getAllTagStrings(user, StringUtils.EMPTY));
		model.addAttribute("testListPage", tests);
		model.addAttribute("queryFilter", queryFilter);
		model.addAttribute("query", query);
		putPageIntoModelMap(model, pageable);
		return "perftest/list";
	}

	/**
	 * Open the new perf test creation form.
	 *
	 * @param user  user
	 * @param model model
	 * @return "perftest/detail"
	 */
	@RequestMapping("/new")
	public String openForm(User user, ModelMap model) {
		return getOne(user, null, model);
	}

	/**
	 * Get the perf test detail on the given perf test id.
	 *
	 * @param user  user
	 * @param id    perf test id
	 * @param model model
	 * @return perftest/detail
	 */
	@RequestMapping("/{id}")
	public String getOne(User user, @PathVariable("id") Long id, ModelMap model) {
		PerfTest test = null;
		if (id != null) {
			test = getOneWithPermissionCheck(user, id, true);
		}

		if (test == null) {
			test = new PerfTest(user);
			test.init();
		}

		model.addAttribute(PARAM_TEST, test);
		// Retrieve the agent count map based on create user, if the test is
		// created by the others.
		user = test.getCreatedUser() != null ? test.getCreatedUser() : user;

		Map<String, MutableInt> agentCountMap = agentManagerService.getAvailableAgentCountMap(user);
		model.addAttribute(PARAM_REGION_AGENT_COUNT_MAP, agentCountMap);
		model.addAttribute(PARAM_REGION_LIST, regionService.getAllVisibleRegionNames());
		model.addAttribute(PARAM_PROCESS_THREAD_POLICY_SCRIPT, perfTestService.getProcessAndThreadPolicyScript());
		addDefaultAttributeOnModel(model);
		return "perftest/detail";
	}

	/**
	 * Get the perf test creation form for quickStart.
	 *
	 * @param user       user
	 * @param urlString  URL string to be tested.
	 * @param scriptType scriptType
	 * @param model      model
	 * @return perftest/detail
	 */
	@RequestMapping("/quickstart")
	public String getQuickStart(User user,
	                            @RequestParam(value = "url", required = true) String urlString,
	                            @RequestParam(value = "scriptType", required = true) String scriptType,
	                            ModelMap model) {
		URL url = checkValidURL(urlString);
		FileEntry newEntry = fileEntryService.prepareNewEntryForQuickTest(user, urlString,
				scriptHandlerFactory.getHandler(scriptType));
		model.addAttribute(PARAM_QUICK_SCRIPT, newEntry.getPath());
		model.addAttribute(PARAM_QUICK_SCRIPT_REVISION, newEntry.getRevision());
		model.addAttribute(PARAM_TEST, createPerfTestFromQuickStart(user, "Test for " + url.getHost(), url.getHost()));
		Map<String, MutableInt> agentCountMap = agentManagerService.getAvailableAgentCountMap(user);
		model.addAttribute(PARAM_REGION_AGENT_COUNT_MAP, agentCountMap);
		model.addAttribute(PARAM_REGION_LIST, getRegions(agentCountMap));
		addDefaultAttributeOnModel(model);
		model.addAttribute(PARAM_PROCESS_THREAD_POLICY_SCRIPT, perfTestService.getProcessAndThreadPolicyScript());
		return "perftest/detail";
	}

	/**
	 * Create a new test or cloneTo a current test.
	 *
	 * @param user     user
	 * @param perfTest {@link PerfTest}
	 * @param isClone  true if cloneTo
	 * @param model    model
	 * @return redirect:/perftest/list
	 */
	@RequestMapping(value = "/new", method = RequestMethod.POST)
	public String saveOne(User user, PerfTest perfTest,
	                      @RequestParam(value = "isClone", required = false, defaultValue = "false") boolean isClone, ModelMap model) {

		validate(user, null, perfTest);
		// Point to the head revision
		perfTest.setTestName(StringUtils.trimToEmpty(perfTest.getTestName()));
		perfTest.setScriptRevision(-1L);
		perfTest.prepare(isClone);
		perfTest = perfTestService.save(user, perfTest);
		model.clear();
		if (perfTest.getStatus() == Status.SAVED || perfTest.getScheduledTime() != null) {
			return "redirect:/perftest/list";
		} else {
			return "redirect:/perftest/" + perfTest.getId();
		}
	}

	/**
	 * Get the running division in perftest configuration page.
	 *
	 * @param user  user
	 * @param model model
	 * @param id    test id
	 * @return perftest/running
	 */
	@RequestMapping(value = "{id}/running_div")
	public String getReportSection(User user, ModelMap model, @PathVariable long id) {
		PerfTest test = getOneWithPermissionCheck(user, id, false);
		model.addAttribute(PARAM_TEST, test);
		return "perftest/running";
	}


	/**
	 * Get the basic report content in perftest configuration page.
	 * <p/>
	 * This method returns the appropriate points based on the given imgWidth.
	 *
	 * @param user     user
	 * @param model    model
	 * @param id       test id
	 * @param imgWidth image width
	 * @return perftest/basic_report
	 */
	@RequestMapping(value = "{id}/basic_report")
	public String getReportSection(User user, ModelMap model, @PathVariable long id, @RequestParam int imgWidth) {
		PerfTest test = getOneWithPermissionCheck(user, id, false);
		int interval = perfTestService.getReportDataInterval(id, "TPS", imgWidth);
		model.addAttribute(PARAM_LOG_LIST, perfTestService.getLogFiles(id));
		model.addAttribute(PARAM_TEST_CHART_INTERVAL, interval * test.getSamplingInterval());
		model.addAttribute(PARAM_TEST, test);
		model.addAttribute(PARAM_TPS, perfTestService.getSingleReportDataAsJson(id, "TPS", interval));
		return "perftest/basic_report";
	}

	/**
	 * Download the CSV report for the given perf test id.
	 *
	 * @param user     user
	 * @param id       test id
	 * @param response response
	 */
	@RequestMapping(value = "/{id}/download_csv")
	public void downloadCSV(User user, @PathVariable("id") long id, HttpServletResponse response) {
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
	@RequestMapping(value = "/{id}/download_log/**")
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
	@RequestMapping(value = "/{id}/show_log/**")
	public void showLog(User user, @PathVariable("id") long id, @RemainedPath String path, HttpServletResponse response) {
		getOneWithPermissionCheck(user, id, false);
		File targetFile = perfTestService.getLogFile(id, path);
		response.reset();
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(targetFile);
			ServletOutputStream outputStream = response.getOutputStream();
			if (FilenameUtils.isExtension(targetFile.getName(), "zip")) {
				// Limit log view to 1MB
				outputStream.println(" Only the last 1MB of a log shows.\n");
				outputStream.println("==========================================================================\n\n");
				LogCompressUtils.decompress(fileInputStream, outputStream, 1 * 1024 * 1024);
			} else {
				IOUtils.copy(fileInputStream, outputStream);
			}
		} catch (Exception e) {
			CoreLogger.LOGGER.error("Error while processing log. {}", targetFile, e);
		} finally {
			IOUtils.closeQuietly(fileInputStream);
		}
	}

	/**
	 * Get the detailed perf test report.
	 *
	 * @param model model
	 * @param id    test id
	 * @return perftest/detail_report
	 */
	@SuppressWarnings("MVCPathVariableInspection")
	@RequestMapping(value = {"/{id}/detail_report", /** for backward compatibility */"/{id}/report"})
	public String getReport(ModelMap model, @PathVariable("id") long id) {
		model.addAttribute("test", perfTestService.getOne(id));
		model.addAttribute("plugins", perfTestService.getAvailableReportPlugins(id));
		return "perftest/detail_report";
	}

	/**
	 * Get the detailed perf test report.
	 *
	 * @param id test id
	 * @return perftest/detail_report/perf
	 */
	@SuppressWarnings({"MVCPathVariableInspection", "UnusedParameters"})
	@RequestMapping("/{id}/detail_report/perf")
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
	@RequestMapping("/{id}/detail_report/monitor")
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
	@RequestMapping("/{id}/detail_report/plugin/{plugin}")
	public String getDetailPluginReport(@PathVariable("id") long id,
	                                    @PathVariable("plugin") String plugin, @RequestParam("kind") String kind, ModelMap modelMap) {
		modelMap.addAttribute("plugin", plugin);
		modelMap.addAttribute("kind", kind);
		return "perftest/detail_report/plugin";
	}
}
