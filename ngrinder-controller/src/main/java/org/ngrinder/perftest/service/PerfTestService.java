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
import static org.ngrinder.perftest.repository.PerfTestSpecification.createdBy;
import static org.ngrinder.perftest.repository.PerfTestSpecification.emptyPredicate;
import static org.ngrinder.perftest.repository.PerfTestSpecification.likeTestNameOrDescription;
import static org.ngrinder.perftest.repository.PerfTestSpecification.statusSetEqual;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.grinder.SingleConsole;
import net.grinder.common.GrinderProperties;
import net.grinder.common.Test;
import net.grinder.console.communication.NGrinderConsoleCommunicationService;
import net.grinder.console.communication.ProcessControl;
import net.grinder.console.model.ConsoleProperties;
import net.grinder.console.model.ModelTestIndex;
import net.grinder.console.model.SampleModel;
import net.grinder.console.model.SampleModelViews;
import net.grinder.statistics.ExpressionView;
import net.grinder.statistics.StatisticsSet;
import net.grinder.util.ConsolePropertiesFactory;
import net.grinder.util.Directory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.util.ReflectionUtil;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.spring.OnlyOnePageRequest;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.perftest.model.PerfTest;
import org.ngrinder.perftest.model.ProcessAndThread;
import org.ngrinder.perftest.model.Status;
import org.ngrinder.perftest.repository.PerfTestRepository;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileType;
import org.ngrinder.script.service.FileEntryService;
import org.picocontainer.MutablePicoContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;

/**
 * Performance Test Service Class.
 * 
 * @author Mavlarn
 * @author JunHo Yoon
 * @since 3.0
 */
@Service
public class PerfTestService implements NGrinderConstants {

	private static final Logger LOGGER = LoggerFactory.getLogger(PerfTestService.class);

	private static final String DATA_FILE_EXTENSION = ".data";

	@Autowired
	private PerfTestRepository perfTestRepository;

	@Autowired
	private ConsoleManager consoleManager;

	@Autowired
	private Config config;

	@Autowired
	private FileEntryService fileEntryService;

