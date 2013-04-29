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

import static org.apache.commons.lang.StringUtils.trimToEmpty;
import static org.ngrinder.common.util.CollectionUtils.buildMap;
import static org.ngrinder.common.util.CollectionUtils.newArrayList;
import static org.ngrinder.common.util.Preconditions.checkArgument;
import static org.ngrinder.common.util.Preconditions.checkNotEmpty;
import static org.ngrinder.common.util.Preconditions.checkNotNull;
import static org.ngrinder.common.util.Preconditions.checkState;
import static org.ngrinder.common.util.Preconditions.checkValidURL;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import net.grinder.SingleConsole;
import net.grinder.util.LogCompressUtil;
import net.grinder.util.Pair;
import net.grinder.util.UnitUtil;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.apache.commons.lang.time.DateUtils;
import org.ngrinder.agent.service.AgentManagerService;
import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.common.controller.NGrinderBaseController;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.util.DateUtil;
import org.ngrinder.common.util.FileDownloadUtil;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.logger.CoreLogger;
import org.ngrinder.infra.spring.RemainedPath;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Role;
import org.ngrinder.model.Status;
import org.ngrinder.model.User;
import org.ngrinder.perftest.service.AgentManager;
import org.ngrinder.perftest.service.PerfTestService;
import org.ngrinder.perftest.service.TagService;
import org.ngrinder.script.handler.ScriptHandlerFactory;
import org.ngrinder.script.model.FileCategory;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.service.FileEntryService;
import org.python.google.common.collect.Lists;
import org.python.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.web.PageableDefaults;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tmatesoft.svn.core.wc.SVNRevision;

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

	@Autowired
	private ScriptHandlerFactory scriptHandlerFactory;

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
	@RequestMapping({ "/list", "/", "" })
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
	 * Get perftest creation form.
	 * 
	 * @param user
	 *            user
	 * @param model
	 *            model
	 * @return "perftest/detail"
	 */
	@RequestMapping("/new")
	public String getPerfTestDetail(User user, ModelMap model) {
		return getPerfTestDetail(user, null, model);
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
	@RequestMapping("/{id}")
	public String getPerfTestDetail(User user, @PathVariable("id") Long id, ModelMap model) {
		PerfTest test = null;
		if (id != null) {
			test = getPerfTestWithPermissionCheck(user, id, true);
		}

		model.addAttribute(PARAM_TEST, test);
		List<FileEntry> allFileEntries = fileEntryService.getAllFileEntries(user);

		CollectionUtils.filter(allFileEntries, new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				return ((FileEntry) object).getFileType().getFileCategory() == FileCategory.SCRIPT;
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
		List<String> allStrings = tagService.getAllTagStrings(user, query);
		if (StringUtils.isNotBlank(query)) {
			allStrings.add(query);
		}
		return toJsonHttpEntity(allStrings);
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
		model.addAttribute(PARAM_SAFE_FILE_DISTRIBUTION,
						getConfig().getSystemProperties().getPropertyBoolean(NGRINDER_PROP_DIST_SAFE, false));
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
		List<FileEntry> scriptList = newArrayList();
		FileEntry newEntry = fileEntryService.prepareNewEntryForQuickTest(user, urlString,
						scriptHandlerFactory.getHandler("jython"));
		scriptList.add(checkNotNull(newEntry, "Create quick test script ERROR!"));
		model.addAttribute(PARAM_QUICK_SCRIPT, newEntry.getPath());
		model.addAttribute(PARAM_QUICK_SCRIPT_REVISION, newEntry.getRevision());
		model.addAttribute(PARAM_TEST_NAME, "Test for " + url.getHost());
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
			test.setTestComment(null);
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
	 * @param id
	 *            testId
	 * @param user
	 *            user
	 * @param testComment
	 *            test comment
	 * @param tagString
	 *            tagString
	 * @return JSON
	 */
	@RequestMapping(value = "{id}/leaveComment", method = RequestMethod.POST)
	@ResponseBody
	public String leaveComment(User user, @PathVariable("id") Long id, @RequestParam("testComment") String testComment,
					@RequestParam(value = "tagString", required = false) String tagString) {
		perfTestService.addCommentOn(user, id, testComment, tagString);
		return returnSuccess();
	}

	/**
	 * Get status of perftests.
	 * 
	 * @param user
	 *            user
	 * @param ids
	 *            comma separated perftest list
	 * @return json string which contains perftest status
	 */
	@RequestMapping(value = "updateStatus")
	public HttpEntity<String> updateStatuses(User user, @RequestParam("ids") String ids) {
		return updateStatus(user, ids);
	}

	/**
	 * Get status of perftest.
	 * 
	 * @param user
	 *            user
	 * @param idString
	 *            comma separated perftest list
	 * @return json string which contains perftest status
	 */
	@RequestMapping(value = "{id}/updateStatus")
	public HttpEntity<String> updateStatus(User user, @PathVariable("id") String idString) {
		String[] numbers = StringUtils.split(idString, ",");
		Long[] id = new Long[numbers.length];
		int i = 0;
		for (String each : numbers) {
			id[i++] = NumberUtils.toLong(each, 0);
		}

		List<PerfTest> perfTests = null;
		if (StringUtils.isNotEmpty(idString)) {
			perfTests = perfTestService.getPerfTest(user, id);
		} else {
			perfTests = Collections.emptyList();
		}
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
		return toJsonHttpEntity(buildMap("perfTestInfo", perfTestService.getCurrentPerfTestStatistics(), "statusList",
						statusList));
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
		FileEntry fileEntry = fileEntryService.getFileEntry(user, scriptPath);
		String targetHosts = (fileEntry == null) ? "" : filterHostString(fileEntry.getProperties().get("targetHosts"));

		List<String> fileStringList = newArrayList();
		List<FileEntry> fileList = fileEntryService.getScriptHandler(fileEntry).getLibAndResourceEntries(user,
						fileEntry, SVNRevision.HEAD.getNumber());
		for (FileEntry each : fileList) {
			fileStringList.add(each.getPath());
		}

		return toJsonHttpEntity(buildMap("targetHosts", trimToEmpty(targetHosts), "resources", fileStringList));
	}

	/**
	 * Filter out please_modify_this.com from hosts string.
	 * 
	 * @param originalString
	 *            original string
	 * @return filtered string
	 */
	private String filterHostString(String originalString) {
		List<String> hosts = Lists.newArrayList();
		for (String each : StringUtils.split(StringUtils.trimToEmpty(originalString), ",")) {
			if (!each.contains("please_modify_this.com")) {
				hosts.add(each);
			}
		}
		return StringUtils.join(hosts, ",");
	}

	/**
	 * Get the detailed report graph data for the given perftest id.<br/>
	 * This method returns the appropriate points based on the given imgWidth.
	 * 
	 * @param model
	 *            model
	 * @param id
	 *            test id
	 * @param dataType
	 *            which data
	 * @param imgWidth
	 *            imageWidth
	 * @return json string.
	 */
	@RequestMapping(value = "{id}/graph")
	@ResponseBody
	public String getReportData(ModelMap model, @PathVariable("id") long id,
					@RequestParam(required = true, defaultValue = "") String dataType, @RequestParam int imgWidth) {
		String[] dataTypes = StringUtils.split(dataType, ",");
		if (dataTypes.length <= 0) {
			return returnError();
		}
		int interval = perfTestService.getReportDataInterval(id, dataTypes[0], imgWidth);
		return toJson(getGraphDataString(perfTestService.getPerfTest(id), dataTypes, interval));
	}

	private Map<String, Object> getGraphDataString(PerfTest perfTest, String[] dataTypes, int interval) {
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put(JSON_SUCCESS, true);
		for (String each : dataTypes) {
			if ("TPS".equals(each)) {
				Pair<ArrayList<String>, ArrayList<String>> tpsResult = perfTestService.getTPSReportDataAsString(
								perfTest.getId(), interval, isMainTPSOnly(perfTest));
				resultMap.put("lables", tpsResult.getFirst());
				resultMap.put("TPS", tpsResult.getSecond());
			} else {
				resultMap.put(StringUtils.replaceChars(each, "()", ""),
								perfTestService.getReportDataAsString(perfTest.getId(), each, interval));
			}
		}
		resultMap.put(PARAM_TEST_CHART_INTERVAL, interval * perfTest.getSamplingInterval());
		return resultMap;
	}

	private boolean isMainTPSOnly(PerfTest perfTest) {
		return (perfTest.getSamplingInterval() * 1000) < SingleConsole.MIN_SAMPLING_INTERVAL_TO_ACTIVATE_TPS_PER_TEST;
	}

	/**
	 * Get the basic report content in perftest configuration page.<br/>
	 * This method returns the appropriate points based on the given imgWidth.
	 * 
	 * @param user
	 *            user
	 * @param model
	 *            model
	 * @param id
	 *            test id
	 * @param imgWidth
	 *            image width
	 * @return "perftest/reportDiv"
	 */
	@RequestMapping(value = "{id}/loadReportDiv")
	public String getReportDiv(User user, ModelMap model, @PathVariable long id, @RequestParam int imgWidth) {
		PerfTest test = getPerfTestWithPermissionCheck(user, id, false);
		int interval = perfTestService.getReportDataInterval(id, "TPS", imgWidth);
		model.addAttribute(PARAM_LOG_LIST, perfTestService.getLogFiles(id));
		model.addAttribute(PARAM_TEST_CHART_INTERVAL, interval * test.getSamplingInterval());
		model.addAttribute(PARAM_TEST, test);
		model.addAttribute(PARAM_TPS, perfTestService.getReportDataAsString(id, "TPS", interval));
		return "perftest/reportDiv";
	}

	/**
	 * Download csv report for the given perf test id.
	 * 
	 * @param user
	 *            user
	 * @param response
	 *            response
	 * @param id
	 *            test id
	 */
	@RequestMapping(value = "{id}/downloadReportData")
	public void downloadReportData(User user, HttpServletResponse response, @PathVariable("id") long id) {
		PerfTest test = getPerfTestWithPermissionCheck(user, id, false);
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
	 * @param id
	 *            test id
	 * @param response
	 *            repsonse
	 */
	@RequestMapping(value = "{id}/downloadLog/**")
	public void downloadLogData(User user, @RemainedPath String path, @PathVariable("id") long id,
					HttpServletResponse response) {
		getPerfTestWithPermissionCheck(user, id, false);
		File targetFile = perfTestService.getLogFile(id, path);
		FileDownloadUtil.downloadFile(response, targetFile);
	}

	/**
	 * Download logs for the given id.
	 * 
	 * @param user
	 *            user
	 * @param id
	 *            test id
	 * @param path
	 *            path in the log folder
	 * @param response
	 *            response
	 */
	@RequestMapping(value = "{id}/showLog/**")
	public void showLogData(User user, @PathVariable("id") long id, @RemainedPath String path,
					HttpServletResponse response) {
		getPerfTestWithPermissionCheck(user, id, false);
		File targetFile = perfTestService.getLogFile(id, path);
		response.reset();
		response.setContentType("text/plain");
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(targetFile);
			// Limit log view to 1MB
			ServletOutputStream outputStream = response.getOutputStream();
			outputStream.println("Only the last 1MB of a log shows.\n");
			outputStream.println("================================\n\n");
			LogCompressUtil.unCompress(fileInputStream, outputStream, 1 * 1024 * 1204);
		} catch (Exception e) {
			CoreLogger.LOGGER.error("Error while uncompress log. {}", targetFile, e);
		} finally {
			IOUtils.closeQuietly(fileInputStream);
		}
	}

	/**
	 * Get the real time test running info for the given test id.
	 * 
	 * @param user
	 *            user
	 * @param model
	 *            model
	 * @param id
	 *            test id
	 * @return "perftest/refreshContent"
	 */
	@RequestMapping(value = "{id}/running/refresh")
	public String refreshTestRunning(User user, ModelMap model, @PathVariable("id") long id) {
		PerfTest test = checkNotNull(getPerfTestWithPermissionCheck(user, id, false), "given test should be exist : "
						+ id);
		if (test.getStatus().equals(Status.TESTING)) {
			model.addAttribute(PARAM_RESULT_SUB, perfTestService.getStatistics(test));
			model.addAttribute(PARAM_RESULT_AGENT_PERF, getStatString(perfTestService.getAgentStat(test)));
			model.addAttribute(PARAM_RESULT_MONITOR_PERF, getStatString(perfTestService.getMonitorStat(test)));
		}
		return "perftest/refreshContent";
	}

	@SuppressWarnings("rawtypes")
	private String getStatString(Map<String, HashMap> statMap) {
		if (statMap == null) {
			return StringUtils.EMPTY;
		}
		List<String> perfStringList = Lists.newArrayList();
		for (Entry<String, HashMap> each : statMap.entrySet()) {
			Map value = each.getValue();
			if (value == null) {
				continue;
			}
			double totalMemory = MapUtils.getLong(value, "totalMemory", 0L);
			double freeMemory = MapUtils.getLong(value, "freeMemory", 0L);
			Float cpuUsedPercentage = MapUtils.getFloat(value, "cpuUsedPercentage", 0f);
			long sentPerSec = MapUtils.getLong(value, "sentPerSec", 0L);
			long recievedPerSec = MapUtils.getLong(value, "recievedPerSec", 0L);

			double memUsage = 0;
			if (totalMemory != 0) {
				memUsage = (((double) (totalMemory - freeMemory)) / totalMemory) * 100;
			}
			DecimalFormat format = new DecimalFormat("#00.0");
			if (cpuUsedPercentage > 99.9f) {
				cpuUsedPercentage = 99.9f;
			}
			if (memUsage > 99.9f) {
				memUsage = 99.9f;
			}
			String perfTString = String.format(" {'agent' : '%s', 'agentFull' : '%s', 'cpu' : '%s',"
							+ " 'mem' : '%s', 'sentPerSec' : '%s', 'recievedPerSec' : '%s'}",
							StringUtils.abbreviate(each.getKey(), 15), each.getKey(), format.format(cpuUsedPercentage),
							format.format(memUsage), UnitUtil.byteCountToDisplaySize(sentPerSec),
							UnitUtil.byteCountToDisplaySize(recievedPerSec));
			perfStringList.add(perfTString);
		}
		return StringUtils.join(perfStringList, ",");
	}

	/**
	 * Get perftest report. This is kept for the compatibility.
	 * 
	 * @param model
	 *            model
	 * @param testId
	 *            test id
	 * @return "perftest/report"
	 */
	@RequestMapping(value = "/report")
	public String getReportRaw(ModelMap model, @RequestParam long testId) {
		return getReport(model, testId);
	}

	/**
	 * Get perftest report.
	 * 
	 * @param model
	 *            model
	 * @param id
	 *            test id
	 * @return "perftest/report"
	 */
	@RequestMapping(value = "{id}/report")
	public String getReport(ModelMap model, @PathVariable("id") long id) {
		model.addAttribute("test", perfTestService.getPerfTest(id));
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

	/**
	 * Get monitor data of agents.
	 * 
	 * @param model
	 *            model
	 * @param id
	 *            test Id
	 * @param monitorIP
	 *            monitorIP
	 * @param imgWidth
	 *            image width
	 * @return json message
	 */
	@RequestMapping("{id}/monitor")
	@ResponseBody
	public String getMonitorData(ModelMap model, @PathVariable("id") long id,
					@RequestParam("monitorIP") String monitorIP, @RequestParam int imgWidth) {
		return toJson(buildMap("SystemData", getMonitorDataSystem(id, monitorIP, imgWidth), JSON_SUCCESS, true));
	}

	private Map<String, String> getMonitorDataSystem(long id, String monitorIP, int imgWidth) {
		int interval = perfTestService.getSystemMonitorDataInterval(id, monitorIP, imgWidth);
		Map<String, String> sysMonitorMap = perfTestService.getSystemMonitorDataAsString(id, monitorIP, interval);
		PerfTest perfTest = perfTestService.getPerfTest(id);
		sysMonitorMap.put(
						"interval",
						String.valueOf(interval
										* (perfTest != null ? perfTest.getSamplingInterval()
														: NGrinderConstants.SAMPLINGINTERVAL_DEFAULT_VALUE)));
		return sysMonitorMap;
	}
}
