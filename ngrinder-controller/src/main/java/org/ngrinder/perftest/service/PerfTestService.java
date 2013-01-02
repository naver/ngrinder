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

import static org.ngrinder.common.util.FileUtil.readObjectFromFile;
import static org.ngrinder.common.util.FileUtil.writeObjectToFile;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.grinder.SingleConsole;
import net.grinder.StopReason;
import net.grinder.common.GrinderProperties;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.console.model.ConsoleProperties;
import net.grinder.util.ConsolePropertiesFactory;
import net.grinder.util.Directory;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hibernate.Hibernate;
import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.common.exception.NGrinderRuntimeException;
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
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileType;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.service.IPerfTestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link PerfTest} Service Class.
 * 
 * This class contains various method which mainly get the {@link PerfTest} matching specific
 * conditions from DB.
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
	 * @see org.ngrinder.perftest.service.IPerfTestService#getPerfTest(org.ngrinder .model.User,
	 * java.lang.Integer)
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
	 * @see org.ngrinder.perftest.service.IPerfTestService#getPerfTest(org.ngrinder .model.User,
	 * java.lang.Integer[])
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
	 * @see org.ngrinder.perftest.service.IPerfTestService#getPerfTestCount(org.ngrinder
	 * .model.User, org.ngrinder.perftest.model.Status)
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.perftest.service.IPerfTestService#savePerfTest(org.ngrinder
	 * .perftest.model.PerfTest )
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
		return perfTestRepository.save(perfTest);
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
	public PerfTest markAbromalTermination(PerfTest perfTest, StopReason reason) {
		return markAbromalTermination(perfTest, reason.name());
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
	public PerfTest markAbromalTermination(PerfTest perfTest, String reason) {
		// Leave last status as test error cause
		perfTest.setTestErrorCause(perfTest.getStatus());
		return markStatusAndProgress(perfTest, Status.ABNORMAL_TESTING, reason);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.service.IPerfTestService#markStatusAndProgress(org.ngrinder.model.PerfTest,
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
		return perfTestRepository.save(perfTest);
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
	 * Add a progress message on the given perfTest and change the status. In addition, the finish
	 * time and various test statistic are recorded as well.
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
	 * Get next runnable PerfTest list.
	 * 
	 * @return found {@link PerfTest} which is ready to run, null otherwise
	 */
	@Transactional
	public PerfTest getPerfTestCandiate() {
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
		final Set<User> currentlyRunningTestOwners = new HashSet<User>();
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
	public List<PerfTest> getAbnoramlTestingPerfTest() {
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
	 * @see org.ngrinder.perftest.service.IPerfTestService#getPerfTestFilePath(org
	 * .ngrinder.perftest. model.PerfTest)
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
	 * @see org.ngrinder.perftest.service.IPerfTestService#getPerfTestFilePath(org
	 * .ngrinder.perftest. model.PerfTest)
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
		FileWriter fileWriter = null;
		try {
			// Copy grinder properties
			File userGrinderPropertiesPath = new File(getPerfTestDistributionPath(perfTest),
							DEFAULT_GRINDER_PROPERTIES_PATH);
			FileUtils.copyFile(config.getHome().getDefaultGrinderProperties(), userGrinderPropertiesPath);
			GrinderProperties grinderProperties = new GrinderProperties(userGrinderPropertiesPath);

			User user = perfTest.getCreatedUser();

			// Get all files in the script path
			FileEntry userDefinedGrinderProperties = fileEntryService.getFileEntry(user, FilenameUtils.concat(
							FilenameUtils.getPath(perfTest.getScriptName()), DEFAULT_GRINDER_PROPERTIES_PATH), perfTest
							.getScriptRevision());
			if (userDefinedGrinderProperties != null) {
				GrinderProperties userProperties = new GrinderProperties();
				grinderProperties.load(new StringReader(userDefinedGrinderProperties.getContent()));
				userProperties.putAll(grinderProperties);
				grinderProperties = userProperties;
			}
			grinderProperties.setAssociatedFile(new File(userGrinderPropertiesPath.getName()));
			grinderProperties.setProperty(GrinderProperties.SCRIPT,
							FilenameUtils.getName(checkNotEmpty(perfTest.getScriptName())));

			grinderProperties.setProperty(GRINDER_PROP_TEST_ID, "test_" + perfTest.getId());
			grinderProperties.setInt(GRINDER_PROP_THREAD, perfTest.getThreads());
			grinderProperties.setInt(GRINDER_PROP_PROCESSES, perfTest.getProcesses());
			if (perfTest.isThreshholdDuration()) {
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
			// grinderProperties.setProperty(GRINDER_PROP_JVM_ARGUMENTS, "-Xms256m -Xmx512m");
			grinderProperties.setProperty(GRINDER_PROP_JVM_CLASSPATH, getCustomClassPath(perfTest));
			grinderProperties.setInt(GRINDER_PROP_IGNORE_SAMPLE_COUNT, perfTest.getIgnoreSampleCount());
			grinderProperties.setBoolean(GRINDER_PROP_SECURITY, config.isSecurityEnabled());
			fileWriter = new FileWriter(userGrinderPropertiesPath);
			grinderProperties.store(fileWriter, perfTest.getTestIdentifier());
			LOGGER.info("Grinder Properties : {} ", grinderProperties);
			return grinderProperties;
		} catch (Exception e) {
			throw new NGrinderRuntimeException("error while prepare grinder property for " + perfTest.getTestName(), e);
		} finally {
			IOUtils.closeQuietly(fileWriter);
		}
	}

	/**
	 * Prepare files for distribution. This method store the files on the path
	 * ${NGRINDER_HOME}/perftest/{test_id}/dist folder.
	 * 
	 * @param perfTest
	 *            perfTest
	 * @return File location in which the perftest should have.
	 */
	public File prepareDistribution(PerfTest perfTest) {
		checkNotNull(perfTest.getId(), "perfTest should have id");
		String scriptName = checkNotEmpty(perfTest.getScriptName(), "perfTest should have script name");
		User user = perfTest.getCreatedUser();

		// Get all files in the script path
		FileEntry scriptEntry = checkNotNull(
						fileEntryService.getFileEntry(user, perfTest.getScriptName(), perfTest.getScriptRevision()),
						"script should exist");

		List<FileEntry> fileEntries = fileEntryService.getLibAndResourcesEntries(user, checkNotEmpty(scriptName),
						perfTest.getScriptRevision());
		fileEntries.add(scriptEntry);

		File perfTestDirectory = getPerfTestDistributionPath(perfTest);
		perfTestDirectory.mkdirs();

		String basePath = FilenameUtils.getPath(scriptEntry.getPath());

		// To minimize log..
		InputStream io = null;
		FileOutputStream fos = null;
		try {
			io = new ClassPathResource("/logback/logback-worker.xml").getInputStream();
			fos = new FileOutputStream(new File(perfTestDirectory, "logback-worker.xml"));
			IOUtils.copy(io, fos);
		} catch (IOException e) {
			LOGGER.error("error while writing logback-worker", e);
		} finally {
			IOUtils.closeQuietly(io);
			IOUtils.closeQuietly(fos);
		}
		// Distribute each files in that folder.
		for (FileEntry each : fileEntries) {
			// Directory is not subject to be distributed.
			if (each.getFileType() == FileType.DIR) {
				continue;
			}
			String path = FilenameUtils.getPath(each.getPath());
			path = path.substring(basePath.length());
			File toDir = new File(perfTestDirectory, path);
			LOGGER.info("{} is being written in {} for test {}",
							new Object[] { each.getPath(), toDir, perfTest.getTestIdentifier() });
			fileEntryService.writeContentTo(user, each.getPath(), toDir);
		}
		LOGGER.info("File write is completed in {}", perfTestDirectory);
		return perfTestDirectory;
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
	 * get the data point interval of report data. Use dataPointCount / imgWidth as the interval. if
	 * interval is 1, it means we will get all point from report. If interval is 2, it means we will
	 * get 1 point from every 2 data.
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
			LOGGER.error("Report data for {} in {} does not exisit.", testId, dataType);
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
			interval = Math.max((int)(lineNumber / pointCount), 1);
		} catch (Exception e) {
			LOGGER.error("Get report data for " + dataType + " failed:" + e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(lnr);
			IOUtils.closeQuietly(isr);
			IOUtils.closeQuietly(in);
		}

		return interval;
	}

	/**
	 * get the test report data as a string. Use interval to control the data point count. interval
	 * is 1, mean get all data point.
	 * 
	 * @param testId
	 *            test id
	 * @param dataType
	 *            data type
	 * @param interval
	 *            interval to collect data
	 * @return report data report data of that type
	 */
	public String getReportDataAsString(long testId, String dataType, int interval) {
		StringBuilder reportData = new StringBuilder("[");
		File reportFolder = config.getHome().getPerfTestReportDirectory(String.valueOf(testId));
		File targetFile = new File(reportFolder, dataType + DATA_FILE_EXTENSION);
		if (!targetFile.exists()) {
			LOGGER.error("Report data for {} in {} does not exisit.", testId, dataType);
			return "[ ]";
		}
		FileReader reader = null;
		BufferedReader br = null;
		try {
			reader = new FileReader(targetFile);
			br = new BufferedReader(reader);
			String data = br.readLine();
			int current = 0;
			while (StringUtils.isNotBlank(data)) {
				if (0 == current) {
					double number = NumberUtils.createDouble(StringUtils.defaultIfBlank(data, "0"));
					reportData.append(number);
					reportData.append(",");
				}
				if (++current >= interval) {
					current = 0;
				}
				data = br.readLine();
			}
			if (reportData.charAt(reportData.length()-1) == ',') {
				reportData.deleteCharAt(reportData.length() - 1);
			}
			reportData.append("]");
		} catch (IOException e) {
			LOGGER.error("Get report data for {} failed: {}", dataType, e.getMessage());
			LOGGER.debug("Trace is : ", e);
		} finally {
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(br);
		}
		return reportData.toString();
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
	 * To save statistics data when test is running and put into cache after that. If the console is
	 * not available, it returns null.
	 * 
	 * @param singleConsole
	 *            console signle console.
	 * @param perfTest
	 *            perfTest
	 */
	public void saveStatistics(SingleConsole singleConsole, PerfTest perfTest) {
		writeObjectToFile(new File(getPerfTestStatisticPath(perfTest), "statistics.stat"),
						singleConsole.getStatictisData());
	}

	/**
	 * get test running statistic data from cache. If there is no cache data, will return empty
	 * statistic data.
	 * 
	 * @param perfTest
	 *            perfTest
	 * 
	 * @return test running statistic data
	 */
	public Map<String, Object> getStatistics(PerfTest perfTest) {
		File file = new File(getPerfTestStatisticPath(perfTest), "statistics.stat");
		ConcurrentHashMap<String, Object> readObjectFromFile = readObjectFromFile(file,
						new ConcurrentHashMap<String, Object>());
		return readObjectFromFile;
	}

	/**
	 * Save system monitor data of all agents connected to one console. If the console is not
	 * available, it returns empty map. After getting, it will be put into cache.
	 * 
	 * @param singleConsole
	 *            singleConsole of the test add this parameter just for the key of cache.
	 * @param perfTest
	 *            perfTest
	 */
	public void saveAgentsInfo(SingleConsole singleConsole, PerfTest perfTest) {
		List<AgentIdentity> allAttachedAgents = singleConsole.getAllAttachedAgents();
		Map<AgentIdentity, SystemDataModel> result = new HashMap<AgentIdentity, SystemDataModel>();
		Set<AgentIdentity> allControllerAgents = agentManager.getAllAttachedAgents();
		for (AgentIdentity eachAgent : allAttachedAgents) {
			for (AgentIdentity eachControllerAgent : allControllerAgents) {
				if (eachControllerAgent.getName().equals(eachAgent.getName())) {
					result.put(eachControllerAgent, agentManager.getSystemDataModel(eachControllerAgent));
				}
			}
		}
		writeObjectToFile(new File(getPerfTestStatisticPath(perfTest), "agent_info.stat"), result);
	}

	/**
	 * Get agent info from saved file.
	 * 
	 * @param perfTest
	 *            perftest
	 * @return agent info map
	 */
	public Map<AgentIdentity, SystemDataModel> getAgentInfo(PerfTest perfTest) {
		HashMap<AgentIdentity, SystemDataModel> readObjectFromFile = readObjectFromFile(new File(
						getPerfTestStatisticPath(perfTest), "agent_info.stat"),
						new HashMap<AgentIdentity, SystemDataModel>());
		return readObjectFromFile;
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
		} catch (Exception e) {
			throw new NGrinderRuntimeException("Error while setting console properties", e);
		}
		return consoleProperties;
	}

	double parseDoubleWithSafety(Map<?, ?> map, Object key, Double defaultValue) {
		Double doubleValue = MapUtils.getDouble(map, key, defaultValue);
		return Math.round(doubleValue * 100D) / 100D;
	}

	/**
	 * Update the given {@link PerfTest} properties after test finished.
	 * 
	 * @param perfTest
	 *            perfTest
	 */
	public void updatePerfTestAfterTestFinish(PerfTest perfTest) {
		checkNotNull(perfTest);
		Map<String, Object> result = getStatistics(perfTest);
		@SuppressWarnings("unchecked")
		Map<String, Object> totalStatistics = MapUtils.getMap(result, "totalStatistics", MapUtils.EMPTY_MAP);
		LOGGER.info("Total Statistics for test {}  is {}", perfTest.getId(), totalStatistics);
		perfTest.setTps(parseDoubleWithSafety(totalStatistics, "TPS", 0D));
		perfTest.setMeanTestTime(parseDoubleWithSafety(totalStatistics, "Mean_Test_Time_(ms)", 0D));
		perfTest.setPeakTps(parseDoubleWithSafety(totalStatistics, "Peak_TPS", 0D));
		perfTest.setTests(MapUtils.getDouble(totalStatistics, "Tests", 0D).intValue());
		perfTest.setErrors(MapUtils.getDouble(totalStatistics, "Errors", 0D).intValue());

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
	 * @see org.ngrinder.perftest.service.IPerfTestService#stopPerfTest(org.ngrinder .model.User,
	 * java.lang.Long)
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
	 * @see org.ngrinder.perftest.service.IPerfTestService#addCommentOn(org.ngrinder .model.User,
	 * int, java.lang.String)
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
		Map<User, PerfTestStatistics> perfTestPerUser = new HashMap<User, PerfTestStatistics>();
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

}
