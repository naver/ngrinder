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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.grinder.util.LogCompressUtils;
import net.grinder.util.Pair;
import net.grinder.util.UnitUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.ngrinder.agent.service.AgentManagerService;
import org.ngrinder.common.constant.Constants;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.common.controller.RestAPI;
import org.ngrinder.common.util.DateUtils;
import org.ngrinder.common.util.FileDownloadUtils;
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
import org.ngrinder.user.service.UserService;
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
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;

import static org.apache.commons.lang.StringUtils.trimToEmpty;
import static org.ngrinder.common.util.CollectionUtils.*;
import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.Preconditions.*;

/**
 * Performance Test Controller.
 *
 * @author Mavlarn
 * @author JunHo Yoon
 */
@Controller
@RequestMapping("/perftest")
public class PerfTestController extends BaseController {

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

	@Autowired
	private UserService userService;

	private Gson fileEntryGson;

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void init() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(FileEntry.class, new FileEntry.FileEntrySerializer());
		fileEntryGson = gsonBuilder.create();
	}

	/**
	 * Get the perf test lists.
	 *
	 * @param user        user
	 * @param query       query string to search the perf test
	 * @param model       modelMap
	 * @param tag         tag
	 * @param queryFilter "F" means get only finished, "S" means get only scheduled tests.
	 * @param pageable    page
	 * @return perftest/list
	 */
	@RequestMapping({"/list", "/", ""})
	public String getAll(User user, @RequestParam(required = false) String query,
	                     @RequestParam(required = false) String tag, @RequestParam(required = false) String queryFilter,
	                     @PageableDefaults(pageNumber = 0, value = 10) Pageable pageable, ModelMap model) {
		PageRequest pageReq = ((PageRequest) pageable);
		Sort sort = pageReq == null ? null : pageReq.getSort();
		if (sort == null && pageReq != null) {
			sort = new Sort(Direction.DESC, "lastModifiedDate");
			pageable = new PageRequest(pageReq.getPageNumber(), pageReq.getPageSize(), sort);
		}
		Page<PerfTest> testList = perfTestService.getPagedAll(user, query, tag, queryFilter, pageable);

		TimeZone userTZ = TimeZone.getTimeZone(getCurrentUser().getTimeZone());
		Calendar userToday = Calendar.getInstance(userTZ);
		Calendar userYesterday = Calendar.getInstance(userTZ);
		userYesterday.add(Calendar.DATE, -1);

		for (PerfTest test : testList) {
			Calendar localedModified = Calendar.getInstance(userTZ);
			localedModified.setTime(DateUtils.convertToUserDate(getCurrentUser().getTimeZone(),
					test.getLastModifiedDate()));
			if (org.apache.commons.lang.time.DateUtils.isSameDay(userToday, localedModified)) {
				test.setDateString("today");
			} else if (org.apache.commons.lang.time.DateUtils.isSameDay(userYesterday, localedModified)) {
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
			Order sortProp = sort.iterator().next();
			model.addAttribute("sortColumn", sortProp.getProperty());
			model.addAttribute("sortDirection", sortProp.getDirection());
		}
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
	 * @param model model
	 * @param id    perf test id
	 * @return perftest/detail
	 */
	@RequestMapping("/{id}")
	public String getOne(User user, @PathVariable("id") Long id, ModelMap model) {
		PerfTest test = null;
		if (id != null) {
			test = getOneWithPermissionCheck(user, id, true);
		}

		model.addAttribute(PARAM_TEST, test);
		// Retrieve the agent count map based on create user, if the test is
		// created by the others.
		if (test != null) {
			user = test.getCreatedUser();
		}
		Map<String, MutableInt> agentCountMap = agentManagerService.getUserAvailableAgentCountMap(user);
		model.addAttribute(PARAM_REGION_AGENT_COUNT_MAP, agentCountMap);
		model.addAttribute(PARAM_REGION_LIST, getRegions(agentCountMap));
		model.addAttribute(PARAM_PROCESSTHREAD_POLICY_SCRIPT, perfTestService.getProcessAndThreadPolicyScript());
		addDefaultAttributeOnModel(model);
		return "perftest/detail";
	}

	private ArrayList<String> getRegions(Map<String, MutableInt> agentCountMap) {
		ArrayList<String> regions = new ArrayList<String>(agentCountMap.keySet());
		Collections.sort(regions);
		return regions;
	}


	/**
	 * Search tags based on the given query.
	 *
	 * @param user  user to search
	 * @param query query string
	 * @return found tag list in json
	 */
	@RequestMapping("/search_tag")
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
	 * @param model model to which put the default values
	 */
	public void addDefaultAttributeOnModel(ModelMap model) {
		model.addAttribute(PARAM_MAX_VUSER_PER_AGENT, agentManager.getMaxVuserPerAgent());
		model.addAttribute(PARAM_MAX_RUN_COUNT, agentManager.getMaxRunCount());
		model.addAttribute(PARAM_SECURITY_MODE, getConfig().isSecurityEnabled());
		model.addAttribute(PARAM_MAX_RUN_HOUR, agentManager.getMaxRunHour());
		model.addAttribute(PARAM_SAFE_FILE_DISTRIBUTION,
				getConfig().getSystemProperties().getPropertyBoolean(NGRINDER_PROP_DIST_SAFE, false));
		String timeZone = getCurrentUser().getTimeZone();
		int offset;
		if (StringUtils.isNotBlank(timeZone)) {
			offset = TimeZone.getTimeZone(timeZone).getOffset(System.currentTimeMillis());
		} else {
			offset = TimeZone.getDefault().getOffset(System.currentTimeMillis());
		}
		model.addAttribute(PARAM_TIMEZONE_OFFSET, offset);
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
	public String getQuickStart(User user, //
	                            @RequestParam(value = "url", required = true) String urlString, // LF
	                            @RequestParam(value = "scriptType", required = true) String scriptType, // LF
	                            ModelMap model) {
		URL url = checkValidURL(urlString);
		FileEntry newEntry = fileEntryService.prepareNewEntryForQuickTest(user, urlString,
				scriptHandlerFactory.getHandler(scriptType));
		model.addAttribute(PARAM_QUICK_SCRIPT, newEntry.getPath());
		model.addAttribute(PARAM_QUICK_SCRIPT_REVISION, newEntry.getRevision());
		model.addAttribute(PARAM_TEST_NAME, "Test for " + url.getHost());
		model.addAttribute(PARAM_TARGET_HOST, url.getHost());
		Map<String, MutableInt> agentCountMap = agentManagerService.getUserAvailableAgentCountMap(user);
		model.addAttribute(PARAM_REGION_AGENT_COUNT_MAP, agentCountMap);
		model.addAttribute(PARAM_REGION_LIST, getRegions(agentCountMap));
		addDefaultAttributeOnModel(model);
		model.addAttribute(PARAM_PROCESSTHREAD_POLICY_SCRIPT, perfTestService.getProcessAndThreadPolicyScript());
		return "perftest/detail";
	}

	/**
	 * Create a new test or cloneTo a current test.
	 *
	 * @param user    user
	 * @param model   model
	 * @param test    {@link PerfTest}
	 * @param isClone true if cloneTo
	 * @return redirect:/perftest/list
	 */
	@RequestMapping(value = "/new", method = RequestMethod.POST)
	public String saveOne(User user, ModelMap model, PerfTest test,
	                      @RequestParam(value = "isClone", required = false, defaultValue = "false") boolean isClone) {

		test.setTestName(StringUtils.trimToEmpty(test.getTestName()));
		checkNotEmpty(test.getTestName(), "test name should be provided");
		checkArgument(test.getStatus().equals(Status.READY) || test.getStatus().equals(Status.SAVED),
				"save test only support for SAVE or READY status");
		checkArgument(test.getRunCount() == null || test.getRunCount() <= agentManager.getMaxRunCount(),
				"test run count should be equal to or less than %s", agentManager.getMaxRunCount());
		checkArgument(test.getDuration() == null
				|| test.getDuration() <= (((long) agentManager.getMaxRunHour()) * 3600000L),
				"test run duration should be equal to or less than %s", agentManager.getMaxRunHour());
		Map<String, MutableInt> agentCountMap = agentManagerService.getUserAvailableAgentCountMap(user);
		MutableInt agentCountObj = agentCountMap.get(isClustered() ? test.getRegion() : Config.NONE_REGION);
		checkNotNull(agentCountObj, "test region should be within current region list");
		int agentMaxCount = agentCountObj.intValue();
		checkArgument(test.getAgentCount() <= agentMaxCount, "test agent should be equal to or less than %s",
				agentMaxCount);
		checkArgument(test.getVuserPerAgent() == null || test.getVuserPerAgent() <= agentManager.getMaxVuserPerAgent(),
				"test vuser should be equal to or less than %s", agentManager.getMaxVuserPerAgent());
		if (getConfig().isSecurityEnabled()) {
			checkArgument(StringUtils.isNotEmpty(test.getTargetHosts()),
					"test target hosts should be provided when security mode is enabled");
		}
		checkArgument(test.getProcesses() != null && 0 != test.getProcesses(), "test process should not be 0");
		checkArgument(test.getThreads() != null && 0 != test.getThreads(), "test thread should not be 0");
		// Point to the head revision
		test.setScriptRevision(-1L);
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
		// In case that run count is used, sampling ignore count should not be applied.
		if (test.isThresholdRunCount()) {
			test.setIgnoreSampleCount(0);
		}
		perfTestService.save(user, test);
		model.clear();
		return "redirect:/perftest/list";
	}

	/**
	 * Leave the comment on the perf test.
	 *
	 * @param id          testId
	 * @param user        user
	 * @param testComment test comment
	 * @param tagString   tagString
	 * @return JSON
	 */
	@RequestMapping(value = "/{id}/leave_comment", method = RequestMethod.POST)
	@ResponseBody
	public String leaveComment(User user, @PathVariable("id") Long id, @RequestParam("testComment") String testComment,
	                           @RequestParam(value = "tagString", required = false) String tagString) {
		perfTestService.addCommentOn(user, id, testComment, tagString);
		return returnSuccess();
	}


	private Long[] convertString2Long(String ids) {
		String[] numbers = StringUtils.split(ids, ",");
		Long[] id = new Long[numbers.length];
		int i = 0;
		for (String each : numbers) {
			id[i++] = NumberUtils.toLong(each, 0);
		}
		return id;
	}

	private List<Map<String, Object>> getStatus(List<PerfTest> perfTests) {
		List<Map<String, Object>> statuses = newArrayList();
		for (PerfTest each : perfTests) {
			Map<String, Object> result = newHashMap();
			result.put(PARAM_STATUS_UPDATE_ID, each.getId());
			result.put(PARAM_STATUS_UPDATE_STATUS_ID, each.getStatus());
			result.put(PARAM_STATUS_UPDATE_STATUS_TYPE, each.getStatus());
			String errorMessages = getMessages(each.getStatus().getSpringMessageKey());
			result.put(PARAM_STATUS_UPDATE_STATUS_NAME, errorMessages);
			result.put(PARAM_STATUS_UPDATE_STATUS_ICON, each.getStatus().getIconName());
			result.put(
					PARAM_STATUS_UPDATE_STATUS_MESSAGE,
					StringUtils.replace(each.getProgressMessage() + "\n<b>" + each.getLastProgressMessage() + "</b>\n"
							+ each.getLastModifiedDateToStr(), "\n", "<br/>"));
			result.put(PARAM_STATUS_UPDATE_DELETABLE, each.getStatus().isDeletable());
			result.put(PARAM_STATUS_UPDATE_STOPPABLE, each.getStatus().isStoppable());
			statuses.add(result);
		}
		return statuses;
	}


	/**
	 * Delete the perf tests having given IDs.
	 *
	 * @param user user
	 * @param ids  comma operated IDs
	 * @return success json messages if succeeded.
	 */
	@RestAPI
	@RequestMapping(value = "/api/delete", method = RequestMethod.POST)
	public HttpEntity<String> delete(User user, @RequestParam(defaultValue = "") String ids) {
		for (String idStr : StringUtils.split(ids, ",")) {
			perfTestService.delete(user, NumberUtils.toLong(idStr, 0));
		}
		return successJsonHttpEntity();
	}

	/**
	 * Stop the perf tests having given IDs.
	 *
	 * @param user user
	 * @param ids  comma separated perf test IDs
	 * @return success json if succeeded.
	 */
	@RestAPI
	@RequestMapping(value = "/api/stop", method = RequestMethod.POST)
	public HttpEntity<String> stop(User user, @RequestParam(value = "ids", defaultValue = "") String ids) {
		for (String idStr : StringUtils.split(ids, ",")) {
			perfTestService.stop(user, NumberUtils.toLong(idStr, 0));
		}
		return successJsonHttpEntity();
	}

	/**
	 * Filter out please_modify_this.com from hosts string.
	 *
	 * @param originalString original string
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


	private Map<String, Object> getReportGraphStrings(PerfTest perfTest, String[] dataTypes, int interval) {
		Map<String, Object> resultMap = Maps.newHashMap();
		for (String each : dataTypes) {
			Pair<ArrayList<String>, ArrayList<String>> tpsResult = perfTestService.getReportData(perfTest.getId(),
					each, interval);
			Map<String, Object> dataMap = Maps.newHashMap();
			dataMap.put("lables", tpsResult.getFirst());
			dataMap.put("data", tpsResult.getSecond());
			resultMap.put(StringUtils.replaceChars(each, "()", ""), dataMap);
		}
		resultMap.put(PARAM_TEST_CHART_INTERVAL, interval * perfTest.getSamplingInterval());
		return resultMap;
	}

	/**
	 * Get the basic report content in perftest configuration page.
	 *
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
	 * @param response response
	 * @param id       test id
	 */
	@RequestMapping(value = "/{id}/download_csv")
	public void downloadCSV(User user, @PathVariable("id") long id, HttpServletResponse response) {
		PerfTest test = getOneWithPermissionCheck(user, id, false);
		File targetFile = perfTestService.getReportFile(test);
		checkState(targetFile.exists(), "File %s doesn't exist!", targetFile.getName());
		FileDownloadUtils.downloadFile(response, targetFile);
	}

	/**
	 * Download logs for the perf test having the given id.
	 *
	 * @param user     user
	 * @param path     path in the log folder
	 * @param id       test id
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
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(targetFile);
			ServletOutputStream outputStream = response.getOutputStream();
			if (FilenameUtils.isExtension(targetFile.getName(), "zip")) {
				// Limit log view to 1MB
				outputStream.println(" Only the last 1MB of a log shows.\n");
				outputStream.println("==========================================================================\n\n");
				LogCompressUtils.decompress(fileInputStream, outputStream, 1 * 1024 * 1204);
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
	 * Get the running perf test info having the given id.
	 *
	 * @param user  user
	 * @param model model
	 * @param id    test id
	 * @return "perftest/sample"
	 */
	@RequestMapping(value = "/{id}/running/sample")
	public String refreshTestRunning(User user, @PathVariable("id") long id, ModelMap model) {
		PerfTest test = checkNotNull(getOneWithPermissionCheck(user, id, false), "given test should be exist : "
				+ id);
		if (test.getStatus().equals(Status.TESTING)) {
			model.addAttribute(PARAM_RESULT_SUB, perfTestService.getStatistics(test));
			model.addAttribute(PARAM_RESULT_AGENT_PERF, getStatString(perfTestService.getAgentStat(test)));
			model.addAttribute(PARAM_RESULT_MONITOR_PERF, getStatString(perfTestService.getMonitorStat(test)));
		}
		return "perftest/sample";
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
			long receivedPerSec = MapUtils.getLong(value, "receivedPerSec", 0L);

			double memUsage = 0;
			if (totalMemory != 0) {
				memUsage = ((totalMemory - freeMemory) / totalMemory) * 100;
			}
			DecimalFormat format = new DecimalFormat("#00.0");
			if (cpuUsedPercentage > 99.9f) {
				cpuUsedPercentage = 99.9f;
			}
			if (memUsage > 99.9f) {
				memUsage = 99.9f;
			}
			String perfString = String.format(" {'agent' : '%s', 'agentFull' : '%s', 'cpu' : '%s',"
					+ " 'mem' : '%s', 'sentPerSec' : '%s', 'receivedPerSec' : '%s'}",
					StringUtils.abbreviate(each.getKey(), 15), each.getKey(), format.format(cpuUsedPercentage),
					format.format(memUsage), UnitUtils.byteCountToDisplaySize(sentPerSec),
					UnitUtils.byteCountToDisplaySize(receivedPerSec));
			perfStringList.add(perfString);
		}
		return StringUtils.join(perfStringList, ",");
	}

	/**
	 * Get the detailed perf test report. This is kept for the compatibility.
	 *
	 * @param model  model
	 * @param testId perf test id
	 * @return perftest/detail_report
	 * @deprecated
	 */
	@RequestMapping(value = "/detail_report")
	public String getRawReport(ModelMap model, @RequestParam long testId) {
		return getReport(model, testId);
	}

	/**
	 * Get the detailed perf test report.
	 *
	 * @param model model
	 * @param id    test id
	 * @return perftest/detail_report
	 */
	@RequestMapping(value = {"/{id}/detail_report", /** for backward compatibility */"/{id}/report"})
	public String getReport(ModelMap model, @PathVariable("id") long id) {
		model.addAttribute("test", perfTestService.getOne(id));
		return "perftest/detail_report";
	}

	private PerfTest getOneWithPermissionCheck(User user, Long id, boolean withTag) {
		PerfTest perfTest = withTag ? perfTestService.getOneWithTag(id) : perfTestService.getOne(id);
		if (user.getRole().equals(Role.ADMIN) || user.getRole().equals(Role.SUPER_USER)) {
			return perfTest;
		}
		if (perfTest != null && !user.equals(perfTest.getCreatedUser())) {
			throw processException("User " + user.getUserId() + " has no right on PerfTest ");
		}
		return perfTest;
	}


	private Map<String, String> getMonitorData(long id, String targetIP, int imgWidth) {
		int interval = perfTestService.getSystemMonitorDataInterval(id, targetIP, imgWidth);
		Map<String, String> sysMonitorMap = perfTestService.getSystemMonitorDataAsString(id, targetIP, interval);
		PerfTest perfTest = perfTestService.getOne(id);
		sysMonitorMap.put(
				"interval",
				String.valueOf(interval
						* (perfTest != null ? perfTest.getSamplingInterval()
						: Constants.SAMPLINGINTERVAL_DEFAULT_VALUE)));
		return sysMonitorMap;
	}

	/**
	 * Get the count of currently running perf test and the detailed progress info for the given perf test IDs.
	 *
	 * @param user user
	 * @param ids  comma separated perf test list
	 * @return JSON message containing perf test status
	 */
	@RestAPI
	@RequestMapping("/api/status")
	public HttpEntity<String> getStatuses(User user, @RequestParam(value = "ids", defaultValue = "") String ids) {
		List<PerfTest> perfTests = perfTestService.getOne(user, convertString2Long(ids));
		return toJsonHttpEntity(buildMap("perfTestInfo", perfTestService.getCurrentPerfTestStatistics(), "status",
				getStatus(perfTests)));
	}

	/**
	 * Get all available scripts in JSON format for the current factual user.
	 *
	 * @param user    user
	 * @param ownerId owner id
	 * @return JSON containing script's list.
	 */
	@RestAPI
	@RequestMapping("/api/script")
	public HttpEntity<String> getScripts(User user, @RequestParam(value = "ownerId", required = false) String ownerId) {
		if (StringUtils.isNotEmpty(ownerId)) {
			user = userService.getOne(ownerId);
		}
		List<FileEntry> allFileEntries = fileEntryService.getAll(user);
		CollectionUtils.filter(allFileEntries, new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				return ((FileEntry) object).getFileType().getFileCategory() == FileCategory.SCRIPT;
			}
		});
		return toJsonHttpEntity(allFileEntries, fileEntryGson);
	}


	/**
	 * Get resources and lib file list from the same folder with the given script path.
	 *
	 * @param user       user
	 * @param scriptPath script path
	 * @param revision   revision
	 * @param ownerId    ownerId
	 * @return json string representing resources and libs.
	 */
	@RequestMapping("/api/resource")
	public HttpEntity<String> getResources(User user, @RequestParam String scriptPath,
	                                       @RequestParam(required = false) String ownerId) {
		if (user.getRole() == Role.ADMIN && StringUtils.isNotBlank(ownerId)) {
			user = userService.getOne(ownerId);
		}
		FileEntry fileEntry = fileEntryService.getOne(user, scriptPath);
		String targetHosts = "";
		List<String> fileStringList = newArrayList();
		if (fileEntry != null) {
			List<FileEntry> fileList = fileEntryService.getScriptHandler(fileEntry).getLibAndResourceEntries(user, fileEntry, -1L);
			for (FileEntry each : fileList) {
				fileStringList.add(each.getPath());
			}
			targetHosts = filterHostString(fileEntry.getProperties().get("targetHosts"));
		}

		return toJsonHttpEntity(buildMap("targetHosts", trimToEmpty(targetHosts), "resources", fileStringList));
	}


	/**
	 * Get the status of the given perf test.
	 *
	 * @param user user
	 * @param id   perftest id
	 * @return JSON message containing perf test status
	 */
	@RestAPI
	@RequestMapping("/api/{id}/status")
	public HttpEntity<String> getStatus(User user, @PathVariable("id") Long id) {
		List<PerfTest> perfTests = perfTestService.getOne(user, new Long[]{id});
		return toJsonHttpEntity(buildMap("status", getStatus(perfTests)));
	}

	/**
	 * Get the detailed report graph data for the given perf test id.
	 *
	 * This method returns the appropriate points based on the given imgWidth.
	 *
	 * @param id       test id
	 * @param dataType which data
	 * @param imgWidth imageWidth
	 * @return json string.
	 */
	@RestAPI
	@RequestMapping("/api/{id}/graph")
	public HttpEntity<String> getGraph(@PathVariable("id") long id,
	                                   @RequestParam(required = true, defaultValue = "") String dataType, @RequestParam int imgWidth) {
		String[] dataTypes = StringUtils.split(dataType, ",");
		if (dataTypes.length <= 0) {
			return errorJsonHttpEntity();
		}
		int interval = perfTestService.getReportDataInterval(id, dataTypes[0], imgWidth);
		return toJsonHttpEntity(getReportGraphStrings(perfTestService.getOne(id), dataTypes, interval));
	}

	/**
	 * Get the monitor data of the target having the given IP.
	 *
	 * @param id       test Id
	 * @param targetIP targetIP
	 * @param imgWidth image width
	 * @return json message
	 */
	@RestAPI
	@RequestMapping("/api/{id}/monitor")
	public HttpEntity<String> getMonitor(@PathVariable("id") long id,
	                                     @RequestParam("targetIP") String targetIP, @RequestParam int imgWidth) {
		return toJsonHttpEntity(getMonitorData(id, targetIP, imgWidth));
	}

	/**
	 * Get the last perf test details in the form of json.
	 *
	 * @param user user
	 * @param size size of retrieved perf test
	 * @return json string
	 */
	@RestAPI
	@RequestMapping(value = {"/api/last", "/api", "/api/"}, method = RequestMethod.GET)
	public HttpEntity<String> getAll(User user, @RequestParam(value = "page", defaultValue = "0") int page,
	                                 @RequestParam(value = "size", defaultValue = "1") int size) {
		PageRequest pageRequest = new PageRequest(page, size, new Sort(Direction.DESC, "id"));
		Page<PerfTest> testList = perfTestService.getPagedAll(user, null, null, null, pageRequest);
		return toJsonHttpEntity(testList.getContent());
	}

	/**
	 * Get the perf test detail in the form of json.
	 *
	 * @param user user
	 * @param id   perftest id
	 * @return json success message if succeeded
	 */
	@RestAPI
	@RequestMapping(value = "/api/{id}", method = RequestMethod.GET)
	public HttpEntity<String> getOne(User user, @PathVariable("id") Long id) {
		PerfTest test = checkNotNull(getOneWithPermissionCheck(user, id, false), "PerfTest %s does not exists", id);
		return toJsonHttpEntity(test);
	}

	/**
	 * Create the given perf test.
	 *
	 * @param user     user
	 * @param perftest perf test
	 * @return json success message if succeeded
	 */
	@RestAPI
	@RequestMapping(value = {"/api/", "/api"}, method = RequestMethod.POST)
	public HttpEntity<String> create(User user, PerfTest perftest) {
		checkNull(perftest.getId(), "id should be null");
		PerfTest savePerfTest = perfTestService.save(user, perftest);
		return toJsonHttpEntity(savePerfTest);
	}

	/**
	 * Delete the given perf test.
	 *
	 * @param user user
	 * @param id   perf test id
	 * @return json success message if succeeded
	 */
	@RestAPI
	@RequestMapping(value = "/api/{id}", method = RequestMethod.DELETE)
	public HttpEntity<String> delete(User user, @PathVariable("id") Long id) {
		PerfTest perfTest = getOneWithPermissionCheck(user, id, false);
		checkNotNull(perfTest, "no perftest for %s exits", id);
		perfTestService.delete(user, id);
		return successJsonHttpEntity();
	}


	/**
	 * Update the given perf test.
	 *
	 * @param user     user
	 * @param id       perf test id
	 * @param perfTest perf test configuration changes
	 * @return json success message if succeeded
	 */
	@RestAPI
	@RequestMapping(value = "/api/{id}", method = RequestMethod.PUT)
	public HttpEntity<String> update(User user, @PathVariable("id") Long id, PerfTest perfTest) {
		perfTest.setId(id);
		return toJsonHttpEntity(perfTestService.save(user, perfTest));
	}

	/**
	 * Stop the given perf test.
	 *
	 * @param user user
	 * @param id   perf test id
	 * @return json success message if succeeded
	 */
	@RestAPI
	@RequestMapping(value = "/api/{id}", params = "action=stop", method = RequestMethod.PUT)
	public HttpEntity<String> stop(User user, @PathVariable("id") Long id) {
		perfTestService.stop(user, id);
		return successJsonHttpEntity();
	}


	/**
	 * Update the given perf test's status.
	 *
	 * @param user   user
	 * @param id     perf test id
	 * @param status Status to be moved to
	 * @return json success message if succeeded
	 */
	@RestAPI
	@RequestMapping(value = "/api/{id}", params = "action=status", method = RequestMethod.PUT)
	public HttpEntity<String> updateStatus(User user, @PathVariable("id") Long id, Status status) {
		PerfTest perfTest = getOneWithPermissionCheck(user, id, false);
		checkNotNull(perfTest, "no perftest for %s exits", id).setStatus(status);
		return toJsonHttpEntity(perfTestService.save(user, perfTest));
	}

	/**
	 * Clone and start the given perf test.
	 *
	 * @param user     user
	 * @param id       perf test id to be cloned
	 * @param perftest option to override while cloning.
	 * @return json string
	 */
	@RestAPI
	@RequestMapping(value = {"/api/{id}/clone_and_start", /* for backward compatibility */ "/api/{id}/cloneAndStart"})
	public HttpEntity<String> cloneAndStart(User user, @PathVariable("id") Long id, PerfTest perftest) {
		PerfTest test = getOneWithPermissionCheck(user, id, false);
		checkNotNull(test, "no perftest for %s exits", id);
		PerfTest newOne = test.cloneTo(new PerfTest());
		newOne.setStatus(Status.READY);
		if (perftest != null) {
			if (perftest.getScheduledTime() != null) {
				newOne.setScheduledTime(perftest.getScheduledTime());
			}
			if (perftest.getScriptRevision() != null) {
				newOne.setScriptRevision(perftest.getScriptRevision());
			}

			if (perftest.getAgentCount() != null) {
				newOne.setAgentCount(perftest.getAgentCount());
			}
		}
		if (newOne.getAgentCount() == null) {
			newOne.setAgentCount(0);
		}
		Map<String, MutableInt> agentCountMap = agentManagerService.getUserAvailableAgentCountMap(user);
		MutableInt agentCountObj = agentCountMap.get(isClustered() ? test.getRegion() : Config.NONE_REGION);
		checkNotNull(agentCountObj, "test region should be within current region list");
		int agentMaxCount = agentCountObj.intValue();
		checkArgument(newOne.getAgentCount() != 0, "test agent should not be %s", agentMaxCount);
		checkArgument(newOne.getAgentCount() <= agentMaxCount, "test agent should be equal to or less than %s",
				agentMaxCount);
		PerfTest savePerfTest = perfTestService.save(user, newOne);
		CoreLogger.LOGGER.info("test {} is created through web api by {}", savePerfTest.getId(), user.getUserId());
		return toJsonHttpEntity(savePerfTest);
	}
}
