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

import static org.ngrinder.common.util.Preconditions.checkArgument;
import static org.ngrinder.common.util.Preconditions.checkNotEmpty;
import static org.ngrinder.common.util.Preconditions.checkNotNull;
import static org.ngrinder.common.util.Preconditions.checkState;
import static org.ngrinder.common.util.Preconditions.checkValidURL;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import javax.servlet.http.HttpServletResponse;

import net.grinder.common.processidentity.AgentIdentity;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.apache.commons.lang.time.DateUtils;
import org.ngrinder.agent.service.AgentManagerService;
import org.ngrinder.common.controller.NGrinderBaseController;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.util.DateUtil;
import org.ngrinder.common.util.FileDownloadUtil;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.spring.RemainedPath;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Role;
import org.ngrinder.model.Status;
import org.ngrinder.model.User;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.perftest.service.AgentManager;
import org.ngrinder.perftest.service.PerfTestService;
import org.ngrinder.perftest.service.TagService;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileType;
import org.ngrinder.script.service.FileEntryService;
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

	@Autowired
	private PerfTestService perfTestService;

	@Autowired
	private FileEntryService fileEntryService;

	@Autowired
	private AgentManager agentManager;

	@Autowired
	private AgentManagerService agentManagerService;

	@Autowired
	private TagService tagService;

	/**
	 * Get Performance test lists.
	 * 
	 * @param user
	 *            user
	 * @param query
	 *            query string to search the perf test
	 * @param model
	 *            modelMap
	 * @param tag
	 *            tag
	 * @param queryFilter
	 *            "F" means get only finished, "S" means get only scheduled tests.
	 * @param pageable
	 *            page
	 * @return perftest/list
	 */
	@RequestMapping({ "/list", "/" })
	public String getPerfTestList(User user, @RequestParam(required = false) String query,
					@RequestParam(required = false) String tag, @RequestParam(required = false) String queryFilter,
					@PageableDefaults(pageNumber = 0, value = 10) Pageable pageable, ModelMap model) {
		PageRequest pageReq = ((PageRequest) pageable);
		Sort sort = pageReq == null ? null : pageReq.getSort();
		if (sort == null && pageReq != null) {
			sort = new Sort(Direction.DESC, "lastModifiedDate");
			pageable = new PageRequest(pageReq.getPageNumber(), pageReq.getPageSize(), sort);
		}
		Page<PerfTest> testList = perfTestService.getPerfTestList(user, query, tag, queryFilter, pageable);

		TimeZone userTZ = TimeZone.getTimeZone(getCurrentUser().getTimeZone());
		Calendar userToday = Calendar.getInstance(userTZ);
		Calendar userYesterday = Calendar.getInstance(userTZ);
		userYesterday.add(Calendar.DATE, -1);

		for (PerfTest test : testList) {
			Calendar localedModified = Calendar.getInstance(userTZ);
			localedModified.setTime(DateUtil.convertToUserDate(getCurrentUser().getTimeZone(),
							test.getLastModifiedDate()));
			if (DateUtils.isSameDay(userToday, localedModified)) {
				test.setDateString("today");
			} else if (DateUtils.isSameDay(userYesterday, localedModified)) {
				test.setDateString("yesterday");
			} else {
				test.setDateString("earlier");
			}
		}
		model.addAttribute("tag", tag);
		model.addAttribute("availTags", tagService.getAllTagStrings(user, StringUtils.EMPTY));
		model.addAttribute("testListPage", testList);
		model.addAttribute("queryFilter", queryFilter);
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
	 * Get performance test detail on give perf test id.
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
			test = getPerfTestWithPermissionCheck(user, id, true);
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
		// Retrieve the agent count map based on create user, if the test is
		// created by the others.
		if (test != null) {
			user = test.getCreatedUser();
		}
		Map<String, MutableInt> agentCountMap = agentManagerService.getUserAvailableAgentCountMap(user);
		model.addAttribute(PARAM_REGION_AGENT_COUNT_MAP, agentCountMap);

		model.addAttribute(PARAM_PROCESSTHREAD_POLICY_SCRIPT, perfTestService.getProcessAndThreadPolicyScript());

		addDefaultAttributeOnModel(model);
		return "perftest/detail";
	}

	/**
	 * Search tag based on the given query.
	 * 
	 * @param user
	 *            user to search
	 * @param query
	 *            query string
	 * @return found tag list in json
	 */
	@RequestMapping("/tagSearch")
	public HttpEntity<String> searchTag(User user, @RequestParam(required = false) String query) {
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("content-type", "application/json; charset=UTF-8");
		List<String> allStrings = tagService.getAllTagStrings(user, query);
		if (StringUtils.isNotBlank(query)) {
			allStrings.add(query);
		}
		return new HttpEntity<String>(toJson(allStrings), responseHeaders);
	}

	/**
	 * Add the various default configuration values on the model.
	 * 
	 * @param model
	 *            model which will contains that value.
	 */
	public void addDefaultAttributeOnModel(ModelMap model) {
		model.addAttribute(PARAM_MAX_VUSER_PER_AGENT, agentManager.getMaxVuserPerAgent());
		model.addAttribute(PARAM_MAX_RUN_COUNT, agentManager.getMaxRunCount());
		model.addAttribute(PARAM_SECURITY_MODE, getConfig().isSecurityEnabled());
		model.addAttribute(PARAM_MAX_RUN_HOUR, agentManager.getMaxRunHour());
	}

	/**
	 * get details view for quickStart.
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
		URL url = checkValidURL(urlString);
		List<FileEntry> scriptList = new ArrayList<FileEntry>();
		FileEntry newEntry = fileEntryService.prepareNewEntryForQuickTest(user, urlString);
		scriptList.add(checkNotNull(newEntry, "Create quick test script ERROR!"));
		model.addAttribute(PARAM_QUICK_SCRIPT, newEntry.getPath());
		model.addAttribute("testName", "Test for " + url.getHost());
		model.addAttribute(PARAM_TARGET_HOST, url.getHost());
		model.addAttribute(PARAM_SCRIPT_LIST, scriptList);
		model.addAttribute(PARAM_REGION_AGENT_COUNT_MAP, agentManagerService.getUserAvailableAgentCountMap(user));
		model.addAttribute(PARAM_PROCESSTHREAD_POLICY_SCRIPT, perfTestService.getProcessAndThreadPolicyScript());
		addDefaultAttributeOnModel(model);
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
	 * @param isClone
	 *            true if clone
	 * @return redirect:/perftest/list
	 */
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public String savePerfTest(User user, ModelMap model, PerfTest test,
					@RequestParam(value = "isClone", required = false, defaultValue = "false") boolean isClone) {
		test.setTestName(StringUtils.trimToEmpty(test.getTestName()));
		checkNotEmpty(test.getTestName(), "test name should be provided");
		checkArgument(test.getStatus().equals(Status.READY) || test.getStatus().equals(Status.SAVED),
						"save test only support for SAVE or READY status");
		checkArgument(test.getRunCount() == null || test.getRunCount() <= agentManager.getMaxRunCount(),
						"test run count should be within %s", agentManager.getMaxRunCount());
		checkArgument(test.getDuration() == null
						|| test.getDuration() <= (((long) agentManager.getMaxRunHour()) * 3600000L),
						"test run duration should be within %s", agentManager.getMaxRunHour());

		Map<String, MutableInt> agentCountMap = agentManagerService.getUserAvailableAgentCountMap(user);
		MutableInt agentCountObj = agentCountMap.get(clustered() ? test.getRegion() : Config.NONE_REGION);
		checkNotNull(agentCountObj, "test region should be within current region list");
		int agentMaxCount = agentCountObj.intValue();
		checkArgument(test.getAgentCount() <= agentMaxCount, "test agent shoule be within %s", agentMaxCount);
		checkArgument(test.getVuserPerAgent() == null || test.getVuserPerAgent() <= agentManager.getMaxVuserPerAgent(),
						"test vuser shoule be within %s", agentManager.getMaxVuserPerAgent());
		if (getConfig().isSecurityEnabled()) {
			checkArgument(StringUtils.isNotEmpty(test.getTargetHosts()),
							"test taget hosts should be provided when security mode is enabled");
		}
		checkArgument(test.getProcesses() != null && 0 != test.getProcesses(), "test process should not be 0");
		checkArgument(test.getThreads() != null && 0 != test.getThreads(), "test thread should not be 0");
		// Point to the head revision
		test.setScriptRevision(-1L);
		// deal with different time zone between user Local and Server
		Date scheduleDate = test.getScheduledTime();
		if (scheduleDate != null) {
			test.setScheduledTime(DateUtil.convertToServerDate(user.getTimeZone(), scheduleDate));
		}
		// NGRINDER-236 hehe
		if (isClone) {
			test.setId(null);
			test.setTps(null);
			test.setCreatedUser(null);
			test.setCreatedDate(null);
			test.setLastModifiedDate(null);
			test.setLastModifiedUser(null);
		}
		if (StringUtils.isBlank(test.getRegion())) {
			test.setRegion(Config.NONE_REGION);
		}
		perfTestService.savePerfTest(user, test);
		model.clear();
		return "redirect:/perftest/list";
	}

	/**
	 * Leave comment on the perftest.
	 * 
	 * @param user
	 *            user
	 * @param testComment
	 *            trest comment
	 * @param testId
	 *            testId
	 * @param tagString
	 *            tagString
	 * @return JSON
	 */
	@RequestMapping(value = "/leaveComment", method = RequestMethod.POST)
	@ResponseBody
	public String leaveComment(User user, @RequestParam("testComment") String testComment,
					@RequestParam(value = "tagString", required = false) String tagString,
					@RequestParam("testId") Long testId) {
		perfTestService.addCommentOn(user, testId, testComment, tagString);
		return returnSuccess();
	}

	/**
	 * Get status of perftest.
	 * 
	 * @param user
	 *            user
	 * @param ids
	 *            comma seperated perftest list
	 * @return json string which contains perftest status
	 */
	@RequestMapping(value = "/updateStatus")
	public HttpEntity<String> updateStatus(User user, @RequestParam(defaultValue = "") String ids) {
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
		Map<String, Object> result = new HashMap<String, Object>(2);
		result.put("perfTestInfo", perfTestService.getCurrentPerfTestStatistics());
		result.put("statusList", statusList);
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("content-type", "application/json; charset=UTF-8");
		return new HttpEntity<String>(toJson(result), responseHeaders);
	}

	/**
	 * Delete the perftest having given ids.
	 * 
	 * @param user
	 *            user
	 * @param model
	 *            model
	 * @param ids
	 *            id string separating ","
	 * @return success json if succeeded.
	 */
	@RequestMapping(value = "/deleteTests", method = RequestMethod.POST)
	@ResponseBody
	public String deletePerfTests(User user, ModelMap model, @RequestParam(defaultValue = "") String ids) {
		for (String idStr : StringUtils.split(ids, ",")) {
			perfTestService.deletePerfTest(user, NumberUtils.toLong(idStr, 0));
		}
		return returnSuccess();
	}

	/**
	 * Request to stop tests having given ids.
	 * 
	 * @param user
	 *            user
	 * @param model
	 *            model
	 * @param ids
	 *            id string separating ","
	 * @return success json if succeeded.
	 */
	@RequestMapping(value = "/stopTests", method = RequestMethod.POST)
	@ResponseBody
	public String stopPerfTests(User user, ModelMap model, @RequestParam(value = "ids", defaultValue = "") String ids) {
		for (String idStr : StringUtils.split(ids, ",")) {
			perfTestService.stopPerfTest(user, NumberUtils.toLong(idStr, 0));
		}
		return returnSuccess();
	}

	/**
	 * Get resources and lib files on the same folder with the given script path.
	 * 
	 * @param user
	 *            user
	 * @param scriptPath
	 *            script path
	 * @param revision
	 *            revision
	 * @return json string representing resources and libs.
	 */
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

		return new HttpEntity<String>(toJson(message), responseHeaders);
	}

	/**
	 * Get the detailed report graph data for the given perftest id.<br/>
	 * This method returns the appropriate points based on the given imgWidth.
	 * 
	 * @param user
	 *            user
	 * @param model
	 *            model
	 * @param testId
	 *            test id
	 * @param dataType
	 *            which data
	 * @param imgWidth
	 *            imageWidth
	 * @return json string.
	 */
	@RequestMapping(value = "/getReportData")
	@ResponseBody
	public String getReportData(User user, ModelMap model, @RequestParam long testId,
					@RequestParam(required = true, defaultValue = "") String dataType, @RequestParam int imgWidth) {
		getPerfTestWithPermissionCheck(user, testId, false);
		String[] dataTypes = StringUtils.split(dataType, ",");
		Map<String, Object> rtnMap = new HashMap<String, Object>(1 + dataTypes.length);
		if (dataTypes.length <= 0) {
			return returnError();
		}
		rtnMap.put(JSON_SUCCESS, true);
		int interval = perfTestService.getReportDataInterval(testId, dataTypes[0], imgWidth);
		for (String dt : dataTypes) {
			String reportData = perfTestService.getReportDataAsString(testId, dt, interval);
			String rtnType = dt.replace("(", "").replace(")", "");
			rtnMap.put(rtnType, reportData);
		}

		rtnMap.put(PARAM_TEST_CHART_INTERVAL, interval);
		return toJson(rtnMap);
	}

	/**
	 * Get the basic report content in perftest configuration page.<br/>
	 * This method returns the appropriate points based on the given imgWidth.
	 * 
	 * @param user
	 *            user
	 * @param model
	 *            model
	 * @param testId
	 *            test id
	 * @param imgWidth
	 *            image width
	 * @return "perftest/reportDiv"
	 */
	@RequestMapping(value = "/loadReportDiv")
	public String getReportDiv(User user, ModelMap model, @RequestParam long testId, @RequestParam int imgWidth) {
		PerfTest test = getPerfTestWithPermissionCheck(user, testId, false);
		int interval = perfTestService.getReportDataInterval(testId, "TPS", imgWidth);
		String reportData = perfTestService.getReportDataAsString(testId, "TPS", interval);
		model.addAttribute(PARAM_LOG_LIST, perfTestService.getLogFiles(testId));
		model.addAttribute(PARAM_TEST_CHART_INTERVAL, interval);
		model.addAttribute(PARAM_TEST, test);
		model.addAttribute(PARAM_TPS, reportData);
		return "perftest/reportDiv";
	}

	/**
	 * Download csv report for the given perf test id.
	 * 
	 * @param user
	 *            user
	 * @param response
	 *            response
	 * @param testId
	 *            test id
	 */
	@RequestMapping(value = "/downloadReportData")
	public void downloadReportData(User user, HttpServletResponse response, @RequestParam long testId) {
		PerfTest test = getPerfTestWithPermissionCheck(user, testId, false);
		File targetFile = perfTestService.getReportFile(test);
		checkState(targetFile.exists(), "File %s doesn't exist!", targetFile.getName());
		FileDownloadUtil.downloadFile(response, targetFile);
	}

	/**
	 * Download logs for the given id.
	 * 
	 * @param user
	 *            user
	 * @param path
	 *            path in the log folder
	 * @param testId
	 *            test id
	 * @param response
	 *            repsonse
	 */
	@RequestMapping(value = "/downloadLog/**")
	public void downloadLogData(User user, @RemainedPath String path, @RequestParam long testId,
					HttpServletResponse response) {
		getPerfTestWithPermissionCheck(user, testId, false);
		File targetFile = perfTestService.getLogFile(testId, path);
		FileDownloadUtil.downloadFile(response, targetFile);
	}

	/**
	 * Get the real time test running info for the given test id.
	 * 
	 * @param user
	 *            user
	 * @param model
	 *            model
	 * @param testId
	 *            test id
	 * @return "perftest/refreshContent"
	 */
	@RequestMapping(value = "/running/refresh")
	public String refreshTestRunning(User user, ModelMap model, @RequestParam long testId) {
		PerfTest test = checkNotNull(getPerfTestWithPermissionCheck(user, testId, false),
						"given test should be exist : " + testId);
		if (test.getStatus().equals(Status.TESTING)) {
			model.addAttribute(PARAM_RESULT_AGENT_PERF, getAgentPerfString(perfTestService.getAgentInfo(test)));
			model.addAttribute(PARAM_RESULT_SUB, perfTestService.getStatistics(test));
		}
		return "perftest/refreshContent";
	}

	private String getAgentPerfString(Map<AgentIdentity, SystemDataModel> agentPerfMap) {
		List<String> perfStringList = new ArrayList<String>();
		for (Entry<AgentIdentity, SystemDataModel> each : agentPerfMap.entrySet()) {
			SystemDataModel value = each.getValue();
			Long totalMemory = value.getTotalMemory();
			float usage = 0;
			if (totalMemory != 0) {
				usage = (((float) (totalMemory - value.getFreeMemory())) / totalMemory) * 100;
			}
			perfStringList.add(String.format(" {'agent' : '%s', 'cpu' : %3.02f, 'mem' : %3.02f }",
							StringUtils.abbreviate(each.getKey().getName(), 25), value.getCpuUsedPercentage(), usage));
		}
		return StringUtils.join(perfStringList, ",");
	}

	/**
	 * Get perftest report.
	 * 
	 * @param user
	 *            user
	 * @param model
	 *            model
	 * @param testId
	 *            test id
	 * @return "perftest/report"
	 */
	@RequestMapping(value = "/report")
	public String getReport(User user, ModelMap model, @RequestParam long testId) {
		model.addAttribute("test", getPerfTestWithPermissionCheck(user, testId, false));
		return "perftest/report";
	}

	private PerfTest getPerfTestWithPermissionCheck(User user, Long id, boolean withTag) {
		PerfTest perfTest = withTag ? perfTestService.getPerfTestWithTag(id) : perfTestService.getPerfTest(id);
		if (user.getRole().equals(Role.ADMIN) || user.getRole().equals(Role.SUPER_USER)) {
			return perfTest;
		}
		if (perfTest != null && !user.equals(perfTest.getCreatedUser())) {
			throw new NGrinderRuntimeException("User " + user.getUserId() + " has no right on PerfTest ");
		}
		return perfTest;
	}

}
