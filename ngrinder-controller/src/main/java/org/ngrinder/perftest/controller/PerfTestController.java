/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ngrinder.perftest.controller;

import static org.ngrinder.common.util.Preconditions.checkArgument;
import static org.ngrinder.common.util.Preconditions.checkNotEmpty;
import static org.ngrinder.common.util.Preconditions.checkNotNull;
import static org.ngrinder.common.util.Preconditions.checkValidURL;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.ngrinder.common.controller.NGrinderBaseController;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.util.FileDownloadUtil;
import org.ngrinder.common.util.JSONUtil;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.perftest.model.PerfTest;
import org.ngrinder.perftest.model.ProcessAndThread;
import org.ngrinder.perftest.model.Status;
import org.ngrinder.perftest.service.AgentManager;
import org.ngrinder.perftest.service.PerfTestService;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileType;
import org.ngrinder.script.service.FileEntryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.web.PageableDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Performance Test Controller.
 * 
 * @author Mavlarn
 * @author JunHo Yoon
 */
@Controller
@RequestMapping("/perftest")
public class PerfTestController extends NGrinderBaseController {

	private static final Logger LOG = LoggerFactory.getLogger(PerfTestController.class);

	@Autowired
	private PerfTestService perfTestService;

	@Autowired
	private FileEntryService fileEntryService;

	@Autowired
	private AgentManager agentManager;

	/**
	 * Get Performance test lists.
	 * 
	 * @param user
	 *            user
	 * @param model
	 *            modelMap
	 * @param isFinished
	 *            only list finished project
	 * @param pageable
	 *            page
	 * @return perftest/list
	 */
	@RequestMapping({"/list", "/"})
	public String getPerfTestList(User user, @RequestParam(required = false) String query,
					@RequestParam(required = false) boolean onlyFinished,
					@PageableDefaults(pageNumber = 0, value = 10) Pageable pageable, ModelMap model) {
		PageRequest pageReq = ((PageRequest) pageable);
		Sort sort = pageReq == null ? null : pageReq.getSort();
		if (sort == null && pageReq != null) {
			sort = new Sort(Direction.DESC, "lastModifiedDate");
			pageable = new PageRequest(pageReq.getPageNumber(), pageReq.getPageSize(), sort);
		}
		Page<PerfTest> testList = perfTestService.getPerfTestList(user, query, onlyFinished, pageable);
		model.addAttribute("testListPage", testList);
		model.addAttribute("onlyFinished", onlyFinished);
		model.addAttribute("query", query);
		model.addAttribute("page", pageable);
		if (sort != null) {
			Order sortProp = (Order) sort.iterator().next();
			model.addAttribute("sortColumn", sortProp.getProperty());
			model.addAttribute("sortDirection", sortProp.getDirection());
		}
		return "perftest/list";
	}

	/**
	 * Get performance test detail on give perf test id
	 * 
	 * @param user
	 *            user
	 * @param model
	 *            model
	 * @param id
	 *            performance test id
	 * @return "perftest/detail"
	 */
	@RequestMapping("/detail")
	public String getPerfTestDetail(User user, @RequestParam(required = false) Long id, ModelMap model) {
		PerfTest test = null;
		if (id != null) {
			test = checkTestPermissionAndGet(user, id);
		}

		model.addAttribute(PARAM_TEST, test);
		model.addAttribute(PARAM_SCRIPT_LIST,
						fileEntryService.getAllFileEntries(user, FileType.PYTHON_SCRIPT));
		addDefaultAttributeOnMode(model);
		return "perftest/detail";
	}

	public void addDefaultAttributeOnMode(ModelMap model) {
		model.addAttribute(PARAM_CURRENT_FREE_AGENTS_COUNT, agentManager.getAllFreeAgents().size());
		model.addAttribute(PARAM_MAX_AGENT_SIZE_PER_CONSOLE, agentManager.getMaxAgentSizePerConsole());
		model.addAttribute(PARAM_MAX_VUSER_PER_AGENT, agentManager.getMaxVuserPerAgent());
		model.addAttribute(PARAM_MAX_RUN_COUNT, agentManager.getMaxRunCount());
		model.addAttribute(PARAM_MAX_RUN_HOUR, agentManager.getMaxRunHour() - 1);
	}

