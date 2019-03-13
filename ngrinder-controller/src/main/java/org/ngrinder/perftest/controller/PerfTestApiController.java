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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.ngrinder.common.controller.RestAPI;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.hazelcast.HazelcastService;
import org.ngrinder.infra.logger.CoreLogger;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Role;
import org.ngrinder.model.Status;
import org.ngrinder.model.User;
import org.ngrinder.perftest.model.SamplingModel;
import org.ngrinder.perftest.service.TagService;
import org.ngrinder.script.model.FileCategory;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang.StringUtils.trimToEmpty;
import static org.ngrinder.common.constant.CacheConstants.DIST_MAP_NAME_MONITORING;
import static org.ngrinder.common.constant.CacheConstants.DIST_MAP_NAME_SAMPLING;
import static org.ngrinder.common.util.CollectionUtils.buildMap;
import static org.ngrinder.common.util.CollectionUtils.newHashMap;
import static org.ngrinder.common.util.Preconditions.*;

/**
 * Performance Test Api Controller.
 *
 * @since 3.5.0
 */
@Profile("production")
@RestController
@RequestMapping("/perftest")
public class PerfTestApiController extends PerfTestBaseController {

	@Autowired
	private FileEntryService fileEntryService;

	@Autowired
	private TagService tagService;

	@Autowired
	private UserService userService;

