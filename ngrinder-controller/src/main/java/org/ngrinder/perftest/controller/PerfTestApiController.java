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

import net.grinder.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.ngrinder.agent.service.AgentManagerService;
import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.common.constants.GrinderConstants;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.common.util.DateUtils;
import org.ngrinder.common.util.JsonUtils;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.hazelcast.HazelcastService;
import org.ngrinder.infra.logger.CoreLogger;
import org.ngrinder.model.*;
import org.ngrinder.perftest.model.SamplingModel;
import org.ngrinder.perftest.service.AgentManager;
import org.ngrinder.perftest.service.PerfTestService;
import org.ngrinder.perftest.service.TagService;
import org.ngrinder.region.service.RegionService;
import org.ngrinder.script.handler.ScriptHandlerFactory;
import org.ngrinder.script.model.FileCategory;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.user.service.UserService;
import org.python.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang.StringUtils.trimToEmpty;
import static org.ngrinder.common.constant.CacheConstants.DIST_MAP_NAME_MONITORING;
import static org.ngrinder.common.constant.CacheConstants.DIST_MAP_NAME_SAMPLING;
import static org.ngrinder.common.util.CollectionUtils.buildMap;
import static org.ngrinder.common.util.CollectionUtils.newHashMap;
import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.Preconditions.*;
import static org.ngrinder.common.util.TypeConvertUtils.cast;

/**
 * Performance Test api Controller.
 *
 * @since 3.5.0
 */
@Profile("production")
@RestController
@RequestMapping("/perftest/api")
public class PerfTestApiController extends BaseController {

	@Autowired
	private PerfTestService perfTestService;

	@Autowired
	private TagService tagService;

	@Autowired
	private AgentManager agentManager;

	@Autowired
	private RegionService regionService;

	@Autowired
	private AgentManagerService agentManagerService;

	@Autowired
	private FileEntryService fileEntryService;

	@Autowired
	private UserService userService;

	@Autowired
	private HazelcastService hazelcastService;

	@Autowired
	private ScriptHandlerFactory scriptHandlerFactory;

	/**
	 * Get the perf test lists.
	 *
	 * @param user        user
	 * @param query       query string to search the perf test
	 * @param tag         tag
	 * @param queryFilter "F" means get only finished, "S" means get only scheduled tests.
	 * @param pageable    page
	 * @return perftest/list
	 */
	@GetMapping("/list")
	public Map<String, Object> getAllList(User user,
										  @RequestParam(required = false) String query,
										  @RequestParam(required = false) String tag,
										  @RequestParam(required = false) String queryFilter,
										  @PageableDefault Pageable pageable) {
		Map<String, Object> result = new HashMap<>();
		Pair<Page<PerfTest>, Pageable> pair = getPerfTests(user, query, tag, queryFilter, pageable);
		Page<PerfTest> tests = pair.getFirst();
		pageable = pair.getSecond();
		annotateDateMarker(tests);
		result.put("tag", tag);
		result.put("totalElements", tests.getTotalElements());
		result.put("number", tests.getNumber());
		result.put("size", tests.getSize());
		result.put("queryFilter", queryFilter);
		result.put("query", query);
		result.put("createdUserId", user.getUserId());
		result.put("tests", tests.getContent());
		putPageIntoModelMap(result, pageable);
		return result;
	}

	/**
	 * Delete the perf tests having given IDs.
	 *
	 * @param user user
	 * @param ids  comma operated IDs
	 * @return success json messages if succeeded.
	 */
	@DeleteMapping("/")
	public void delete(User user, @RequestParam(defaultValue = "") String ids) {
		for (String idStr : StringUtils.split(ids, ",")) {
			perfTestService.delete(user, NumberUtils.toLong(idStr, 0));
		}
	}

	/**
	 * Stop the perf tests having given IDs.
	 *
	 * @param user user
	 * @param ids  comma separated perf test IDs
	 * @return success json if succeeded.
	 */
	@PutMapping(value="/", params = "action=stop")
	public void stop(User user, @RequestParam(defaultValue = "") String ids) {
		for (String idStr : StringUtils.split(ids, ",")) {
			perfTestService.stop(user, NumberUtils.toLong(idStr, 0));
		}
	}

