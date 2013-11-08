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
package org.ngrinder.perftest.service;

import static org.ngrinder.common.util.CollectionUtils.newHashMap;
import static org.ngrinder.common.util.CollectionUtils.newHashSet;
import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.NoOp.noOp;
import static org.ngrinder.common.util.Preconditions.checkNotEmpty;
import static org.ngrinder.common.util.Preconditions.checkNotNull;
import static org.ngrinder.model.Status.getProcessingOrTestingTestStatus;
import static org.ngrinder.perftest.repository.PerfTestSpecification.createdBy;
import static org.ngrinder.perftest.repository.PerfTestSpecification.hasTag;
import static org.ngrinder.perftest.repository.PerfTestSpecification.idEmptyPredicate;
import static org.ngrinder.perftest.repository.PerfTestSpecification.idEqual;
import static org.ngrinder.perftest.repository.PerfTestSpecification.idRegionEqual;
import static org.ngrinder.perftest.repository.PerfTestSpecification.idSetEqual;
import static org.ngrinder.perftest.repository.PerfTestSpecification.likeTestNameOrDescription;
import static org.ngrinder.perftest.repository.PerfTestSpecification.scheduledTimeNotEmptyPredicate;
import static org.ngrinder.perftest.repository.PerfTestSpecification.statusSetEqual;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.grinder.SingleConsole;
import net.grinder.StopReason;
import net.grinder.common.GrinderProperties;
import net.grinder.console.communication.AgentProcessControlImplementation.AgentStatus;
import net.grinder.console.model.ConsoleProperties;
import net.grinder.util.ConsolePropertiesFactory;
import net.grinder.util.Directory;
import net.grinder.util.Pair;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hibernate.Hibernate;
import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Permission;
import org.ngrinder.model.Role;
import org.ngrinder.model.Status;
import org.ngrinder.model.Tag;
import org.ngrinder.model.User;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.perftest.model.PerfTestStatistics;
import org.ngrinder.perftest.model.ProcessAndThread;
import org.ngrinder.perftest.repository.PerfTestRepository;
import org.ngrinder.script.handler.NullScriptHandler;
import org.ngrinder.script.handler.ProcessingResultPrintStream;
import org.ngrinder.script.handler.ScriptHandler;
import org.ngrinder.script.handler.ScriptHandlerFactory;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.service.IPerfTestService;
import org.python.google.common.collect.Lists;
import org.python.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * {@link PerfTest} Service Class.
 * 
 * This class contains various method which mainly get the {@link PerfTest} matching specific conditions from DB.
 * 
 * @author JunHo Yoon
 * @author Mavlarn
 * @since 3.0
 */
public class PerfTestService implements NGrinderConstants, IPerfTestService {

	private static final int MAX_POINT_COUNT = 100;

	private static final Logger LOGGER = LoggerFactory.getLogger(PerfTestService.class);

	private static final String DATA_FILE_EXTENSION = ".data";

	@Autowired
	private PerfTestRepository perfTestRepository;

	@Autowired
	private ConsoleManager consoleManager;

	@Autowired
	private AgentManager agentManager;

	@Autowired
	private Config config;

	@Autowired
	private FileEntryService fileEntryService;

	@Autowired
	private TagService tagSerivce;

	@Autowired
	private ScriptHandlerFactory scriptHandlerFactory;

	/**
	 * Get {@link PerfTest} list on the user.
	 * 
	 * @param user
	 *            user
	 * @param query
	 *            query string on test name or description
	 * @param tag
	 *            seach tag.
	 * @param queryFilter
	 *            "S" for querying scheduled test, "F" for querying finished test
	 * @param pageable
	 *            paging info
	 * @return found {@link PerfTest} list
	 */
	public Page<PerfTest> getPerfTestList(User user, String query, String tag, String queryFilter, Pageable pageable) {
		Specifications<PerfTest> spec = Specifications.where(idEmptyPredicate());
		// User can see only his own test
		if (user.getRole().equals(Role.USER)) {
			spec = spec.and(createdBy(user));
		}

		if (StringUtils.isNotBlank(tag)) {
			spec = spec.and(hasTag(tag));
		}
		if ("F".equals(queryFilter)) {
			spec = spec.and(statusSetEqual(Status.FINISHED));
		} else if ("R".equals(queryFilter)) {
			spec = spec.and(statusSetEqual(Status.TESTING));
		} else if ("S".equals(queryFilter)) {
			spec = spec.and(statusSetEqual(Status.READY));
			spec = spec.and(scheduledTimeNotEmptyPredicate());
		}
		if (StringUtils.isNotBlank(query)) {
			spec = spec.and(likeTestNameOrDescription(query));
		}
		return perfTestRepository.findAll(spec, pageable);
	}