	@Autowired
	private HazelcastService hazelcastService;

	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * Search tags based on the given query.
	 *
	 * @param user  user to search
	 * @param query query string
	 * @return found tag list in json
	 */
	@RequestMapping("/search_tag")
	public List<String> searchTag(User user, @RequestParam(required = false) String query) {
		List<String> allStrings = tagService.getAllTagStrings(user, query);
		if (StringUtils.isNotBlank(query)) {
			allStrings.add(query);
		}
		return allStrings;
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

	/**
	 * Delete the perf tests having given IDs.
	 *
	 * @param user user
	 * @param ids  comma operated IDs
	 * @return success json messages if succeeded.
	 */
	@RestAPI
	@RequestMapping(value = "/api", method = RequestMethod.DELETE)
	public String delete(User user, @RequestParam(value = "ids", defaultValue = "") String ids) {
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
	@RequestMapping(value = "/api", params = "action=stop", method = RequestMethod.PUT)
	public String stop(User user, @RequestParam(value = "ids", defaultValue = "") String ids) {
		for (String idStr : StringUtils.split(ids, ",")) {
			perfTestService.stop(user, NumberUtils.toLong(idStr, 0));
		}
		return returnSuccess();
	}

	/**
	 * Get the running perf test info having the given id.
	 *
	 * @param user user
	 * @param id   test id
	 * @return JSON message	containing test,agent and monitor status.
	 */
	@RestAPI
	@RequestMapping(value = "/{id}/api/sample")
	public Map<String, Object> refreshTestRunning(User user, @PathVariable("id") long id) throws IOException {
		PerfTest test = checkNotNull(getOneWithPermissionCheck(user, id, false), "given test should be exist : " + id);
		Map<String, Object> map = newHashMap();

		SamplingModel samplingModel = hazelcastService.get(DIST_MAP_NAME_SAMPLING, test.getId());
		if (samplingModel != null) {
			map.put("perf", objectMapper.readValue(samplingModel.getRunningSample(), HashMap.class));
			map.put("agent", objectMapper.readValue(samplingModel.getAgentState(), HashMap.class));
		}

		String monitoringJson = hazelcastService.get(DIST_MAP_NAME_MONITORING, test.getId());
		if (monitoringJson != null) {
			map.put("monitor", objectMapper.readValue(monitoringJson, HashMap.class));
		}

		map.put("status", test.getStatus());
		return map;
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
	public Map<String, Object> getStatuses(User user, @RequestParam(value = "ids", defaultValue = "") String ids) {
		List<PerfTest> perfTests = perfTestService.getAll(user, convertString2Long(ids));
		return buildMap("perfTestInfo", perfTestService.getCurrentPerfTestStatistics(), "status", getStatus(perfTests));
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
	public List<FileEntry> getScripts(User user, @RequestParam(value = "ownerId", required = false) String ownerId) {
		if (StringUtils.isNotEmpty(ownerId)) {
			user = userService.getOne(ownerId);
		}
		return newArrayList(fileEntryService.getAll(user).stream()
			.filter(input -> input != null && input.getFileType().getFileCategory() == FileCategory.SCRIPT).collect(Collectors.toList()));
	}

	/**
	 * Get resources and lib file list from the same folder with the given script path.
	 *
	 * @param user       user
	 * @param scriptPath script path
	 * @param ownerId    ownerId
	 * @return json string representing resources and libs.
	 */
	@RequestMapping("/api/resource")
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
	 * Get the status of the given perf test.
	 *
	 * @param user user
	 * @param id   perftest id
	 * @return JSON message containing perf test status
	 */
	@RestAPI
	@RequestMapping("/api/{id}/status")
	public Map<String, Object> getStatus(User user, @PathVariable("id") Long id) {
		List<PerfTest> perfTests = perfTestService.getAll(user, new Long[]{id});
		return buildMap("status", getStatus(perfTests));
	}

	/**
	 * Get the logs of the given perf test.
	 *
	 * @param user user
	 * @param id   perftest id
	 * @return JSON message containing log file names
	 */
	@RestAPI
	@RequestMapping("/api/{id}/logs")
	public List<String> getLogs(User user, @PathVariable("id") Long id) {
		// Check permission
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
	@SuppressWarnings("MVCPathVariableInspection")
	@RestAPI
	@RequestMapping({"/api/{id}/perf", "/api/{id}/graph"})
	public Map<String, Object> getPerfGraph(@PathVariable("id") long id, @RequestParam String dataType,
                                            @RequestParam(defaultValue = "false") boolean onlyTotal, @RequestParam int imgWidth) {
		String[] dataTypes = checkNotEmpty(StringUtils.split(dataType, ","), "dataType argument should be provided");
		return getPerfGraphData(id, dataTypes, onlyTotal, imgWidth);
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
	public Map<String, String> getMonitorGraph(@PathVariable("id") long id, @RequestParam("targetIP") String targetIP,
                                               @RequestParam int imgWidth) {
		return getMonitorGraphData(id, targetIP, imgWidth);
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
	@RestAPI
	@RequestMapping("/api/{id}/plugin/{plugin}")
	public Map<String, Object> getPluginGraph(@PathVariable("id") long id, @PathVariable("plugin") String plugin,
                                              @RequestParam("kind") String kind, @RequestParam int imgWidth) {
		return getReportPluginGraphData(id, plugin, kind, imgWidth);
	}

	/**
	 * Get the last perf test details in the form of json.
	 *
	 * @param user user
	 * @param page page
	 * @param size size of retrieved perf test
	 * @return json string
	 */
	@RestAPI
	@RequestMapping({"/api/last", "/api", "/api/"})
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
	@RestAPI
	@RequestMapping("/api/{id}")
	public PerfTest getOne(User user, @PathVariable("id") Long id) {
		return checkNotNull(getOneWithPermissionCheck(user, id, false), "PerfTest %s does not exists", id);
	}

	/**
	 * Create the given perf test.
	 *
	 * @param user     user
	 * @param perfTest perf test
	 * @return json message containing test info.
	 */
	@RestAPI
	@RequestMapping(value = {"/api/", "/api"}, method = RequestMethod.POST)
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
	@RestAPI
	@RequestMapping(value = "/api/{id}", method = RequestMethod.DELETE)
	public String delete(User user, @PathVariable("id") Long id) {
		PerfTest perfTest = getOneWithPermissionCheck(user, id, false);
		checkNotNull(perfTest, "no perftest for %s exits", id);
		perfTestService.delete(user, id);
		return returnSuccess();
	}


	/**
	 * Update the given perf test.
	 *
	 * @param user     user
	 * @param id       perf test id
	 * @param perfTest perf test configuration changes
	 * @return json message
	 */
	@RestAPI
	@RequestMapping(value = "/api/{id}", method = RequestMethod.PUT)
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
	@RestAPI
	@RequestMapping(value = "/api/{id}", params = "action=stop", method = RequestMethod.PUT)
	public String stop(User user, @PathVariable("id") Long id) {
		perfTestService.stop(user, id);
		return returnSuccess();
	}


	/**
	 * Update the given perf test's status.
	 *
	 * @param user   user
	 * @param id     perf test id
	 * @param status Status to be moved to
	 * @return json message
	 */
	@RestAPI
	@RequestMapping(value = "/api/{id}", params = "action=status", method = RequestMethod.PUT)
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
	@RestAPI
	@RequestMapping(value = {"/api/{id}/clone_and_start", /* for backward compatibility */ "/api/{id}/cloneAndStart"})
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
