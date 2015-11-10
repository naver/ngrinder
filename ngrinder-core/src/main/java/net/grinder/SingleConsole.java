// Copyright (C) 2000 - 2012 Philip Aston
// All rights reserved.
//
// This file is part of The Grinder software distribution. Refer to
// the file LICENSE which is part of The Grinder distribution for
// licensing details. The Grinder distribution is available on the
// Internet at http://grinder.sourceforge.net/
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
// FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
// COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
// OF THE POSSIBILITY OF SUCH DAMAGE.
package net.grinder;

import net.grinder.common.GrinderException;
import net.grinder.common.GrinderProperties;
import net.grinder.common.Test;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.common.processidentity.WorkerProcessReport;
import net.grinder.console.ConsoleFoundationEx;
import net.grinder.console.common.Resources;
import net.grinder.console.common.ResourcesImplementation;
import net.grinder.console.communication.ProcessControl;
import net.grinder.console.communication.ProcessControl.Listener;
import net.grinder.console.communication.ProcessControl.ProcessReports;
import net.grinder.console.distribution.AgentCacheState;
import net.grinder.console.distribution.FileDistribution;
import net.grinder.console.distribution.FileDistributionHandler;
import net.grinder.console.model.*;
import net.grinder.statistics.*;
import net.grinder.util.*;
import net.grinder.util.ListenerSupport.Informer;
import net.grinder.util.thread.Condition;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.util.DateUtils;
import org.ngrinder.common.util.ReflectionUtils;
import org.ngrinder.common.util.ThreadUtils;
import org.ngrinder.service.AbstractSingleConsole;
import org.python.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;

import static org.ngrinder.common.util.CollectionUtils.*;
import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

/**
 * Single console for multiple test. This is the customized version of
 * {@link Console} which grinder has.
 *
 * @author Grinder Developers.
 * @author JunHo Yoon (clone Console and modify this for nGrinder)
 * @since 3.0
 */
public class SingleConsole extends AbstractSingleConsole implements Listener, SampleListener {
	private static final String RESOURCE_CONSOLE = "net.grinder.console.common.resources.Console";
	private Thread consoleFoundationThread;
	private ConsoleFoundationEx consoleFoundation;
	public static final Resources RESOURCE = new ResourcesImplementation(RESOURCE_CONSOLE);
	public static final Logger LOGGER = LoggerFactory.getLogger("console");

	private static final String REPORT_CSV = "output.csv";
	private static final String REPORT_DATA = ".data";

	private final Condition eventSyncCondition = new Condition();
	private ProcessReports[] processReports;
	private boolean cancel = false;

	// for displaying tps graph in test running page
	private double tpsValue = 0;
	// for displaying tps graph in test running page
	private double peakTpsForGraph = 0;
	private SampleModel sampleModel;
	private SampleModelViews modelView;
	private long startTime = 0;
	private long momentWhenTpsBeganToHaveVerySmall;
	private long lastMomentWhenErrorsMoreThanHalfOfTotalTPSValue;
	private final ListenerSupport<ConsoleShutdownListener> showdownListner = ListenerHelper.create();
	private final ListenerSupport<SamplingLifeCycleListener> samplingLifeCycleListener = ListenerHelper.create();
	private final ListenerSupport<SamplingLifeCycleFollowUpListener> samplingLifeCycleFollowupListener = ListenerHelper
			.create();
	public static final int MIN_SAMPLING_INTERVAL_TO_ACTIVATE_TPS_PER_TEST = 3000;
	private boolean capture = false;
	private File reportPath;

	private Map<String, Object> statisticData;

	private boolean headerAdded = false;
	private GrinderProperties properties;

	private Map<String, BufferedWriter> fileWriterMap = newHashMap();
	/**
	 * the count of current sampling.
	 */
	private long samplingCount = 0;

	/**
	 * The count of currently running thread.
	 */
	private int runningThread = 0;

	/**
	 * The count of currently r running process.
	 */
	private int runningProcess = 0;

	/**
	 * The count of process which is not finished yet.
	 */
	private int currentNotFinishedProcessCount = 0;

	private static final int TOO_LOW_TPS_TIME = 60000;
	private static final int TOO_MANY_ERROR_TIME = 10000;
	private Map<Test, StatisticsSet> intervalStatisticMapPerTest = Collections
			.synchronizedMap(new LinkedHashMap<Test, StatisticsSet>());
	private Map<Test, StatisticsSet> accumulatedStatisticMapPerTest = Collections
			.synchronizedMap(new LinkedHashMap<Test, StatisticsSet>());

	/**
	 * Constructor to bind all ip and the given port.
	 * <p/>
	 * This is for unit test.
	 *
	 * @param port PORT
	 */
	public SingleConsole(int port) {
		this("", port, ConsoleCommunicationSetting.asDefault(), ConsolePropertiesFactory.createEmptyConsoleProperties());
	}

	/**
	 * Constructor with IP, port, and properties.
	 *
	 * @param ip                IP
	 * @param port              PORT
	 * @param consoleProperties {@link ConsoleProperties} used.
	 */
	public SingleConsole(String ip, int port, ConsoleCommunicationSetting consoleCommunicationSetting,
						 ConsoleProperties consoleProperties) {
		init(ip, port, consoleCommunicationSetting, consoleProperties);
	}

