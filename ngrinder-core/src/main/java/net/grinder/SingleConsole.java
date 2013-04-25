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
package net.grinder;

import static org.ngrinder.common.util.CollectionUtils.newArrayList;
import static org.ngrinder.common.util.CollectionUtils.newHashMap;
import static org.ngrinder.common.util.CollectionUtils.newLinkedHashMap;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import net.grinder.console.communication.ProcessControlImplementation;
import net.grinder.console.distribution.AgentCacheState;
import net.grinder.console.distribution.FileDistribution;
import net.grinder.console.distribution.FileDistributionHandler;
import net.grinder.console.model.ConsoleProperties;
import net.grinder.console.model.ModelTestIndex;
import net.grinder.console.model.SampleListener;
import net.grinder.console.model.SampleModel;
import net.grinder.console.model.SampleModelImplementationEx;
import net.grinder.console.model.SampleModelViews;
import net.grinder.statistics.ExpressionView;
import net.grinder.statistics.StatisticExpression;
import net.grinder.statistics.StatisticsIndexMap;
import net.grinder.statistics.StatisticsServicesImplementation;
import net.grinder.statistics.StatisticsSet;
import net.grinder.util.AllocateLowestNumber;
import net.grinder.util.ConsolePropertiesFactory;
import net.grinder.util.Directory;
import net.grinder.util.FileContents;
import net.grinder.util.ListenerHelper;
import net.grinder.util.ListenerSupport;
import net.grinder.util.ListenerSupport.Informer;
import net.grinder.util.NetworkUtil;
import net.grinder.util.thread.Condition;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.util.DateUtil;
import org.ngrinder.common.util.ReflectionUtil;
import org.ngrinder.common.util.ThreadUtil;
import org.ngrinder.service.ISingleConsole;
import org.python.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Single console for multiple test. This is the customized version of {@link Console} which grinder
 * has.
 * 
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public class SingleConsole implements Listener, SampleListener, ISingleConsole {
	private static final String REOSURCE_CONSOLE = "net.grinder.console.common.resources.Console";
	private Thread consoleFoundationThread;
	private ConsoleFoundationEx consoleFoundation;
	public static final Resources RESOURCE = new ResourcesImplementation(REOSURCE_CONSOLE);
	public static final Logger LOGGER = LoggerFactory.getLogger(SingleConsole.class);

	private static final String REPORT_CSV = "output.csv";
	private static final String REPORT_DATA = ".data";

	private Condition eventSyncCondition = new Condition();
	private ProcessReports[] processReports;
	private boolean cancel = false;

	// for displaying tps graph in running page
	private double tpsValue = 0;
	// for displaying tps graph in running page
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
	// private NumberFormat simpleFormatter = new DecimalFormat("###");

	private Map<String, Object> statisticData;

	private boolean headerAdded = false;

	private Map<String, BufferedWriter> fileWriterMap = newHashMap();
	/** Current count of sampling. */
	private long samplingCount = 0;

	/**
	 * Currently running thread.
	 */
	private int runningThread = 0;

	/**
	 * Currently running process.
	 */
	private int runningProcess = 0;

	/**
	 * Currently not finished process count.
	 */
	private int currentNotFinishedProcessCount = 0;

	private static final int TOO_LOW_TPS_TIME = 60000;
	private static final int TOO_MANY_ERROR_TIME = 10000;
	private Map<Test, StatisticsSet> intervalStatisticMapPerTest = Collections
					.synchronizedMap(new LinkedHashMap<Test, StatisticsSet>());
	private Map<Test, StatisticsSet> accumulatedStatisticMapPerTest = Collections
					.synchronizedMap(new LinkedHashMap<Test, StatisticsSet>());

	/**
	 * Constructor with console ip and port.
	 * 
	 * @param ip
	 *            IP
	 * @param port
	 *            PORT
	 */
	public SingleConsole(String ip, int port) {
		this(ip, port, ConsolePropertiesFactory.createEmptyConsoleProperties());
	}

	/**
	 * Constructor with console port and properties.
	 * 
	 * @param port
	 *            PORT
	 * @param consoleProperties
	 *            {@link ConsoleProperties} used.
	 */
	public SingleConsole(int port, ConsoleProperties consoleProperties) {
		this("", port, consoleProperties);
	}

	/**
	 * Constructor with IP, port, and properties.
	 * 
	 * @param ip
	 *            IP
	 * @param port
	 *            PORT
	 * @param consoleProperties
	 *            {@link ConsoleProperties} used.
	 */
	public SingleConsole(String ip, int port, ConsoleProperties consoleProperties) {
		// if port is 0, it is Null singleConsole.
		if (port == 0) {
			return;
		}
		try {
			if (StringUtils.isNotEmpty(ip)) {
				consoleProperties.setConsoleHost(ip);
			}
			consoleProperties.setConsolePort(port);
			this.consoleFoundation = new ConsoleFoundationEx(RESOURCE, LOGGER, consoleProperties, eventSyncCondition);

			modelView = getConsoleComponent(SampleModelViews.class);
			getConsoleComponent(ProcessControl.class).addProcessStatusListener(this);

		} catch (GrinderException e) {
			throw new NGrinderRuntimeException("Exception occurs while creating SingleConsole", e);

		}
	}

	/**
	 * Simple constructor only setting port. It automatically binds all ip addresses.
	 * 
	 * @param port
	 *            PORT number
	 */
	public SingleConsole(int port) {
		this("", port);
	}

	/**
	 * Return the assigned console port.
	 * 
	 * @return console port
	 */
	public int getConsolePort() {
		return this.getConsoleProperties().getConsolePort();
	}

	/**
	 * Return the assigned console host. If it's empty, it returns host IP
	 * 
	 * @return console host
	 */
	public String getConsoleHost() {
		String consoleHost = this.getConsoleProperties().getConsoleHost();
		if (StringUtils.isEmpty(consoleHost)) {
			consoleHost = NetworkUtil.getLocalHostAddress();
		}
		return consoleHost;
	}

	/**
	 * Start {@link SingleConsole} and wait until it's ready to get agent messages.
	 */
	public void start() {
		if (consoleFoundation == null) {
			return; // the console is not a valid console.(NullSingleConsole)
		}
		synchronized (eventSyncCondition) {
			consoleFoundationThread = new Thread(new Runnable() {
				public void run() {
					consoleFoundation.run();
				}
			}, "SingleConsole on port " + getConsolePort());
			consoleFoundationThread.setDaemon(true);
			consoleFoundationThread.start();
			eventSyncCondition.waitNoInterrruptException(5000);
		}
	}

	/**
	 * For test.
	 */
	public void startSync() {
		consoleFoundation.run();
	}

	/**
	 * Shutdown this {@link SingleConsole} and wait until the underlying console logic is stopped.
	 */
	public void shutdown() {
		try {
			synchronized (this) {
				consoleFoundation.shutdown();
				if (consoleFoundationThread != null && !consoleFoundationThread.isInterrupted()) {
					consoleFoundationThread.interrupt();
					consoleFoundationThread.join(1000);
				}
				samplingCount = 0;
			}

		} catch (Exception e) {
			throw new NGrinderRuntimeException("Exception occurs while shutting down SingleConsole", e);
		} finally {
			// close all report file
			for (BufferedWriter bw : fileWriterMap.values()) {
				IOUtils.closeQuietly(bw);
			}
			fileWriterMap.clear();
		}
	}

	/**
	 * Get all attached agents count.
	 * 
	 * @return count of agents
	 */
	public int getAllAttachedAgentsCount() {
		return ((ProcessControlImplementation) consoleFoundation.getComponent(ProcessControl.class))
						.getNumberOfLiveAgents();
	}

	/**
	 * Get all attached agent list on this console.
	 * 
	 * @return agent list
	 */
	public List<AgentIdentity> getAllAttachedAgents() {
		final List<AgentIdentity> agentIdentities = newArrayList();
		AllocateLowestNumber agentIdentity = (AllocateLowestNumber) checkNotNull(ReflectionUtil.getFieldValue(
						(ProcessControlImplementation) consoleFoundation.getComponent(ProcessControl.class),
						"m_agentNumberMap"),
						"m_agentNumberMap on ProcessControlImplemenation is not available in this grinder version");
		agentIdentity.forEach(new AllocateLowestNumber.IteratorCallback() {
			public void objectAndNumber(Object object, int number) {
				agentIdentities.add((AgentIdentity) object);
			}
		});
		return agentIdentities;
	}

	/**
	 * Get the console Component.
	 * 
	 * @param <T>
	 *            componentType component type
	 * @param componentType
	 *            component type
	 * @return the consoleFoundation
	 */
	public <T> T getConsoleComponent(Class<T> componentType) {
		return consoleFoundation.getComponent(componentType);
	}

	/**
	 * Get {@link ConsoleProperties} which is used to configure {@link SingleConsole}.
	 * 
	 * @return {@link ConsoleProperties}
	 */
	public ConsoleProperties getConsoleProperties() {
		return getConsoleComponent(ConsoleProperties.class);
	}

	/**
	 * Start test with given {@link GrinderProperties}.
	 * 
	 * @param properties
	 *            {@link GrinderProperties}
	 * @return current time
	 */
	public long startTest(GrinderProperties properties) {
		properties.setInt(GrinderProperties.CONSOLE_PORT, getConsolePort());
		getConsoleComponent(ProcessControl.class).startWorkerProcesses(properties);
		this.startTime = System.currentTimeMillis();
		return this.startTime;
	}

	/**
	 * Set the file distribution directory.
	 * 
	 * @param filePath
	 *            file path.
	 */
	public void setDistributionDirectory(File filePath) {
		final ConsoleProperties properties = getConsoleComponent(ConsoleProperties.class);
		Directory directory;
		try {
			directory = new Directory(filePath);
			properties.setAndSaveDistributionDirectory(directory);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new NGrinderRuntimeException(e.getMessage(), e);
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
	 * Distribute files on given filePath to attached agents.
	 * 
	 * @param filePath
	 *            the distribution files
	 */
	public void distributeFiles(File filePath) {
		distributeFiles(filePath, null, true);
	}

	/**
	 * Distribute files on given filePath to attached agents.
	 * 
	 * @param filePath
	 *            the distribution files
	 * @param listener
	 *            listener
	 * @param safe
	 *            safe file transition
	 */
	public void distributeFiles(File filePath, ListenerSupport<FileDistributionListener> listener, boolean safe) {
		setDistributionDirectory(filePath);
		distributFiles(listener, safe);
	}

	/**
	 * File distribution even listener.
	 * 
	 * @author JunHo Yoon
	 */
	public abstract static class FileDistributionListener {

		/**
		 * Notify the distribute starting event and the returns the safe mode (if you want to enable
		 * safe mode by force depending on the file. It should return true.
		 * 
		 * @param dir
		 *            Distribution dir
		 * @param safe
		 *            safe file transition
		 * @return true if safe
		 */
		public abstract boolean start(File dir, boolean safe);

		/**
		 * Notify the given file is distributed.
		 * 
		 * @param fileName
		 *            file name
		 */
		public abstract void distributed(String fileName);
	}

	/**
	 * Distribute files on agents.
	 */
	public void distributFiles() {
		distributFiles(null, true);
	}

	/**
	 * Distribute files on agents.
	 * 
	 * @param listener
	 *            listener
	 * @param safe
	 *            safe
	 */
	public void distributFiles(ListenerSupport<FileDistributionListener> listener, final boolean safe) {
		final FileDistribution fileDistribution = (FileDistribution) getConsoleComponent(FileDistribution.class);
		final AgentCacheState agentCacheState = fileDistribution.getAgentCacheState();
		final Condition cacheStateCondition = new Condition();
		agentCacheState.addListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent ignored) {
				synchronized (cacheStateCondition) {
					cacheStateCondition.notifyAll();
				}
			}
		});
		final MutableBoolean mutableBoolean = new MutableBoolean(safe);
		ConsoleProperties consoleComponent = getConsoleComponent(ConsoleProperties.class);
		final File file = consoleComponent.getDistributionDirectory().getFile();
		if (listener != null) {
			listener.apply(new Informer<FileDistributionListener>() {
				@Override
				public void inform(FileDistributionListener listener) {
					mutableBoolean.setValue(listener.start(file, safe));
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

				if (mutableBoolean.isTrue()) {
					// The cache status is updated asynchronously by agent
					// reports.
					// If we have a listener, we wait for up to five seconds for
					// all
					// agents to indicate that they are up to date.
					checkSafetyWithCacheState(fileDistribution, cacheStateCondition, 1);
				}
			} catch (FileContents.FileContentsException e) {
				throw new NGrinderRuntimeException("Error while distribute files for " + getConsolePort());
			}
		}
		if (mutableBoolean.isFalse()) {
			ThreadUtil.sleep(1000);
			checkSafetyWithCacheState(fileDistribution, cacheStateCondition, fileCount);
		}
	}

	private void checkSafetyWithCacheState(final FileDistribution fileDistribution,
					final Condition cacheStateCondition, int fileCount) {
		synchronized (cacheStateCondition) {
			for (int i = 0; i < (10 * fileCount) && shouldEnable(fileDistribution); ++i) {
				cacheStateCondition.waitNoInterrruptException(500);
			}
		}
	}

	/**
	 * Wait until the given size of agents are all connected. It wait until 10 sec.
	 * 
	 * @param size
	 *            size of agent.
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
			} else if (isCanceled()) {
				return;
			} else {
				return;
			}
		}
		throw new NGrinderRuntimeException("Connection is not completed until 10 sec");
	}

	/**
	 * Wait until runningThread is 0. If it's over 20 seconds, Exception occurs
	 */
	public void waitUntilAllAgentDisconnected() {
		int trial = 1;
		while (trial++ < 40) {
			// when agent finished one test, processReports will be updated as
			// null
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
		throw new NGrinderRuntimeException("Connection is not completed until 10 sec");
	}

	/**
	 * Check all test is finished. To be safe, this counts thread count and not finished
	 * workprocess. If one of them is 0, It thinks test is finished.
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
	 * Set the current TPS value. and it update max peakTPS as well.
	 * 
	 * @param newValue
	 *            TPS value
	 */
	public void setTpsValue(double newValue) {
		peakTpsForGraph = Math.max(peakTpsForGraph, newValue);
		tpsValue = newValue;
	}

	public double getTpsValues() {
		return tpsValue;
	}

	public long getStartTime() {
		return startTime;
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

	protected Map<String, Object> getStatisticData() {
		return statisticData;
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
	 * Get all expression entry set (display name and {@link StatisticExpression} pair.
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
	 * The last timestamp when the sampling is ran.
	 */
	private long lastSamplingPeriod = 0;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.grinder.console.model.SampleListener#update(net.grinder.statistics .StatisticsSet,
	 * net.grinder.statistics.StatisticsSet)
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
			// takes longer than 1
			// sec.

			samplingLifeCycleListener.apply(new Informer<SamplingLifeCycleListener>() {
				@Override
				public void inform(SamplingLifeCycleListener listener) {
					listener.onSampling(getReportPath(), intervalStatistics, cumulativeStatistics);
				}
			});
			for (int i = 0; i < (gap + 1); i++) {
				final boolean lastCall = (samplingCount == 1 && i == 0) || (samplingCount != 1 && i == gap);
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
			LOGGER.error("Error occurs while update statistics " + e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * Writer interval summary data per each test.
	 * 
	 * @param intervalStatisticMapPerTest
	 *            statistics map
	 * @param lastCall
	 *            true if it's the last call of consequent call in a single sampling
	 */
	private void writeIntervalSummaryDataPerTest(Map<Test, StatisticsSet> intervalStatisticMapPerTest, //
					boolean lastCall) {
		StatisticExpression tpsExpression = sampleModel.getTPSExpression();
		for (Entry<Test, StatisticsSet> entry : intervalStatisticMapPerTest.entrySet()) {
			if (lastCall) {
				StatisticsSet value = entry.getValue();
				writeReportData("TPS-" + entry.getKey().getDescription().replaceAll("\\s+", "_") + REPORT_DATA,
								formatValue(getRealDoubleValue(tpsExpression.getDoubleValue(value))));
			} else {
				writeReportData("TPS-" + entry.getKey().getDescription().replaceAll("\\s+", "_") + REPORT_DATA, "null");
			}

		}
	}

	/**
	 * Write total test interval statistic data into file.
	 * 
	 * @param intervalStatistics
	 *            interval statistics
	 * @param firstCall
	 *            true if it's the last call of consequent call in a single sampling
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
	}

	/**
	 * Write each interval statistic data into CSV.
	 * 
	 * @param intervalStatistics
	 *            interval statistics
	 */
	public void writeIntervalCsvData(StatisticsSet intervalStatistics) {

		StringBuilder csvLine = new StringBuilder();
		csvLine.append(DateUtil.dateToString(new Date()));

		// add header into csv file.
		if (!headerAdded) {
			StringBuilder csvHeader = new StringBuilder();
			csvHeader.append("DateTime");

			// get the key list from lastStatistic map, use list to keep the
			// order
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
										formatValue(getRealDoubleValue(each.getValue().getDoubleValue(
														eachPair.getValue()))));
					}
				}
			}
		}

		writeCSVDataLine(csvLine.toString());
	}

	/**
	 * Check if the TPS is too low. the TPS is lower than 0.001 for 2 minutes, It notifies a
	 * shutdown event to the {@link ConsoleShutdownListener}
	 * 
	 * @param tps
	 *            current TPS
	 */
	private void checkTooLowTps(double tps) {
		// If the tps is low that it's can be the agents or scripts goes wrong.
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
			// only if tps value is not too small ,It should be displayed
		}
	}

	/**
	 * Check if too many error occurs. If the half of total transaction is error for 10 sec. It
	 * notifies the {@link ConsoleShutdownListener}
	 * 
	 * @param cumulativeStatistics
	 *            accumulated Statistics
	 */
	private void checkTooManyError(StatisticsSet cumulativeStatistics) {
		StatisticsIndexMap statisticsIndexMap = getStatisticsIndexMap();
		long testSum = cumulativeStatistics.getCount(statisticsIndexMap.getLongSampleIndex("timedTests"));
		long errors = cumulativeStatistics.getValue(statisticsIndexMap.getLongIndex("errors"));
		if (((double) (testSum + errors)) / 2 < errors) {
			if (lastMomentWhenErrorsMoreThanHalfOfTotalTPSValue == 0) {
				lastMomentWhenErrorsMoreThanHalfOfTotalTPSValue = System.currentTimeMillis();
			} else if (isOverLowTpsThreshhold()) {
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

	private boolean isOverLowTpsThreshhold() {
		return (System.currentTimeMillis() - lastMomentWhenErrorsMoreThanHalfOfTotalTPSValue) >= TOO_MANY_ERROR_TIME;
	}

	public static final Set<String> INTERESTING_STATISTICS = Sets.newHashSet("Tests", "Errors", "TPS",
					"Response_bytes_per_second", "Mean_time_to_first_byte", "Peak_TPS", "Mean_Test_Time_(ms)",
					"User_defined");

	/**
	 * Build up statistic for current moment.
	 * 
	 * @param accumulatedStatistics
	 *            intervalStatistics
	 * @param intervalStatistics
	 *            cumulatedStatistics
	 */
	protected void updateStatistics(StatisticsSet intervalStatistics, StatisticsSet accumulatedStatistics) {
		Map<String, Object> result = newHashMap();
		result.put("test_time", getCurrentRunningTime() / 1000);
		List<Map<String, Object>> cumulativeStatistics = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> lastSampleStatistics = new ArrayList<Map<String, Object>>();
		for (Test test : accumulatedStatisticMapPerTest.keySet()) {
			Map<String, Object> accumulatedStatisticMap = newHashMap();
			Map<String, Object> intervalStatisticsMap = newHashMap();
			StatisticsSet accumulatedSet = this.accumulatedStatisticMapPerTest.get(test);
			StatisticsSet intervalSet = this.intervalStatisticMapPerTest.get(test);

			accumulatedStatisticMap.put("testNumber", test.getNumber());
			accumulatedStatisticMap.put("testDescription", test.getDescription());
			// remove description from statistic, otherwise, it will be
			// saved in report data. and the character like ',' in this field
			// will affect the csv
			// file too.
			intervalStatisticsMap.put("testNumber", test.getNumber());
			intervalStatisticsMap.put("testDescription", test.getDescription());
			// When only 1 test is running, it's better to use the parameterized
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
		Map<?, ?> totalStatistics = (Map<?, ?>) getStatictisData().get("totalStatistics");
		Double testCount = MapUtils.getDoubleValue(totalStatistics, "Tests", 0D);
		Double errorCount = MapUtils.getDoubleValue(totalStatistics, "Errors", 0D);
		return testCount.longValue() + errorCount.longValue();
	}

	private static Object getRealDoubleValue(Double doubleValue) {
		if (doubleValue == null) {
			return 0;
		}
		return (doubleValue.isInfinite() || doubleValue.isNaN()) ? 0 : doubleValue;
	}

	/**
	 * Listener interface to detect sampling start and end point.
	 * 
	 * @author JunHo Yoon
	 * @since 3.0
	 */
	public static interface SamplingLifeCycleListener {
		/**
		 * Called when the sampling is started.
		 */
		void onSamplingStarted();

		/**
		 * Called whenever the sampling is performed.
		 * 
		 * @param file
		 *            report path
		 * @param intervalStatistics
		 *            interval statistics snapshot
		 * @param cumulativeStatistics
		 *            cumulative statistics snapshot
		 * @since 3.0.2
		 */
		void onSampling(File file, StatisticsSet intervalStatistics, StatisticsSet cumulativeStatistics);

		/**
		 * Called when the sampling is started.
		 */
		void onSamplingEnded();

	}

	/**
	 * Listener interface to detect sampling start point and end point and each sampling. This is
	 * used when the sampling listener for each second is necessary.
	 * 
	 * @author JunHo Yoon
	 * @since 3.1.3
	 */
	public static interface SamplingLifeCycleFollowUpListener {
		/**
		 * Called when the sampling is started.
		 */
		void onSamplingStarted();

		/**
		 * Called whenever the sampling is performed. The first call in the each sampling will
		 * contains true followUp parameter and false for other calls. This method is
		 * 
		 * @param file
		 *            report path
		 * @param intervalStatistics
		 *            interval statistics snapshot
		 * @param cumulativeStatistics
		 *            cumulative statistics snapshot
		 * @param lastCall
		 *            true if it's the last call in the consequent following up samplings
		 * @since 3.0.3
		 */
		void onSampling(File file, StatisticsSet intervalStatistics, StatisticsSet cumulativeStatistics,
						boolean lastCall);

		/**
		 * Called when the sampling is started.
		 */
		void onSamplingEnded();

	}

	/**
	 * Listener interface to detect console shutdown condition.
	 * 
	 * @author JunHo Yoon
	 */
	public interface ConsoleShutdownListener {
		/**
		 * Called when the console should be shutdown.
		 * 
		 * @param stopReason
		 *            the reason of shutdown..
		 */
		void readyToStop(StopReason stopReason);
	}

	/**
	 * get the list of added {@link ConsoleShutdownListener}.
	 * 
	 * @return the list of added {@link ConsoleShutdownListener}.
	 * @see ConsoleShutdownListener
	 */
	public ListenerSupport<ConsoleShutdownListener> getListeners() {
		return this.showdownListner;
	}

	/**
	 * Add {@link ConsoleShutdownListener} to get notified when console is shutdowned.
	 * 
	 * @param listener
	 *            listener to be used.
	 */
	public void addListener(ConsoleShutdownListener listener) {
		showdownListner.add(listener);
	}

	/**
	 * Add {@link SamplingLifeCycleListener} to get notified when sampling is started and ended.
	 * 
	 * @param listener
	 *            listener to be used.
	 * 
	 */
	public void addSamplingLifeCyleListener(SamplingLifeCycleListener listener) {
		samplingLifeCycleListener.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.grinder.console.communication.ProcessControl.Listener#update(net. grinder.console.
	 * communication.ProcessControl.ProcessReports[])
	 */
	@Override
	public void update(ProcessReports[] processReports) {
		synchronized (eventSyncCondition) {
			checkExeuctionErrors(processReports);
			this.processReports = processReports;
			// The reason I passed porcessReport as parameter here is to prevent
			// the synchronization problem.
			updateCurrentProcessAndThread(processReports);
			eventSyncCondition.notifyAll();
		}
	}

	private void checkExeuctionErrors(ProcessReports[] processReports) {
		if (samplingCount == 0 && ArrayUtils.isNotEmpty(this.processReports) && ArrayUtils.isEmpty(processReports)) {
			getListeners().apply(new Informer<ConsoleShutdownListener>() {
				public void inform(ConsoleShutdownListener listener) {
					listener.readyToStop(StopReason.SCRIPT_ERROR);
				}
			});
		}
	}

	/**
	 * Update current processes and threads.
	 * 
	 * @param processReports
	 *            ProcessReports array.
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
			throw new NGrinderRuntimeException(e.getMessage(), e);
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
	 * Get the statistics data. This method returns the map whose key is string and it's mapped to
	 * the various statistics. Please refer {@link #updateStatistics()}
	 * 
	 * @return map which contains statistics data
	 */
	public Map<String, Object> getStatictisData() {
		return this.statisticData != null ? this.statisticData : getNullStatictisData();
	}

	private Map<String, Object> getNullStatictisData() {
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
	 * Set report path.
	 * 
	 * @param reportPath
	 *            path in which report will be stored.
	 */
	public void setReportPath(File reportPath) {
		checkNotNull(reportPath, "the report folder should not be empty!").mkdirs();
		this.reportPath = reportPath;
	}

	/**
	 * Send stop message to attached agents to shutdown.
	 */
	public void sendStopMessageToAgents() {
		getConsoleComponent(ProcessControl.class).stopAgentAndWorkerProcesses();
	}

	/**
	 * Start sampling with ignore count.
	 * 
	 * @param ignoreSampleCount
	 *            the count how many sample will be ignored.
	 */
	public void startSampling(int ignoreSampleCount) {
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
		this.sampleModel.reset();
		this.sampleModel.stop();
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
					LOGGER.error("Error occurs while running sampling start listener", e);
				}
			}
		});
		samplingLifeCycleFollowupListener.apply(new Informer<SamplingLifeCycleFollowUpListener>() {
			@Override
			public void inform(SamplingLifeCycleFollowUpListener listener) {
				try {
					listener.onSamplingStarted();
				} catch (Exception e) {
					LOGGER.error("Error occurs while running sampling start listener", e);
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
					LOGGER.error("Error occurs while running sampling end listener", e);
				}
			}
		});

		samplingLifeCycleFollowupListener.apply(new Informer<SamplingLifeCycleFollowUpListener>() {
			@Override
			public void inform(SamplingLifeCycleFollowUpListener listener) {
				try {
					listener.onSamplingEnded();
				} catch (Exception e) {
					LOGGER.error("Error occurs while running sampling end listener", e);
				}
			}
		});
	}

	/**
	 * If the test error is over 20%.. return true;
	 * 
	 * @return true if error is over 20%
	 */
	public boolean hasTooManyError() {
		long currentTestsCount = getCurrentExecutionCount();
		double errors = MapUtils.getDoubleValue((Map<?, ?>) getStatictisData().get("totalStatistics"), "Errors", 0D);
		return currentTestsCount == 0 ? false : (errors / currentTestsCount) > 0.2;
	}

	/**
	 * Check the test is performed at least once.
	 * 
	 * @return true if performed.
	 * @since 3.1.1
	 */
	public boolean hasNoPerformedTest() {
		return (getCurrentExecutionCount() == 0);
	}

	/**
	 * Check this singleConsole is canceled.
	 * 
	 * @return true if yes.
	 */
	public boolean isCanceled() {
		return cancel;
	}

	/**
	 * Check if the current Running time is over given duration.
	 * 
	 * @param duration
	 *            duration
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
	 * @param sampleModel
	 *            sample model
	 */
	void setSampleModel(SampleModel sampleModel) {
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
	 * Add {@link SamplingLifeCycleFollowUpListener}.
	 * 
	 * @param listener
	 *            listener to be added
	 * @since 3.1.3
	 */
	public void addSamplingLifeCycleFollowUpCycleListener(SamplingLifeCycleFollowUpListener listener) {
		samplingLifeCycleFollowupListener.add(listener);
	}
}
