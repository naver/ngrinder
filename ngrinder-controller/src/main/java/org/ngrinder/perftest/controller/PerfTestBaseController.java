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
import org.apache.commons.lang.mutable.MutableInt;
import org.ngrinder.agent.service.AgentManagerService;
import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.common.constants.GrinderConstants;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.common.util.DateUtils;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.*;
import org.ngrinder.perftest.service.AgentManager;
import org.ngrinder.perftest.service.PerfTestService;
import org.python.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.ui.ModelMap;

import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static org.ngrinder.common.util.CollectionUtils.newHashMap;
import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.Preconditions.*;


/**
 * Perftest controller base containing common used methods.
 *
 * @since 3.5.0
 */
public class PerfTestBaseController extends BaseController {

	@Autowired
	protected PerfTestService perfTestService;

	@Autowired
	protected AgentManager agentManager;

	@Autowired
	protected AgentManagerService agentManagerService;

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

	protected List<Map<String, Object>> getStatus(List<PerfTest> perfTests) {
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
	 * Filter out please_modify_this.com from hosts string.
	 *
	 * @param originalString original string
	 * @return filtered string
	 */
	protected String filterHostString(String originalString) {
		List<String> hosts = newArrayList();
		for (String each : StringUtils.split(StringUtils.trimToEmpty(originalString), ",")) {
			if (!each.contains("please_modify_this.com")) {
				hosts.add(each);
			}
		}
		return StringUtils.join(hosts, ",");
	}


	protected Map<String, String> getMonitorGraphData(long id, String targetIP, int imgWidth) {
		int interval = perfTestService.getMonitorGraphInterval(id, targetIP, imgWidth);
		Map<String, String> sysMonitorMap = perfTestService.getMonitorGraph(id, targetIP, interval);
		PerfTest perfTest = perfTestService.getOne(id);
		sysMonitorMap.put("interval", String.valueOf(interval * (perfTest != null ? perfTest.getSamplingInterval() : 1)));
		return sysMonitorMap;
	}

	protected Map<String, Object> getReportPluginGraphData(long id, String plugin, String kind, int imgWidth) {
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

	protected PerfTest getOneWithPermissionCheck(User user, Long id, boolean withTag) {
		PerfTest perfTest = withTag ? perfTestService.getOneWithTag(id) : perfTestService.getOne(id);
		if (user.getRole().equals(Role.ADMIN) || user.getRole().equals(Role.SUPER_USER)) {
			return perfTest;
		}
		if (perfTest != null && !user.equals(perfTest.getCreatedUser())) {
			throw processException("User " + user.getUserId() + " has no right on PerfTest " + id);
		}
		return perfTest;
	}

	protected Map<String, Object> getPerfGraphData(Long id, String[] dataTypes, boolean onlyTotal, int imgWidth) {
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

	@SuppressWarnings("ConstantConditions")
	protected void validate(User user, PerfTest oldOne, PerfTest newOne) {
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

	protected ArrayList<String> getRegions(Map<String, MutableInt> agentCountMap) {
		ArrayList<String> regions = new ArrayList<>(agentCountMap.keySet());
		Collections.sort(regions);
		return regions;
	}

	protected void annotateDateMarker(Page<PerfTest> tests) {
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

	/**
	 * Create a new test from quick start mode.
	 *
	 * @param user       user
	 * @param testName   test name
	 * @param targetHost target host
	 * @return test    {@link PerfTest}
	 */
	protected PerfTest createPerfTestFromQuickStart(User user, String testName, String targetHost) {
		PerfTest test = new PerfTest(user);
		test.init();
		test.setTestName(testName);
		test.setTargetHosts(targetHost);
		return test;
	}
}