	protected void init(String ip, int port, ConsoleCommunicationSetting consoleCommunicationSetting,
						ConsoleProperties consoleProperties) {
		try {
			if (StringUtils.isNotEmpty(ip)) {
				consoleProperties.setConsoleHost(ip);
			}
			consoleProperties.setConsolePort(port);
			this.consoleFoundation = new ConsoleFoundationEx(RESOURCE, LOGGER, consoleProperties,
					consoleCommunicationSetting, eventSyncCondition);
			modelView = getConsoleComponent(SampleModelViews.class);
			getConsoleComponent(ProcessControl.class).addProcessStatusListener(this);
		} catch (GrinderException e) {
			throw processException("Exception occurred while creating SingleConsole", e);
		}
	}

	/**
	 * Get the assigned console port.
	 *
	 * @return console port
	 */
	public int getConsolePort() {
		return this.getConsoleProperties().getConsolePort();
	}

	/**
	 * Start {@link SingleConsole} and wait until it's ready to get agent
	 * messages.
	 */
	public void start() {
		if (getConsoleFoundation() == null) {
			return; // the console is not a valid console.(NullSingleConsole)
		}
		synchronized (eventSyncCondition) {
			consoleFoundationThread = new Thread(new Runnable() {
				public void run() {
					getConsoleFoundation().run();
				}
			}, "console on port " + getConsolePort());
			consoleFoundationThread.setDaemon(true);
			consoleFoundationThread.start();
			eventSyncCondition.waitNoInterrruptException(5000);
		}
	}

	/**
	 * Shutdown this {@link SingleConsole} instance and wait until the
	 * underlying console logic is stopped.
	 */
	public void shutdown() {
		try {
			synchronized (this) {
				getConsoleFoundation().shutdown();
				if (consoleFoundationThread != null && !consoleFoundationThread.isInterrupted()) {
					consoleFoundationThread.interrupt();
					consoleFoundationThread.join(1000);
				}
				samplingCount = 0;
			}
		} catch (Exception e) {
			throw processException("Exception occurred while shutting down console", e);
		} finally {
			// close all report file
			for (BufferedWriter bw : fileWriterMap.values()) {
				IOUtils.closeQuietly(bw);
			}
			fileWriterMap.clear();
		}
	}

	/**
	 * Get the count of all attached agents.
	 *
	 * @return count of agents
	 */
	public int getAllAttachedAgentsCount() {
		return getConsoleFoundation().getComponent(ProcessControl.class).getNumberOfLiveAgents();
	}

	protected ConsoleFoundationEx getConsoleFoundation() {
		return checkNotNull(consoleFoundation);
	}

	/**
	 * Get all attached agent list on this console.
	 *
	 * @return agent list
	 */
	public List<AgentIdentity> getAllAttachedAgents() {
		final List<AgentIdentity> agentIdentities = newArrayList();
		AllocateLowestNumber agentIdentity = (AllocateLowestNumber) checkNotNull(
				ReflectionUtils.getFieldValue(
						getConsoleFoundation().getComponent(ProcessControl.class),
						"m_agentNumberMap"),
				"m_agentNumberMap on ProcessControlImplementation is not available in this grinder version");
		agentIdentity.forEach(new AllocateLowestNumber.IteratorCallback() {
			public void objectAndNumber(Object object, int number) {
				agentIdentities.add((AgentIdentity) object);
			}
		});
		return agentIdentities;
	}

	/**
	 * Get the console component having the given type.
	 *
	 * @param <T>           componentType component type
	 * @param componentType component type
	 * @return the consoleFoundation
	 */
	public <T> T getConsoleComponent(Class<T> componentType) {
		return getConsoleFoundation().getComponent(componentType);
	}

	/**
	 * Get {@link ConsoleProperties} which is used to configure
	 * {@link SingleConsole}.
	 *
	 * @return {@link ConsoleProperties}
	 */
	public ConsoleProperties getConsoleProperties() {
		return getConsoleComponent(ConsoleProperties.class);
	}

	/**
	 * Start a test with the given {@link GrinderProperties}.
	 *
	 * @param properties {@link GrinderProperties}
	 * @return current time
	 */
	public long startTest(GrinderProperties properties) {
		properties.setInt(GrinderProperties.CONSOLE_PORT, getConsolePort());
		getConsoleComponent(ProcessControl.class).startWorkerProcesses(properties);
		this.properties = properties;
		this.startTime = System.currentTimeMillis();
		return this.startTime;
	}