	/**
	 * get details view for quickStart
	 * 
	 * @param user
	 *            user
	 * @param urlString
	 *            url string to be tested.
	 * @param model
	 *            model
	 * @return "perftest/detail"
	 */
	@RequestMapping("/quickStart")
	public String getQuickStart(User user, @RequestParam(value = "url", required = true) String urlString,
					ModelMap model) {
		checkValidURL(urlString);
		List<FileEntry> scriptList = new ArrayList<FileEntry>();
		scriptList.add(fileEntryService.prepareNewEntryForQuickTest(user, urlString));
		model.addAttribute(PARAM_SCRIPT_LIST, scriptList);
		addDefaultAttributeOnMode(model);
		return "perftest/detail";
	}

	/**
	 * Create a new test or clone a current test.
	 * 
	 * @param user
	 *            user
	 * @param model
	 *            model
	 * @param test
	 *            {@link PerfTest}
	 * @return redirect:/perftest/list
	 */
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public String savePerfTest(User user, ModelMap model, PerfTest test) {
		test.setTestName(StringUtils.trimToEmpty(test.getTestName()));
		checkNotEmpty(test.getTestName(), "test name should be provided");
		checkArgument(test.getStatus().equals(Status.READY) || test.getStatus().equals(Status.SAVED),
						"save test only support for SAVE or READY status");
		checkArgument(test.getDuration() == null
						|| test.getDuration() <= (1000 * 60 * 60 * agentManager.getMaxRunHour()),
						"test duration should be within " + agentManager.getMaxRunHour());
		checkArgument(test.getRunCount() == null || test.getRunCount() <= agentManager.getMaxRunCount(),
						"test run count should be within " + agentManager.getMaxRunCount());
		checkArgument(test.getAgentCount() == null
						|| test.getAgentCount() <= agentManager.getMaxAgentSizePerConsole(),
						"test agent shoule be within " + agentManager.getMaxAgentSizePerConsole());
		checkArgument(test.getVuserPerAgent() == null
						|| test.getVuserPerAgent() <= agentManager.getMaxVuserPerAgent(),
						"test vuser shoule be within " + agentManager.getMaxVuserPerAgent());

		perfTestService.savePerfTest(test);
		return "redirect:/perftest/list";
	}

	/**
	 * Calculate vuser assignment policy based on request vuser number.
	 * 
	 * @param model
	 * @param newVuser
	 *            how many vusers will be used.
	 * @return JSON
	 */
	@RequestMapping(value = "/updateVuser")
	public @ResponseBody
	String updateVuser(@RequestParam int newVuser, ModelMap model) {
		ProcessAndThread processAndThread = perfTestService.calcProcessAndThread(newVuser);
		Map<String, Object> rtnMap = new HashMap<String, Object>(3);
		rtnMap.put(JSON_SUCCESS, true);
		rtnMap.put(PARAM_THREAD_COUNT, processAndThread.getThreadCount());
		rtnMap.put(PARAM_PROCESS_COUNT, processAndThread.getProcessCount());
		return JSONUtil.toJson(rtnMap);
	}

	@RequestMapping(value = "/updateStatus")
	public @ResponseBody
	String updateSatus(User user, @RequestParam(defaultValue = "") String ids, ModelMap map) {
		String[] numbers = StringUtils.split(ids, ",");
		Integer[] id = new Integer[numbers.length];
		int i = 0;
		for (String each : numbers) {
			id[i++] = NumberUtils.toInt(each, 0);
		}
		List<PerfTest> perfTests = perfTestService.getPerfTest(user, id);
		List<Map<String, Object>> statusList = new ArrayList<Map<String, Object>>();
		for (PerfTest each : perfTests) {
			Map<String, Object> rtnMap = new HashMap<String, Object>(3);
			rtnMap.put(PARAM_STATUS_UPDATE_ID, each.getId());
			rtnMap.put(PARAM_STATUS_UPDATE_STATUS_NAME, each.getStatus().name());
			rtnMap.put(PARAM_STATUS_UPDATE_STATUS_ICON, each.getStatus().getIconName());
			// FIXME each.getLastModifiedDateToStr() use the server side time, need to consider
			// locale later.

			rtnMap.put(PARAM_STATUS_UPDATE_STATUS_MESSAGE,
							StringUtils.replace(
											each.getProgressMessage() + "\n<b>"
															+ each.getLastProgressMessage() + "</b>\n"
															+ each.getLastModifiedDateToStr(), "\n", "<br/>"));
			rtnMap.put(PARAM_STATUS_UPDATE_DELETABLE, each.getStatus().isDeletable());
			rtnMap.put(PARAM_STATUS_UPDATE_STOPPABLE, each.getStatus().isStoppable());
			statusList.add(rtnMap);
		}
		return JSONUtil.toJson(statusList);
	}

