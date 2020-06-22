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

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.grinder.SingleConsole;
import net.grinder.StopReason;
import net.grinder.common.GrinderProperties;
import net.grinder.console.communication.AgentProcessControlImplementation.AgentStatus;
import net.grinder.console.model.ConsoleProperties;
import net.grinder.util.ConsolePropertiesFactory;
import net.grinder.util.Directory;
import net.grinder.util.Pair;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.common.constants.GrinderConstants;
import org.ngrinder.common.exception.PerfTestPrepareException;
import org.ngrinder.common.util.JsonUtils;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.hazelcast.HazelcastService;
import org.ngrinder.model.*;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.perftest.model.PerfTestStatistics;
import org.ngrinder.perftest.model.ProcessAndThread;
import org.ngrinder.perftest.model.SamplingModel;
import org.ngrinder.perftest.repository.PerfTestRepository;
import org.ngrinder.script.handler.NullScriptHandler;
import org.ngrinder.script.handler.ProcessingResultPrintStream;
import org.ngrinder.script.handler.ScriptHandler;
import org.ngrinder.script.handler.ScriptHandlerFactory;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.GitHubConfig;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.script.service.GitHubFileEntryService;
import org.ngrinder.service.AbstractPerfTestService;
import org.python.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import static java.lang.Long.valueOf;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static java.util.Arrays.asList;
import static net.grinder.SingleConsole.REPORT_DATA;
import static org.apache.commons.io.FilenameUtils.removeExtension;
import static org.ngrinder.common.constant.CacheConstants.*;
import static org.ngrinder.common.constants.MonitorConstants.MONITOR_FILE_PREFIX;
import static org.ngrinder.common.util.AccessUtils.getSafe;
import static org.ngrinder.common.util.AopUtils.proxy;
import static org.ngrinder.common.util.CollectionUtils.*;
import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.NoOp.noOp;
import static org.ngrinder.common.util.Preconditions.checkNotEmpty;
import static org.ngrinder.common.util.Preconditions.checkNotNull;
import static org.ngrinder.common.util.TypeConvertUtils.cast;
import static org.ngrinder.model.Status.PREPARE_DISTRIBUTION;
import static org.ngrinder.model.Status.getProcessingOrTestingTestStatus;
import static org.ngrinder.perftest.repository.PerfTestSpecification.*;

/**
 * {@link PerfTest} Service Class.
 * <p/>
 * This class contains various method which mainly get the {@link PerfTest} matching specific conditions from DB.
 *
 * @since 3.0
 */
@RequiredArgsConstructor
public class PerfTestService extends AbstractPerfTestService implements ControllerConstants, GrinderConstants {

	private static final int MAX_POINT_COUNT = 100;

	private static final Logger LOGGER = LoggerFactory.getLogger(PerfTestService.class);

	private static final String NULL_STRING = "null";
	private static final String UNDEFINED_STRING = "undefined";

	@Getter
	private final PerfTestRepository perfTestRepository;

	private final ConsoleManager consoleManager;

	private final AgentManager agentManager;

	@Getter
	private final Config config;

	private final FileEntryService fileEntryService;

	private final TagService tagService;

	private final ScriptHandlerFactory scriptHandlerFactory;

	private final HazelcastService hazelcastService;

	private final GitHubFileEntryService gitHubFileEntryService;