	/**
	 * Search tags based on the given query.
	 *
	 * @param user  user to search
	 * @param query query string
	 * @return found tag list in json
	 */
	@GetMapping("/search_tag")
	public List<String> searchTag(User user, @RequestParam(required = false) String query) {
		List<String> allStrings = tagService.getAllTagStrings(user, query);
		if (StringUtils.isNotBlank(query)) {
			allStrings.add(query);
		}
		return allStrings;
	}

	/**
	 * Get resources and lib file list from the same folder with the given script path.
	 *
	 * @param user       user
	 * @param scriptPath script path
	 * @param ownerId    ownerId
	 * @return json string representing resources and libs.
	 */
	@GetMapping("/resource")
	public Map<String, Object> getResources(User user, @RequestParam String scriptPath, @RequestParam(required = false) String ownerId) {
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

		return buildMap("targetHosts", trimToEmpty(targetHosts), "resources", fileStringList);
	}

	/**
	 * Filter out please_modify_this.com from hosts string.
	 *
	 * @param originalString original string
	 * @return filtered string
	 */
	private String filterHostString(String originalString) {
		List<String> hosts = newArrayList();
		for (String each : StringUtils.split(StringUtils.trimToEmpty(originalString), ",")) {
			if (!each.contains("please_modify_this.com")) {
				hosts.add(each);
			}
		}
		return StringUtils.join(hosts, ",");
	}

	/**
	 * Get the basic report content in perftest configuration page.
	 * <p/>
	 * This method returns the appropriate points based on the given imgWidth.
	 *
	 * @param user     user
	 * @param id       test id
	 * @param imgWidth image width
	 */
	@GetMapping("/{id}/basic_report")
	public Map<String, Object> getReportSection(User user, @PathVariable long id, @RequestParam int imgWidth) {
		Map<String, Object> model = new HashMap<>();
		PerfTest test = getOneWithPermissionCheck(user, id, false);
		int interval = perfTestService.getReportDataInterval(id, "TPS", imgWidth);
		model.put(PARAM_LOG_LIST, perfTestService.getLogFiles(id));
		model.put(PARAM_TEST_CHART_INTERVAL, interval * test.getSamplingInterval());
		model.put(PARAM_TEST, test);
		model.put(PARAM_TPS, perfTestService.getSingleReportDataAsJson(id, "TPS", interval));
		return model;
	}

	/**
	 * Create a new test or cloneTo a current test.
	 *
	 * @param user     user
	 * @param perfTest {@link PerfTest}
	 * @param isClone  true if cloneTo
	 * @return saved perftest
	 */
	@PostMapping("/save")
	public PerfTest saveOne(User user, PerfTest perfTest, @RequestParam(defaultValue = "false") boolean isClone) {
		validate(user, null, perfTest);

		// Point to the head revision
		perfTest.setTestName(StringUtils.trimToEmpty(perfTest.getTestName()));
		perfTest.setScriptRevision(-1L);
		perfTest.prepare(isClone);
		perfTest = perfTestService.save(user, perfTest);

		return perfTest;
	}

	/**
	 * Get the perf test detail on the given perf test id.
	 *
	 * @param user  user
	 * @param id    perf test id
	 * @return attributes for perftest detail
	 */
	@GetMapping({"/{id}/detail", "/create"})
	public Map<String, Object> getOneDetail(User user, @PathVariable(required = false) Long id) {
		Map<String, Object> model = new HashMap<>();
		PerfTest test = null;
		if (id != null) {
			test = getOneWithPermissionCheck(user, id, true);
		}

		if (test == null) {
			test = new PerfTest(user);
			test.init();
		}

		model.put(PARAM_TEST, test);
		// Retrieve the agent count map based on create user, if the test is
		// created by the others.
		user = test.getCreatedUser() != null ? test.getCreatedUser() : user;

		Map<String, MutableInt> agentCountMap = agentManagerService.getAvailableAgentCountMap(user);
		model.put(PARAM_REGION_AGENT_COUNT_MAP, agentCountMap);
		model.put(PARAM_REGION_LIST, regionService.getAllVisibleRegionNames());
		addDefaultAttributeOnModel(model);
		return model;
	}