	/**
	 * Set the file distribution directory.
	 *
	 * @param filePath file path.
	 */
	public void setDistributionDirectory(File filePath) {
		final ConsoleProperties properties = getConsoleComponent(ConsoleProperties.class);
		Directory directory;
		try {
			directory = new Directory(filePath);
			properties.setAndSaveDistributionDirectory(directory);
		} catch (Exception e) {
			LOGGER.error("Error occurred during setting distribution directory to {} : {}", filePath, e.getMessage());
			throw processException(e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.grinder.ISingleConsole2#cancel()
	 */
	@Override
	public void cancel() {
		cancel = true;
	}

	private boolean shouldEnable(FileDistribution fileDistribution) {
		return fileDistribution.getAgentCacheState().getOutOfDate();
	}

	/**
	 * Distribute files in the given filePath to the attached agents.
	 *
	 * @param filePath the distribution files
	 * @param listener listener
	 * @param safe     safe file transition
	 */
	public void distributeFiles(File filePath, ListenerSupport<FileDistributionListener> listener, boolean safe) {
		setDistributionDirectory(filePath);
		distributeFiles(listener, safe);
	}

	public String getConsoleIP() {
		return this.getConsoleProperties().getConsoleHost();
	}

	/**
	 * File distribution event listener.
	 *
	 * @author JunHo Yoon
	 */
	public abstract static class FileDistributionListener {
		/**
		 * Notify the file distribution start event and the returns if the safe
		 * mode is enabled or not.
		 *
		 * @param dir  Distribution dir
		 * @param safe safe file transition
		 * @return true if safe
		 */
		public abstract boolean start(File dir, boolean safe);

		/**
		 * Notify the progress showing that the given file was distributed.
		 *
		 * @param fileName distributed file name
		 */
		public abstract void distributed(String fileName);
	}

	/**
	 * Distribute files on agents.
	 *
	 * @param listener listener
	 * @param safe     safe mode
	 */
	public void distributeFiles(ListenerSupport<FileDistributionListener> listener, final boolean safe) {
		final FileDistribution fileDistribution = getConsoleComponent(FileDistribution.class);
		final AgentCacheState agentCacheState = fileDistribution.getAgentCacheState();
		final Condition cacheStateCondition = new Condition();
		agentCacheState.addListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent ignored) {
				synchronized (cacheStateCondition) {
					cacheStateCondition.notifyAll();
				}
			}
		});
		final MutableBoolean safeDist = new MutableBoolean(safe);
		ConsoleProperties consoleComponent = getConsoleComponent(ConsoleProperties.class);
		final File file = consoleComponent.getDistributionDirectory().getFile();
		if (listener != null) {
			listener.apply(new Informer<FileDistributionListener>() {
				@Override
				public void inform(FileDistributionListener listener) {
					safeDist.setValue(listener.start(file, safe));
				}
			});
		}
		final FileDistributionHandler distributionHandler = fileDistribution.getHandler();
		// When cancel is called.. stop processing.
		int fileCount = 0;
		while (!cancel) {
			try {
				final FileDistributionHandler.Result result = distributionHandler.sendNextFile();
				fileCount++;
				if (result == null) {
					break;
				}
				if (listener != null) {
					listener.apply(new Informer<FileDistributionListener>() {
						@Override
						public void inform(FileDistributionListener listener) {
							listener.distributed(result.getFileName());
						}
					});
				}

				if (safeDist.isTrue()) {
					// The cache status is updated asynchronously by agent
					// reports. If the listener is registered, this waits for up
					// to five seconds for
					// all agents to indicate that they are up to date.
					checkSafetyWithCacheState(fileDistribution, cacheStateCondition, 1);
				}
			} catch (FileContents.FileContentsException e) {
				throw processException("Error while distribute files for " + getConsolePort());
			}
		}
		if (safeDist.isFalse()) {
			ThreadUtils.sleep(1000);
			checkSafetyWithCacheState(fileDistribution, cacheStateCondition, fileCount);
		}
	}

