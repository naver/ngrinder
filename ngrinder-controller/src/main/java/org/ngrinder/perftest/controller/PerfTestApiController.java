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
import net.grinder.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.ngrinder.agent.service.AgentManagerService;
import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.common.constants.GrinderConstants;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.common.controller.RestAPI;
import org.ngrinder.common.util.DateUtils;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.hazelcast.HazelcastService;
import org.ngrinder.model.*;
import org.ngrinder.perftest.model.SamplingModel;
import org.ngrinder.perftest.service.AgentManager;
import org.ngrinder.perftest.service.PerfTestService;
import org.ngrinder.perftest.service.TagService;
import org.ngrinder.region.service.RegionService;
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
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
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

	private Gson fileEntryGson;

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void init() {
		fileEntryGson = new GsonBuilder().registerTypeAdapter(FileEntry.class, new FileEntry.FileEntrySerializer()).create();
	}

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
	@RestAPI
	@RequestMapping("/list")
	public HttpEntity<String> getAllList(User user,
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
		return toJsonHttpEntity(result);
	}

	/**
	 * Delete the perf tests having given IDs.
	 *
	 * @param user user
	 * @param ids  comma operated IDs
	 * @return success json messages if succeeded.
	 */
	@RestAPI
	@DeleteMapping
	public String delete(User user, @RequestParam(defaultValue = "") String ids) {
		for (String idStr : StringUtils.split(ids, ",")) {
			perfTestService.delete(user, NumberUtils.toLong(idStr, 0));
		}
		return returnSuccess();
	}

	/**
	 * Stop the perf tests having given IDs.
	 *
	 * @param user user
	 * @param ids  comma separated perf test IDs
	 * @return success json if succeeded.
	 */
	@RestAPI
	@PutMapping(params = "action=stop")
	public String stop(User user, @RequestParam(defaultValue = "") String ids) {
		for (String idStr : StringUtils.split(ids, ",")) {
			perfTestService.stop(user, NumberUtils.toLong(idStr, 0));
		}
		return returnSuccess();
	}

	/**
	 * Stop the given perf test.
	 *
	 * @param user user
	 * @param id   perf test id
	 * @return json success message if succeeded
	 */
	@RestAPI
	@PutMapping(value = "/{id}", params = "action=stop")
	public String stop(User user, @PathVariable Long id) {
		perfTestService.stop(user, id);
		return returnSuccess();
	}

	/**
	 * Get the status of the given perf test.
	 *
	 * @param user user
	 * @param id   perftest id
	 * @return JSON message containing perf test status
	 */
	@RestAPI
	@GetMapping("/{id}/status")
	public HttpEntity<String> getStatus(User user, @PathVariable Long id) {
		List<PerfTest> perfTests = perfTestService.getAll(user, new Long[]{id});
		return toJsonHttpEntity(buildMap("status", getStatus(perfTests)));
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
	@GetMapping("/api/{id}/monitor")
	public Map<String, String> getMonitorGraph(@PathVariable long id,
											   @RequestParam String targetIP, @RequestParam int imgWidth) {
		return getMonitorGraphData(id, targetIP, imgWidth);
	}

	/**
	 * Get the count of currently running perf test and the detailed progress info for the given perf test IDs.
	 *
	 * @param user user
	 * @param ids  comma separated perf test list
	 * @return JSON message containing perf test status
	 */
	@RestAPI
	@GetMapping("/status")
	public HttpEntity<String> getStatuses(User user, @RequestParam(defaultValue = "") String ids) {
		List<PerfTest> perfTests = perfTestService.getAll(user, convertString2Long(ids));
		return toJsonHttpEntity(buildMap("perfTestInfo", perfTestService.getCurrentPerfTestStatistics(), "status",
			getStatus(perfTests)));
	}

	/**
	 * Search tags based on the given query.
	 *
	 * @param user  user to search
	 * @param query query string
	 * @return found tag list in json
	 */
	@RestAPI
	@GetMapping("/search_tag")
	public List<String> searchTag(User user, @RequestParam(required = false) String query) {
		List<String> allStrings = tagService.getAllTagStrings(user, query);
		if (StringUtils.isNotBlank(query)) {
			allStrings.add(query);
		}
		return allStrings;
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
	@RestAPI
	@GetMapping({"/{id}/perf", "/{id}/graph"})
	public Map<String, Object> getPerfGraph(@PathVariable long id,
											@RequestParam(defaultValue = "") String dataType,
											@RequestParam(defaultValue = "false") boolean onlyTotal,
											@RequestParam int imgWidth) {
		String[] dataTypes = checkNotEmpty(StringUtils.split(dataType, ","), "dataType argument should be provided");
		return getPerfGraphData(id, dataTypes, onlyTotal, imgWidth);
	}

	/**
	 * Get resources and lib file list from the same folder with the given script path.
	 *
	 * @param user       user
	 * @param scriptPath script path
	 * @param ownerId    ownerId
	 * @return json string representing resources and libs.
	 */
	@RestAPI
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
	 * Get all available scripts in JSON format for the current factual user.
	 *
	 * @param user    user
	 * @param ownerId owner id
	 * @return JSON containing script's list.
	 */
	@RestAPI
	@RequestMapping("/script")
	public HttpEntity<String> getScripts(User user, @RequestParam(required = false) String ownerId) {
		if (StringUtils.isNotEmpty(ownerId)) {
			user = userService.getOne(ownerId);
		}
		List<FileEntry> scripts = fileEntryService.getAll(user)
			.stream()
			.filter(input -> input != null && input.getFileType().getFileCategory() == FileCategory.SCRIPT)
			.collect(Collectors.toList());
		return toJsonHttpEntity(scripts, fileEntryGson);
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
	@RestAPI
	@GetMapping("/{id}/basic_report")
	public HttpEntity<String> getReportSection(User user, @PathVariable long id, @RequestParam int imgWidth) {
		Map<String, Object> model = new HashMap<>();
		PerfTest test = getOneWithPermissionCheck(user, id, false);
		int interval = perfTestService.getReportDataInterval(id, "TPS", imgWidth);
		model.put(PARAM_LOG_LIST, perfTestService.getLogFiles(id));
		model.put(PARAM_TEST_CHART_INTERVAL, interval * test.getSamplingInterval());
		model.put(PARAM_TEST, test);
		model.put(PARAM_TPS, perfTestService.getSingleReportDataAsJson(id, "TPS", interval));
		return toJsonHttpEntity(model);
	}

	@RestAPI
	@GetMapping("/{id}/logs")
	public List<String> getLogs(@PathVariable long id) {
		return perfTestService.getLogFiles(id);
	}

	/**
	 * Create a new test or cloneTo a current test.
	 *
	 * @param user     user
	 * @param perfTest {@link PerfTest}
	 * @param isClone  true if cloneTo
	 * @return redirect:/perftest/list
	 */
	@RestAPI
	@PostMapping("/new")
	public String saveOne(User user, PerfTest perfTest, @RequestParam(defaultValue = "false") boolean isClone) {
		validate(user, null, perfTest);

		// Point to the head revision
		perfTest.setTestName(StringUtils.trimToEmpty(perfTest.getTestName()));
		perfTest.setScriptRevision(-1L);
		perfTest.prepare(isClone);
		perfTest = perfTestService.save(user, perfTest);

		if (perfTest.getStatus() == Status.SAVED || perfTest.getScheduledTime() != null) {
			return "list";
		} else {
			return perfTest.getId().toString();
		}
	}

	/**
	 * Get the perf test detail on the given perf test id.
	 *
	 * @param user  user
	 * @param id    perf test id
	 * @return perftest/detail
	 */
	@RestAPI
	@GetMapping({"/{id}", "/create"})
	public HttpEntity<String> getOne(User user, @PathVariable(required = false) Long id) {
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
		model.put(PARAM_PROCESS_THREAD_POLICY_SCRIPT, perfTestService.getProcessAndThreadPolicyScript());
		addDefaultAttributeOnModel(model);
		return toJsonHttpEntity(model);
	}

	/**
	 * Get the running perf test info having the given id.
	 *
	 * @param user user
	 * @param id   test id
	 * @return JSON message	containing test,agent and monitor status.
	 */
	@RequestMapping(value = "/{id}/sample")
	@RestAPI
	public HttpEntity<String> refreshTestRunning(User user, @PathVariable long id) {
		PerfTest test = checkNotNull(getOneWithPermissionCheck(user, id, false), "given test should be exist : " + id);
		Map<String, Object> map = newHashMap();

		SamplingModel samplingModel = hazelcastService.get(DIST_MAP_NAME_SAMPLING, test.getId());
		if (samplingModel != null) {
			map.put("perf", fileEntryGson.fromJson(samplingModel.getRunningSample(), HashMap.class));
			map.put("agent", fileEntryGson.fromJson(samplingModel.getAgentState(), HashMap.class));
		}

		String monitoringJson = hazelcastService.get(DIST_MAP_NAME_MONITORING, test.getId());
		if (monitoringJson != null) {
			map.put("monitor", fileEntryGson.fromJson(monitoringJson, HashMap.class));
		}

		map.put("status", test.getStatus());
		return toJsonHttpEntity(map);
	}

	@RestAPI
	@GetMapping("/{id}/detail_report")
	public HttpEntity<String> getReport(@PathVariable long id) {
		Map<String, Object> model = newHashMap();
		model.put("test", perfTestService.getOne(id));
		model.put("plugins", perfTestService.getAvailableReportPlugins(id));
		return toJsonHttpEntity(model);
	}

	/**
	 * Leave the comment on the perf test.
	 *
	 * @param id          testId
	 * @param user        user
	 * @param params	  {testComment, tagString}
	 * @return JSON
	 */
	@RestAPI
	@PostMapping("/{id}/leave_comment")
	public String leaveComment(User user, @PathVariable Long id, @RequestBody Map<String, Object> params) {
		perfTestService.addCommentOn(user, id, cast(params.get("testComment")), cast(params.get("tagString")));
		return returnSuccess();
	}

	private Map<String, String> getMonitorGraphData(long id, String targetIP, int imgWidth) {
		int interval = perfTestService.getMonitorGraphInterval(id, targetIP, imgWidth);
		Map<String, String> sysMonitorMap = perfTestService.getMonitorGraph(id, targetIP, interval);
		PerfTest perfTest = perfTestService.getOne(id);
		sysMonitorMap.put("interval", String.valueOf(interval * (perfTest != null ? perfTest.getSamplingInterval() : 1)));
		return sysMonitorMap;
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

}