	/**
	 * Get the running perf test info having the given id.
	 *
	 * @param user user
	 * @param id   test id
	 * @return JSON message	containing test,agent and monitor status.
	 */
	@GetMapping("/{id}/sample")
	public Map<String, Object> refreshTestRunning(User user, @PathVariable long id) {
		PerfTest test = checkNotNull(getOneWithPermissionCheck(user, id, false), "given test should be exist : " + id);
		Map<String, Object> map = newHashMap();

		SamplingModel samplingModel = hazelcastService.get(DIST_MAP_NAME_SAMPLING, test.getId());
		if (samplingModel != null) {
			map.put("perf", JsonUtils.deserialize(samplingModel.getRunningSample(), HashMap.class));
			map.put("agent", JsonUtils.deserialize(samplingModel.getAgentState(), HashMap.class));
		}

		String monitoringJson = hazelcastService.get(DIST_MAP_NAME_MONITORING, test.getId());
		if (monitoringJson != null) {
			map.put("monitor", JsonUtils.deserialize(monitoringJson, HashMap.class));
		}

		map.put("status", test.getStatus());
		return map;
	}

	@GetMapping("/{id}/detail_report")
	public Map<String, Object> getReport(@PathVariable long id) {
		Map<String, Object> model = newHashMap();
		model.put("test", perfTestService.getOne(id));
		model.put("plugins", perfTestService.getAvailableReportPlugins(id));
		return model;
	}

	/**
	 * Leave the comment on the perf test.
	 *
	 * @param id          testId
	 * @param user        user
	 * @param params	  {testComment, tagString}
	 * @return JSON
	 */
	@PostMapping("/{id}/leave_comment")
	public void leaveComment(User user, @PathVariable Long id, @RequestBody Map<String, Object> params) {
		perfTestService.addCommentOn(user, id, cast(params.get("testComment")), cast(params.get("tagString")));
	}

	/**
	 * Get the perf test creation form for quickStart.
	 */
	@PostMapping("/quickstart")
	public Map<String, Object> getQuickStart(User user, @RequestBody Map<String, Object> params) {
		String urlString = cast(params.get("url"));
		String scriptType = cast(params.get("scriptType"));

		URL url = checkValidURL(urlString);
		FileEntry newEntry = fileEntryService.prepareNewEntryForQuickTest(user, urlString, scriptHandlerFactory.getHandler(scriptType));

		Map<String, Object> model = new HashMap<>();
		PerfTest perfTest = createPerfTestFromQuickStart(user, "Test for " + url.getHost(), url.getHost());
		perfTest.setScriptName(newEntry.getPath());
		perfTest.setScriptRevision(newEntry.getRevision());
		model.put(PARAM_TEST, perfTest);
		Map<String, MutableInt> agentCountMap = agentManagerService.getAvailableAgentCountMap(user);
		model.put(PARAM_REGION_AGENT_COUNT_MAP, agentCountMap);
		model.put(PARAM_REGION_LIST, getRegions(agentCountMap));
		addDefaultAttributeOnModel(model);

		return model;
	}

	/**
	 * Create a new test from quick start mode.
	 *
	 * @param user       user
	 * @param testName   test name
	 * @param targetHost target host
	 * @return test    {@link PerfTest}
	 */
	private PerfTest createPerfTestFromQuickStart(User user, String testName, String targetHost) {
		PerfTest test = new PerfTest(user);
		test.init();
		test.setTestName(testName);
		test.setTargetHosts(targetHost);
		return test;
	}

	private PerfTest getOneWithPermissionCheck(User user, Long id, boolean withTag) {
		PerfTest perfTest = withTag ? perfTestService.getOneWithTag(id) : perfTestService.getOne(id);
		if (user.getRole().equals(Role.ADMIN) || user.getRole().equals(Role.SUPER_USER)) {
			return perfTest;
		}
		if (perfTest != null && !user.equals(perfTest.getCreatedUser())) {
			throw processException("User " + user.getUserId() + " has no right on PerfTest " + id);
		}
		return perfTest;
	}

	private ArrayList<String> getRegions(Map<String, MutableInt> agentCountMap) {
		ArrayList<String> regions = new ArrayList<>(agentCountMap.keySet());
		Collections.sort(regions);
		return regions;
	}

