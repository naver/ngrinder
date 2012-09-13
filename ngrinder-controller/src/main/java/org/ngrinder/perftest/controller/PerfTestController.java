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
import static org.ngrinder.common.util.Preconditions.checkState;
import static org.ngrinder.common.util.Preconditions.checkValidURL;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import net.grinder.common.processidentity.AgentIdentity;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.ngrinder.common.controller.NGrinderBaseController;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.util.DateUtil;
import org.ngrinder.common.util.FileDownloadUtil;
import org.ngrinder.common.util.JSONUtil;
import org.ngrinder.infra.spring.RemainedPath;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Role;
import org.ngrinder.model.Status;
import org.ngrinder.model.User;
import org.ngrinder.monitor.controller.model.SystemDataModel;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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
	@RequestMapping({ "/list", "/" })
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

		int rawOffset = getTimeZoneOffSet(user);
		Calendar cal = Calendar.getInstance();
		Date localToday = new Date(cal.getTime().getTime() - rawOffset);
		cal.add(Calendar.DATE, -1);
		Date localYesterday = new Date(cal.getTime().getTime() - rawOffset);

		for (PerfTest test : testList) {
			Date localModified = new Date(test.getLastModifiedDate().getTime() - rawOffset);
			if (DateUtil.compareDateEndWithDay(localModified, localToday))
				test.setDateString("today");
			else if (DateUtil.compareDateEndWithDay(localModified, localYesterday))
				test.setDateString("yesterday");
			else
				test.setDateString("earlier");
		}

		model.addAttribute("testListPage", testList);
		model.addAttribute("onlyFinished", onlyFinished);
		model.addAttribute("query", query);
		model.addAttribute("page", pageable);
		if (sort != null) {
			Order sortProp = (Order) sort.iterator().next();
			model.addAttribute("sortColumn", sortProp.getProperty());
			model.addAttribute("sortDirection", sortProp.getDirection());
		}
		addCurrentlyRunningTest(model);
		return "perftest/list";
	}

	private void addCurrentlyRunningTest(ModelMap model) {
		model.addAttribute("perfTestStatisticsList", perfTestService.getCurrentPerfTestStatistics());
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
		List<FileEntry> allFileEntries = fileEntryService.getAllFileEntries(user);
		CollectionUtils.filter(allFileEntries, new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				return ((FileEntry) object).getFileType() == FileType.PYTHON_SCRIPT;
			}
		});

		model.addAttribute(PARAM_SCRIPT_LIST, allFileEntries);

		model.addAttribute(PARAM_PROCESSTHREAD_POLICY_SCRIPT, perfTestService.getProcessAndThreadPolicyScript());
		addDefaultAttributeOnMode(model);
		return "perftest/detail";
	}

	/**
	 * Add the various default configuration values on the model.
	 * 
	 * @param model
	 *            model which will contains that value.
	 */
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
		FileEntry newEntry = fileEntryService.prepareNewEntryForQuickTest(user, urlString);
		scriptList.add(newEntry);
		model.addAttribute(PARAM_SCRIPT_LIST, scriptList);
		addDefaultAttributeOnMode(model);
		model.addAttribute(PARAM_PROCESSTHREAD_POLICY_SCRIPT, perfTestService.getProcessAndThreadPolicyScript());
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
						"test duration should be within %s", agentManager.getMaxRunHour());
		checkArgument(test.getRunCount() == null || test.getRunCount() <= agentManager.getMaxRunCount(),
						"test run count should be within %s", agentManager.getMaxRunCount());
		checkArgument(test.getAgentCount() == null || test.getAgentCount() <= agentManager.getMaxAgentSizePerConsole(),
						"test agent shoule be within %s", agentManager.getMaxAgentSizePerConsole());
		checkArgument(test.getVuserPerAgent() == null || test.getVuserPerAgent() <= agentManager.getMaxVuserPerAgent(),
						"test vuser shoule be within %s", agentManager.getMaxVuserPerAgent());
		test.setScriptRevision(-1L);

		// deal with different time zone between user Local and Server
		Date scheduleDate = test.getScheduledTime();
		if (scheduleDate != null) {
			int rawOffset = getTimeZoneOffSet(user);
			test.setScheduledTime(new Date(scheduleDate.getTime() + rawOffset));
		}
		perfTestService.savePerfTest(user, test);
		return "redirect:/perftest/list";
	}

	/**
	 * leave comment on the perftest
	 * 
	 * @param testComment
	 *            trest comment
	 * @param testId
	 *            testId
	 * @return JSON
	 */
	@RequestMapping(value = "/leaveComment", method = RequestMethod.POST)
	public @ResponseBody
	String leaveComment(User user, @RequestParam("testComment") String testComment, @RequestParam("testId") Long testId) {
		perfTestService.addCommentOn(user, testId, testComment);
		return JSONUtil.returnSuccess();
	}

	/**
	 * Get status of perftest
	 * 
	 * @param user
	 *            user
	 * @param ids
	 *            comma seperated perftest list
	 * @return json string which contains perftest status
	 */
	@RequestMapping(value = "/updateStatus")
	public HttpEntity<String> updateSatus(User user, @RequestParam(defaultValue = "") String ids) {
		String[] numbers = StringUtils.split(ids, ",");
		Long[] id = new Long[numbers.length];
		int i = 0;
		for (String each : numbers) {
			id[i++] = NumberUtils.toLong(each, 0);
		}
		List<PerfTest> perfTests = perfTestService.getPerfTest(user, id);
		List<Map<String, Object>> statusList = new ArrayList<Map<String, Object>>();
		for (PerfTest each : perfTests) {
			Map<String, Object> rtnMap = new HashMap<String, Object>(3);
			rtnMap.put(PARAM_STATUS_UPDATE_ID, each.getId());
			rtnMap.put(PARAM_STATUS_UPDATE_STATUS_ID, each.getStatus());
			rtnMap.put(PARAM_STATUS_UPDATE_STATUS_TYPE, each.getStatus());
			String errorMessages = getMessages(each.getStatus().getSpringMessageKey());
			rtnMap.put(PARAM_STATUS_UPDATE_STATUS_NAME, errorMessages);
			rtnMap.put(PARAM_STATUS_UPDATE_STATUS_ICON, each.getStatus().getIconName());
			rtnMap.put(PARAM_STATUS_UPDATE_STATUS_MESSAGE,
							StringUtils.replace(each.getProgressMessage() + "\n<b>" + each.getLastProgressMessage()
											+ "</b>\n" + each.getLastModifiedDateToStr(), "\n", "<br/>"));
			rtnMap.put(PARAM_STATUS_UPDATE_DELETABLE, each.getStatus().isDeletable());
			rtnMap.put(PARAM_STATUS_UPDATE_STOPPABLE, each.getStatus().isStoppable());
			statusList.add(rtnMap);
		}
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("content-type", "application/json; charset=UTF-8");
		return new HttpEntity<String>(JSONUtil.toJson(statusList), responseHeaders);
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
	public HttpEntity<String> getResourcesOnScriptFolder(User user, @RequestParam String scriptPath,
					@RequestParam(value = "r", required = false) Long revision) {
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("content-type", "application/json; charset=UTF-8");

		Map<String, Object> message = new HashMap<String, Object>();
		FileEntry fileEntry = fileEntryService.getFileEntry(user, scriptPath);
		String targetHosts = (fileEntry == null) ? "" : fileEntry.getProperties().get("targetHosts");

		List<String> fileStringList = new ArrayList<String>();
		message.put("targetHosts", StringUtils.trimToEmpty(targetHosts));
		message.put("resources", fileStringList);

		List<FileEntry> fileList = fileEntryService.getLibAndResourcesEntries(user, scriptPath, revision);
		for (FileEntry each : fileList) {
			fileStringList.add(each.getPath());
		}

		return new HttpEntity<String>(JSONUtil.toJson(message), responseHeaders);
	}

	@RequestMapping(value = "/getReportData")
	public @ResponseBody
	String getReportData(User user, ModelMap model, @RequestParam long testId,
					@RequestParam(required = true) String dataType, @RequestParam int imgWidth) {
		checkTestPermissionAndGet(user, testId);
		String[] dataTypes = StringUtils.split(dataType, ",");
		Map<String, Object> rtnMap = new HashMap<String, Object>(1 + dataTypes.length);
		if (dataTypes.length <= 0) {
			return JSONUtil.returnError();
		}
		rtnMap.put(JSON_SUCCESS, true);
		int interval = perfTestService.getReportDataInterval(testId, dataTypes[0], imgWidth);
		for (String dt : dataTypes) {
			String reportData = perfTestService.getReportDataAsString(testId, dt, interval);
			String rtnType = dt.replace("(", "").replace(")", "");
			rtnMap.put(rtnType, reportData);
		}

		rtnMap.put(PARAM_TEST_CHART_INTERVAL, interval);
		return JSONUtil.toJson(rtnMap);
	}

	@RequestMapping(value = "/loadReportDiv")
	public String getReportDiv(User user, ModelMap model, @RequestParam long testId, @RequestParam int imgWidth) {
		PerfTest test = checkTestPermissionAndGet(user, testId);
		int interval = perfTestService.getReportDataInterval(testId, "TPS", imgWidth);
		String reportData = perfTestService.getReportDataAsString(testId, "TPS", interval);

		model.addAttribute(PARAM_LOG_LIST, perfTestService.getLogFiles(testId));
		model.addAttribute(PARAM_TEST_CHART_INTERVAL, interval);
		model.addAttribute(PARAM_TEST, test);
		model.addAttribute(PARAM_TPS, reportData);
		return "perftest/reportDiv";
	}

	@RequestMapping(value = "/downloadReportData")
	public void downloadReportData(User user, HttpServletResponse response, @RequestParam long testId) {
		checkTestPermissionAndGet(user, testId);
		File targetFile = perfTestService.getReportFile(testId);
		checkState(targetFile.exists(), "File %s doesn't exist!", targetFile.getName());
		FileDownloadUtil.downloadFile(response, targetFile);
	}

	@RequestMapping(value = "/downloadLog/**")
	public void downloadLogData(User user, @RemainedPath String path, @RequestParam long testId,
					HttpServletResponse response) {
		checkTestPermissionAndGet(user, testId);
		File targetFile = perfTestService.getLogFile(testId, path);
		FileDownloadUtil.downloadFile(response, targetFile);
	}

	@RequestMapping(value = "/running/refresh")
	public String refreshTestRunning(User user, ModelMap model, @RequestParam long testId) {
		checkTestPermissionAndGet(user, testId);
		PerfTest test = perfTestService.getPerfTest(testId);
		checkNotNull(test);
		if (test.getStatus() == Status.TESTING) {
			model.addAttribute(PARAM_RESULT_AGENT_PERF,
							getAgentPerfString(perfTestService.getAgentsInfo(test.getPort())));
			model.addAttribute(PARAM_RESULT_SUB, perfTestService.getStatistics(test.getPort()));
		}
		return "perftest/refreshContent";
	}

	private String getAgentPerfString(Map<AgentIdentity, SystemDataModel> agentPerfMap) {
		List<String> perfStringList = new ArrayList<String>();
		for (Entry<AgentIdentity, SystemDataModel> each : agentPerfMap.entrySet()) {
			SystemDataModel value = each.getValue();
			long totalMemory = value.getTotalMemory();
			float usage = 0;
			if (totalMemory != 0) {
				usage = (totalMemory - value.getFreeMemory()) / totalMemory;
			}
			perfStringList.add(String.format(" {'agent' : '%s', 'cpu' : %3.2f, 'mem' : %3.2f }", each.getKey().getName(),
							value.getCpuUsedPercentage(), usage));
		}
		return StringUtils.join(perfStringList, ",");
	}

	@RequestMapping(value = "/report")
	public String getReport(User user, ModelMap model, @RequestParam long testId) {
		checkTestPermissionAndGet(user, testId);
		PerfTest test = perfTestService.getPerfTest(testId);
		model.addAttribute("test", test);
		return "perftest/report";
	}

	private PerfTest checkTestPermissionAndGet(User user, Long id) {
		PerfTest test = perfTestService.getPerfTest(id);
		if (user.getRole().equals(Role.ADMIN) || user.getRole().equals(Role.SUPER_USER)) {
			return test;
		}
		if (test != null && !user.equals(test.getLastModifiedUser())) {
			throw new NGrinderRuntimeException("User " + user.getUserId() + " has no right on PerfTest ");
		}
		return test;
	}

}