	private NumberFormat formatter = new DecimalFormat("###.###");
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
			spec = spec.and(createdBy(user));
		}

		if (isFinished) {
			spec = spec.and(statusSetEqual(Status.FINISHED));
		}
		if (StringUtils.isNotBlank(query)) {
			spec = spec.and(likeTestNameOrDescription(query));
		}
		return perfTestRepository.findAll(spec, pageable);
	}

	/**
	 * Save {@link PerfTest}.
	 * 
	 * @param perfTest
	 *            {@link PerfTest} instance to be saved.
	 * @return Saved {@link PerfTest}
	 */
	@CacheEvict(value = { "perftest", "perftestlist" }, allEntries = true)
	public PerfTest savePerfTest(PerfTest perfTest) {
		checkNotNull(perfTest);
		// Merge if necessary
		if (perfTest.exist()) {
			PerfTest existingPerfTest = perfTestRepository.findOne(perfTest.getId());
			perfTest = existingPerfTest.merge(perfTest);
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
	@CacheEvict(value = { "perftest", "perftestlist" }, allEntries = true)
	public PerfTest savePerfTest(PerfTest perfTest, Status status) {
		checkNotNull(perfTest);
		checkNotNull(perfTest.getId(), "perfTest with status should save Id");
		perfTest.setStatus(checkNotNull(status, "status should not be null"));
		return perfTestRepository.save(perfTest);
	}

	/**
	 * Get PerfTest by testId.
	 * 
	 * @param testId
	 *            PerfTest id
	 * @return found {@link PerfTest}, null otherwise
	 */
	@Cacheable(value = "perftest")
	public PerfTest getPerfTest(long testId) {
		return perfTestRepository.findOne(testId);
	}

	/**
	 * Get next runnable PerfTest.
	 * 
	 * @return found {@link PerfTest}, null otherwise
	 */
	@Cacheable(value = "perftest")
	public PerfTest getPerfTestCandiate() {
		Page<PerfTest> perfTest = perfTestRepository.findAllByStatusOrderByScheduledTimeAsc(Status.READY,
				new OnlyOnePageRequest());
		return (perfTest.getNumberOfElements() == 0) ? null : perfTest.getContent().get(0);
	}

	/**
	 * Get currently testing PerfTest.
	 * 
	 * @return found {@link PerfTest} list
	 */
	@Cacheable(value = "perftestlist")
	public List<PerfTest> getTestingPerfTest() {
		return perfTestRepository.findAllByStatusOrderByCreatedDateAsc(Status.TESTING);
	}

	/**
	 * Delete PerfTest by id.
	 * 
	 * Never use this method in runtime. This method is used only for testing.
	 * 
	 * @param id
	 *            {@link PerfTest} it
	 */
	@CacheEvict(value = { "perftest", "perftestlist" }, allEntries = true)
	public void deletePerfTest(long id) {
		perfTestRepository.delete(id);
	}

	/**
	 * Get PerfTest Directory in which the distributed file is stored.
	 * 
	 * @param perfTest
	 *            pefTest from which distribution dire.ctory calculated
	 * @return path on in files are saved.
	 */
	public File getPerfTestFilePath(PerfTest perfTest) {
		return config.getHome().getPerfTestDirectory(
				checkNotZero(perfTest.getId(), "perftest id should not be 0 or zero").toString());
	}

	public GrinderProperties getGrinderProperties(PerfTest perfTest) {
		try {
			// Copy grinder properties
			File userGrinderPropertiesPath = new File(getUserPerfTestDirectory(perfTest),
					DEFAULT_GRINDER_PROPERTIES_PATH);
			FileUtils.copyFile(config.getHome().getDefaultGrinderProperties(), userGrinderPropertiesPath);
			GrinderProperties grinderProperties = new GrinderProperties(userGrinderPropertiesPath);
			grinderProperties.setAssociatedFile(new File(userGrinderPropertiesPath.getName()));
			grinderProperties.setProperty(GrinderProperties.SCRIPT,
					FilenameUtils.getName(checkNotEmpty(perfTest.getScriptName())));
			ProcessAndThread calcProcessAndThread = calcProcessAndThread(checkNotZero(perfTest.getVuserPerAgent(),
					"vuser count should be provided"));
			grinderProperties.setInt(GRINDER_PROP_THREAD, calcProcessAndThread.getThreadCount());
			grinderProperties.setInt(GRINDER_PROP_PROCESSES, calcProcessAndThread.getProcessCount());
			if ("D".equals(perfTest.getThreshold())) {
				grinderProperties.setLong(GRINDER_PROP_DURATION, perfTest.getDuration());
			} else {
				grinderProperties.setInt(GRINDER_PROP_RUNS, perfTest.getRunCount());
			}
			grinderProperties.setBoolean(GRINDER_PROP_USE_CONSOLE, true);
			grinderProperties.setInt(GRINDER_PROP_INITIAL_SLEEP_TIME, perfTest.getInitSleepTime());
			grinderProperties.setInt(GRINDER_PROP_PROCESS_INCREMENT, perfTest.getProcessIncrement());
			grinderProperties.setInt(GRINDER_PROP_PROCESS_INCREMENT_INTERVAL, perfTest.getProcessIncrementInterval());
			return grinderProperties;
		} catch (Exception e) {
			throw new NGrinderRuntimeException("error while prepare grinder property for " + perfTest.getTestName(), e);
		}
	}

	/**
	 * Prepare files for distribution
	 * 
	 * @param perfTest
	 * @return
	 */
	public File prepareDistribution(PerfTest perfTest) {
		checkNotNull(perfTest.getId(), "perfTest should have id");
		String scriptName = checkNotEmpty(perfTest.getScriptName(), "perfTest should have script name");
		User user = perfTest.getCreatedUser();

		// Get all files in the script path
		List<FileEntry> fileEntries = fileEntryService.getFileEntries(user,
				FilenameUtils.getPath(checkNotEmpty(scriptName)));
		File perfTestDirectory = getUserPerfTestDirectory(perfTest);

		// clean up Distribution folders
		FileUtils.deleteQuietly(perfTestDirectory);
		perfTestDirectory.mkdirs();

		// Distribute each files in that folder.
		for (FileEntry each : fileEntries) {
			// Directory is not subject to be distributed.
			if (each.getFileType() == FileType.DIR) {
				continue;
			}
			LOGGER.info(each.getPath() + " is being written in " + perfTestDirectory);
			fileEntryService.writeContentTo(user, each.getPath(), perfTestDirectory);
		}
		LOGGER.info("File write is completed in " + perfTestDirectory);
		return perfTestDirectory;
	}

	public File getUserPerfTestDirectory(PerfTest perfTest) {
		return config.getHome().getPerfTestDirectory(perfTest.getId().toString());
	}

	/**
	 * Get the optimal process and thread count.
	 * 
	 * FIXME : This method should be optimized more.
	 * 
	 * @param newVuser
	 *            the count of virtual users per agent
	 * @return optimal process thread count
	 */
	public ProcessAndThread calcProcessAndThread(int newVuser) {
		int threadCount = 2;
		int processCount = newVuser / threadCount + newVuser % threadCount;
		return new ProcessAndThread(processCount, threadCount);
	}

	/**
	 * get report data by test id, data type, and image width
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
	public List<Object> getReportData(long testId, String dataType, int imgWidth) throws IOException {
		//TODO: later, we can make the file content as the string of list, then we can 
		// just return the file content directly, it will be much faster.
		List<Object> reportData = new ArrayList<Object>();
		File reportFolder = config.getHome().getPerfTestDirectory(
				testId + File.separator + NGrinderConstants.PATH_REPORT);
		if (imgWidth < 100) {
			imgWidth = 100;
		}
		int pointCount = imgWidth / 10;
		int lineNumber;
		File targetFile = null;
		targetFile = new File(reportFolder, dataType.toLowerCase() + DATA_FILE_EXTENSION);
		if (!targetFile.exists()) {
			return reportData;
		}
		LineNumberReader lnr = null;
		try {
			lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(targetFile)));
			lnr.skip(targetFile.length());
			lineNumber = lnr.getLineNumber() + 1;

		} finally {
			IOUtils.closeQuietly(lnr);
		}
		FileReader reader = null;
		BufferedReader br = null;
		try {
			reader = new FileReader(targetFile);
			br = new BufferedReader(reader);
			String data = null;
			int current = 0;
			int interval = lineNumber / pointCount;
			// TODO should get average data
			// FIXME : NEVER NEVER DO IT. Be aware of memory size.!!
			while (StringUtils.isNotBlank(data = br.readLine())) {
				if (0 == current) {
					long number = NumberUtils.createLong(data);
					reportData.add(number);
				}
				if (++current >= interval) {
					current = 0;
				}
			}
		} finally {
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(br);
		}

		return reportData;
	}

	public File getReportFile(long testId) {
		File reportFolder = config.getHome().getPerfTestDirectory(
				testId + File.separator + NGrinderConstants.PATH_REPORT);
		File targetFile = new File(reportFolder, NGrinderConstants.REPORT_CSV);
		return targetFile;
	}

	/**
	 * To get statistics data when test is running
	 */
	public Map<String, Object> getStatistics(int port) {
		checkNotNull(port, "perfTest is testing ,Its port should not be null!");

		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<String, Object>> cumulativeStatistics = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> lastSampleStatistics = new ArrayList<Map<String, Object>>();
		SingleConsole singleConsole = consoleManager.getConsoleUsingPort(port);

		final SampleModel model = (SampleModel) singleConsole.getConsoleComponent(SampleModel.class);
		final SampleModelViews modelView = (SampleModelViews) singleConsole.getConsoleComponent(SampleModelViews.class);
		ExpressionView[] views = modelView.getCumulativeStatisticsView().getExpressionViews();
		ModelTestIndex modelIndex = (ModelTestIndex) ReflectionUtil.getFieldValue(model, "modelTestIndex");
		if (modelIndex != null) {
			for (int i = 0; i < modelIndex.getNumberOfTests(); i++) {
				Map<String, Object> statistics = new HashMap<String, Object>();
				Map<String, Object> lastStatistics = new HashMap<String, Object>();

				Test test = modelIndex.getTest(i);
				statistics.put("testNumber", test.getNumber());
				statistics.put("testDescription", test.getDescription());
				lastStatistics.put("testNumber", test.getNumber());
				lastStatistics.put("testDescription", test.getDescription());

				StatisticsSet set = modelIndex.getCumulativeStatistics(i);
				StatisticsSet lastSet = modelIndex.getLastSampleStatistics(i);
				for (ExpressionView expressionView : views) { // TODO : expressionView == null?
					statistics.put(expressionView.getDisplayName().replaceAll("\\s+", "_"),
							getRealDoubleValue(expressionView.getExpression().getDoubleValue(set)));
					lastStatistics.put(expressionView.getDisplayName().replaceAll("\\s+", "_"),
							getRealDoubleValue(expressionView.getExpression().getDoubleValue(lastSet)));
				}

				// Tests
				Double tests = (Double) statistics.get("Tests");
				Double errors = (Double) statistics.get("Errors");
				statistics.put("TestsStr", formatter.format(tests));
				statistics.put("ErrorsStr", formatter.format(errors));

				Double lastTests = (Double) lastStatistics.get("Tests");
				Double lastErrors = (Double) lastStatistics.get("Errors");
				lastStatistics.put("TestsStr", formatter.format(lastTests));
				lastStatistics.put("ErrorsStr", formatter.format(lastErrors));

				cumulativeStatistics.add(statistics);
				lastSampleStatistics.add(lastStatistics);
			}
		}

		StatisticsSet totalSet = model.getTotalCumulativeStatistics();
		Map<String, Object> totalStatistics = new HashMap<String, Object>();

		for (ExpressionView expressionView : views) { // TODO : expressionView == null ?
			totalStatistics.put(expressionView.getDisplayName().replaceAll("\\s+", "_"),
					getRealDoubleValue(expressionView.getExpression().getDoubleValue(totalSet)));
			totalStatistics.put(expressionView.getDisplayName().replaceAll("\\s+", "_"),
					getRealDoubleValue(expressionView.getExpression().getDoubleValue(totalSet)));
		}

		Double tests = (Double) totalStatistics.get("Tests");
		Double errors = (Double) totalStatistics.get("Errors");
		totalStatistics.put("TestsStr", formatter.format(tests));
		totalStatistics.put("ErrorsStr", formatter.format(errors));

		result.put("totalStatistics", totalStatistics);
		result.put("cumulativeStatistics", cumulativeStatistics);
		result.put("lastSampleStatistics", lastSampleStatistics);
		
		result.put("tpsChartData", singleConsole.getTpsValues());

		
        MutablePicoContainer container = (MutablePicoContainer) singleConsole.getConsoleContainer();
        ProcessControl processControl = (ProcessControl) container.getComponent(ProcessControl.class);
        NGrinderConsoleCommunicationService.collectWorkerAndThreadInfo(processControl, result);
		
        result.put("success", true);
		
		return result;
	}

	private static Object getRealDoubleValue(Double doubleValue) {
		if (doubleValue.isInfinite() || doubleValue.isNaN()) {
			return null;
		}
		return doubleValue;
	}

	public List<PerfTest> getAllPerfTest() {
		return perfTestRepository.findAll();
	}

	public ConsoleProperties createConsoleProperties(PerfTest perfTest) {
		ConsoleProperties consoleProperties = ConsolePropertiesFactory.createEmptyConsoleProperties();
		try {
			consoleProperties.setAndSaveDistributionDirectory(new Directory(getUserPerfTestDirectory(perfTest)));
		} catch (Exception e) {
			throw new NGrinderRuntimeException("Error while setting console properties", e);
		}
		return consoleProperties;
	}
	
	public PerfTest updatePerfTestAfterTestFinish(PerfTest perfTest) {
		checkNotNull(perfTest);
		int port = perfTest.getPort();
		Map<String, Object> result = getStatistics(port);
		checkNotNull(result);
		@SuppressWarnings("unchecked")
		Map<String, Object> totalStatistics = (Map<String, Object>) result.get("totalStatistics");

		perfTest.setErrors((int) ((Double) totalStatistics.get("Errors")).doubleValue());
		perfTest.setTps(Double.parseDouble(formatter.format(totalStatistics.get("TPS"))));
		perfTest.setMeanTestTime(Double.parseDouble(formatter.format(totalStatistics.get("Mean_Test_Time_(ms)"))));
		perfTest.setPeakTps(Double.parseDouble(formatter.format(totalStatistics.get("Peak_TPS"))));
		return perfTest;
	}

}