	private Pair<Page<PerfTest>, Pageable> getPerfTests(User user, String query, String tag, String queryFilter, Pageable pageableParam) {
		Pageable pageable = PageRequest.of(pageableParam.getPageNumber(), pageableParam.getPageSize(),
			pageableParam.getSort().isUnsorted() ? new Sort(Direction.DESC, "id") : pageableParam.getSort());
		Page<PerfTest> tests = perfTestService.getPagedAll(user, query, tag, queryFilter, pageable);
		if (tests.getNumberOfElements() == 0) {
			pageable = PageRequest.of(0, pageableParam.getPageSize(),
				pageableParam.getSort().isUnsorted() ? new Sort(Direction.DESC, "id") : pageableParam.getSort());
			tests = perfTestService.getPagedAll(user, query, tag, queryFilter, pageableParam);
		}
		return Pair.of(tests, pageable);
	}

	/**
	 * Add the various default configuration values on the model.
	 *
	 * @param model model to which put the default values
	 */
	private void addDefaultAttributeOnModel(Map<String, Object> model) {
		model.put(PARAM_AVAILABLE_RAMP_UP_TYPE, RampUp.values());
		model.put(PARAM_MAX_VUSER_PER_AGENT, agentManager.getMaxVuserPerAgent());
		model.put(PARAM_MAX_RUN_COUNT, agentManager.getMaxRunCount());
		if (getConfig().isSecurityEnabled()) {
			model.put(PARAM_SECURITY_LEVEL, getConfig().getSecurityLevel());
		}
		model.put(PARAM_MAX_RUN_HOUR, agentManager.getMaxRunHour());
		model.put(PARAM_SAFE_FILE_DISTRIBUTION,
			getConfig().getControllerProperties().getPropertyBoolean(ControllerConstants.PROP_CONTROLLER_SAFE_DIST));
		String timeZone = getCurrentUser().getTimeZone();
		int offset;
		if (StringUtils.isNotBlank(timeZone)) {
			offset = TimeZone.getTimeZone(timeZone).getOffset(System.currentTimeMillis());
		} else {
			offset = TimeZone.getDefault().getOffset(System.currentTimeMillis());
		}
		model.put(PARAM_TIMEZONE_OFFSET, offset);
	}