	/**
	 * Get {@link PerfTest} list for the given user.
	 *
	 * @param user        user
	 * @param query       query string on test name or description
	 * @param tag         search tag.
	 * @param queryFilter "S" for querying scheduled test, "F" for querying finished test
	 * @param pageable    paging info
	 * @return found {@link PerfTest} list
	 */
	public Page<PerfTest> getPagedAll(User user, String query, String tag, String queryFilter, Pageable pageable) {
		Specification<PerfTest> spec = Specification.where(idEmptyPredicate());
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
		} else if ("RS".equals(queryFilter) || "SR".equals(queryFilter)) {
			spec = spec.and(statusSetEqual(Status.TESTING, Status.READY));
		}
		if (StringUtils.isNotBlank(query)) {
			spec = spec.and(likeTestNameOrDescription(query));
		}
		return perfTestRepository.findAll(spec, pageable);
	}

	/**
	 * Get {@link PerfTest} list on the user.
	 *
	 * @param user user
	 * @return found {@link PerfTest} list
	 */
	private List<PerfTest> getAll(User user) {
		Specification<PerfTest> spec = Specification.where(createdBy(user));
		return perfTestRepository.findAll(spec);
	}


	@Override
	public PerfTest getOne(User user, Long id) {
		Specification<PerfTest> spec = Specification.where(idEmptyPredicate());

		// User can see only his own test
		if (user.getRole().equals(Role.USER)) {
			spec = spec.and(createdBy(user));
		}
		spec = spec.and(idEqual(id));

		return perfTestRepository.findOne(spec).orElse(null);
	}

	@Override
	public List<PerfTest> getAll(User user, Long[] ids) {
		if (ids.length == 0) {
			return newArrayList();
		}
		Specification<PerfTest> spec = Specification.where(idEmptyPredicate());

		// User can see only his own test
		if (user.getRole().equals(Role.USER)) {
			spec = spec.and(createdBy(user));
		}
		spec = spec.and(idSetEqual(ids));
		return perfTestRepository.findAll(spec);
	}

	@Override
	public long count(User user, Status[] statuses) {
		Specification<PerfTest> spec = Specification.where(idEmptyPredicate());

		// User can see only his own test
		if (user != null) {
			spec = spec.and(createdBy(user));
		}

		if (statuses.length == 0) {
			return 0;
		} else {
			return perfTestRepository.count(spec.and(statusSetEqual(statuses)));
		}

	}

	@Override
	public List<PerfTest> getAll(User user, Status[] statuses) {
		Specification<PerfTest> spec = Specification.where(idEmptyPredicate());

		// User can see only his own test
		if (user != null) {
			spec = spec.and(createdBy(user));
		}
		if (statuses.length != 0) {
			spec = spec.and(statusSetEqual(statuses));
		}

		return perfTestRepository.findAll(spec);
	}

	private List<PerfTest> getAll(User user, String region, Status[] statuses) {
		Specification<PerfTest> spec = Specification.where(idEmptyPredicate());
		// User can see only his own test
		if (user != null) {
			spec = spec.and(createdBy(user));
		}
		if (config.isClustered()) {
			spec = spec.and(idRegionEqual(region));
		}
		if (statuses.length != 0) {
			spec = spec.and(statusSetEqual(statuses));
		}

		return perfTestRepository.findAll(spec);
	}


	@Override
	@Transactional
	public PerfTest save(User user, PerfTest perfTest) {
		attachFileRevision(user, perfTest);
		attachTags(user, perfTest, perfTest.getTagString());
		return save(perfTest);
	}

	private PerfTest save(PerfTest perfTest) {
		checkNotNull(perfTest);
		// Merge if necessary
		if (perfTest.exist()) {
			Optional<PerfTest> existingPerfTest = perfTestRepository.findOne(idEqual(perfTest.getId()));
			existingPerfTest.ifPresent(perfTest1 -> perfTest1.merge(perfTest));
		} else {
			perfTest.clearMessages();
			perfTestRepository.saveAndFlush(perfTest);
		}
		return perfTest;
	}

	private void attachFileRevision(User user, PerfTest perfTest) {
		if (perfTest.getStatus() == Status.READY) {
			if (perfTest.isGitHubScm()) {
				perfTest.setScriptRevision(String.valueOf(perfTest.getScriptRevision()));
			} else {
				FileEntry scriptEntry = fileEntryService.getOne(user, perfTest.getScriptName());
				long revision = scriptEntry != null ? scriptEntry.getRevision() : -1L;
				perfTest.setScriptRevision(String.valueOf(revision));
			}
		}
	}

	private void attachTags(User user, PerfTest perfTest, String tagString) {
		SortedSet<Tag> tags = tagService.addTags(user,
			StringUtils.split(StringUtils.trimToEmpty(tagString), ","));
		perfTest.setTags(tags);
		perfTest.setTagString(buildTagString(tags));
	}

	private String buildTagString(Set<Tag> tags) {
		List<String> tagStringResult = new ArrayList<>();
		for (Tag each : tags) {
			tagStringResult.add(each.getTagValue());
		}
		return StringUtils.join(tagStringResult, ",");
	}

	/**
	 * Mark test error on {@link PerfTest} instance.
	 *
	 * @param perfTest {@link PerfTest}
	 * @param reason   stop reason
	 * @return perftest with updated data
	 */
	@Transactional
	public PerfTest markAbnormalTermination(PerfTest perfTest, StopReason reason) {
		return markAbnormalTermination(perfTest, reason.getDisplay());
	}

	/**
	 * Mark test error on {@link PerfTest} instance.
	 *
	 * @param perfTest {@link PerfTest}
	 * @param reason   stop reason
	 * @return perftest with updated data
	 */
	@Transactional
	public PerfTest markAbnormalTermination(PerfTest perfTest, String reason) {
		// Leave last status as test error cause
		perfTest.setTestErrorCause(cast(perfTest.getStatus()));
		return markStatusAndProgress(perfTest, Status.ABNORMAL_TESTING, reason);
	}


	@Transactional
	@Override
	public PerfTest markStatusAndProgress(PerfTest perfTest, Status status, String message) {
		perfTest.setStatus(checkNotNull(status, "status should not be null"));
		return markProgress(perfTest, message);
	}

	/**
	 * Add a progress message on the given perfTest.
	 *
	 * @param perfTest perf test
	 * @param message  message to be recorded.
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
	 * @param perfTest perf test
	 * @param status   status to be recorded.
	 * @param message  message to be recorded.
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
	 * @param perfTest perf test
	 * @param status   status to be recorded.
	 * @param message  message to be recorded.
	 * @return perftest with latest status and data
	 */
	@Transactional
	public PerfTest markProgressAndStatusAndFinishTimeAndStatistics(PerfTest perfTest, Status status, String message) {
		perfTest.setFinishTime(new Date());
		updatePerfTestAfterTestFinish(perfTest);
		return markProgressAndStatus(perfTest, status, message);
	}

	/**
	 * mark the perftest to "START_CONSOLE_FINISHED" status .
	 *
	 * @param perfTest    perftest to mark
	 * @param consolePort port of the console, on which the test is running
	 * @return saved perftest
	 */
	@Transactional
	public PerfTest markPerfTestConsoleStart(PerfTest perfTest, int consolePort) {
		perfTest.setPort(consolePort);
		return markProgressAndStatus(perfTest, Status.START_CONSOLE_FINISHED, "Console is started on port "
			+ consolePort);
	}


	@Transactional
	@Override
	public PerfTest getOneWithTag(Long testId) {
		Optional<PerfTest> findOne = perfTestRepository.findOne(idEqual(testId));
		PerfTest perfTest = null;

		if (findOne.isPresent()) {
			perfTest = findOne.get();
			Hibernate.initialize(perfTest.getTags());
		}
		return perfTest;
	}


	@Override
	public PerfTest getOne(Long testId) {
		return perfTestRepository.findOne(idEqual(testId)).orElse(null);
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
		return getAll(null, Status.getProcessingOrTestingTestStatus());
	}

	/**
	 * Filter out {@link PerfTest} whose owner is running another test now..
	 *
	 * @param perfTestLists perf test
	 * @return filtered perf test
	 */
	protected List<PerfTest> filterCurrentlyRunningTestUsersTest(List<PerfTest> perfTestLists) {
		List<PerfTest> currentlyRunningTests = getCurrentlyRunningTest();
		final Set<User> currentlyRunningTestOwners = newHashSet();
		for (PerfTest each : currentlyRunningTests) {
			currentlyRunningTestOwners.add(each.getCreatedUser());
		}
		return perfTestLists.stream()
			.filter(perfTest -> !currentlyRunningTestOwners.contains(perfTest.getCreatedUser()))
			.collect(toList());
	}

	@Override
	public List<PerfTest> getAllTesting() {
		return getAll(null, config.getRegion(), Status.getTestingTestStates());
	}

	public List<PerfTest> getAllAbnormalTesting() {
		return getAll(null, config.getRegion(), new Status[]{Status.ABNORMAL_TESTING});
	}

	/**
	 * Delete test {@link PerfTest} by user and test id.
	 *
	 * @param user user
	 * @param id   test id
	 */
	@Transactional
	public void delete(User user, long id) {
		PerfTest perfTest = getOne(id);
		// If it's not requested by user who started job. It's wrong request.
		if (!hasPermission(perfTest, user, Permission.DELETE_TEST_OF_OTHER)) {
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
	 * @param perfTest perfTest
	 */
	private void deletePerfTestDirectory(PerfTest perfTest) {
		deleteQuietly(getPerfTestDirectory(perfTest));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ngrinder.service.IPerfTestService#getPerfTestFilePath(org .ngrinder.perftest. model.PerfTest)
	 */
	@Override
	public File getStatisticPath(PerfTest perfTest) {
		return config.getHome().getPerfTestStatisticPath(perfTest);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ngrinder.service.IPerfTestService#getPerfTestFilePath(org .ngrinder.perftest. model.PerfTest)
	 */
	@Override
	public File getDistributionPath(PerfTest perfTest) {
		return config.getHome().getPerfTestDistDirectory(perfTest);
	}

	/**
	 * Build user library classpath on the given {@link PerfTest}.
	 *
	 * @param perfTest perftest
	 * @return custom class path.
	 */
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public String geUserLibraryClassPath(PerfTest perfTest) {
		File perfTestDirectory = getDistributionPath(perfTest);
		File libFolder = new File(perfTestDirectory, "lib");

		final StringBuffer customClassPath = new StringBuffer();
		customClassPath.append(".");
		if (libFolder.exists()) {
			customClassPath.append(File.pathSeparator).append("lib");
			libFolder.list((dir, name) -> {
				if (name.endsWith(".jar")) {
					customClassPath.append(File.pathSeparator).append("lib/").append(name);
				}
				return true;
			});
		}
		return customClassPath.toString();
	}

	/**
	 * Create {@link GrinderProperties} based on the passed {@link PerfTest}.
	 *
	 * @param perfTest base data
	 * @return created {@link GrinderProperties} instance
	 */
	public GrinderProperties getGrinderProperties(PerfTest perfTest) {
		return getGrinderProperties(perfTest, new NullScriptHandler());
	}

	/**
	 * Create {@link GrinderProperties} based on the passed {@link PerfTest}.
	 *
	 * @param perfTest      base data
	 * @param scriptHandler scriptHandler
	 * @return created {@link GrinderProperties} instance
	 */
	public GrinderProperties getGrinderProperties(PerfTest perfTest, ScriptHandler scriptHandler) {
		try {
			// Use default properties first
			GrinderProperties grinderProperties = new GrinderProperties(config.getHome().getDefaultGrinderProperties());

			User user = perfTest.getCreatedUser();

			// Get all files in the script path
			String scriptName = perfTest.getScriptName();
			FileEntry userDefinedGrinderProperties = fileEntryService.getOne(user,
				FilenameUtils.concat(FilenameUtils.getPath(scriptName), DEFAULT_GRINDER_PROPERTIES), -1L);
			if (!config.isSecurityEnabled() && userDefinedGrinderProperties != null) {
				// Make the property overridden by user property.
				GrinderProperties userProperties = new GrinderProperties();
				userProperties.load(new StringReader(userDefinedGrinderProperties.getContent()));
				grinderProperties.putAll(userProperties);
			}
			grinderProperties.setAssociatedFile(new File(DEFAULT_GRINDER_PROPERTIES));
			grinderProperties.setProperty(GRINDER_PROP_SCRIPT, scriptHandler.getScriptExecutePath(scriptName));

			grinderProperties.setProperty(GRINDER_PROP_TEST_ID, "test_" + perfTest.getId());
			grinderProperties.setInt(GRINDER_PROP_AGENTS, getSafe(perfTest.getAgentCount()));
			grinderProperties.setInt(GRINDER_PROP_PROCESSES, getSafe(perfTest.getProcesses()));
			grinderProperties.setInt(GRINDER_PROP_THREAD, getSafe(perfTest.getThreads()));
			if (perfTest.isThresholdDuration()) {
				grinderProperties.setLong(GRINDER_PROP_DURATION, getSafe(perfTest.getDuration()));
				grinderProperties.setInt(GRINDER_PROP_RUNS, 0);
			} else {
				grinderProperties.setInt(GRINDER_PROP_RUNS, getSafe(perfTest.getRunCount()));
				if (grinderProperties.containsKey(GRINDER_PROP_DURATION)) {
					grinderProperties.remove(GRINDER_PROP_DURATION);
				}
			}
			grinderProperties.setProperty(GRINDER_PROP_ETC_HOSTS,
					StringUtils.defaultIfBlank(perfTest.getTargetHosts(), ""));
			grinderProperties.setBoolean(GRINDER_PROP_USE_CONSOLE, true);
			if (BooleanUtils.isTrue(perfTest.getUseRampUp())) {
				grinderProperties.setBoolean(GRINDER_PROP_THREAD_RAMPUP, perfTest.getRampUpType() == RampUp.THREAD);
				grinderProperties.setInt(GRINDER_PROP_PROCESS_INCREMENT, getSafe(perfTest.getRampUpStep()));
				grinderProperties.setInt(GRINDER_PROP_PROCESS_INCREMENT_INTERVAL,
						getSafe(perfTest.getRampUpIncrementInterval()));
				if (perfTest.getRampUpType() == RampUp.PROCESS) {
					grinderProperties.setInt(GRINDER_PROP_INITIAL_SLEEP_TIME, getSafe(perfTest.getRampUpInitSleepTime()));
				} else {
					grinderProperties.setInt(GRINDER_PROP_INITIAL_THREAD_SLEEP_TIME,
							getSafe(perfTest.getRampUpInitSleepTime()));
				}
				grinderProperties.setInt(GRINDER_PROP_INITIAL_PROCESS, getSafe(perfTest.getRampUpInitCount()));
			} else {
				grinderProperties.setInt(GRINDER_PROP_PROCESS_INCREMENT, 0);
			}
			grinderProperties.setInt(GRINDER_PROP_REPORT_TO_CONSOLE, 500);
			grinderProperties.setProperty(GRINDER_PROP_USER, perfTest.getCreatedUser().getUserId());
			grinderProperties.setProperty(GRINDER_PROP_JVM_USER_LIBRARY_CLASSPATH, geUserLibraryClassPath(perfTest));
			grinderProperties.setInt(GRINDER_PROP_IGNORE_SAMPLE_COUNT, getSafe(perfTest.getIgnoreSampleCount()));
			grinderProperties.setBoolean(GRINDER_PROP_SECURITY, config.isSecurityEnabled());
			grinderProperties.setProperty(GRINDER_PROP_SECURITY_LEVEL, config.getSecurityLevel());
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
		}
	}

	/**
	 * Prepare files for distribution. This method stores the files on the path
	 * ${NGRINDER_HOME}/perftest/{test_id}/dist folder.
	 *
	 * @param perfTest perfTest
	 * @return File location in which the perftest script and resources are distributed.
	 */
	public ScriptHandler prepareDistribution(PerfTest perfTest) throws IOException {
		File perfTestDistDirectory = getDistributionPath(perfTest);
		User user = perfTest.getCreatedUser();
		String scm = perfTest.getScm();
		FileEntry scriptEntry;

		if (isGitHubScript(scm)) {
			String gitHubConfigName = extractGitHubConfigName(scm);

			GitHubConfig gitHubConfig = gitHubFileEntryService.getGitHubConfig(user, gitHubConfigName);
			GitHub gitHub = gitHubFileEntryService.getGitHubClient(gitHubConfig);
			GHRepository ghRepository = gitHub.getRepository(gitHubConfig.getOwner() + "/" + gitHubConfig.getRepo());

			String scriptName = perfTest.getScriptName();
			gitHubFileEntryService.checkoutGitHubScript(perfTest, ghRepository, gitHubConfig);
			scriptEntry = gitHubFileEntryService.getOne(ghRepository, gitHubConfig, scriptName);
			gitHubFileEntryService.evictGitHubMavenGroovyCache(ghRepository, scriptName, gitHubConfig.getBranch());
		} else {
			scriptEntry = checkNotNull(
				fileEntryService.getOne(user,
					checkNotEmpty(perfTest.getScriptName(), "perfTest should have script name"),
					getSafe(valueOf(perfTest.getScriptRevision()))), "script should exist");
		}

		// Get all files in the script path.
		ScriptHandler handler = scriptHandlerFactory.getHandler(scriptEntry);

		ProcessingResultPrintStream processingResult = new ProcessingResultPrintStream(new ByteArrayOutputStream());
		handler.prepareDist(perfTest.getId(), user, scriptEntry, perfTestDistDirectory, config.getControllerProperties(),
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

	public GrinderProperties prepareTest(PerfTest perfTest) {
		try {
			proxy(this).markStatusAndProgress(perfTest,
				PREPARE_DISTRIBUTION, "Distribution files are being prepared.");
			cleanUpPerftestDistributionFolder(perfTest);
			ScriptHandler prepareDistribution = prepareDistribution(perfTest);
			GrinderProperties grinderProperties = getGrinderProperties(perfTest, prepareDistribution);
			proxy(this).markStatusAndProgress(perfTest,
				PREPARE_DISTRIBUTION, "Distribution files are prepared.");
			return grinderProperties;
		} catch (Throwable e) {
			throw new PerfTestPrepareException(e.getMessage(), e);
		}
	}

	// scm format in db is name:revision
	private String extractGitHubConfigName(String scm) {
		return scm.split(":")[0];
	}

	private boolean isGitHubScript(String scm) {
		return scm != null && !scm.equals("svn");
	}

	private void cleanUpPerftestDistributionFolder(PerfTest perfTest) {
		File distributedFolder = config.getHome().getDistributedFolderName(perfTest.getId().toString());
		if (distributedFolder.exists()) {
			deleteQuietly(distributedFolder);
		}
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
	 * @param newVuser the count of virtual users per agent
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
	 * @param testId   test id
	 * @param dataType data type
	 * @param imgWidth image width
	 * @return interval interval value
	 */
	public int getReportDataInterval(long testId, String dataType, int imgWidth) {
		int pointCount = Math.max(imgWidth, MAX_POINT_COUNT);
		File reportFolder = config.getHome().getPerfTestReportDirectory(String.valueOf(testId));
		int interval = 0;
		File targetFile = new File(reportFolder, dataType + REPORT_DATA);
		if (!targetFile.exists()) {
			LOGGER.warn("Report {} for test {} does not exist.", dataType, testId);
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
			int lineNumber = lnr.getLineNumber() + 1;
			interval = Math.max(lineNumber / pointCount, 1);
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
	 * @param perfTest test
	 * @return reportFile data report file
	 */
	public File getCsvReportFile(PerfTest perfTest) {
		return config.getHome().getPerfTestCsvFile(perfTest);
	}

	/**
	 * Get log file names for give test id.
	 *
	 * @param testId   test id
	 * @param fileName file name of one logs of the test
	 * @return file report file path
	 */
	public File getLogFile(long testId, String fileName) {
		return new File(getLogFileDirectory(String.valueOf(testId)), fileName);
	}

	/**
	 * Get report file directory for give test .
	 *
	 * @param perfTest perfTest
	 * @return logDir log file path of the test
	 */

	public File getLogFileDirectory(PerfTest perfTest) {
		return config.getHome().getPerfTestLogDirectory(perfTest);
	}

	/**
	 * Get report file directory for give test id.
	 *
	 * @param testId test id
	 * @return logDir log file path of the test
	 */

	public File getLogFileDirectory(String testId) {
		return config.getHome().getPerfTestLogDirectory(testId);
	}

	/**
	 * Get log files list on the given test is.
	 *
	 * @param testId testId
	 * @return logFilesList log file list of that test
	 */
	public List<String> getLogFiles(long testId) {
		File logFileDirectory = getLogFileDirectory(String.valueOf(testId));
		if (!logFileDirectory.exists() || !logFileDirectory.isDirectory()) {
			return Collections.emptyList();
		}
		return asList(logFileDirectory.list());
	}

	/**
	 * Get report file directory for give test .
	 *
	 * @param perfTest perftest
	 * @return reportDir report file path
	 */
	public File getReportFileDirectory(PerfTest perfTest) {
		return config.getHome().getPerfTestReportDirectory(perfTest);
	}

	/**
	 * To save statistics data when test is running and put into cache after that. If the console is not available, it
	 * returns null.
	 *
	 * @param singleConsole single console.
	 * @param perfTestId    perfTest Id
	 */
	public void saveStatistics(SingleConsole singleConsole, Long perfTestId) {
		String runningSample = getProperSizeRunningSample(singleConsole);
		String agentState = getProperSizedStatusString(singleConsole);
		hazelcastService.put(DIST_MAP_NAME_SAMPLING, perfTestId, new SamplingModel(runningSample, agentState));
	}

	private String getProperSizeRunningSample(SingleConsole singleConsole) {
		Map<String, Object> statisticData = singleConsole.getStatisticsData();
		String runningSample = JsonUtils.serialize(statisticData);

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
			runningSample = JsonUtils.serialize(tempData);
		}
		return runningSample;
	}

	/**
	 * Get the limited size of agent status json string.
	 *
	 * @param singleConsole console which is connecting agents
	 * @return converted json
	 */
	public String getProperSizedStatusString(SingleConsole singleConsole) {
		Map<String, SystemDataModel> agentStatusMap = Maps.newHashMap();
		final int singleConsolePort = singleConsole.getConsolePort();
		for (AgentStatus each : agentManager.getAttachedAgentStatusSetConnectingToPort(singleConsolePort)) {
			agentStatusMap.put(each.getAgentName(), each.getSystemDataModel());
		}
		return getProperSizedStatusString(agentStatusMap);
	}

	String getProperSizedStatusString(Map<String, SystemDataModel> agentStatusMap) {
		String json = JsonUtils.serialize(agentStatusMap);
		int statusLength = StringUtils.length(json);
		if (statusLength > 9950) { // max column size is 10,000
			LOGGER.info("Agent status string length: {}, too long to save into table.", statusLength);
			double ratio = 9900.0 / statusLength;
			int pickSize = (int) (agentStatusMap.size() * ratio);
			Map<String, SystemDataModel> pickAgentStateMap = Maps.newHashMap();

			int pickIndex = 0;
			for (Entry<String, SystemDataModel> each : agentStatusMap.entrySet()) {
				if (pickIndex < pickSize) {
					pickAgentStateMap.put(each.getKey(), each.getValue());
					pickIndex++;
				}
			}
			json = JsonUtils.serialize(pickAgentStateMap);
			LOGGER.debug("Agent status string get {} outof {} agents, new size is {}.", pickSize, agentStatusMap.size(), json.length());
		}
		return json;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ngrinder.service.IPerfTestService#getAllPerfTest()
	 */
	@Override
	public List<PerfTest> getAllPerfTest() {
		return perfTestRepository.findAll();
	}

	/**
	 * Create {@link ConsoleProperties} based on given {@link PerfTest} instance.
	 *
	 * @param perfTest perfTest
	 * @return {@link ConsoleProperties}
	 */
	public ConsoleProperties createConsoleProperties(PerfTest perfTest) {
		ConsoleProperties consoleProperties = ConsolePropertiesFactory.createEmptyConsoleProperties();
		try {
			consoleProperties.setAndSaveDistributionDirectory(new Directory(getDistributionPath(perfTest)));
			consoleProperties.setConsoleHost(config.getCurrentIP());
			consoleProperties.setIgnoreSampleCount(getSafe(perfTest.getIgnoreSampleCount()));
			consoleProperties.setSampleInterval(1000 * getSafe(perfTest.getSamplingInterval()));
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
	 * Check if the given perfTest has too many errors. (30%)
	 *
	 * @param perfTest perftest
	 * @return true if too many errors.
	 */
	@SuppressWarnings("unchecked")
	public boolean hasTooManyError(PerfTest perfTest) {
		SamplingModel samplingModel = hazelcastService.get(DIST_MAP_NAME_SAMPLING, perfTest.getId());
		Map<String, Object> result = JsonUtils.deserialize(samplingModel.getRunningSample(), HashMap.class);
		Map<String, Object> totalStatistics = MapUtils.getMap(result, "totalStatistics", MapUtils.EMPTY_MAP);
		long tests = MapUtils.getDouble(totalStatistics, "Tests", 0D).longValue();
		long errors = MapUtils.getDouble(totalStatistics, "Errors", 0D).longValue();
		return ((((double) errors) / (tests + errors)) > 0.3d);
	}

	/**
	 * Update the given {@link PerfTest} properties after test finished.
	 *
	 * @param perfTest perfTest
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
		return config.getControllerProperties().getPropertyInt(PROP_CONTROLLER_MAX_CONCURRENT_TEST);
	}

	/**
	 * Check the test can be executed more.
	 *
	 * @return true if possible
	 * @deprecated
	 */
	@SuppressWarnings("UnusedDeclaration")
	public boolean canExecuteTestMore() {
		return count(null, Status.getProcessingOrTestingTestStatus()) < getMaximumConcurrentTestCount();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ngrinder.service.IPerfTestService#stop(org.ngrinder .model.User, java.lang.Long)
	 */
	@Override
	@Transactional
	public void stop(User user, Long id) {
		PerfTest perfTest = getOne(id);
		// If it's not requested by user who started job. It's wrong request.
		if (!hasPermission(perfTest, user, Permission.STOP_TEST_OF_OTHER)) {
			return;
		}
		// If it's not stoppable status.. It's wrong request.
		if (!perfTest.getStatus().isStoppable()) {
			return;
		}
		// Just mark cancel on console
		// This will be not be effective on cluster mode.
		consoleManager.getConsoleUsingPort(perfTest.getPort()).cancel();
		perfTest.setStopRequest(true);
		perfTestRepository.save(perfTest);
	}

	/**
	 * Check if given user has a permission on perftest.
	 *
	 * @param perfTest perf test
	 * @param user     user
	 * @param type     permission type to check
	 * @return true if it has
	 */
	public boolean hasPermission(PerfTest perfTest, User user, Permission type) {
		return perfTest != null && (user.getRole().hasPermission(type) || user.equals(perfTest.getCreatedUser()));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ngrinder.service.IPerfTestService#getAllStopRequested()
	 */
	@Override
	public List<PerfTest> getAllStopRequested() {
		final List<PerfTest> perfTests = getAll(null, config.getRegion(), getProcessingOrTestingTestStatus());
		return perfTests.stream()
			.filter(PerfTest::isStopRequest)
			.collect(toList());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ngrinder.service.IPerfTestService#addCommentOn(org.ngrinder .model.User, int, java.lang.String)
	 */
	@Override
	@Transactional
	public void addCommentOn(User user, Long testId, String testComment, String tagString) {
		PerfTest perfTest = getOne(user, testId);
		perfTest.setTestComment(testComment);
		attachTags(user, perfTest, tagString);
		perfTestRepository.save(perfTest);
	}

	/**
	 * get current running test status, which is, how many user run how many tests with some agents.
	 *
	 * @return PerfTestStatisticsList PerfTestStatistics list
	 */
	@Cacheable(CACHE_CURRENT_PERFTEST_STATISTICS)
	@Transactional
	public Collection<PerfTestStatistics> getCurrentPerfTestStatistics() {
		Map<User, PerfTestStatistics> perfTestPerUser = newHashMap();
		for (PerfTest each : getAll(null, getProcessingOrTestingTestStatus())) {
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
	 * @param perfTest perfTest
	 * @return directory
	 */
	@Override
	public File getPerfTestDirectory(PerfTest perfTest) {
		return config.getHome().getPerfTestDirectory(perfTest);
	}

	/**
	 * Delete All PerfTests and related tags belonging to given user.
	 *
	 * @param user user
	 * @return deleted {@link PerfTest} list
	 */
	@Transactional
	public List<PerfTest> deleteAll(User user) {
		List<PerfTest> perfTestList = getAll(user);
		for (PerfTest each : perfTestList) {
			each.getTags().clear();
		}
		perfTestRepository.saveAll(perfTestList);
		perfTestRepository.flush();
		perfTestRepository.deleteAll(perfTestList);
		perfTestRepository.flush();
		tagService.deleteTags(user);
		return perfTestList;
	}

	/**
	 * Delete the distribution folder for the give perf test.
	 *
	 * @param perfTest perf test
	 */
	public void cleanUpDistFolder(PerfTest perfTest) {
		deleteQuietly(getDistributionPath(perfTest));
	}

	/**
	 * Put the given {@link org.ngrinder.monitor.share.domain.SystemInfo} maps into the given perftest entity.
	 *
	 * @param perfTestId  id of perf test
	 * @param systemInfos systemDataModel map
	 */
	public void updateMonitorStat(Long perfTestId, Map<String, SystemDataModel> systemInfos) {
		String json = JsonUtils.serialize(systemInfos);
		if (json.length() >= 2000) {
			Map<String, SystemDataModel> systemInfo = Maps.newHashMap();
			int i = 0;
			for (Entry<String, SystemDataModel> each : systemInfos.entrySet()) {
				if (i++ > 3) {
					break;
				}
				systemInfo.put(each.getKey(), each.getValue());
			}
			json = JsonUtils.serialize(systemInfo);
		}
		hazelcastService.put(DIST_MAP_NAME_MONITORING , perfTestId , json);
	}

	/**
	 * Get the monitor data interval value. In the normal, the image width is 700, and if the data count is too big,
	 * there will be too many points in the chart. So we will calculate the interval to get appropriate count of data to
	 * display. For example, interval value "2" means, get one record for every "2" records.
	 *
	 * @param testId     test id
	 * @param targetIP   ip address of monitor target
	 * @param imageWidth image with of the chart.
	 * @return interval value.
	 */
	public int getMonitorGraphInterval(long testId, String targetIP, int imageWidth) {
		File monitorDataFile = new File(config.getHome().getPerfTestReportDirectory(String.valueOf(testId)),
				MONITOR_FILE_PREFIX + targetIP + ".data");

		int pointCount = Math.max(imageWidth, MAX_POINT_COUNT);
		FileInputStream in = null;
		InputStreamReader isr = null;
		LineNumberReader lnr = null;
		int interval = 0;
		try {
			in = new FileInputStream(monitorDataFile);
			isr = new InputStreamReader(in);
			lnr = new LineNumberReader(isr);
			lnr.skip(monitorDataFile.length());
			int lineNumber = lnr.getLineNumber() + 1;
			interval = Math.max(lineNumber / pointCount, 1);
		} catch (FileNotFoundException e) {
			LOGGER.info("Monitor data file does not exist at {}", monitorDataFile);
		} catch (IOException e) {
			LOGGER.info("Error while getting monitor:{} data file:{}", targetIP, monitorDataFile);
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
	 * @param testId       test id
	 * @param targetIP     ip address of the monitor target
	 * @param dataInterval interval value to get data. Interval value "2" means, get one record for every "2" records.
	 * @return return the data in map
	 */
	public Map<String, Object> getMonitorGraph(long testId, String targetIP, int dataInterval) {
		Map<String, Object> returnMap = Maps.newHashMap();
		File monitorDataFile = new File(config.getHome().getPerfTestReportDirectory(String.valueOf(testId)),
				MONITOR_FILE_PREFIX + targetIP + ".data");
		BufferedReader br = null;
		try {
			List<Long> userMemoryMetrics = new ArrayList<>();
			List<String> cpuUsedMetrics = new ArrayList<>();
			List<String> networkReceivedMetrics = new ArrayList<>();
			List<String> networkSentMetrics = new ArrayList<>();
			List<String> customData1Metrics = new ArrayList<>();
			List<String> customData2Metrics = new ArrayList<>();
			List<String> customData3Metrics = new ArrayList<>();
			List<String> customData4Metrics = new ArrayList<>();
			List<String> customData5Metrics = new ArrayList<>();

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
					line = br.readLine();
				} else {
					skipCount = 0;
					String[] dataList = StringUtils.split(line, ",");
					if (NULL_STRING.equals(dataList[4]) || UNDEFINED_STRING.equals(dataList[4])) {
						userMemoryMetrics.add(null);
					} else {
						userMemoryMetrics.add(valueOf(dataList[4]) - valueOf(dataList[3]));
					}
					addCustomData(cpuUsedMetrics, 5, dataList);
					addCustomData(networkReceivedMetrics, 6, dataList);
					addCustomData(networkSentMetrics, 7, dataList);
					addCustomData(customData1Metrics, 8, dataList);
					addCustomData(customData2Metrics, 9, dataList);
					addCustomData(customData3Metrics, 10, dataList);
					addCustomData(customData4Metrics, 11, dataList);
					addCustomData(customData5Metrics, 12, dataList);
				}
			}
			returnMap.put("cpu", cpuUsedMetrics);
			returnMap.put("memory", userMemoryMetrics);
			returnMap.put("received", networkReceivedMetrics);
			returnMap.put("sent", networkSentMetrics);
			returnMap.put("customData1", customData1Metrics);
			returnMap.put("customData2", customData2Metrics);
			returnMap.put("customData3", customData3Metrics);
			returnMap.put("customData4", customData4Metrics);
			returnMap.put("customData5", customData5Metrics);
		} catch (IOException e) {
			LOGGER.info("Error while getting monitor {} data file at {}", targetIP, monitorDataFile);
		} finally {
			IOUtils.closeQuietly(br);
		}
		return returnMap;
	}


	private <T> void addCustomData(List<T> list, int index, T[] data) {
		if (data.length > index) {
			list.add(data[index]);
		}
	}

	/**
	 * Get report file directory for give test id .
	 *
	 * @param testId testId
	 * @return reportDir report file path
	 */
	public File getReportFileDirectory(long testId) {
		return config.getHome().getPerfTestReportDirectory(String.valueOf(testId));
	}

	/**
	 * Get interval value of the monitor data of a plugin, like jvm monitor plugin.
	 * The usage of interval value is same as system monitor data.
	 *
	 * @param testId     test id
	 * @param plugin     plugin name
	 * @param kind       plugin kind
	 * @param imageWidth image with of the chart.
	 * @return interval value.
	 */
	public int getReportPluginGraphInterval(long testId, String plugin, String kind, int imageWidth) {
		return getRecordInterval(imageWidth, getReportPluginDataFile(testId, plugin, kind));
	}

	/**
	 * Get available report plugins list for the given test.
	 *
	 * @param testId test id
	 * @return plugin names
	 */
	public List<Pair<String, String>> getAvailableReportPlugins(Long testId) {
		List<Pair<String, String>> result = newArrayList();
		File reportDir = getReportFileDirectory(testId);
		if (reportDir.exists()) {
			for (File plugin : checkNotNull(reportDir.listFiles())) {
				if (plugin.isDirectory()) {
					for (String kind : checkNotNull(plugin.list())) {
						if (kind.endsWith(".data")) {
							result.add(Pair.of(plugin.getName(), FilenameUtils.getBaseName(kind)));
						}
					}
				}
			}
		}
		return result;
	}

	/*
	 * Plugin monitor data should be {TestReportDir}/{plugin}/{kind}.data
	 */
	private File getReportPluginDataFile(Long testId, String plugin, String kind) {
		File reportDir = getReportFileDirectory(testId);
		File pluginDir = new File(reportDir, plugin);
		return new File(pluginDir, kind + ".data");
	}

	/*
	 * Get the interval value. In the normal, the image width is 700, and if the data count is too big,
	 * there will be too many points in the chart. So we will calculate the interval to get appropriate count of data to
	 * display. For example, interval value "2" means, get one record for every "2" records.
	 */
	private int getRecordInterval(int imageWidth, File dataFile) {
		int pointCount = Math.max(imageWidth, MAX_POINT_COUNT);
		FileInputStream in = null;
		InputStreamReader isr = null;
		LineNumberReader lnr = null;
		int interval = 0;
		try {
			in = new FileInputStream(dataFile);
			isr = new InputStreamReader(in);
			lnr = new LineNumberReader(isr);
			lnr.skip(dataFile.length());
			interval = Math.max((lnr.getLineNumber() + 1) / pointCount, 1);
		} catch (FileNotFoundException e) {
			LOGGER.error("data file not exist:{}", dataFile);
			LOGGER.error(e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.error("Error while getting data file:{}", dataFile);
			LOGGER.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(lnr);
			IOUtils.closeQuietly(isr);
			IOUtils.closeQuietly(in);
		}
		return interval;
	}

	/**
	 * Get plugin monitor data and wrap the data as a string value like "[22,11,12,34,....]", which can be used directly
	 * in JS as a vector.
	 *
	 * @param testId   test id
	 * @param plugin   plugin name
	 * @param kind     kind
	 * @param interval interval value to get data. Interval value "2" means, get one record for every "2" records.
	 * @return return the data in map
	 */
	public Map<String, Object> getReportPluginGraph(long testId, String plugin, String kind, int interval) {
		Map<String, Object> returnMap = Maps.newHashMap();
		File pluginDataFile = getReportPluginDataFile(testId, plugin, kind);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(pluginDataFile));
			String header = br.readLine();

			StringBuilder headerSB = new StringBuilder("[");
			String[] headers = StringUtils.split(header, ",");
			String[] refinedHeaders = StringUtils.split(header, ",");
			List<StringBuilder> dataStringBuilders = new ArrayList<>(headers.length);

			for (int i = 0; i < headers.length; i++) {
				dataStringBuilders.add(new StringBuilder("["));
				String refinedHead = headers[i].trim().replaceAll(" ", "_");
				refinedHeaders[i] = refinedHead;
				headerSB.append("'").append(refinedHead).append("'").append(",");
			}
			String headerStringInJSONList = headerSB.deleteCharAt(headerSB.length() - 1).append("]").toString();
			returnMap.put("header", headerStringInJSONList);

			String line = br.readLine();
			int skipCount = interval;
			// to be compatible with previous version, check the length before adding
			while (StringUtils.isNotBlank(line)) {
				if (skipCount < interval) {
					skipCount++;
					line = br.readLine();
				} else {
					skipCount = 0;
					String[] records = StringUtils.split(line, ",");
					for (int i = 0; i < records.length; i++) {
						if ("null".equals(records[i]) || "undefined".equals(records[i])) {
							dataStringBuilders.get(i).append("null").append(",");
						} else {
							dataStringBuilders.get(i).append(records[i]).append(",");
						}
					}
				}
			}
			for (int i = 0; i < refinedHeaders.length; i++) {
				StringBuilder dataSB = dataStringBuilders.get(i);
				if (dataSB.charAt(dataSB.length() - 1) == ',') {
					dataSB.deleteCharAt(dataSB.length() - 1);
				}
				dataSB.append("]");
				returnMap.put(refinedHeaders[i], dataSB.toString());
			}
		} catch (IOException e) {
			LOGGER.error("Error while getting monitor: {} data file:{}", plugin, pluginDataFile);
			LOGGER.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(br);
		}
		return returnMap;
	}

	/**
	 * Get json string that contains test report data as a json string.
	 *
	 * @param testId   test id
	 * @param key      key
	 * @param interval interval to collect data
	 * @return json list
	 */
	public List<Float> getSingleReportData(long testId, String key, int interval) {
		File reportDataFile = getReportDataFile(testId, key);
		return getFileDataAsList(reportDataFile, interval);
	}

	/**
	 * Get list that contains test report data as a string.
	 *
	 * @param testId   test id
	 * @param key      report key
	 * @param onlyTotal true if only total show be passed
	 * @param interval interval to collect data
	 * @return list containing label and tps value list
	 */
	public Map<String, List<Float>> getReportData(long testId, String key, boolean onlyTotal, int interval) {
		Map<String, List<Float>> resultMap = new TreeMap<>();
		List<File> reportDataFiles = onlyTotal ? Lists.newArrayList(getReportDataFile(testId, key)) : getReportDataFiles(testId, key);
		reportDataFiles.forEach(each -> resultMap.put(removeExtension(each.getName()), getFileDataAsList(each, interval)));
		return resultMap;
	}

	/**
	 * Get a single file for the given report key.
	 *
	 * @param testId test id
	 * @param key    key
	 * @return return file
	 */
	public File getReportDataFile(long testId, String key) {
		File reportFolder = config.getHome().getPerfTestReportDirectory(String.valueOf(testId));
		return new File(reportFolder, key + ".data");
	}

	/**
	 * Get files respectively if there are multiple tests.
	 *
	 * @param testId test id
	 * @param key    report key
	 * @return return file list
	 */
	public List<File> getReportDataFiles(long testId, String key) {
		File reportFolder = config.getHome().getPerfTestReportDirectory(String.valueOf(testId));
		FileFilter fileFilter = new WildcardFileFilter(key + "*.data");
		File[] files = reportFolder.listFiles(fileFilter);
		return Arrays.stream(files)
			.sorted(Comparator.comparing(o -> FilenameUtils.getBaseName(o.getName())))
			.collect(toList());
	}

	private List<Float> getFileDataAsList(File targetFile, int interval) {
		if (!targetFile.exists()) {
			return Collections.emptyList();
		}

		List<Float> metrics = new ArrayList<>();
		try (FileReader reader = new FileReader(targetFile);
			 BufferedReader br = new BufferedReader(reader);){

			String data = br.readLine();
			int current = 0;
			while (StringUtils.isNotBlank(data)) {
				if (0 == current) {
					if (data.equals(NULL_STRING)) {
						metrics.add(null);
					} else {
						metrics.add(Float.parseFloat(data));
					}
				}
				if (++current >= interval) {
					current = 0;
				}
				data = br.readLine();
			}
		} catch (IOException e) {
			LOGGER.error("Report data retrieval is failed: {}", e.getMessage());
			LOGGER.debug("Trace is : ", e);
		}
		return metrics;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ngrinder.service.IPerfTestService#getAll(java.util.Date, java.util.Date)
	 */
	@Override
	public List<PerfTest> getAll(Date start, Date end) {
		return perfTestRepository.findAllByCreatedTime(start, end);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ngrinder.service.IPerfTestService#getAll(java.util.Date, java.util.Date, java.lang.String)
	 */
	@Override
	public List<PerfTest> getAll(Date start, Date end, String region) {
		return perfTestRepository.findAllByCreatedTimeAndRegion(start, end, region);
	}


}
