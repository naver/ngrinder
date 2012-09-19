/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ngrinder.perftest.service;

import static org.ngrinder.common.util.Preconditions.checkNotEmpty;
import static org.ngrinder.common.util.Preconditions.checkNotNull;
import static org.ngrinder.common.util.Preconditions.checkNotZero;
import static org.ngrinder.model.Status.getProcessingOrTestingTestStatus;
import static org.ngrinder.perftest.repository.PerfTestSpecification.emptyPredicate;
import static org.ngrinder.perftest.repository.PerfTestSpecification.idEqual;
import static org.ngrinder.perftest.repository.PerfTestSpecification.idSetEqual;
import static org.ngrinder.perftest.repository.PerfTestSpecification.lastModifiedOrCreatedBy;
import static org.ngrinder.perftest.repository.PerfTestSpecification.likeTestNameOrDescription;
import static org.ngrinder.perftest.repository.PerfTestSpecification.statusSetEqual;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Role;
import org.ngrinder.model.Status;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link PerfTest} Service Class.
 * 
 * This class contains various method which mainly get the {@link PerfTest} matching specific
 * conditions from DB.
 * 
 * @author Mavlarn
 * @author JunHo Yoon
 * @since 3.0
 */
@Service
public class PerfTestService implements NGrinderConstants, IPerfTestService {

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

	private NumberFormat formatter = new DecimalFormat("###.###");

	private int MaximumConcurrentTestCount = 0;