	private void annotateDateMarker(Page<PerfTest> tests) {
		TimeZone userTZ = TimeZone.getTimeZone(getCurrentUser().getTimeZone());
		Calendar userToday = Calendar.getInstance(userTZ);
		Calendar userYesterday = Calendar.getInstance(userTZ);
		userYesterday.add(Calendar.DATE, -1);
		for (PerfTest test : tests) {
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
	}

	private List<Map<String, Object>> getStatus(List<PerfTest> perfTests) {
		List<Map<String, Object>> statuses = newArrayList();
		for (PerfTest each : perfTests) {
			Map<String, Object> result = newHashMap();
			result.put("id", each.getId());
			result.put("status_id", each.getStatus());
			result.put("status_type", each.getStatus().getCategory());
			result.put("name", getMessages(each.getStatus().getSpringMessageKey()));
			result.put("icon", each.getStatus().getIconName());
			result.put("message",
				StringUtils.replace(each.getProgressMessage() + "\n<b>" + each.getLastProgressMessage() + "</b>\n"
					+ each.getLastModifiedDateToStr(), "\n", "<br/>"));
			result.put("deletable", each.getStatus().isDeletable());
			result.put("stoppable", each.getStatus().isStoppable());
			result.put("reportable", each.getStatus().isReportable());
			statuses.add(result);
		}
		return statuses;
	}

	@SuppressWarnings("ConstantConditions")
	private void validate(User user, PerfTest oldOne, PerfTest newOne) {
		if (oldOne == null) {
			oldOne = new PerfTest();
			oldOne.init();
		}
		newOne = oldOne.merge(newOne);
		checkNotEmpty(newOne.getTestName(), "testName should be provided");
		checkArgument(newOne.getStatus().equals(Status.READY) || newOne.getStatus().equals(Status.SAVED),
			"status only allows SAVE or READY");
		if (newOne.isThresholdRunCount()) {
			final Integer runCount = newOne.getRunCount();
			checkArgument(runCount > 0 && runCount <= agentManager
					.getMaxRunCount(),
				"runCount should be equal to or less than %s", agentManager.getMaxRunCount());
		} else {
			final Long duration = newOne.getDuration();
			checkArgument(duration > 0 && duration <= (((long) agentManager.getMaxRunHour()) *
					3600000L),
				"duration should be equal to or less than %s", agentManager.getMaxRunHour());
		}
		Map<String, MutableInt> agentCountMap = agentManagerService.getAvailableAgentCountMap(user);
		MutableInt agentCountObj = agentCountMap.get(isClustered() ? newOne.getRegion() : Config.NONE_REGION);
		checkNotNull(agentCountObj, "region should be within current region list");
		int agentMaxCount = agentCountObj.intValue();
		checkArgument(newOne.getAgentCount() <= agentMaxCount, "test agent should be equal to or less than %s",
			agentMaxCount);
		if (newOne.getStatus().equals(Status.READY)) {
			checkArgument(newOne.getAgentCount() >= 1, "agentCount should be more than 1 when it's READY status.");
		}

		checkArgument(newOne.getVuserPerAgent() <= agentManager.getMaxVuserPerAgent(),
			"vuserPerAgent should be equal to or less than %s", agentManager.getMaxVuserPerAgent());
		if (getConfig().isSecurityEnabled() && GrinderConstants.GRINDER_SECURITY_LEVEL_NORMAL.equals(getConfig().getSecurityLevel())) {
			checkArgument(StringUtils.isNotEmpty(newOne.getTargetHosts()),
				"targetHosts should be provided when security mode is enabled");
		}
		if (newOne.getStatus() != Status.SAVED) {
			checkArgument(StringUtils.isNotBlank(newOne.getScriptName()), "scriptName should be provided.");
		}
		checkArgument(newOne.getVuserPerAgent() == newOne.getProcesses() * newOne.getThreads(),
			"vuserPerAgent should be equal to (processes * threads)");
	}

	/**
	 * Get the count of currently running perf test and the detailed progress info for the given perf test IDs.
	 *
	 * @param user user
	 * @param ids  comma separated perf test list
	 * @return JSON message containing perf test status
	 */
	@GetMapping("/status")
	public Map<String, Object> getStatuses(User user, @RequestParam(defaultValue = "") String ids) {
		List<PerfTest> perfTests = perfTestService.getAll(user, convertString2Long(ids));
		return buildMap("perfTestInfo", perfTestService.getCurrentPerfTestStatistics(),
			"status", getStatus(perfTests));
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

	/**
	 * Get all available scripts in JSON format for the current factual user.
	 *
	 * @param user    user
	 * @param ownerId owner id
	 * @return JSON containing script's list.
	 */
	@GetMapping("/script")
	public List<FileEntry> getScripts(User user, @RequestParam(required = false) String ownerId) {
		if (StringUtils.isNotEmpty(ownerId)) {
			user = userService.getOne(ownerId);
		}
		return fileEntryService.getAll(user)
			.stream()
			.filter(input -> input != null && input.getFileType().getFileCategory() == FileCategory.SCRIPT)
			.collect(Collectors.toList());
	}

	/**
	 * Get the status of the given perf test.
	 *
	 * @param user user
	 * @param id   perftest id
	 * @return JSON message containing perf test status
	 */
	@GetMapping("/{id}/status")
	public Map<String, Object> getStatus(User user, @PathVariable Long id) {
		List<PerfTest> perfTests = perfTestService.getAll(user, new Long[]{id});
		return buildMap("status", getStatus(perfTests));
	}

	/**
	 * Get the logs of the given perf test.
	 *
	 * @param user user
	 * @param id   perftest id
	 * @return log file names
	 */
	@GetMapping("/{id}/logs")
	public List<String> getLogs(User user, @PathVariable long id) {
		getOneWithPermissionCheck(user, id, false);
		return perfTestService.getLogFiles(id);
	}

	/**
	 * Get the detailed report graph data for the given perf test id.
	 * This method returns the appropriate points based on the given imgWidth.
	 *
	 * @param id       test id
	 * @param dataType which data
	 * @param imgWidth imageWidth
	 * @return json string.
	 */
	@GetMapping({"/{id}/perf", "/{id}/graph"})
	public Map<String, Object> getPerfGraph(@PathVariable long id,
											@RequestParam(defaultValue = "") String dataType,
											@RequestParam(defaultValue = "false") boolean onlyTotal,
											@RequestParam int imgWidth) {
		String[] dataTypes = checkNotEmpty(StringUtils.split(dataType, ","), "dataType argument should be provided");
		return getPerfGraphData(id, dataTypes, onlyTotal, imgWidth);
	}

	private Map<String, Object> getPerfGraphData(Long id, String[] dataTypes, boolean onlyTotal, int imgWidth) {
		final PerfTest test = perfTestService.getOne(id);
		int interval = perfTestService.getReportDataInterval(id, dataTypes[0], imgWidth);
		Map<String, Object> resultMap = Maps.newHashMap();
		for (String each : dataTypes) {
			Pair<ArrayList<String>, ArrayList<String>> tpsResult = perfTestService.getReportData(id, each, onlyTotal, interval);
			Map<String, Object> dataMap = Maps.newHashMap();
			dataMap.put("labels", tpsResult.getFirst());
			dataMap.put("data", tpsResult.getSecond());
			resultMap.put(StringUtils.replaceChars(each, "()", ""), dataMap);
		}
		resultMap.put(PARAM_TEST_CHART_INTERVAL, interval * test.getSamplingInterval());
		return resultMap;
	}

	/**
	 * Get the monitor data of the target having the given IP.
	 *
	 * @param id       test Id
	 * @param targetIP targetIP
	 * @param imgWidth image width
	 * @return json message
	 */
	@GetMapping("/{id}/monitor")
	public Map<String, String> getMonitorGraph(@PathVariable long id,
											   @RequestParam String targetIP, @RequestParam int imgWidth) {
		return getMonitorGraphData(id, targetIP, imgWidth);
	}

	private Map<String, String> getMonitorGraphData(long id, String targetIP, int imgWidth) {
		int interval = perfTestService.getMonitorGraphInterval(id, targetIP, imgWidth);
		Map<String, String> sysMonitorMap = perfTestService.getMonitorGraph(id, targetIP, interval);
		PerfTest perfTest = perfTestService.getOne(id);
		sysMonitorMap.put("interval", String.valueOf(interval * (perfTest != null ? perfTest.getSamplingInterval() : 1)));
		return sysMonitorMap;
	}

	/**
	 * Get the plugin monitor data of the target.
	 *
	 * @param id       test Id
	 * @param plugin   monitor plugin category
	 * @param kind     kind
	 * @param imgWidth image width
	 * @return json message
	 */
	@GetMapping("/{id}/plugin/{plugin}")
	public Map<String, Object> getPluginGraph(@PathVariable("id") long id,
											 @PathVariable("plugin") String plugin,
											 @RequestParam("kind") String kind, @RequestParam int imgWidth) {
		return getReportPluginGraphData(id, plugin, kind, imgWidth);
	}

	private Map<String, Object> getReportPluginGraphData(long id, String plugin, String kind, int imgWidth) {
		int interval = perfTestService.getReportPluginGraphInterval(id, plugin, kind, imgWidth);
		Map<String, Object> pluginMonitorData = perfTestService.getReportPluginGraph(id, plugin, kind, interval);
		final PerfTest perfTest = perfTestService.getOne(id);
		int samplingInterval = 3;
		if (perfTest != null) {
			samplingInterval = perfTest.getSamplingInterval();
		}
		pluginMonitorData.put("interval", interval * samplingInterval);
		return pluginMonitorData;
	}


	/**
	 * Get the last perf test details in the form of json.
	 *
	 * @param user user
	 * @param page page
	 * @param size size of retrieved perf test
	 * @return json string
	 */
	@GetMapping({"/last", "", "/"})
	public List<PerfTest> getAll(User user, @RequestParam(value = "page", defaultValue = "0") int page,
									 @RequestParam(value = "size", defaultValue = "1") int size) {
		PageRequest pageRequest = PageRequest.of(page, size, new Sort(Direction.DESC, "id"));
		Page<PerfTest> testList = perfTestService.getPagedAll(user, null, null, null, pageRequest);
		return testList.getContent();
	}

	/**
	 * Get the perf test detail in the form of json.
	 *
	 * @param user user
	 * @param id   perftest id
	 * @return json message containing test info.
	 */
	@GetMapping("/{id}")
	public PerfTest getOne(User user, @PathVariable Long id) {
		return checkNotNull(getOneWithPermissionCheck(user, id, false), "PerfTest %s does not exists", id);
	}

	/**
	 * Create the given perf test.
	 *
	 * @param user     user
	 * @param perfTest perf test
	 * @return json message containing test info.
	 */
	@PostMapping({"/", ""})
	public PerfTest create(User user, PerfTest perfTest) {
		checkNull(perfTest.getId(), "id should be null");
		// Make the vuser count optional.
		if (perfTest.getVuserPerAgent() == null && perfTest.getThreads() != null && perfTest.getProcesses() != null) {
			perfTest.setVuserPerAgent(perfTest.getThreads() * perfTest.getProcesses());
		}
		validate(user, null, perfTest);
		return perfTestService.save(user, perfTest);
	}

	/**
	 * Delete the given perf test.
	 *
	 * @param user user
	 * @param id   perf test id
	 * @return json success message if succeeded
	 */
	@DeleteMapping("/{id}")
	public void delete(User user, @PathVariable("id") Long id) {
		PerfTest perfTest = getOneWithPermissionCheck(user, id, false);
		checkNotNull(perfTest, "no perftest for %s exits", id);
		perfTestService.delete(user, id);
	}


	/**
	 * Update the given perf test.
	 *
	 * @param user     user
	 * @param id       perf test id
	 * @param perfTest perf test configuration changes
	 * @return json message
	 */
	@PutMapping("/{id}")
	public PerfTest update(User user, @PathVariable("id") Long id, PerfTest perfTest) {
		PerfTest existingPerfTest = getOneWithPermissionCheck(user, id, false);
		perfTest.setId(id);
		validate(user, existingPerfTest, perfTest);
		return perfTestService.save(user, perfTest);
	}

	/**
	 * Stop the given perf test.
	 *
	 * @param user user
	 * @param id   perf test id
	 * @return json success message if succeeded
	 */
	@PutMapping(value = "/{id}", params = "action=stop")
	public void stop(User user, @PathVariable Long id) {
		perfTestService.stop(user, id);
	}

	/**
	 * Update the given perf test's status.
	 *
	 * @param user   user
	 * @param id     perf test id
	 * @param status Status to be moved to
	 * @return json message
	 */
	@PutMapping(value = "/{id}", params = "action=status")
	public PerfTest updateStatus(User user, @PathVariable("id") Long id, Status status) {
		PerfTest perfTest = getOneWithPermissionCheck(user, id, false);
		checkNotNull(perfTest, "no perftest for %s exits", id).setStatus(status);
		validate(user, null, perfTest);
		return perfTestService.save(user, perfTest);
	}

	/**
	 * Clone and start the given perf test.
	 *
	 * @param user     user
	 * @param id       perf test id to be cloned
	 * @param perftest option to override while cloning.
	 * @return json string
	 */
	@SuppressWarnings("MVCPathVariableInspection")
	@GetMapping({"/{id}/clone_and_start", /* for backward compatibility */ "/{id}/cloneAndStart"})
	public PerfTest cloneAndStart(User user, @PathVariable("id") Long id, PerfTest perftest) {
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
		Map<String, MutableInt> agentCountMap = agentManagerService.getAvailableAgentCountMap(user);
		MutableInt agentCountObj = agentCountMap.get(isClustered() ? test.getRegion() : Config.NONE_REGION);
		checkNotNull(agentCountObj, "test region should be within current region list");
		int agentMaxCount = agentCountObj.intValue();
		checkArgument(newOne.getAgentCount() != 0, "test agent should not be %s", agentMaxCount);
		checkArgument(newOne.getAgentCount() <= agentMaxCount, "test agent should be equal to or less than %s",
			agentMaxCount);
		PerfTest savePerfTest = perfTestService.save(user, newOne);
		CoreLogger.LOGGER.info("test {} is created through web api by {}", savePerfTest.getId(), user.getUserId());
		return savePerfTest;
	}

}