	/**
	 * Get {@link PerfTest} list on the user.
	 * 
	 * @param user
	 *            user
	 * @return found {@link PerfTest} list
	 */
	List<PerfTest> getPerfTestList(User user) {
		Specifications<PerfTest> spec = Specifications.where(createdBy(user));
		return perfTestRepository.findAll(spec);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.perftest.service.IPerfTestService#getPerfTest(org.ngrinder .model.User, java.lang.Integer)
	 */
	@Override
	public PerfTest getPerfTest(User user, Long id) {
		Specifications<PerfTest> spec = Specifications.where(idEmptyPredicate());

		// User can see only his own test
		if (user.getRole().equals(Role.USER)) {
			spec = spec.and(createdBy(user));
		}
		spec = spec.and(idEqual(id));
		return perfTestRepository.findOne(spec);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.perftest.service.IPerfTestService#getPerfTest(org.ngrinder .model.User, java.lang.Integer[])
	 */
	@Override
	public List<PerfTest> getPerfTest(User user, Long[] ids) {
		Specifications<PerfTest> spec = Specifications.where(idEmptyPredicate());

		// User can see only his own test
		if (user.getRole().equals(Role.USER)) {
			spec = spec.and(createdBy(user));
		}

		spec = spec.and(idSetEqual(ids));
		return perfTestRepository.findAll(spec);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.perftest.service.IPerfTestService#getPerfTestCount(org.ngrinder .model.User,
	 * org.ngrinder.perftest.model.Status)
	 */
	@Override
	public long getPerfTestCount(User user, Status... statuses) {
		Specifications<PerfTest> spec = Specifications.where(idEmptyPredicate());

		// User can see only his own test
		if (user != null) {
			spec = spec.and(createdBy(user));
		}

		if (statuses.length != 0) {
			spec = spec.and(statusSetEqual(statuses));
		}

		return perfTestRepository.count(spec);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.perftest.service.IPerfTestService#getPerfTest(org.ngrinder .model.User,
	 * org.ngrinder.perftest.model.Status)
	 */
	@Override
	public List<PerfTest> getPerfTest(User user, Status... statuses) {
		Specifications<PerfTest> spec = Specifications.where(idEmptyPredicate());

		// User can see only his own test
		if (user != null) {
			spec = spec.and(createdBy(user));
		}
		if (statuses.length != 0) {
			spec = spec.and(statusSetEqual(statuses));
		}

		return perfTestRepository.findAll(spec);
	}

	private List<PerfTest> getPerfTest(User user, String region, Status... statuses) {
		Specifications<PerfTest> spec = Specifications.where(idEmptyPredicate());

		// User can see only his own test
		if (user != null) {
			spec = spec.and(createdBy(user));
		}
		spec = spec.and(idRegionEqual(region));
		if (statuses.length != 0) {
			spec = spec.and(statusSetEqual(statuses));
		}

		return perfTestRepository.findAll(spec);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.perftest.service.IPerfTestService#savePerfTest(org.ngrinder .model.User,
	 * org.ngrinder.perftest.model.PerfTest)
	 */
	@Override
	@Transactional
	public PerfTest savePerfTest(User user, PerfTest perfTest) {
		if (perfTest.getStatus() == Status.READY) {
			FileEntry scriptEntry = fileEntryService.getFileEntry(user, perfTest.getScriptName());
			long revision = scriptEntry != null ? scriptEntry.getRevision() : -1;
			perfTest.setScriptRevision(revision);
		}
		SortedSet<Tag> tags = tagSerivce.addTags(user,
				StringUtils.split(StringUtils.trimToEmpty(perfTest.getTagString()), ","));
		perfTest.setTags(tags);
		perfTest.setTagString(buildTagString(tags));
		return savePerfTest(perfTest);
	}

	private String buildTagString(Set<Tag> tags) {
		List<String> tagStringResult = new ArrayList<String>();
		for (Tag each : tags) {
			tagStringResult.add(each.getTagValue());
		}
		return StringUtils.join(tagStringResult, ",");
	}

	/**
	 * Update runtime statistics on {@link PerfTest} having the given id.
	 * 
	 * @param id
	 *            id of {@link PerfTest}
	 * @param runningSample
	 *            runningSample json string
	 * @param agentStatus
	 *            agentStatus json string
	 */
	@Transactional
	public void updateRuntimeStatistics(Long id, String runningSample, String agentStatus) {
		perfTestRepository.updateRuntimeStatistics(id, runningSample, agentStatus);
		perfTestRepository.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.perftest.service.IPerfTestService#savePerfTest(org.ngrinder .perftest.model.PerfTest )
	 */
	@Override
	@Transactional
	public PerfTest savePerfTest(PerfTest perfTest) {
		checkNotNull(perfTest);
		// Merge if necessary
		if (perfTest.exist()) {
			PerfTest existingPerfTest = perfTestRepository.findOne(perfTest.getId());
			perfTest = existingPerfTest.merge(perfTest);
		} else {
			perfTest.clearMessages();
		}
		return perfTestRepository.saveAndFlush(perfTest);
	}

	/**
	 * Mark test error on {@link PerfTest} instance.
	 * 
	 * @param perfTest
	 *            {@link PerfTest}
	 * @param reason
	 *            stop reason
	 * @return perftest with updated data
	 */
	@Transactional
	public PerfTest markAbnormalTermination(PerfTest perfTest, StopReason reason) {
		return markAbnormalTermination(perfTest, reason.getDisplay());
	}

	/**
	 * Mark test error on {@link PerfTest} instance.
	 * 
	 * @param perfTest
	 *            {@link PerfTest}
	 * @param reason
	 *            stop reason
	 * @return perftest with updated data
	 */
	@Transactional
	public PerfTest markAbnormalTermination(PerfTest perfTest, String reason) {
		// Leave last status as test error cause
		perfTest.setTestErrorCause(perfTest.getStatus());
		return markStatusAndProgress(perfTest, Status.ABNORMAL_TESTING, reason);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.service.IPerfTestService#markStatusAndProgress(org.ngrinder .model.PerfTest,
	 * org.ngrinder.model.Status, java.lang.String)
	 */
	@Transactional
	@Override
	public PerfTest markStatusAndProgress(PerfTest perfTest, Status status, String message) {
		perfTest.setStatus(checkNotNull(status, "status should not be null"));
		return markProgress(perfTest, message);
	}

	/**
	 * Add a progress message on the given perfTest.
	 * 
	 * @param perfTest
	 *            perf test
	 * @param message
	 *            message to be recored.
	 * @return saved {@link PerfTest}
	 */
	@Transactional
	public PerfTest markProgress(PerfTest perfTest, String message) {
		checkNotNull(perfTest);
		checkNotNull(perfTest.getId(), "perfTest should save Id");
		perfTest.setLastProgressMessage(message);
		LOGGER.debug("Progress : Test - {} : {}", perfTest.getId(), message);
		return perfTestRepository.saveAndFlush(perfTest);
	}

	/**
	 * Add a progress message on the given perfTest and change the status.
	 * 
	 * @param perfTest
	 *            perf test
	 * @param status
	 *            status to be recorded.
	 * @param message
	 *            message to be recored.
	 * @return perftest with latest status and data
	 */
	@Transactional
	public PerfTest markProgressAndStatus(PerfTest perfTest, Status status, String message) {
		perfTest.setStatus(status);
		return markProgress(perfTest, message);
	}

	/**
	 * Add a progress message on the given perfTest and change the status. In addition, the finish time and various test
	 * statistic are recorded as well.
	 * 
	 * @param perfTest
	 *            perf test
	 * @param status
	 *            status to be recorded.
	 * @param message
	 *            message to be recored.
	 * @return perftest with latest status and data
	 */
	@Transactional
	public PerfTest markProgressAndStatusAndFinishTimeAndStatistics(PerfTest perfTest, Status status, String message) {
		perfTest.setFinishTime(new Date());
		updatePerfTestAfterTestFinish(perfTest);
		return markProgressAndStatus(perfTest, status, message);
	}

	/**
	 * mark the perftest as {@link Status.START_CONSOLE_FINISHED} .
	 * 
	 * @param perfTest
	 *            perftest to mark
	 * @param consolePort
	 *            port of the console, on which the test is running
	 * 
	 * @return saved perftest
	 */
	@Transactional
	public PerfTest markPerfTestConsoleStart(PerfTest perfTest, int consolePort) {
		perfTest.setPort(consolePort);
		return markProgressAndStatus(perfTest, Status.START_CONSOLE_FINISHED, "Console is started on port "
				+ consolePort);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.perftest.service.IPerfTestService#getPerfTest(long)
	 */
	@Transactional
	@Override
	public PerfTest getPerfTestWithTag(Long testId) {
		PerfTest findOne = perfTestRepository.findOne(testId);
		if (findOne != null) {
			Hibernate.initialize(findOne.getTags());
		}
		return findOne;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.perftest.service.IPerfTestService#getPerfTest(long)
	 */
	@Override
	public PerfTest getPerfTest(Long testId) {
		return perfTestRepository.findOne(testId);
	}

	/**
	 * Get the next runnable {@link PerfTest}.
	 * 
	 * @return found {@link PerfTest} which is ready to run, null otherwise
	 */
	@Transactional
	public PerfTest getNextRunnablePerfTestPerfTestCandidate() {
		List<PerfTest> readyPerfTests = perfTestRepository.findAllByStatusOrderByScheduledTimeAsc(Status.READY);
		List<PerfTest> usersFirstPerfTests = filterCurrentlyRunningTestUsersTest(readyPerfTests);
		return usersFirstPerfTests.isEmpty() ? null : readyPerfTests.get(0);
	}

	/**
	 * Get currently running {@link PerfTest} list.
	 * 
	 * @return running test list
	 */
	public List<PerfTest> getCurrentlyRunningTest() {
		return getPerfTest(null, Status.getProcessingOrTestingTestStatus());
	}

	/**
	 * Filter out {@link PerfTest} whose owner is running another test now..
	 * 
	 * @param perfTestLists
	 *            perf test
	 * @return filtered perf test
	 */
	protected List<PerfTest> filterCurrentlyRunningTestUsersTest(List<PerfTest> perfTestLists) {
		List<PerfTest> currentlyRunningTests = getCurrentlyRunningTest();
		final Set<User> currentlyRunningTestOwners = newHashSet();
		for (PerfTest each : currentlyRunningTests) {
			currentlyRunningTestOwners.add(each.getCreatedUser());
		}
		CollectionUtils.filter(perfTestLists, new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				PerfTest perfTest = (PerfTest) object;
				return !currentlyRunningTestOwners.contains(perfTest.getCreatedUser());
			}
		});
		return perfTestLists;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.perftest.service.IPerfTestService#getTestingPerfTest()
	 */
	@Override
	public List<PerfTest> getTestingPerfTest() {
		return getPerfTest(null, config.getRegion(), Status.getTestingTestStates());
	}

	/**
	 * Get abnormally testing PerfTest.
	 * 
	 * @return found {@link PerfTest} list
	 */
	public List<PerfTest> getAbnormalTestingPerfTest() {
		return getPerfTest(null, config.getRegion(), Status.ABNORMAL_TESTING);
	}

	/**
	 * Delete PerfTest by id.
	 * 
	 * Never use this method in runtime. This method is used only for testing.
	 * 
	 * @param user
	 *            user
	 * @param id
	 *            {@link PerfTest} id
	 */
	@Transactional
	public void deletePerfTest(User user, long id) {
		PerfTest perfTest = getPerfTest(id);
		// If it's not requested by user who started job. It's wrong request.
		if (!hasPermission(perfTest, user, Permission.DELETE_TEST_OFOTHER)) {
			return;
		}
		SortedSet<Tag> tags = perfTest.getTags();
		if (tags != null) {
			tags.clear();
		}
		perfTestRepository.save(perfTest);
		perfTestRepository.delete(perfTest);
		deletePerfTestDirectory(perfTest);
	}

	/**
	 * Delete {@link PerfTest} directory.
	 * 
	 * @param perfTest
	 *            perfTest
	 */
	private void deletePerfTestDirectory(PerfTest perfTest) {
		FileUtils.deleteQuietly(getPerfTestDirectory(perfTest));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.perftest.service.IPerfTestService#getPerfTestFilePath(org .ngrinder.perftest. model.PerfTest)
	 */
	@Override
	public File getPerfTestStatisticPath(PerfTest perfTest) {
		File perfTestStatisticPath = config.getHome().getPerfTestStatisticPath(perfTest);
		if (!perfTestStatisticPath.exists()) {
			perfTestStatisticPath.mkdirs();
		}
		return perfTestStatisticPath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.perftest.service.IPerfTestService#getPerfTestFilePath(org .ngrinder.perftest. model.PerfTest)
	 */
	@Override
	public File getPerfTestDistributionPath(PerfTest perfTest) {
		return config.getHome().getPerfTestDistDirectory(perfTest);
	}

	/**
	 * Build custom class path on the given {@link PerfTest}.
	 * 
	 * @param perfTest
	 *            perftest
	 * @return custom class path.
	 */
	public String getCustomClassPath(PerfTest perfTest) {
		File perfTestDirectory = getPerfTestDistributionPath(perfTest);
		File libFolder = new File(perfTestDirectory, "lib");

		final StringBuffer customClassPath = new StringBuffer();
		customClassPath.append(".");
		if (libFolder.exists()) {
			customClassPath.append(File.pathSeparator).append("lib");
			libFolder.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					if (name.endsWith(".jar")) {
						customClassPath.append(File.pathSeparator).append("lib/").append(name);
					}
					return true;
				}
			});
		}
		return customClassPath.toString();
	}

	/**
	 * Create {@link GrinderProperties} based on the passed {@link PerfTest}.
	 * 
	 * @param perfTest
	 *            base data
	 * @return created {@link GrinderProperties} instance
	 */
	public GrinderProperties getGrinderProperties(PerfTest perfTest) {
		return getGrinderProperties(perfTest, new NullScriptHandler());
	}

	/**
	 * Create {@link GrinderProperties} based on the passed {@link PerfTest}.
	 * 
	 * @param perfTest
	 *            base data
	 * @param scriptHandler
	 *            scriptHandler
	 * @return created {@link GrinderProperties} instance
	 */
	public GrinderProperties getGrinderProperties(PerfTest perfTest, ScriptHandler scriptHandler) {
		FileWriter fileWriter = null;
		try {
			// Use default properties first
			GrinderProperties grinderProperties = new GrinderProperties(config.getHome().getDefaultGrinderProperties());

			User user = perfTest.getCreatedUser();

			// Get all files in the script path
			String scriptName = perfTest.getScriptName();
			FileEntry userDefinedGrinderProperties = fileEntryService.getFileEntry(user,
					FilenameUtils.concat(FilenameUtils.getPath(scriptName), DEFAULT_GRINDER_PROPERTIES_PATH), -1);
			if (!config.isSecurityEnabled() && userDefinedGrinderProperties != null) {
				// Make the property overridden by user property.
				GrinderProperties userProperties = new GrinderProperties();
				userProperties.load(new StringReader(userDefinedGrinderProperties.getContent()));
				grinderProperties.putAll(userProperties);
			}
			grinderProperties.setAssociatedFile(new File(DEFAULT_GRINDER_PROPERTIES_PATH));
			grinderProperties.setProperty(GrinderProperties.SCRIPT, scriptHandler.getScriptExecutePath(scriptName));

			grinderProperties.setProperty(GRINDER_PROP_TEST_ID, "test_" + perfTest.getId());
			grinderProperties.setInt(GRINDER_PROP_AGENTS, perfTest.getAgentCount());
			grinderProperties.setInt(GRINDER_PROP_PROCESSES, perfTest.getProcesses());
			grinderProperties.setInt(GRINDER_PROP_THREAD, perfTest.getThreads());
			if (perfTest.isThresholdDuration()) {
				grinderProperties.setLong(GRINDER_PROP_DURATION, perfTest.getDuration());
				grinderProperties.setInt(GRINDER_PROP_RUNS, 0);
			} else {
				grinderProperties.setInt(GRINDER_PROP_RUNS, perfTest.getRunCount());
				if (grinderProperties.containsKey(GRINDER_PROP_DURATION)) {
					grinderProperties.remove(GRINDER_PROP_DURATION);
				}
			}
			grinderProperties.setProperty(NGRINDER_PROP_ETC_HOSTS,
					StringUtils.defaultIfBlank(perfTest.getTargetHosts(), ""));
			grinderProperties.setBoolean(GRINDER_PROP_USE_CONSOLE, true);
			if (BooleanUtils.isTrue(perfTest.getUseRampUp())) {
				grinderProperties.setInt(GRINDER_PROP_PROCESS_INCREMENT, perfTest.getProcessIncrement());
				grinderProperties.setInt(GRINDER_PROP_PROCESS_INCREMENT_INTERVAL,
						perfTest.getProcessIncrementInterval());
				grinderProperties.setInt(GRINDER_PROP_INITIAL_SLEEP_TIME, perfTest.getInitSleepTime());
			} else {
				grinderProperties.setInt(GRINDER_PROP_PROCESS_INCREMENT, 0);
			}
			grinderProperties.setInt(GRINDER_PROP_REPORT_TO_CONSOLE, 500);
			grinderProperties.setProperty(GRINDER_PROP_USER, perfTest.getCreatedUser().getUserId());
			grinderProperties.setProperty(GRINDER_PROP_JVM_CLASSPATH, getCustomClassPath(perfTest));
			grinderProperties.setProperty(GRINDER_PROP_JVM_CLASSPATH, getCustomClassPath(perfTest));
			grinderProperties.setInt(GRINDER_PROP_IGNORE_SAMPLE_COUNT, perfTest.getIgnoreSampleCount());
			grinderProperties.setBoolean(GRINDER_PROP_SECURITY, config.isSecurityEnabled());
			// For backward agent compatibility.
			// If the security is not enabled, pass it as jvm argument.
			// If enabled, pass it to grinder.param. In this case, I drop the
			// compatibility.
			if (StringUtils.isNotBlank(perfTest.getParam())) {
				String param = perfTest.getParam().replace("'", "\\'").replace(" ", "");
				if (config.isSecurityEnabled()) {
					grinderProperties.setProperty(GRINDER_PROP_PARAM, StringUtils.trimToEmpty(param));
				} else {
					String property = grinderProperties.getProperty(GRINDER_PROP_JVM_ARGUMENTS, "");
					property = property + " -Dparam=" + param + " ";
					grinderProperties.setProperty(GRINDER_PROP_JVM_ARGUMENTS, property);
				}
			}
			LOGGER.info("Grinder Properties : {} ", grinderProperties);
			return grinderProperties;
		} catch (Exception e) {
			throw processException("error while prepare grinder property for " + perfTest.getTestName(), e);
		} finally {
			IOUtils.closeQuietly(fileWriter);
		}
	}

	/**
	 * Prepare files for distribution. This method store the files on the path ${NGRINDER_HOME}/perftest/{test_id}/dist
	 * folder.
	 * 
	 * @param perfTest
	 *            perfTest
	 * @return File location in which the perftest script and resources are distributed.
	 */
	public ScriptHandler prepareDistribution(PerfTest perfTest) {
		File perfTestDistDirectory = getPerfTestDistributionPath(perfTest);
		perfTestDistDirectory.mkdirs();
		User user = perfTest.getCreatedUser();
		FileEntry scriptEntry = checkNotNull(
				fileEntryService.getFileEntry(user,
						checkNotEmpty(perfTest.getScriptName(), "perfTest should have script name"),
						perfTest.getScriptRevision()), "script should exist");
		// Get all files in the script path
		ScriptHandler handler = scriptHandlerFactory.getHandler(scriptEntry);

		ProcessingResultPrintStream processingResult = new ProcessingResultPrintStream(new ByteArrayOutputStream());
		handler.prepareDist(perfTest.getId(), user, scriptEntry, perfTestDistDirectory, config.getSystemProperties(),
				processingResult);
		LOGGER.info("File write is completed in {}", perfTestDistDirectory);
		if (!processingResult.isSuccess()) {
			File logDir = new File(getLogFileDirectory(perfTest), "distribution_log.txt");
			try {
				FileUtils.writeByteArrayToFile(logDir, processingResult.getLogByteArray());
			} catch (IOException e) {
				noOp();
			}
			throw processException("Error while file distribution is prepared.");
		}
		return handler;
	}

	/**
	 * Get the process and thread policy java script.
	 * 
	 * @return policy javascript
	 */
	public String getProcessAndThreadPolicyScript() {
		return config.getProcessAndThreadPolicyScript();
	}

	/**
	 * Get the optimal process and thread count.
	 * 
	 * 
	 * @param newVuser
	 *            the count of virtual users per agent
	 * @return optimal process thread count
	 */
	public ProcessAndThread calcProcessAndThread(int newVuser) {
		try {
			String script = getProcessAndThreadPolicyScript();
			ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
			engine.eval(script);
			int processCount = ((Double) engine.eval("getProcessCount(" + newVuser + ")")).intValue();
			int threadCount = ((Double) engine.eval("getThreadCount(" + newVuser + ")")).intValue();
			return new ProcessAndThread(processCount, threadCount);
		} catch (ScriptException e) {
			LOGGER.error("Error occurs while calc process and thread", e);
		}
		return new ProcessAndThread(1, 1);
	}

	/**
	 * get the data point interval of report data. Use dataPointCount / imgWidth as the interval. if interval is 1, it
	 * means we will get all point from report. If interval is 2, it means we will get 1 point from every 2 data.
	 * 
	 * @param testId
	 *            test id
	 * @param dataType
	 *            data type
	 * @param imgWidth
	 *            image width
	 * @return interval interval value
	 */
	public int getReportDataInterval(long testId, String dataType, int imgWidth) {
		int pointCount = Math.max(imgWidth, MAX_POINT_COUNT);
		File reportFolder = config.getHome().getPerfTestReportDirectory(String.valueOf(testId));
		int lineNumber;
		int interval = 0;
		File targetFile = new File(reportFolder, dataType + DATA_FILE_EXTENSION);
		if (!targetFile.exists()) {
			LOGGER.warn("Report data for {} in {} does not exist.", dataType, testId);
			return 0;
		}
		LineNumberReader lnr = null;

		FileInputStream in = null;
		InputStreamReader isr = null;
		try {
			in = new FileInputStream(targetFile);
			isr = new InputStreamReader(in);
			lnr = new LineNumberReader(isr);
			lnr.skip(targetFile.length());
			lineNumber = lnr.getLineNumber() + 1;
			interval = Math.max((int) (lineNumber / pointCount), 1);
		} catch (Exception e) {
			LOGGER.error("Failed to get report data for {}", dataType, e);
		} finally {
			IOUtils.closeQuietly(lnr);
			IOUtils.closeQuietly(isr);
			IOUtils.closeQuietly(in);
		}

		return interval;
	}

	/**
	 * Get report file(csv data) for give test .
	 * 
	 * @param perfTest
	 *            test
	 * @return reportFile data report file
	 */
	public File getReportFile(PerfTest perfTest) {
		return new File(config.getHome().getPerfTestReportDirectory(perfTest), NGrinderConstants.REPORT_CSV);
	}

	/**
	 * Get log file names for give test .
	 * 
	 * @param perfTest
	 *            perfTest
	 * @param fileName
	 *            file name of one logs of the test
	 * @return file report file path
	 */
	public File getLogFile(PerfTest perfTest, String fileName) {
		return new File(getLogFileDirectory(perfTest), fileName);
	}

	/**
	 * Get log file names for give test id.
	 * 
	 * @param testId
	 *            test id
	 * @param fileName
	 *            file name of one logs of the test
	 * @return file report file path
	 */
	public File getLogFile(long testId, String fileName) {
		return new File(getLogFileDirectory(String.valueOf(testId)), fileName);
	}

	/**
	 * Get report file directory for give test .
	 * 
	 * @param perfTest
	 *            perfTest
	 * @return logDir log file path of the test
	 */

	public File getLogFileDirectory(PerfTest perfTest) {
		return config.getHome().getPerfTestLogDirectory(perfTest);
	}

	/**
	 * Get report file directory for give test id.
	 * 
	 * @param testId
	 *            test id
	 * @return logDir log file path of the test
	 */

	public File getLogFileDirectory(String testId) {
		return config.getHome().getPerfTestLogDirectory(testId);
	}

	/**
	 * Get log files list on the given test is.
	 * 
	 * @param testId
	 *            testId
	 * @return logFilesList log file list of that test
	 */
	public List<String> getLogFiles(long testId) {
		File logFileDirectory = getLogFileDirectory(String.valueOf(testId));
		if (!logFileDirectory.exists() || !logFileDirectory.isDirectory()) {
			return Collections.emptyList();
		}
		return Arrays.asList(logFileDirectory.list());
	}

	/**
	 * Get report file directory for give test id .
	 * 
	 * @param testId
	 *            testId
	 * @return reportDir report file path
	 */
	public File getReportFileDirectory(long testId) {
		return config.getHome().getPerfTestReportDirectory(String.valueOf(testId));
	}

	/**
	 * Get report file directory for give test .
	 * 
	 * @param perfTest
	 *            perftest
	 * @return reportDir report file path
	 */
	public File getReportFileDirectory(PerfTest perfTest) {
		return config.getHome().getPerfTestReportDirectory(perfTest);
	}

	/**
	 * To save statistics data when test is running and put into cache after that. If the console is not available, it
	 * returns null.
	 * 
	 * @param singleConsole
	 *            console signle console.
	 * @param perfTestId
	 *            perfTest Id
	 * @return statistics
	 */
	/**
	 * To save statistics data when test is running and put into cache after that. If the console is not available, it
	 * returns null.
	 * 
	 * @param singleConsole
	 *            single console.
	 * @param perfTestId
	 *            perfTest Id
	 */
	@Transactional
	public void saveStatistics(SingleConsole singleConsole, Long perfTestId) {
		String runningSample = getProperSizeRunningSample(singleConsole);
		String agentStatus = getProperSizedStatusString(singleConsole);
		updateRuntimeStatistics(perfTestId, runningSample, agentStatus);
	}

	private String getProperSizeRunningSample(SingleConsole singleConsole) {
		Map<String, Object> statisticData = singleConsole.getStatisticsData();
		String runningSample = gson.toJson(statisticData);

		if (runningSample.length() > 9950) { // max column size is 10,000
			Map<String, Object> tempData = newHashMap();
			for (Entry<String, Object> each : statisticData.entrySet()) {
				String key = each.getKey();
				if (key.equals("totalStatistics") || key.equals("cumulativeStatistics")
						|| key.equals("lastSampleStatistics")) {
					continue;
				}
				tempData.put(key, each.getValue());
			}
			runningSample = gson.toJson(tempData);
		}
		return runningSample;
	}

	/**
	 * Get the limited size of agent status json string.
	 * 
	 * @param singleConsole
	 *            console which is connecting agents
	 * @return converted json
	 */
	public String getProperSizedStatusString(SingleConsole singleConsole) {
		Map<String, SystemDataModel> agentStatusMap = Maps.newHashMap();
		final int singleConsolePort = singleConsole.getConsolePort();
		for (AgentStatus each : agentManager.getAgentStatusSetConnectingToPort(singleConsolePort)) {
			agentStatusMap.put(each.getAgentName(), each.getSystemDataModel());
		}
		return getProperSizedStatusString(agentStatusMap);
	}

	String getProperSizedStatusString(Map<String, SystemDataModel> agentStatusMap) {
		String json = gson.toJson(agentStatusMap);
		int statusLength = StringUtils.length(json);
		if (statusLength > 9950) { // max column size is 10,000
			LOGGER.info("Agent status string length: {}, too long to save into table.", statusLength);
			double ratio = 9900.0 / statusLength;
			int pickSize = (int) (agentStatusMap.size() * ratio);
			Map<String, SystemDataModel> pickAgentStatusMap = Maps.newHashMap();

			int pickIndex = 0;
			for (Entry<String, SystemDataModel> each : agentStatusMap.entrySet()) {
				if (pickIndex < pickSize) {
					pickAgentStatusMap.put(each.getKey(), each.getValue());
					pickIndex++;
				}
			}
			json = gson.toJson(pickAgentStatusMap);
			LOGGER.debug("Agent status string get:{} of:{} agents, new size is: {}.", new Object[] { pickSize,
					agentStatusMap.size(), json.length() });
		}
		return json;
	}

	/**
	 * get test running statistic data from cache. If there is no cache data, will return empty statistic data.
	 * 
	 * @param perfTest
	 *            perfTest
	 * 
	 * @return test running statistic data
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getStatistics(PerfTest perfTest) {
		return gson.fromJson(perfTest.getRunningSample(), HashMap.class);
	}

	/**
	 * get test running statistic data from cache. If there is no cache data, will return empty statistic data.
	 * 
	 * @param perfTest
	 *            perfTest
	 * 
	 * @return test running statistic data
	 */
	public String getStatisticsJson(PerfTest perfTest) {
		return perfTest.getRunningSample();
	}

	private Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

	/**
	 * Save system monitor data of all agents connected to one console. If the console is not available, it returns
	 * empty map. After getting, it will be put into cache.
	 * 
	 * @param singleConsole
	 *            singleConsole of the test add this parameter just for the key of cache.
	 * @param perfTest
	 *            perfTest
	 */
	public void saveAgentsInfo(SingleConsole singleConsole, PerfTest perfTest) {
		savePerfTest(perfTest);
	}

	/**
	 * Get agent info from saved file.
	 * 
	 * @param perfTest
	 *            perftest
	 * @return agent info map
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, HashMap> getAgentStat(PerfTest perfTest) {
		return gson.fromJson(perfTest.getAgentStatus(), HashMap.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.perftest.service.IPerfTestService#getAllPerfTest()
	 */
	@Override
	public List<PerfTest> getAllPerfTest() {
		return perfTestRepository.findAll();
	}

	/**
	 * Create {@link ConsoleProperties} based on given {@link PerfTest} instance.
	 * 
	 * @param perfTest
	 *            perfTest
	 * @return {@link ConsoleProperties}
	 */
	public ConsoleProperties createConsoleProperties(PerfTest perfTest) {
		ConsoleProperties consoleProperties = ConsolePropertiesFactory.createEmptyConsoleProperties();
		try {
			consoleProperties.setAndSaveDistributionDirectory(new Directory(getPerfTestDistributionPath(perfTest)));
			consoleProperties.setConsoleHost(config.getCurrentIP());
			consoleProperties.setIgnoreSampleCount(perfTest.getIgnoreSampleCount());
			consoleProperties.setSampleInterval(1000 * perfTest.getSamplingInterval());
		} catch (Exception e) {
			throw processException("Error while setting console properties", e);
		}
		return consoleProperties;
	}

	double parseDoubleWithSafety(Map<?, ?> map, Object key, Double defaultValue) {
		Double doubleValue = MapUtils.getDouble(map, key, defaultValue);
		return Math.round(doubleValue * 100D) / 100D;
	}

	/**
	 * Check if the given perfTest has too many errors. (20%)
	 * 
	 * @param perfTest
	 *            perftes t
	 * @return true if too many errors.
	 */
	@SuppressWarnings("unchecked")
	public boolean hasTooManError(PerfTest perfTest) {
		Map<String, Object> result = getStatistics(perfTest);
		Map<String, Object> totalStatistics = MapUtils.getMap(result, "totalStatistics", MapUtils.EMPTY_MAP);
		long tests = MapUtils.getDouble(totalStatistics, "Tests", 0D).longValue();
		long errors = MapUtils.getDouble(totalStatistics, "Errors", 0D).longValue();
		return ((((double) errors) / (tests + errors)) > 0.3d);
	}

	/**
	 * Update the given {@link PerfTest} properties after test finished.
	 * 
	 * @param perfTest
	 *            perfTest
	 */
	public void updatePerfTestAfterTestFinish(PerfTest perfTest) {
		checkNotNull(perfTest);
		Map<String, Object> result = consoleManager.getConsoleUsingPort(perfTest.getPort()).getStatisticsData();
		@SuppressWarnings("unchecked")
		Map<String, Object> totalStatistics = MapUtils.getMap(result, "totalStatistics", MapUtils.EMPTY_MAP);
		LOGGER.info("Total Statistics for test {}  is {}", perfTest.getId(), totalStatistics);
		perfTest.setTps(parseDoubleWithSafety(totalStatistics, "TPS", 0D));
		perfTest.setMeanTestTime(parseDoubleWithSafety(totalStatistics, "Mean_Test_Time_(ms)", 0D));
		perfTest.setPeakTps(parseDoubleWithSafety(totalStatistics, "Peak_TPS", 0D));
		perfTest.setTests(MapUtils.getDouble(totalStatistics, "Tests", 0D).longValue());
		perfTest.setErrors(MapUtils.getDouble(totalStatistics, "Errors", 0D).longValue());

	}

	/**
	 * Get maximum concurrent test count.
	 * 
	 * @return maximum concurrent test
	 */
	public int getMaximumConcurrentTestCount() {
		return config.getSystemProperties().getPropertyInt(NGrinderConstants.NGRINDER_PROP_MAX_CONCURRENT_TEST,
				NGrinderConstants.NGRINDER_PROP_MAX_CONCURRENT_TEST_VALUE);
	}

	/**
	 * Check the test can be executed more.
	 * 
	 * @return true if possible
	 */
	public boolean canExecuteTestMore() {
		return getPerfTestCount(null, Status.getProcessingOrTestingTestStatus()) < getMaximumConcurrentTestCount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.perftest.service.IPerfTestService#stopPerfTest(org.ngrinder .model.User, java.lang.Long)
	 */
	@Override
	@Transactional
	public void stopPerfTest(User user, Long id) {
		PerfTest perfTest = getPerfTest(id);
		// If it's not requested by user who started job. It's wrong request.
		if (!hasPermission(perfTest, user, Permission.STOP_TEST_OFOTHER)) {
			return;
		}
		// If it's not stoppable status.. It's wrong request.
		if (!perfTest.getStatus().isStoppable()) {
			return;
		}
		// Just mark cancel on console
		consoleManager.getConsoleUsingPort(perfTest.getPort()).cancel();
		perfTest.setStopRequest(true);
		perfTestRepository.save(perfTest);
	}

	/**
	 * Check if given user has a permission on perftest.
	 * 
	 * @param perfTest
	 *            perf test
	 * @param user
	 *            user
	 * @param type
	 *            permission type to check
	 * @return true if it has
	 */
	public boolean hasPermission(PerfTest perfTest, User user, Permission type) {
		if (perfTest == null) {
			return false;
		}
		if (perfTest.getCreatedUser().equals(user)) {
			return true;
		}
		return user.getRole().hasPermission(type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.perftest.service.IPerfTestService#getStopRequestedPerfTest()
	 */
	@Override
	public List<PerfTest> getStopRequestedPerfTest() {
		final List<PerfTest> perfTests = getPerfTest(null, config.getRegion(), getProcessingOrTestingTestStatus());
		CollectionUtils.filter(perfTests, new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				return (((PerfTest) object).getStopRequest() == Boolean.TRUE);
			}
		});
		return perfTests;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.perftest.service.IPerfTestService#addCommentOn(org.ngrinder .model.User, int, java.lang.String)
	 */
	@Override
	@Transactional
	public void addCommentOn(User user, Long testId, String testComment, String tagString) {
		PerfTest perfTest = getPerfTest(user, testId);
		perfTest.setTestComment(testComment);
		perfTest.setTagString(tagString);
		perfTest.setTags(tagSerivce.addTags(user, StringUtils.split(StringUtils.trimToEmpty(tagString), ",")));
		perfTestRepository.save(perfTest);
	}

	/**
	 * get current running test status, which is, how many user run how many tests with some agents.
	 * 
	 * @return PerfTestStatisticsList PerfTestStatistics list
	 */
	@Cacheable("current_perftest_statistics")
	@Transactional
	public Collection<PerfTestStatistics> getCurrentPerfTestStatistics() {
		Map<User, PerfTestStatistics> perfTestPerUser = newHashMap();
		for (PerfTest each : getPerfTest(null, getProcessingOrTestingTestStatus())) {
			User lastModifiedUser = each.getCreatedUser().getUserBaseInfo();
			PerfTestStatistics perfTestStatistics = perfTestPerUser.get(lastModifiedUser);
			if (perfTestStatistics == null) {
				perfTestStatistics = new PerfTestStatistics(lastModifiedUser);
				perfTestPerUser.put(lastModifiedUser, perfTestStatistics);
			}
			perfTestStatistics.addPerfTest(each);
		}
		return perfTestPerUser.values();
	}

	/**
	 * Get PerfTest directory in which {@link PerfTest} related files are saved.
	 * 
	 * @param perfTest
	 *            perfTest
	 * @return directory
	 */
	@Override
	public File getPerfTestDirectory(PerfTest perfTest) {
		return config.getHome().getPerfTestDirectory(perfTest);
	}

	/**
	 * Delete All PerfTests and related tags belonging to given user.
	 * 
	 * @param user
	 *            user
	 * @return deleted {@link PerfTest} list
	 */
	@Transactional
	public List<PerfTest> deleteAllPerfTests(User user) {
		List<PerfTest> perfTestList = getPerfTestList(user);
		for (PerfTest each : perfTestList) {
			each.getTags().clear();
		}
		perfTestRepository.save(perfTestList);
		perfTestRepository.flush();
		perfTestRepository.delete(perfTestList);
		perfTestRepository.flush();
		tagSerivce.deleteTags(user);
		return perfTestList;
	}

	/**
	 * Remove given tag on the collection of {@link PerfTest}.
	 * 
	 * @param perfTests
	 *            perftest collection
	 * @param tag
	 *            tag to be deleted
	 */
	@Transactional
	public void removeTag(Collection<PerfTest> perfTests, Tag tag) {
		for (PerfTest each : perfTests) {
			each.getTags().remove(tag);
		}
		perfTestRepository.save(perfTests);
	}

	public PerfTestRepository getPerfTestRepository() {
		return perfTestRepository;
	}

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

	/**
	 * Delete the distribution folder for the give perf test.
	 * 
	 * @param perfTest
	 *            perf test
	 */
	public void cleanUpDistFolder(PerfTest perfTest) {
		FileUtils.deleteQuietly(getPerfTestDistributionPath(perfTest));
	}

	/**
	 * Clean up the data which is used in runtime only.
	 * 
	 * @param perfTest
	 *            perfTest
	 */
	public void cleanUpRuntimeOnlyData(PerfTest perfTest) {
		perfTest.setRunningSample(null);
		perfTest.setAgentStatus(null);
		perfTest.setMonitorStatus(null);
		savePerfTest(perfTest);
	}

	/**
	 * Put the given {@link org.ngrinder.monitor.share.domain.SystemInfo} maps into the given perftest entity.
	 * 
	 * @param perfTestId
	 *            id of perf test
	 * @param systemInfos
	 *            systemDataModel map
	 */
	@Transactional
	public void updateMonitorStat(Long perfTestId, Map<String, SystemDataModel> systemInfos) {
		String json = gson.toJson(systemInfos);
		if (json.length() >= 2000) {
			Map<String, SystemDataModel> systemInfo = Maps.newHashMap();
			int i = 0;
			for (Entry<String, SystemDataModel> each : systemInfos.entrySet()) {
				if (i++ > 3) {
					break;
				}
				systemInfo.put(each.getKey(), each.getValue());
			}
			json = gson.toJson(systemInfo);
		}
		perfTestRepository.updatetMonitorStatus(perfTestId, json);
	}

	/**
	 * Get monitor status map for the given perfTest.
	 * 
	 * @param perfTest
	 *            perf test
	 * @return map of monitor name and monitor status.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, HashMap> getMonitorStat(PerfTest perfTest) {
		return gson.fromJson(perfTest.getMonitorStatus(), HashMap.class);
	}

	/**
	 * Get the monitor data interval value. In the normal, the image width is 700, and if the data count is too big,
	 * there will be too many points in the chart. So we will calculate the interval to get appropriate count of data to
	 * display. For example, interval value "2" means, get one record for every "2" records.
	 * 
	 * @param testId
	 *            test id
	 * @param monitorIP
	 *            ip address of monitor target
	 * @param imageWidth
	 *            image with of the chart.
	 * @return interval value.
	 */
	public int getSystemMonitorDataInterval(long testId, String monitorIP, int imageWidth) {
		File monitorDataFile = new File(config.getHome().getPerfTestReportDirectory(String.valueOf(testId)),
				NGrinderConstants.MONITOR_FILE_PREFIX + monitorIP + ".data");

		int pointCount = Math.max(imageWidth, MAX_POINT_COUNT);
		FileInputStream in = null;
		InputStreamReader isr = null;
		LineNumberReader lnr = null;
		int lineNumber = 0;
		int interval = 0;
		try {
			in = new FileInputStream(monitorDataFile);
			isr = new InputStreamReader(in);
			lnr = new LineNumberReader(isr);
			lnr.skip(monitorDataFile.length());
			lineNumber = lnr.getLineNumber() + 1;
			interval = Math.max((int) (lineNumber / pointCount), 1);
		} catch (FileNotFoundException e) {
			LOGGER.error("Monitor data file not exist:{}", monitorDataFile);
			LOGGER.error(e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.error("Error while getting monitor:{} data file:{}", monitorIP, monitorDataFile);
			LOGGER.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(lnr);
			IOUtils.closeQuietly(isr);
			IOUtils.closeQuietly(in);
		}
		return interval;
	}

	/**
	 * Get system monitor data and wrap the data as a string value like "[22,11,12,34,....]", which can be used directly
	 * in JS as a vector.
	 * 
	 * @param testId
	 *            test id
	 * @param monitorIP
	 *            ip address of the monitor target
	 * @param dataInterval
	 *            interval value to get data. Interval value "2" means, get one record for every "2" records.
	 * @return return the data in map
	 */
	public Map<String, String> getSystemMonitorDataAsString(long testId, String monitorIP, int dataInterval) {
		Map<String, String> returnMap = Maps.newHashMap();
		File monitorDataFile = new File(config.getHome().getPerfTestReportDirectory(String.valueOf(testId)),
				NGrinderConstants.MONITOR_FILE_PREFIX + monitorIP + ".data");
		BufferedReader br = null;
		try {

			StringBuilder sbUsedMem = new StringBuilder("[");
			StringBuilder sbCPUUsed = new StringBuilder("[");
			StringBuilder sbNetReceived = new StringBuilder("[");
			StringBuilder sbNetSent = new StringBuilder("[");
			StringBuilder customData1 = new StringBuilder("[");
			StringBuilder customData2 = new StringBuilder("[");
			StringBuilder customData3 = new StringBuilder("[");
			StringBuilder customData4 = new StringBuilder("[");
			StringBuilder customData5 = new StringBuilder("[");

			br = new BufferedReader(new FileReader(monitorDataFile));
			br.readLine(); // skip the header.
			// "ip,system,collectTime,freeMemory,totalMemory,cpuUsedPercentage,receivedPerSec,sentPerSec"
			String line = br.readLine();
			int skipCount = dataInterval;
			// to be compatible with previous version, check the length before
			// adding
			while (StringUtils.isNotBlank(line)) {
				if (skipCount < dataInterval) {
					skipCount++;
					continue;
				} else {
					skipCount = 1;
					String[] datalist = StringUtils.split(line, ",");
					if ("null".equals(datalist[4]) || "undefined".equals(datalist[4])) {
						sbUsedMem.append("null").append(",");
					} else {
						sbUsedMem.append(Long.valueOf(datalist[4]) - Long.valueOf(datalist[3])).append(",");
					}
					addCustomData(sbCPUUsed, 5, datalist);
					addCustomData(sbNetReceived, 6, datalist);
					addCustomData(sbNetSent, 7, datalist);
					addCustomData(customData1, 8, datalist);
					addCustomData(customData2, 9, datalist);
					addCustomData(customData3, 10, datalist);
					addCustomData(customData4, 11, datalist);
					addCustomData(customData5, 12, datalist);
					line = br.readLine();
				}
			}
			completeCustomData(returnMap, "cpu", sbCPUUsed);
			completeCustomData(returnMap, "memory", sbUsedMem);
			completeCustomData(returnMap, "received", sbNetReceived);
			completeCustomData(returnMap, "sent", sbNetSent);
			completeCustomData(returnMap, "customData1", customData1);
			completeCustomData(returnMap, "customData2", customData2);
			completeCustomData(returnMap, "customData3", customData3);
			completeCustomData(returnMap, "customData4", customData4);
			completeCustomData(returnMap, "customData5", customData5);
		} catch (IOException e) {
			LOGGER.error("Error while getting monitor:{} data file:{}", monitorIP, monitorDataFile);
			LOGGER.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(br);
		}
		return returnMap;
	}

	private void addCustomData(StringBuilder customData, int index, String[] data) {
		if (data.length > index) {
			customData.append(data[index]).append(",");
		}
	}

	private void completeCustomData(Map<String, String> returnMap, String key, StringBuilder customData) {
		if (customData.charAt(customData.length() - 1) == ',') {
			customData.deleteCharAt(customData.length() - 1);
		}
		returnMap.put(key, customData.append("]").toString());
	}

	/**
	 * Get all{@link SystemDataModel} from monitor data file of one test and target.
	 * 
	 * @param testId
	 *            test id
	 * @param monitorIP
	 *            IP address of the monitor target
	 * @return SystemDataModel list
	 */
	public List<SystemDataModel> getSystemMonitorData(long testId, String monitorIP) {
		LOGGER.debug("Get SystemMonitorData of test:{} ip:{}", testId, monitorIP);
		List<SystemDataModel> rtnList = Lists.newArrayList();
		File monitorDataFile = new File(config.getHome().getPerfTestReportDirectory(String.valueOf(testId)),
				NGrinderConstants.MONITOR_FILE_PREFIX + monitorIP + ".data");
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(monitorDataFile));
			br.readLine(); // skip the header.
			// header:
			// "ip,system,collectTime,freeMemory,totalMemory,cpuUsedPercentage"
			String line = br.readLine();
			while (StringUtils.isNotBlank(line)) {
				SystemDataModel model = new SystemDataModel();
				String[] datalist = StringUtils.split(line, ",");
				model.setIp(datalist[0]);
				model.setSystem(datalist[1]);
				model.setCollectTime(NumberUtils.toLong(datalist[2]));
				model.setFreeMemory(NumberUtils.toLong(datalist[3]));
				model.setTotalMemory(NumberUtils.toLong(datalist[4]));
				model.setCpuUsedPercentage(NumberUtils.toFloat(datalist[5]));
				rtnList.add(model);
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			LOGGER.error("Monitor data file not exist:{}", monitorDataFile);
			LOGGER.error(e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.error("Error while getting monitor:{} data file:{}", monitorIP, monitorDataFile);
			LOGGER.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(br);
		}
		LOGGER.debug("Finish getSystemMonitorData of test:{} ip:{}", testId, monitorIP);
		return rtnList;
	}

	/**
	 * Get list that contains test report data as a json string.
	 * 
	 * @param testId
	 *            test id
	 * @param key
	 *            key
	 * @param interval
	 *            interval to collect data
	 * @return json list
	 */
	public String getSingleReportDataAsJson(long testId, String key, int interval) {
		File reportDataFile = getReportDataFile(testId, key);
		return getFileDataAsJson(reportDataFile, interval);
	}

	/**
	 * Get list that contains test report data as a string.
	 * 
	 * @param testId
	 *            test id
	 * @param key
	 *            report key
	 * @param interval
	 *            interval to collect data
	 * @return list containing label and tps value list
	 */
	public Pair<ArrayList<String>, ArrayList<String>> getReportData(long testId, String key, int interval) {
		Pair<ArrayList<String>, ArrayList<String>> resultPair = Pair.of(new ArrayList<String>(),
				new ArrayList<String>());
		for (File file : getReportDataFiles(testId, key)) {
			String buildReportName = buildReportName(key, file);
			if (key.equals(buildReportName)) {
				buildReportName = "Total";
			} else {
				buildReportName = buildReportName.replace("_", " ");
			}
			resultPair.getFirst().add(buildReportName);
			resultPair.getSecond().add(getFileDataAsJson(file, interval));
		}
		return resultPair;
	}

	private String buildReportName(String key, File file) {
		String reportName = FilenameUtils.removeExtension(file.getName());
		if (key.equals(reportName)) {
			return reportName;
		}
		String[] baseName = StringUtils.split(reportName, "-", 2);
		if (SingleConsole.INTERESTING_PER_TEST_STATISTICS.contains(baseName[0]) && baseName.length >= 2) {
			reportName = baseName[1];
		}
		return reportName;
	}

	/**
	 * Get a single file for the given report key.
	 * 
	 * @param testId
	 *            test id
	 * @param key
	 *            key
	 * @return return file
	 */
	public File getReportDataFile(long testId, String key) {
		File reportFolder = config.getHome().getPerfTestReportDirectory(String.valueOf(testId));
		return new File(reportFolder, key + ".data");
	}

	/**
	 * Get files respectively if there are multiple tests.
	 * 
	 * @param testId
	 *            test id
	 * @param key
	 *            report key
	 * @return return file list
	 */
	public List<File> getReportDataFiles(long testId, String key) {
		File reportFolder = config.getHome().getPerfTestReportDirectory(String.valueOf(testId));
		FileFilter fileFilter = new WildcardFileFilter(key + "*.data");
		File[] files = reportFolder.listFiles(fileFilter);
		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return FilenameUtils.getBaseName(o1.getName()).compareTo(FilenameUtils.getBaseName(o2.getName()));
			}
		});
		return Arrays.asList(files);
	}

	/**
	 * Get the test report data as a json string.
	 * 
	 * @param targetFile
	 *            target file
	 * @param interval
	 *            interval to collect data
	 * @return json string
	 */
	private String getFileDataAsJson(File targetFile, int interval) {
		if (!targetFile.exists()) {
			return "[]";
		}
		StringBuilder reportData = new StringBuilder("[");
		FileReader reader = null;
		BufferedReader br = null;
		try {
			reader = new FileReader(targetFile);
			br = new BufferedReader(reader);
			String data = br.readLine();
			int current = 0;
			while (StringUtils.isNotBlank(data)) {
				if (0 == current) {
					reportData.append(data);
					reportData.append(",");
				}
				if (++current >= interval) {
					current = 0;
				}
				data = br.readLine();
			}
			if (reportData.charAt(reportData.length() - 1) == ',') {
				reportData.deleteCharAt(reportData.length() - 1);
			}
		} catch (IOException e) {
			LOGGER.error("Report data retrieval is failed: {}", e.getMessage());
			LOGGER.debug("Trace is : ", e);
		} finally {
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(br);
		}
		return reportData.append("]").toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.service.IPerfTestService#getPerfTest(java.util.Date, java.util.Date)
	 */
	@Override
	public List<PerfTest> getPerfTest(Date start, Date end) {
		return perfTestRepository.findAllByCreatedTime(start, end);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.service.IPerfTestService#getPerfTest(java.util.Date, java.util.Date, java.lang.String)
	 */
	@Override
	public List<PerfTest> getPerfTest(Date start, Date end, String region) {
		return perfTestRepository.findAllByCreatedTimeAndRegion(start, end, region);
	}
}