	private void checkSafetyWithCacheState(final FileDistribution fileDistribution,
										   final Condition cacheStateCondition, int fileCount) {
		// noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized (cacheStateCondition) {
			for (int i = 0; i < (10 * fileCount) && shouldEnable(fileDistribution); ++i) {
				cacheStateCondition.waitNoInterrruptException(500);
			}
		}
	}

	/**
	 * Wait until the given size of agents are all connected. It wait until 10
	 * sec.
	 *
	 * @param size size of agent.
	 */
	public void waitUntilAgentConnected(int size) {
		int trial = 1;
		while (trial++ < 10) {
			// when agent finished one test, processReports will be updated as
			// null
			if (processReports == null || this.processReports.length != size) {
				synchronized (eventSyncCondition) {
					eventSyncCondition.waitNoInterrruptException(1000);
				}
			} else if (isCanceled()) { // TODO Is it required?
				return;
			} else {
				return;
			}
		}
		throw processException("Connection is not completed until 10 sec");
	}

	/**
	 * Wait until the count of running threads becomes 0. If the elapsed time
	 * is over 10 seconds, an {@link NGrinderRuntimeException} is thrown.
	 */
	public void waitUntilAllAgentDisconnected() {
		int trial = 1;
		while (trial++ < 40) {
			if (this.runningThread != 0) {
				synchronized (eventSyncCondition) {
					eventSyncCondition.waitNoInterrruptException(500);
				}
				// Every 10 times send the signal again.
				if (trial % 10 == 0) {
					sendStopMessageToAgents();
				}
			} else {
				return;
			}
		}
		throw processException("Connection is not completed for 20 sec");
	}

	/**
	 * Check all test is finished. To be safe, this counts the running thread
	 * and unfinished worker processes. If one of them is 0, It assumes the test
	 * is finished.
	 *
	 * @return true if finished
	 */
	public boolean isAllTestFinished() {
		synchronized (this) {
			// Mostly running thread count is ok to determine it's finished.
			if (this.runningThread == 0) {
				return true;
				// However sometimes runningThread is over 0 but all process is
				// marked as
				// FINISHED.. It can be treated as finished status as well.
			} else if (this.currentNotFinishedProcessCount == 0) {
				return true;
			}
			return false;
		}
	}

	/**
	 * Set the current TPS value. and it updates the max peak TPS as well.
	 *
	 * @param newValue TPS value
	 */
	public void setTpsValue(double newValue) {
		peakTpsForGraph = Math.max(peakTpsForGraph, newValue);
		tpsValue = newValue;
	}

	public double getTpsValues() {
		return tpsValue;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.grinder.ISingleConsole2#getCurrentRunningTime()
	 */
	@Override
	public long getCurrentRunningTime() {
		return System.currentTimeMillis() - startTime;
	}

	/*
     * (non-Javadoc)
	 * 
	 * @see net.grinder.ISingleConsole2#getStatisticsIndexMap()
	 */
	public StatisticsIndexMap getStatisticsIndexMap() {
		return StatisticsServicesImplementation.getInstance().getStatisticsIndexMap();
	}

	private ExpressionView[] expressionViews = null;

	/**
	 * Get all expression views.
	 *
	 * @return {@link ExpressionView} array
	 * @since 3.0.2
	 */
	public ExpressionView[] getExpressionView() {
		if (this.expressionViews == null) {
			this.expressionViews = modelView.getCumulativeStatisticsView().getExpressionViews();
		}
		return this.expressionViews;
	}

	private Set<Entry<String, StatisticExpression>> statisticExpressionMap;

	/**
	 * Get all expression entry set (display name and
	 * {@link StatisticExpression} pair).
	 *
	 * @return entry set of display name and {@link StatisticExpression} pair
	 * @since 3.1.2
	 */
	public Set<Entry<String, StatisticExpression>> getExpressionEntrySet() {
		if (this.statisticExpressionMap == null) {
			Map<String, StatisticExpression> expressionMap = newLinkedHashMap();
			for (ExpressionView each : getExpressionView()) {
				expressionMap.put(each.getDisplayName().replaceAll("\\s+", "_"), each.getExpression());
			}
			this.statisticExpressionMap = expressionMap.entrySet();
		}
		return this.statisticExpressionMap;
	}

	/**
	 * The last timestamp when the sampling is done.
	 */
	private long lastSamplingPeriod = 0;

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * net.grinder.console.model.SampleListener#update(net.grinder.statistics
	 * .StatisticsSet, net.grinder.statistics.StatisticsSet)
	 */
	@Override
	public void update(final StatisticsSet intervalStatistics, final StatisticsSet cumulativeStatistics) {
		try {
			if (!capture) {
				return;
			}
			samplingCount++;
			long currentPeriod = cumulativeStatistics.getValue(getSampleModel().getPeriodIndex());
			setTpsValue(sampleModel.getTPSExpression().getDoubleValue(intervalStatistics));
			checkTooLowTps(getTpsValues());
			updateStatistics(intervalStatistics, cumulativeStatistics);

			writeIntervalCsvData(intervalStatistics);
			int interval = getSampleModel().getSampleInterval();
			long gap = 1;
			if (samplingCount == 1) {
				lastSamplingPeriod = currentPeriod;
			} else {
				lastSamplingPeriod = lastSamplingPeriod + interval;
				gap = ((currentPeriod - lastSamplingPeriod) / interval);
			}
			// Adjust sampling delay.. run write data multiple times... when it
			// takes longer than 1 sec.

			samplingLifeCycleListener.apply(new Informer<SamplingLifeCycleListener>() {
				@Override
				public void inform(SamplingLifeCycleListener listener) {
					listener.onSampling(getReportPath(), intervalStatistics, cumulativeStatistics);
				}
			});
			for (long index = 0, repeatCounts = gap + 1; index < repeatCounts; index++) {
				final boolean lastCall = (samplingCount == 1 && index == 0) || (samplingCount != 1 && index == gap);
				writeIntervalSummaryData(intervalStatistics, lastCall);
				if (interval >= (MIN_SAMPLING_INTERVAL_TO_ACTIVATE_TPS_PER_TEST)) {
					writeIntervalSummaryDataPerTest(intervalStatisticMapPerTest, lastCall);
				}
				samplingLifeCycleFollowupListener.apply(new Informer<SamplingLifeCycleFollowUpListener>() {
					@Override
					public void inform(SamplingLifeCycleFollowUpListener listener) {
						listener.onSampling(getReportPath(), intervalStatistics, cumulativeStatistics, lastCall);
					}
				});
			}
			checkTooManyError(cumulativeStatistics);
			lastSamplingPeriod = lastSamplingPeriod + (interval * gap);
		} catch (RuntimeException e) {
			LOGGER.error("Error occurred while updating the statistics : {}", e.getMessage());
			LOGGER.debug("Details : ", e);
			throw e;
		}
	}

	/**
	 * Write the interval summary data per each test. This is activated only
	 * when there are more than 1 registed test.
	 *
	 * @param intervalStatisticMapPerTest statistics map
	 * @param lastCall                    true if it's the last call of consequent call in a single
	 *                                    sampling
	 */
	private void writeIntervalSummaryDataPerTest(Map<Test, StatisticsSet> intervalStatisticMapPerTest, //
												 boolean lastCall) {
		if (intervalStatisticMapPerTest.size() > 1) {
			for (Entry<String, StatisticExpression> each : getExpressionEntrySet()) {
				if (INTERESTING_PER_TEST_STATISTICS.contains(each.getKey())) {
					for (Entry<Test, StatisticsSet> entry : intervalStatisticMapPerTest.entrySet()) {
						if (lastCall) {
							StatisticsSet value = entry.getValue();
							writeReportData(each.getKey() + "-" + entry.getKey().getNumber() + "_"
									+ entry.getKey().getDescription().replaceAll("\\s+", "_") + REPORT_DATA,
									formatValue(getRealDoubleValue(each.getValue().getDoubleValue(value))));
						} else {
							writeReportData(each.getKey() + "-" + entry.getKey().getNumber() + "_"
									+ entry.getKey().getDescription().replaceAll("\\s+", "_") + REPORT_DATA, "null");
						}

					}
				}
			}
		}
	}

	/**
	 * Write the total test interval statistic data into file.
	 *
	 * @param intervalStatistics interval statistics
	 * @param firstCall          true if it's the last call of consecutive calls in a single
	 *                           sampling
	 */
	public void writeIntervalSummaryData(StatisticsSet intervalStatistics, boolean firstCall) {
		for (Entry<String, StatisticExpression> each : getExpressionEntrySet()) {
			if (firstCall) {
				double doubleValue = each.getValue().getDoubleValue(intervalStatistics);
				writeReportData(each.getKey() + REPORT_DATA, formatValue(getRealDoubleValue(doubleValue)));
			} else {
				writeReportData(each.getKey() + REPORT_DATA, "null");
			}
		}
		writeReportData("Vuser" + REPORT_DATA, formatValue(runningThread));
	}

	/**
	 * Write the each interval statistic data as the form of CSV.
	 *
	 * @param intervalStatistics interval statistics
	 */
	public void writeIntervalCsvData(StatisticsSet intervalStatistics) {
		// add headers into the csv file.
		if (!headerAdded) {
			StringBuilder csvHeader = new StringBuilder();
			csvHeader.append("DateTime").append(",").append("vuser");

			// Get the key list from lastStatistic map, use this list to keep
			// the write order
			for (Entry<String, StatisticExpression> each : getExpressionEntrySet()) {
				if (!each.getKey().equals("Peak_TPS")) {
					csvHeader.append(",").append(each.getKey());
				}
			}
			if (intervalStatisticMapPerTest.size() != 1) {
				for (int i = 1; i <= intervalStatisticMapPerTest.size(); i++) {
					csvHeader.append(",").append("Description");
					// get the key list from lastStatistic map, use list to keep
					// the order
					for (Entry<String, StatisticExpression> each : getExpressionEntrySet()) {
						if (!each.getKey().equals("Peak_TPS")) {
							csvHeader.append(",").append(each.getKey()).append("-").append(i);
						}
					}

				}
			}
			writeCSVDataLine(csvHeader.toString());
			headerAdded = true;
		}

		StringBuilder csvLine = new StringBuilder();
		csvLine.append(DateUtils.dateToString(new Date())).append(",").append(runningThread);
		for (Entry<String, StatisticExpression> each : getExpressionEntrySet()) {
			if (!each.getKey().equals("Peak_TPS")) {
				double doubleValue = each.getValue().getDoubleValue(intervalStatistics);
				csvLine.append(",").append(formatValue(getRealDoubleValue(doubleValue)));
			}
		}

		if (intervalStatisticMapPerTest.size() != 1) {
			for (Entry<Test, StatisticsSet> eachPair : intervalStatisticMapPerTest.entrySet()) {
				String description = eachPair.getKey().getDescription();
				csvLine.append(",").append(description);
				for (Entry<String, StatisticExpression> each : getExpressionEntrySet()) {
					if (!each.getKey().equals("Peak_TPS")) {
						csvLine.append(",").append(
								formatValue(getRealDoubleValue(each.getValue().getDoubleValue(eachPair.getValue()))));
					}
				}
			}
		}

		writeCSVDataLine(csvLine.toString());
	}

	/**
	 * Check if the TPS is too low. the TPS is lower than 0.001 for 1 minutes,
	 * It emits a shutdown event to the {@link ConsoleShutdownListener}
	 *
	 * @param tps current TPS
	 */
	private void checkTooLowTps(double tps) {
		// If the tps is too low, which means the agents or scripts went wrong.
		if (tps < 0.001) {
			if (momentWhenTpsBeganToHaveVerySmall == 0) {
				momentWhenTpsBeganToHaveVerySmall = System.currentTimeMillis();
			} else if (new Date().getTime() - momentWhenTpsBeganToHaveVerySmall >= TOO_LOW_TPS_TIME) {
				LOGGER.warn("Stop the test because its tps is less than 0.001 for more than {} minitue.",
						TOO_LOW_TPS_TIME / 60000);
				getListeners().apply(new Informer<ConsoleShutdownListener>() {
					public void inform(ConsoleShutdownListener listener) {
						listener.readyToStop(StopReason.TOO_LOW_TPS);
					}
				});
				momentWhenTpsBeganToHaveVerySmall = 0;

			}
		} else {
			momentWhenTpsBeganToHaveVerySmall = 0;
		}
	}

	/**
	 * Check if too many error has been occurred. If the half of total
	 * transaction is error for the last 10 secs. It notifies the
	 * {@link ConsoleShutdownListener}
	 *
	 * @param cumulativeStatistics accumulated Statistics
	 */
	private void checkTooManyError(StatisticsSet cumulativeStatistics) {
		StatisticsIndexMap statisticsIndexMap = getStatisticsIndexMap();
		long testSum = cumulativeStatistics.getCount(statisticsIndexMap.getLongSampleIndex("timedTests"));
		long errors = cumulativeStatistics.getValue(statisticsIndexMap.getLongIndex("errors"));
		if (((double) (testSum + errors)) / 2 < errors) {
			if (lastMomentWhenErrorsMoreThanHalfOfTotalTPSValue == 0) {
				lastMomentWhenErrorsMoreThanHalfOfTotalTPSValue = System.currentTimeMillis();
			} else if (isOverLowTpsThreshold()) {
				LOGGER.warn("Stop the test because the count of test error is more than"
						+ " half of total tps for last {} seconds.", TOO_MANY_ERROR_TIME / 1000);
				getListeners().apply(new Informer<ConsoleShutdownListener>() {
					public void inform(ConsoleShutdownListener listener) {
						listener.readyToStop(StopReason.TOO_MANY_ERRORS);
					}
				});
				lastMomentWhenErrorsMoreThanHalfOfTotalTPSValue = 0;
			}
		}
	}

	private boolean isOverLowTpsThreshold() {
		return (System.currentTimeMillis() - lastMomentWhenErrorsMoreThanHalfOfTotalTPSValue) >= TOO_MANY_ERROR_TIME;
	}

	public static final Set<String> INTERESTING_PER_TEST_STATISTICS = Sets.newHashSet("Errors", "TPS",
			"Mean_time_to_first_byte", "Mean_Test_Time_(ms)", "User_defined");

	public static final Set<String> INTERESTING_STATISTICS = Sets.newHashSet("Tests", "Errors", "TPS",
			"Response_bytes_per_second", "Mean_time_to_first_byte", "Peak_TPS", "Mean_Test_Time_(ms)", "User_defined");

	/**
	 * Build up statistics for current sampling.
	 *
	 * @param accumulatedStatistics intervalStatistics
	 * @param intervalStatistics    accumulatedStatistics
	 */
	protected void updateStatistics(StatisticsSet intervalStatistics, StatisticsSet accumulatedStatistics) {
		Map<String, Object> result = newHashMap();
		result.put("testTime", getCurrentRunningTime() / 1000);
		List<Map<String, Object>> cumulativeStatistics = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> lastSampleStatistics = new ArrayList<Map<String, Object>>();
		for (Test test : accumulatedStatisticMapPerTest.keySet()) {
			Map<String, Object> accumulatedStatisticMap = newHashMap();
			Map<String, Object> intervalStatisticsMap = newHashMap();
			StatisticsSet accumulatedSet = this.accumulatedStatisticMapPerTest.get(test);
			StatisticsSet intervalSet = this.intervalStatisticMapPerTest.get(test);

			accumulatedStatisticMap.put("testNumber", test.getNumber());
			accumulatedStatisticMap.put("testDescription", test.getDescription());
			intervalStatisticsMap.put("testNumber", test.getNumber());
			intervalStatisticsMap.put("testDescription", test.getDescription());
			// When only 1 test is running, it's better to use the parametrized
			// snapshot.
			for (Entry<String, StatisticExpression> each : getExpressionEntrySet()) {
				if (INTERESTING_STATISTICS.contains(each.getKey())) {
					accumulatedStatisticMap.put(each.getKey(),
							getRealDoubleValue(each.getValue().getDoubleValue(accumulatedSet)));
					intervalStatisticsMap.put(each.getKey(),
							getRealDoubleValue(each.getValue().getDoubleValue(intervalSet)));
				}
			}
			cumulativeStatistics.add(accumulatedStatisticMap);
			lastSampleStatistics.add(intervalStatisticsMap);
		}

		Map<String, Object> totalStatistics = newHashMap();

		for (Entry<String, StatisticExpression> each : getExpressionEntrySet()) {
			if (INTERESTING_STATISTICS.contains(each.getKey())) {
				totalStatistics.put(each.getKey(),
						getRealDoubleValue(each.getValue().getDoubleValue(accumulatedStatistics)));
			}
		}

		result.put("totalStatistics", totalStatistics);
		result.put("cumulativeStatistics", cumulativeStatistics);
		result.put("lastSampleStatistics", lastSampleStatistics);
		result.put("tpsChartData", getTpsValues());
		result.put("peakTpsForGraph", this.peakTpsForGraph);
		synchronized (this) {
			result.put(GrinderConstants.P_PROCESS, this.runningProcess);
			result.put(GrinderConstants.P_THREAD, this.runningThread);
			result.put("success", !isAllTestFinished());
		}
		// Finally overwrite.. current one.
		this.statisticData = result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.grinder.ISingleConsole2#getCurrentExecutionCount()
	 */
	@Override
	public long getCurrentExecutionCount() {
		Map<?, ?> totalStatistics = (Map<?, ?>) getStatisticsData().get("totalStatistics");
		Double testCount = MapUtils.getDoubleValue(totalStatistics, "Tests", 0D);
		Double errorCount = MapUtils.getDoubleValue(totalStatistics, "Errors", 0D);
		return testCount.longValue() + errorCount.longValue();
	}

	private static Object getRealDoubleValue(Double doubleValue) {
		if (doubleValue == null) {
			return (double) 0;
		}
		return (doubleValue.isInfinite() || doubleValue.isNaN()) ? (double) 0 : doubleValue;
	}

	/**
	 * Listener interface to detect the sampling start and end moment.
	 *
	 * @author JunHo Yoon
	 * @since 3.0
	 */
	public interface SamplingLifeCycleListener {
		/**
		 * Called when the sampling is started.
		 */
		void onSamplingStarted();

		/**
		 * Called whenever the sampling is performed.
		 *
		 * @param file                 report path
		 * @param intervalStatistics   interval statistics snapshot
		 * @param cumulativeStatistics cumulative statistics snapshot
		 * @since 3.0.2
		 */
		void onSampling(File file, StatisticsSet intervalStatistics, StatisticsSet cumulativeStatistics);

		/**
		 * Called when the sampling is ended.
		 */
		void onSamplingEnded();
	}

	/**
	 * Listener interface to detect sampling start and end moment and each
	 * sampling. This is used when the consecutive sampling should be counted.
	 *
	 * @author JunHo Yoon
	 * @since 3.1.3
	 */
	public interface SamplingLifeCycleFollowUpListener {
		/**
		 * Called when the sampling is started.
		 */
		void onSamplingStarted();

		/**
		 * Called whenever the sampling is performed. The last call in the each
		 * consecutive sampling will pass the true in the last call parameter.
		 *
		 * @param file                 report path
		 * @param intervalStatistics   interval statistics snapshot
		 * @param cumulativeStatistics cumulative statistics snapshot
		 * @param lastCall             true if it's the last call of the consecutive calls in a
		 *                             single sampling
		 * @since 3.0.3
		 */
		void onSampling(File file, StatisticsSet intervalStatistics, StatisticsSet cumulativeStatistics,
						boolean lastCall);

		/**
		 * Called when the sampling is ended.
		 */
		void onSamplingEnded();
	}

	/**
	 * Listener interface to detect the console shutdown condition.
	 *
	 * @author JunHo Yoon
	 */
	public interface ConsoleShutdownListener {
		/**
		 * Called when the console should be shutdown.
		 *
		 * @param stopReason the reason of shutdown
		 */
		void readyToStop(StopReason stopReason);
	}

	/**
	 * Get the list of the registered {@link ConsoleShutdownListener}.
	 *
	 * @return the list of the registered {@link ConsoleShutdownListener}.
	 * @see ConsoleShutdownListener
	 */
	public ListenerSupport<ConsoleShutdownListener> getListeners() {
		return this.showdownListner;
	}

	/**
	 * Add the given {@link ConsoleShutdownListener} to get notified when
	 * console is shutdown
	 *
	 * @param listener listener to be registered.
	 */
	public void addListener(ConsoleShutdownListener listener) {
		showdownListner.add(listener);
	}

	/**
	 * Add the given {@link SamplingLifeCycleListener} to get notified when
	 * sampling is started and ended.
	 *
	 * @param listener listener to be registered.
	 */
	public void addSamplingLifeCyleListener(SamplingLifeCycleListener listener) {
		samplingLifeCycleListener.add(listener);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * net.grinder.console.communication.ProcessControl.Listener#update(net.
	 * grinder.console. communication.ProcessControl.ProcessReports[])
	 */
	@Override
	public void update(ProcessReports[] processReports) {
		synchronized (eventSyncCondition) {
			checkExecutionErrors(processReports);
			this.processReports = processReports;
			// The reason I passed processReport as parameter here is to prevent
			// the synchronization problem.
			updateCurrentProcessAndThread(processReports);
			eventSyncCondition.notifyAll();
		}
	}

	private void checkExecutionErrors(ProcessReports[] processReports) {
		if (samplingCount == 0 && ArrayUtils.isNotEmpty(this.processReports) && ArrayUtils.isEmpty(processReports)) {
			getListeners().apply(new Informer<ConsoleShutdownListener>() {
				public void inform(ConsoleShutdownListener listener) {
					listener.readyToStop(StopReason.SCRIPT_ERROR);
				}
			});
		}
	}

	/**
	 * Update the count of current processes and threads.
	 *
	 * @param processReports ProcessReports array.
	 */
	private void updateCurrentProcessAndThread(ProcessReports[] processReports) {
		int notFinishedWorkerCount = 0;
		int processCount = 0;
		int threadCount = 0;
		// Per agents
		for (ProcessReports agentReport : processReports) {
			// Per process
			for (WorkerProcessReport processReport : agentReport.getWorkerProcessReports()) {
				// There might be the processes which is not finished but no
				// running thread in it.
				if (processReport.getState() != 3) {
					notFinishedWorkerCount++;
				}
				processCount++;
				threadCount += processReport.getNumberOfRunningThreads();
			}
		}

		synchronized (this) {
			this.runningProcess = processCount;
			this.runningThread = threadCount;
			this.currentNotFinishedProcessCount = notFinishedWorkerCount;
		}
	}

	private void writeReportData(String name, String value) {
		try {
			BufferedWriter bw = fileWriterMap.get(name);
			if (bw == null) {
				bw = new BufferedWriter(new FileWriter(new File(this.reportPath, name), true));
				fileWriterMap.put(name, bw);
			}
			bw.write(value);
			bw.newLine();
			bw.flush();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw processException(e);
		}
	}

	private void writeCSVDataLine(String line) {
		writeReportData(REPORT_CSV, line);
	}

	private String formatValue(Object val) {
		if (val instanceof Double) {
			DecimalFormat formatter = new DecimalFormat("###.###");
			formatter.setGroupingUsed(false);
			return formatter.format(val);
		} else if (String.valueOf(val).equals("null")) {
			// if target server is too slow, there is no response in this
			// second, then the
			// statistic data
			// like mean time will be null.
			// currently, we set these kind of value as 0.
			return "0";
		}
		return String.valueOf(val);
	}

	/**
	 * Get the statistics data. This method returns {@link Map} whose key is
	 * string and it's mapped to the various statistics. Please refer
	 * {@link #update(net.grinder.console.communication.ProcessControl.ProcessReports[])} ()}
	 *
	 * @return map which contains the statistics data
	 */
	public Map<String, Object> getStatisticsData() {
		return this.statisticData != null ? this.statisticData : getNullStatisticsData();
	}

	protected Map<String, Object> getNullStatisticsData() {
		Map<String, Object> result = new HashMap<String, Object>(1);
		result.put("test_time", getCurrentRunningTime() / 1000);
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.grinder.ISingleConsole2#getReportPath()
	 */
	@Override
	public File getReportPath() {
		return reportPath;
	}

	/**
	 * Set the report path.
	 *
	 * @param reportPath path in which report will be stored.
	 */
	public void setReportPath(File reportPath) {
		if (checkNotNull(reportPath, "the report folder should not be empty!").mkdirs()) {
			LOGGER.debug("the report folder is created");
		}

		this.reportPath = reportPath;
	}

	/**
	 * Send the stop message to the attached agents to shutdown.
	 */
	public void sendStopMessageToAgents() {
		getConsoleComponent(ProcessControl.class).stopAgentAndWorkerProcesses();
	}

	/**
	 * Start sampling with sampling ignore count.
	 */
	public void startSampling() {
		this.sampleModel = getConsoleComponent(SampleModelImplementationEx.class);
		this.sampleModel.addTotalSampleListener(this);
		this.sampleModel.addModelListener(new SampleModel.Listener() {
			@Override
			public void stateChanged() {
				capture = SingleConsole.this.sampleModel.getState().isCapturing();
			}

			@Override
			public void resetTests() {
				intervalStatisticMapPerTest.clear();
				accumulatedStatisticMapPerTest.clear();
			}

			@Override
			public void newTests(Set<Test> newTests, ModelTestIndex modelTestIndex) {
				for (final Test each : newTests) {
					SingleConsole.this.sampleModel.addSampleListener(each, new SampleListener() {
						@Override
						public void update(StatisticsSet intervalStatistics, StatisticsSet cumulativeStatistics) {
							intervalStatisticMapPerTest.put(each, intervalStatistics.snapshot());
							accumulatedStatisticMapPerTest.put(each, cumulativeStatistics.snapshot());
						}
					});
				}
			}

			@Override
			public void newSample() {
			}
		});
		informTestSamplingStart();
		this.sampleModel.start();
		LOGGER.info("Sampling is started");
	}

	/**
	 * Stop sampling.
	 */
	public void unregisterSampling() {
		this.currentNotFinishedProcessCount = 0;
		if (sampleModel != null) {
			this.sampleModel.reset();
			this.sampleModel.stop();
		}
		LOGGER.info("Sampling is stopped");
		informTestSamplingEnd();
	}

	private void informTestSamplingStart() {
		samplingLifeCycleListener.apply(new Informer<SamplingLifeCycleListener>() {
			@Override
			public void inform(SamplingLifeCycleListener listener) {
				try {
					listener.onSamplingStarted();
				} catch (Exception e) {
					LOGGER.error("Error occurred while running sampling start listener", e);
				}
			}
		});
		samplingLifeCycleFollowupListener.apply(new Informer<SamplingLifeCycleFollowUpListener>() {
			@Override
			public void inform(SamplingLifeCycleFollowUpListener listener) {
				try {
					listener.onSamplingStarted();
				} catch (Exception e) {
					LOGGER.error("Error occurred while running sampling start listener", e);
				}
			}
		});
	}

	private void informTestSamplingEnd() {
		samplingLifeCycleListener.apply(new Informer<SamplingLifeCycleListener>() {
			@Override
			public void inform(SamplingLifeCycleListener listener) {
				try {
					listener.onSamplingEnded();
				} catch (Exception e) {
					LOGGER.error("Error occurred while running sampling end listener", e);
				}
			}
		});
		samplingLifeCycleFollowupListener.apply(new Informer<SamplingLifeCycleFollowUpListener>() {
			@Override
			public void inform(SamplingLifeCycleFollowUpListener listener) {
				try {
					listener.onSamplingEnded();
				} catch (Exception e) {
					LOGGER.error("Error occurred while running sampling end listener", e);
				}
			}
		});
	}

	/**
	 * Get the associated grinder properties to run this console.
	 *
	 * @return properties. null if the test is not started.
	 */
	public GrinderProperties getGrinderProperties() {
		return properties;
	}

	/**
	 * Check if the current test contains too many errors.
	 *
	 * @return true if error is over 20%
	 */
	public boolean hasTooManyError() {
		long currentTestsCount = getCurrentExecutionCount();
		double errors = MapUtils.getDoubleValue((Map<?, ?>) getStatisticsData().get("totalStatistics"), "Errors", 0D);
		return currentTestsCount != 0 && (errors / currentTestsCount) > 0.2;
	}

	/**
	 * Check if the test is performed at least once.
	 *
	 * @return true if performed.
	 * @since 3.1.1
	 */
	public boolean hasNoPerformedTest() {
		return (getCurrentExecutionCount() == 0);
	}

	/**
	 * Check if this {@link SingleConsole} is canceled.
	 *
	 * @return true if yes.
	 */
	public boolean isCanceled() {
		return cancel;
	}

	/**
	 * Check if the current test running time is over given duration.
	 *
	 * @param duration duration in millisecond
	 * @return true if it's over.
	 */
	public boolean isCurrentRunningTimeOverDuration(long duration) {
		return getCurrentRunningTime() > (duration);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.grinder.ISingleConsole2#getPeakTpsForGraph()
	 */
	@Override
	public double getPeakTpsForGraph() {
		return peakTpsForGraph;
	}

	public SampleModelImplementationEx getSampleModel() {
		return (SampleModelImplementationEx) sampleModel;
	}

	/**
	 * Only for unit test.
	 *
	 * @param sampleModel sample model
	 */
	public void setSampleModel(SampleModel sampleModel) {
		this.sampleModel = sampleModel;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.grinder.ISingleConsole2#getRunningThread()
	 */
	@Override
	public int getRunningThread() {
		return runningThread;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.grinder.ISingleConsole2#getRunningProcess()
	 */
	@Override
	public int getRunningProcess() {
		return runningProcess;
	}

	/**
	 * Add the {@link SamplingLifeCycleFollowUpListener}.
	 *
	 * @param listener listener to be registered
	 * @since 3.1.3
	 */
	@SuppressWarnings("UnusedDeclaration")
	public void addSamplingLifeCycleFollowUpCycleListener(SamplingLifeCycleFollowUpListener listener) {
		samplingLifeCycleFollowupListener.add(listener);
	}
}
