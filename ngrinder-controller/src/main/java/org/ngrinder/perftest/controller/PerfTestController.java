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
import org.ngrinder.agent.service.AgentManagerService;
import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.common.constants.GrinderConstants;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.common.controller.RestAPI;
import org.ngrinder.common.util.FileDownloadUtils;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.logger.CoreLogger;
import org.ngrinder.infra.spring.RemainedPath;
import org.ngrinder.model.*;
import org.ngrinder.perftest.service.AgentManager;
import org.ngrinder.perftest.service.PerfTestService;
import org.ngrinder.script.handler.ScriptHandlerFactory;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.service.FileEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static org.ngrinder.common.util.CollectionUtils.newHashMap;
import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.Preconditions.*;
import static org.ngrinder.common.util.TypeConvertUtils.cast;

/**
 * Performance Test Controller.
 *
 * @author Mavlarn
 * @author JunHo Yoon
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
@Profile("production")
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
	private ScriptHandlerFactory scriptHandlerFactory;

	/**
	 * Get the perf test lists.
	 */
	@RequestMapping({"/list", "/", ""})
	public String getAll(User user) {
		return "app";
	}

	/**
	 * Open the new perf test creation form.
	 */
	@RequestMapping("/new")
	public String openForm() {
		return "app";
	}

	/**
	 * perf test detail on the given perf test id.
	 *
	 * @param id perf test id
	 */
	@RequestMapping("/{id}")
	public String detail(@PathVariable Long id) {
		return "app";
	}

	private ArrayList<String> getRegions(Map<String, MutableInt> agentCountMap) {
		ArrayList<String> regions = new ArrayList<String>(agentCountMap.keySet());
		Collections.sort(regions);
		return regions;
	}


	public void addDefaultAttributeOnModel(Map<String, Object> model) {
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

	/**
	 * Add the various default configuration values on the model.
	 *
	 * @param model model to which put the default values
	 */
	public void addDefaultAttributeOnModel(ModelMap model) {
		model.addAttribute(PARAM_AVAILABLE_RAMP_UP_TYPE, RampUp.values());
		model.addAttribute(PARAM_MAX_VUSER_PER_AGENT, agentManager.getMaxVuserPerAgent());
		model.addAttribute(PARAM_MAX_RUN_COUNT, agentManager.getMaxRunCount());
		if (getConfig().isSecurityEnabled()) {
			model.addAttribute(PARAM_SECURITY_LEVEL, getConfig().getSecurityLevel());
		}
		model.addAttribute(PARAM_MAX_RUN_HOUR, agentManager.getMaxRunHour());
		model.addAttribute(PARAM_SAFE_FILE_DISTRIBUTION,
				getConfig().getControllerProperties().getPropertyBoolean(ControllerConstants.PROP_CONTROLLER_SAFE_DIST));
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
	 */
	@RestAPI
	@ResponseBody
	@PostMapping("/quickstart")
	public Map<String, Object> getQuickStart(User user, @RequestBody Map<String, Object> params) {
		String urlString = cast(params.get("url"));
		String scriptType = cast(params.get("scriptType"));

		URL url = checkValidURL(urlString);
		FileEntry newEntry = fileEntryService.prepareNewEntryForQuickTest(user, urlString, scriptHandlerFactory.getHandler(scriptType));

		Map<String, Object> model = new HashMap<>();
		model.put(PARAM_QUICK_SCRIPT, newEntry.getPath());
		model.put(PARAM_QUICK_SCRIPT_REVISION, newEntry.getRevision());
		// TODO seialize perftest.
		// model.put(PARAM_TEST, createPerfTestFromQuickStart(user, "Test for " + url.getHost(), url.getHost()));
		Map<String, MutableInt> agentCountMap = agentManagerService.getAvailableAgentCountMap(user);
		model.put(PARAM_REGION_AGENT_COUNT_MAP, agentCountMap);
		model.put(PARAM_REGION_LIST, getRegions(agentCountMap));
		addDefaultAttributeOnModel(model);
		model.put(PARAM_PROCESS_THREAD_POLICY_SCRIPT, perfTestService.getProcessAndThreadPolicyScript());

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

	private List<Map<String, Object>> getStatus(List<PerfTest> perfTests) {
		List<Map<String, Object>> statuses = newArrayList();
		for (PerfTest each : perfTests) {
			Map<String, Object> result = newHashMap();
			result.put("id", each.getId());
			result.put("status_id", each.getStatus());
			result.put("status_type", each.getStatus());
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
	 * @return perftest/detail_report
	 */
	@GetMapping(value = {"/{id}/detail_report", /** for backward compatibility */"/{id}/report"})
	public String getReport(@PathVariable long id) {
		return "app";
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


	private Map<String, String> getMonitorGraphData(long id, String targetIP, int imgWidth) {
		int interval = perfTestService.getMonitorGraphInterval(id, targetIP, imgWidth);
		Map<String, String> sysMonitorMap = perfTestService.getMonitorGraph(id, targetIP, interval);
		PerfTest perfTest = perfTestService.getOne(id);
		sysMonitorMap.put("interval", String.valueOf(interval * (perfTest != null ? perfTest.getSamplingInterval() : 1)));
		return sysMonitorMap;
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
	public HttpEntity<String> getLogs(User user, @PathVariable("id") Long id) {
		// Check permission
		getOneWithPermissionCheck(user, id, false);
		return toJsonHttpEntity(perfTestService.getLogFiles(id));
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
	public HttpEntity<String> getMonitorGraph(@PathVariable("id") long id,
	                                          @RequestParam("targetIP") String targetIP, @RequestParam int imgWidth) {
		return toJsonHttpEntity(getMonitorGraphData(id, targetIP, imgWidth));
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
	public HttpEntity<String> getPluginGraph(@PathVariable("id") long id,
	                                         @PathVariable("plugin") String plugin,
	                                         @RequestParam("kind") String kind, @RequestParam int imgWidth) {
		return toJsonHttpEntity(getReportPluginGraphData(id, plugin, kind, imgWidth));
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
	@RestAPI
	@RequestMapping(value = {"/api/last", "/api", "/api/"}, method = RequestMethod.GET)
	public HttpEntity<String> getAll(User user, @RequestParam(value = "page", defaultValue = "0") int page,
	                                 @RequestParam(value = "size", defaultValue = "1") int size) {
		PageRequest pageRequest = PageRequest.of(page, size, new Sort(Direction.DESC, "id"));
		Page<PerfTest> testList = perfTestService.getPagedAll(user, null, null, null, pageRequest);
		return toJsonHttpEntity(testList.getContent());
	}

	/**
	 * Get the perf test detail in the form of json.
	 *
	 * @param user user
	 * @param id   perftest id
	 * @return json message containing test info.
	 */

	// TODO
/*	@RestAPI
	@GetMapping(value = "/api/{id}")
	public HttpEntity<String> getOne(User user, @PathVariable Long id) {
		PerfTest test = checkNotNull(getOneWithPermissionCheck(user, id, false), "PerfTest %s does not exists", id);
		return toJsonHttpEntity(test);
	}*/

	/**
	 * Create the given perf test.
	 *
	 * @param user     user
	 * @param perfTest perf test
	 * @return json message containing test info.
	 */
	@RestAPI
	@RequestMapping(value = {"/api/", "/api"}, method = RequestMethod.POST)
	public HttpEntity<String> create(User user, PerfTest perfTest) {
		checkNull(perfTest.getId(), "id should be null");
		// Make the vuser count optional.
		if (perfTest.getVuserPerAgent() == null && perfTest.getThreads() != null && perfTest.getProcesses() != null) {
			perfTest.setVuserPerAgent(perfTest.getThreads() * perfTest.getProcesses());
		}
		validate(user, null, perfTest);
		PerfTest savePerfTest = perfTestService.save(user, perfTest);
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
	 * @return json message
	 */
	@RestAPI
	@RequestMapping(value = "/api/{id}", method = RequestMethod.PUT)
	public HttpEntity<String> update(User user, @PathVariable("id") Long id, PerfTest perfTest) {
		PerfTest existingPerfTest = getOneWithPermissionCheck(user, id, false);
		perfTest.setId(id);
		validate(user, existingPerfTest, perfTest);
		return toJsonHttpEntity(perfTestService.save(user, perfTest));
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
	public HttpEntity<String> updateStatus(User user, @PathVariable("id") Long id, Status status) {
		PerfTest perfTest = getOneWithPermissionCheck(user, id, false);
		checkNotNull(perfTest, "no perftest for %s exits", id).setStatus(status);
		validate(user, null, perfTest);
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
	@SuppressWarnings("MVCPathVariableInspection")
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
		Map<String, MutableInt> agentCountMap = agentManagerService.getAvailableAgentCountMap(user);
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