	@RequestMapping(value = "/deleteTests", method = RequestMethod.POST)
	public @ResponseBody
	String deletePerfTests(User user, ModelMap model, @RequestParam String ids) {
		String[] idList = StringUtils.split(ids, ",");
		for (String idStr : idList) {
			try {
				long delId = Long.valueOf(idStr);
				checkTestPermissionAndGet(user, delId);
				perfTestService.deletePerfTest(delId);
			} catch (NumberFormatException e) {
				LOG.error("Can't delete a test (id=" + idStr + ") : {}", e);
			}
		}
		return JSONUtil.returnSuccess();
	}

	@RequestMapping(value = "/stopTests", method = RequestMethod.POST)
	public @ResponseBody
	String stopPerfTests(User user, ModelMap model, @RequestParam("ids") String ids) {
		String[] idList = StringUtils.split(ids, ",");
		for (String idStr : idList) {
			try {
				perfTestService.stopPerfTest(user, Long.valueOf(idStr));
			} catch (NumberFormatException e) {
				LOG.error("Can't stop a test (id={})", idStr);
				LOG.error("Exception occured in stopPerfTest", e);
			}
		}
		return JSONUtil.returnSuccess();
	}

	@RequestMapping(value = "/getResourcesOnScriptFolder")
	public @ResponseBody
	String getResourcesOnScriptFolder(User user, @RequestParam String scriptPath) {
		List<String> fileStringList = new ArrayList<String>();
		if (StringUtils.isEmpty(scriptPath)) {
			return JSONUtil.toJson(fileStringList);
		}
		List<FileEntry> fileList = fileEntryService.getLibAndResourcesEntries(user, scriptPath);
		for (FileEntry each : fileList) {
			fileStringList.add(each.getPath());
		}
		return JSONUtil.toJson(fileStringList);
	}

	@RequestMapping(value = "/getReportData")
	public @ResponseBody
	String getPerfTestReportData(User user, ModelMap model, @RequestParam long testId,
					@RequestParam(required = true) String dataType, @RequestParam int imgWidth) {
		checkTestPermissionAndGet(user, testId);
		List<Object> reportData = null;
		String[] dataTypes = StringUtils.split(dataType, ",");
		Map<String, Object> rtnMap = new HashMap<String, Object>(1 + dataTypes.length);
		rtnMap.put(JSON_SUCCESS, true);
		for (String dt : dataTypes) {
			try {
				reportData = perfTestService.getReportData(testId, dt, imgWidth);
				rtnMap.put(dt, reportData);
			} catch (Exception e) {
				// just skip if one report data doesn't exist.
				rtnMap.put(dt, "Get report data failed. type: " + dt);
				LOG.error("Get report data failed. type: " + dt, e);
			}
		}

		return JSONUtil.toJson(rtnMap);
	}

	@RequestMapping(value = "/downloadReportData")
	public void downloadReportData(User user, HttpServletResponse response, @RequestParam long testId) {
		checkTestPermissionAndGet(user, testId);
		File targetFile = perfTestService.getReportFile(testId);
		FileDownloadUtil.downloadFile(response, targetFile);
	}

	@RequestMapping(value = "/running/refresh")
	public String refreshTestRunning(User user, ModelMap model, @RequestParam long testId) {
		checkTestPermissionAndGet(user, testId);
		PerfTest test = perfTestService.getPerfTest(testId);
		checkNotNull(test);
		Map<String, Object> result = null;
		if (test.getStatus() == Status.TESTING) {
			result = perfTestService.getStatistics(test.getPort());
			model.addAttribute(PARAM_RESULT_SUB, result);
		}
		return "perftest/refreshContent";
	}

	@RequestMapping(value = "/report")
	public String getReport(User user, ModelMap model, @RequestParam long testId) {
		checkTestPermissionAndGet(user, testId);
		PerfTest test = perfTestService.getPerfTest(testId);
		model.addAttribute("test", test);
		return "perftest/report";
	}

	private PerfTest checkTestPermissionAndGet(User user, long id) {
		PerfTest test = perfTestService.getPerfTest(id);
		if (user.getRole().equals(Role.ADMIN) || user.getRole().equals(Role.SUPER_USER)) {
			return test;
		}
		if (test != null && !test.getLastModifiedUser().equals(user)) {
			throw new NGrinderRuntimeException("User " + getCurrentUser().getUserId()
							+ " has no right on PerfTest ");
		}
		return test;
	}
}