	/**
	 * Get {@link PerfTest} list on the user.
	 * 
	 * @param user
	 *            user
	 * @param query
	 * @param isFinished
	 *            only find finished test
	 * @param pageable
	 *            paging info
	 * @return found {@link PerfTest} list
	 */
	public Page<PerfTest> getPerfTestList(User user, String query, boolean isFinished, Pageable pageable) {

		Specifications<PerfTest> spec = Specifications.where(emptyPredicate());

		// User can see only his own test
		if (user.getRole().equals(Role.USER)) {
			spec = spec.and(lastModifiedOrCreatedBy(user));
		}

		if (isFinished) {
			spec = spec.and(statusSetEqual(Status.FINISHED));
		}
		if (StringUtils.isNotBlank(query)) {
			spec = spec.and(likeTestNameOrDescription(query));
		}
		return perfTestRepository.findAll(spec, pageable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.perftest.service.IPerfTestService#getPerfTest(org.ngrinder .model.User,
	 * java.lang.Integer)
	 */
	@Override
	public PerfTest getPerfTest(User user, Long id) {
		Specifications<PerfTest> spec = Specifications.where(emptyPredicate());

		// User can see only his own test
		if (user.getRole().equals(Role.USER)) {
			spec = spec.and(lastModifiedOrCreatedBy(user));
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

		Specifications<PerfTest> spec = Specifications.where(emptyPredicate());

		// User can see only his own test
		if (user.getRole().equals(Role.USER)) {
			spec = spec.and(lastModifiedOrCreatedBy(user));
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
		Specifications<PerfTest> spec = Specifications.where(emptyPredicate());

		// User can see only his own test
		if (user != null) {
			spec = spec.and(lastModifiedOrCreatedBy(user));
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
		Specifications<PerfTest> spec = Specifications.where(emptyPredicate());

		// User can see only his own test
		if (user != null) {
			spec = spec.and(lastModifiedOrCreatedBy(user));
		}
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
		return savePerfTest(perfTest);
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
	 * Save performance test with given status.
	 * 
	 * This method is only used for changing {@link Status}
	 * 
	 * @param perfTest
	 *            {@link PerfTest} instance which will be saved.
	 * @param status
	 *            Status to be assigned
	 * @return saved {@link PerfTest}
	 */
	@Transactional
	public PerfTest setRecodingStarting(PerfTest perfTest, long systemTimeMills) {
		checkNotNull(perfTest);
		checkNotNull(perfTest.getId(), "perfTest with status should save Id");
		PerfTest findOne = perfTestRepository.findOne(perfTest.getId());
		if (findOne == null) {
			return null;
		}
		findOne.setStartTime(new Date(systemTimeMills));
		return perfTestRepository.save(findOne);
	}

	/**
	 * Mark test error on {@link PerfTest} instance
	 * 
	 * @param perfTest
	 *            {@link PerfTest}
	 * @param singleConsole
	 *            console in use
	 * @param e
	 *            exception occurs.
	 * @return
	 */
	public PerfTest markAbromalTermination(PerfTest perfTest, StopReason reason) {
		// Leave last status as test error cause
		PerfTest findOne = perfTestRepository.findOne(perfTest.getId());
		if (findOne == null) {
			return null;
		}

		findOne.setTestErrorCause(perfTest.getStatus());
		findOne.setLastProgressMessage(reason.name());
		findOne.setStatus(Status.ABNORMAL_TESTING);
		return perfTestRepository.save(findOne);
	}

	/**
	 * Save performance test with given status.
	 * 
	 * This method is only used for changing {@link Status}
	 * 
	 * @param perfTest
	 *            {@link PerfTest} instance which will be saved.
	 * @param status
	 *            Status to be assigned
	 * @return saved {@link PerfTest}
	 */
	@Transactional
	public PerfTest changePerfTestStatus(PerfTest perfTest, Status status, String message) {
		checkNotNull(perfTest);
		checkNotNull(perfTest.getId(), "perfTest with status should save Id");
		perfTest.setStatus(checkNotNull(status, "status should not be null"));
		PerfTest findOne = perfTestRepository.findOne(perfTest.getId());
		if (findOne == null) {
			return null;
		}
		findOne.setStatus(status);
		findOne.setLastProgressMessage(message);
		return perfTestRepository.save(findOne);
	}

	/**
	 * Add a progress message on the given perfTest
	 * 
	 * @param perfTest
	 *            perf test
	 * @param message
	 *            message to be recored.
	 */
	@Transactional
	public void markProgress(PerfTest perfTest, String message) {
		PerfTest findOne = perfTestRepository.findOne(perfTest.getId());
		if (findOne == null) {
			return;
		}
		findOne.setLastProgressMessage(message);
		perfTestRepository.save(findOne);
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
	 */
	@Transactional
	public PerfTest markProgressAndStatus(PerfTest perfTest, Status status, String message) {
		PerfTest findOne = perfTestRepository.findOne(perfTest.getId());
		if (findOne == null) {
			return null;
		}
		findOne.setStatus(status);
		findOne.setLastProgressMessage(message);
		return perfTestRepository.save(findOne);
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
	 */

	@Transactional
	public PerfTest markProgressAndStatusAndFinishTimeAndStatistics(PerfTest perfTest, Status status, String message) {
		PerfTest findOne = perfTestRepository.findOne(perfTest.getId());
		if (findOne == null) {
			return null;
		}
		findOne.setStatus(status);
		findOne.setLastProgressMessage(message);
		findOne.setFinishTime(new Date());
		updatePerfTestAfterTestFinish(findOne);
		return perfTestRepository.save(findOne);
	}

	/**
	 * Mark test error on {@link PerfTest} instance
	 * 
	 * @param perfTest
	 *            {@link PerfTest}
	 * @param reason
	 *            error reason
	 */
	@Transactional
	public void markPerfTestError(PerfTest perfTest, String message) {
		PerfTest findOne = perfTestRepository.findOne(perfTest.getId());
		if (findOne == null) {
			return;
		}
		findOne.setStatus(Status.ABNORMAL_TESTING);
		findOne.setTestErrorCause(findOne.getStatus());
		findOne.setLastProgressMessage(message);
		perfTestRepository.save(findOne);
	}

	@Transactional
	public void markPerfTestConsoleStart(PerfTest perfTest, int consolePort) {
		PerfTest findOne = perfTestRepository.findOne(perfTest.getId());
		if (findOne == null) {
			return;
		}
		findOne.setPort(consolePort);
		findOne.setStatus(Status.START_CONSOLE_FINISHED);
		findOne.setLastProgressMessage("Console is started on port " + consolePort);
		perfTestRepository.save(findOne);
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
	 * @return found {@link PerfTest}, null otherwise
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
	 * @return
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
	private List<PerfTest> filterCurrentlyRunningTestUsersTest(List<PerfTest> perfTestLists) {
		List<PerfTest> currentlyRunningTests = getCurrentlyRunningTest();
		final Set<User> currentlyRunningTestOwners = new HashSet<User>();
		for (PerfTest each : currentlyRunningTests) {
			currentlyRunningTestOwners.add((User) ObjectUtils.defaultIfNull(each.getLastModifiedUser(),
							each.getCreatedUser()));
		}
		CollectionUtils.filter(perfTestLists, new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				PerfTest perfTest = (PerfTest) object;
				return !currentlyRunningTestOwners.contains(ObjectUtils.defaultIfNull(perfTest.getLastModifiedUser(),
								perfTest.getCreatedUser()));
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
		return getPerfTest(null, Status.getTestingTestStates());
	}

	/**
	 * Get abnormally testing PerfTest.
	 * 
	 * @return found {@link PerfTest} list
	 */
	public List<PerfTest> getAbnoramlTestingPerfTest() {
		return getPerfTest(null, Status.ABNORMAL_TESTING);
	}

	/**
	 * Delete PerfTest by id.
	 * 
	 * Never use this method in runtime. This method is used only for testing.
	 * 
	 * @param id
	 *            {@link PerfTest} it
	 */
	public void deletePerfTest(long id) {
		perfTestRepository.delete(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.perftest.service.IPerfTestService#getPerfTestFilePath(org
	 * .ngrinder.perftest. model.PerfTest)
	 */
	@Override
	public File getPerfTestFilePath(PerfTest perfTest) {
		return config.getHome().getPerfTestDirectory(
						checkNotZero(perfTest.getId(), "perftest id should not be 0 or zero").toString());
	}

	public String getCustomClassPath(PerfTest perfTest) {
		File perfTestDirectory = getPerfTestDirectory(perfTest);
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
	 * Create {@link GrinderProperties} based on the passed {@link PerfTest}
	 * 
	 * @param perfTest
	 *            base data
	 * @return created {@link GrinderProperties} instance
	 */
	public GrinderProperties getGrinderProperties(PerfTest perfTest) {
		try {
			// Copy grinder properties
			File userGrinderPropertiesPath = new File(getPerfTestDirectory(perfTest), DEFAULT_GRINDER_PROPERTIES_PATH);
			FileUtils.copyFile(config.getHome().getDefaultGrinderProperties(), userGrinderPropertiesPath);
			GrinderProperties grinderProperties = new GrinderProperties(userGrinderPropertiesPath);
			grinderProperties.setAssociatedFile(new File(userGrinderPropertiesPath.getName()));
			grinderProperties.setProperty(GrinderProperties.SCRIPT,
							FilenameUtils.getName(checkNotEmpty(perfTest.getScriptName())));

			grinderProperties.setProperty(GRINDER_PROP_TEST_ID, "test_" + perfTest.getId());
			grinderProperties.setInt(GRINDER_PROP_THREAD, perfTest.getThreads());
			grinderProperties.setInt(GRINDER_PROP_PROCESSES, perfTest.getProcesses());
			if ("D".equals(perfTest.getThreshold())) {
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
			if (perfTest.isUseRampUp()) {
				grinderProperties.setInt(GRINDER_PROP_PROCESS_INCREMENT, perfTest.getProcessIncrement());
				grinderProperties.setInt(GRINDER_PROP_PROCESS_INCREMENT_INTERVAL,
								perfTest.getProcessIncrementInterval());
				grinderProperties.setInt(GRINDER_PROP_INITIAL_SLEEP_TIME, perfTest.getInitSleepTime());
			} else {
				grinderProperties.setInt(GRINDER_PROP_PROCESS_INCREMENT, 0);
			}
			grinderProperties.setProperty(GRINDER_PROP_JVM_CLASSPATH, getCustomClassPath(perfTest));
			grinderProperties.setInt(GRINDER_PROP_IGNORE_SAMPLE_COUNT, perfTest.getIgnoreSampleCount());
			boolean securityEnabled = config.isSecurityEnabled();
			grinderProperties.setBoolean(GRINDER_PROP_SECURITY, securityEnabled);
			// set security.manager argument
			if (securityEnabled) {
				String jvmArguments = "-Djava.security.manager=org.ngrinder.sm.NGrinderSecurityManager";
				grinderProperties.setProperty(GRINDER_PROP_JVM_ARGUMENTS, jvmArguments);
			}
			LOGGER.info("Grinder Properties : {} ", grinderProperties);
			return grinderProperties;
		} catch (Exception e) {
			throw new NGrinderRuntimeException("error while prepare grinder property for " + perfTest.getTestName(), e);
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
		FileEntry scriptEntry = fileEntryService.getFileEntry(user, perfTest.getScriptName(),
						perfTest.getScriptRevision());
		List<FileEntry> fileEntries = fileEntryService.getLibAndResourcesEntries(user, checkNotEmpty(scriptName),
						perfTest.getScriptRevision());
		File perfTestDirectory = getPerfTestDirectory(perfTest);
		fileEntries.add(scriptEntry);

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
			LOGGER.info("{} is being written in {} for test {}", new Object[] { each.getPath(), toDir.toString(),
					perfTest.getTestIdentifier() });
			fileEntryService.writeContentTo(user, each.getPath(), toDir);
		}
		LOGGER.info("File write is completed in " + perfTestDirectory);
		return perfTestDirectory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.perftest.service.IPerfTestService#getPerfTestBaseDirectory
	 * (org.ngrinder.perftest .model.PerfTest)
	 */
	@Override
	public File getPerfTestBaseDirectory(PerfTest perfTest) {
		return config.getHome().getPerfTestDirectory(perfTest.getId().toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.perftest.service.IPerfTestService#getPerfTestDirectory(org
	 * .ngrinder.perftest .model.PerfTest)
	 */
	@Override
	public File getPerfTestDirectory(PerfTest perfTest) {
		return new File(getPerfTestBaseDirectory(perfTest), NGrinderConstants.PATH_DIST);
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
	 * get the data point interval of report data.
	 * 
	 * @param testId
	 * @param dataType
	 * @param imgWidth
	 * @return interval value
	 */
	public int getReportDataInterval(long testId, String dataType, int imgWidth) {
		if (imgWidth < 100) {
			imgWidth = 100;
		}
		int pointCount = imgWidth;
		File reportFolder = config.getHome().getPerfTestDirectory(
						testId + File.separator + NGrinderConstants.PATH_REPORT);
		int lineNumber;
		int interval = 0;
		File targetFile = null;
		targetFile = new File(reportFolder, dataType + DATA_FILE_EXTENSION);
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
			interval = lineNumber / pointCount;
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
	 * get the test report data as a string.
	 * 
	 * @param testId
	 *            test id
	 * @param dataType
	 *            data type
	 * @param imgWidth
	 *            image width
	 * @return report data
	 * @throws IOException
	 */
	public String getReportDataAsString(long testId, String dataType, int interval) {

		StringBuilder reportData = new StringBuilder("[");
		File reportFolder = config.getHome().getPerfTestDirectory(
						testId + File.separator + NGrinderConstants.PATH_REPORT);

		File targetFile = null;
		targetFile = new File(reportFolder, dataType + DATA_FILE_EXTENSION);
		if (!targetFile.exists()) {
			LOGGER.error("Report data for {} in {} does not exisit.", testId, dataType);
			return "[ ]";
		}
		FileReader reader = null;
		BufferedReader br = null;
		try {
			reader = new FileReader(targetFile);
			br = new BufferedReader(reader);
			String data = null;
			int current = 0;
			while (StringUtils.isNotBlank(data = br.readLine())) {
				if (0 == current) {
					double number = NumberUtils.createDouble(data);
					reportData.append(number);
					reportData.append(",");
				}
				if (++current >= interval) {
					current = 0;
				}
			}

			reportData.append("]");
		} catch (IOException e) {
			LOGGER.error("Get report data for " + dataType + " failed:" + e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(br);
		}

		return reportData.toString();
	}

	/**
	 * Get report file name for give test id.
	 * 
	 * @param testId
	 * @return report file path
	 */
	public File getReportFile(long testId) {
		return new File(getReportFileDirectory(testId), NGrinderConstants.REPORT_CSV);
	}

	/**
	 * Get log file names for give test id.
	 * 
	 * @param testId
	 * @return report file path
	 */
	public File getLogFile(long testId, String fileName) {
		return new File(getLogFileDirectory(testId), fileName);
	}

	/**
	 * Get report file directory for give test id.
	 * 
	 * @param testId
	 * @return report file path
	 */

	public File getLogFileDirectory(long testId) {
		return new File(config.getHome().getPerfTestDirectory(String.valueOf(testId)), NGrinderConstants.PATH_LOG);
	}

	/**
	 * Get log files list on the given test
	 * 
	 * @param testId
	 * @return
	 */
	public List<String> getLogFiles(long testId) {
		File logFileDirectory = getLogFileDirectory(testId);
		if (!logFileDirectory.exists() || !logFileDirectory.isDirectory()) {
			return Collections.emptyList();
		}
		return Arrays.asList(logFileDirectory.list());
	}

	/**
	 * Get report file directory for give test id.
	 * 
	 * @param testId
	 * @return report file path
	 */

	public File getReportFileDirectory(long testId) {
		return new File(config.getHome().getPerfTestDirectory(String.valueOf(testId)), NGrinderConstants.PATH_REPORT);
	}

	/**
	 * To get statistics data when test is running If the console is not available.. it returns
	 * empty map.
	 */
	public Map<String, Object> getStatistics(int port) {
		return consoleManager.getConsoleUsingPort(port).getStatictisData();
	}

	/**
	 * To get statistics data when test is running If the console is not available.. it returns
	 * empty map.
	 */
	public Map<AgentIdentity, SystemDataModel> getAgentsInfo(int port) {
		List<AgentIdentity> allAttachedAgents = consoleManager.getConsoleUsingPort(port).getAllAttachedAgents();
		Set<AgentIdentity> allControllerAgents = agentManager.getAllAttachedAgents();
		Map<AgentIdentity, SystemDataModel> result = new HashMap<AgentIdentity, SystemDataModel>();
		for (AgentIdentity eachAgent : allAttachedAgents) {
			for (AgentIdentity eachControllerAgent : allControllerAgents) {
				if (eachControllerAgent.getName().equals(eachAgent.getName())) {
					result.put(eachControllerAgent, agentManager.getSystemDataModel(eachControllerAgent));
				}
			}
		}
		return result;
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
			consoleProperties.setAndSaveDistributionDirectory(new Directory(getPerfTestDirectory(perfTest)));
		} catch (Exception e) {
			throw new NGrinderRuntimeException("Error while setting console properties", e);
		}
		return consoleProperties;
	}

	/**
	 * Update the given {@link PerfTest} properties after test finished.
	 * 
	 * @param perfTest
	 *            perfTest
	 */
	public void updatePerfTestAfterTestFinish(PerfTest perfTest) {
		checkNotNull(perfTest);
		int port = perfTest.getPort();
		Map<String, Object> result = getStatistics(port);
		if (result == null) {
			return;
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> totalStatistics = (Map<String, Object>) result.get("totalStatistics");
		perfTest.setErrors((int) ((Double) totalStatistics.get("Errors")).doubleValue());
		perfTest.setTps(Double.parseDouble(formatter.format(totalStatistics.get("TPS"))));
		perfTest.setMeanTestTime(Double.parseDouble(formatter.format(ObjectUtils.defaultIfNull(
						totalStatistics.get("Mean_Test_Time_(ms)"), 0D))));
		perfTest.setPeakTps(Double.parseDouble(formatter.format(ObjectUtils.defaultIfNull(
						totalStatistics.get("Peak_TPS"), 0D))));
		perfTest.setTests((int) ((Double) totalStatistics.get("Tests")).doubleValue());
		LOGGER.info("Total Statics for test {}  is {}", perfTest.getId(), totalStatistics);
	}

	/**
	 * Get maximum concurrent test count.
	 * 
	 * @return maximum concurrent test
	 */
	public int getMaximumConcurrentTestCount() {
		if (MaximumConcurrentTestCount == 0) {
			MaximumConcurrentTestCount = config.getSystemProperties().getPropertyInt(
							NGrinderConstants.NGRINDER_PROP_MAX_CONCURRENT_TEST,
							NGrinderConstants.NGRINDER_PROP_MAX_CONCURRENT_TEST_VALUE);
		}
		return MaximumConcurrentTestCount;
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
		if (user.getRole() != Role.ADMIN && !perfTest.getLastModifiedUser().equals(user)) {
			return;
		}
		// If it's not stoppable status.. It's wrong request.
		if (!perfTest.getStatus().isStoppable()) {
			return;
		}
		perfTest.setStopRequest(true);
		// Just mark cancel on console
		SingleConsole consoleUsingPort = consoleManager.getConsoleUsingPort(perfTest.getPort());
		if (consoleUsingPort != null) {
			consoleUsingPort.cancel();
		}
		perfTestRepository.save(perfTest);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.perftest.service.IPerfTestService#getStopRequestedPerfTest()
	 */
	@Override
	public List<PerfTest> getStopRequestedPerfTest() {
		final List<PerfTest> perfTests = getPerfTest(null, getProcessingOrTestingTestStatus());
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
	public void addCommentOn(User user, Long testId, String testComment) {
		PerfTest perfTest = getPerfTest(user, testId);
		perfTest.setTestComment(testComment);
		perfTestRepository.save(perfTest);
	}

	@Transactional
	public Collection<PerfTestStatistics> getCurrentPerfTestStatistics() {
		Map<User, PerfTestStatistics> perfTestPerUser = new HashMap<User, PerfTestStatistics>();
		for (PerfTest each : getPerfTest(null, getProcessingOrTestingTestStatus())) {
			User lastModifiedUser = each.getLastModifiedUser();
			PerfTestStatistics perfTestStatistics = perfTestPerUser.get(lastModifiedUser);
			if (perfTestStatistics == null) {
				perfTestStatistics = new PerfTestStatistics(lastModifiedUser);
				perfTestPerUser.put(lastModifiedUser, perfTestStatistics);
			}
			perfTestStatistics.addPerfTest(each);
		}
		return perfTestPerUser.values();
	}

}
