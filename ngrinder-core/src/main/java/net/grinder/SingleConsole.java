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
package net.grinder;

import static org.ngrinder.common.util.NoOp.noOp;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
import net.grinder.util.thread.Condition;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.util.DateUtil;
import org.ngrinder.common.util.ReflectionUtil;
import org.ngrinder.service.ISingleConsole;
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
	private Date momentWhenTpsBeganToHaveVerySmall;
	private Date lastMomentWhenErrorsMoreThanHalfOfTotalTPSValue;
	private static final int TEST_DURATION_CHECK_MARGIN = 5000;
	private final ListenerSupport<ConsoleShutdownListener> showdownListner = ListenerHelper.create();
	private final ListenerSupport<SamplingLifeCycleListener> samplingLifeCycleListener = ListenerHelper.create();

	private File reportPath;
	// private NumberFormat simpleFormatter = new DecimalFormat("###");

	private Map<String, Object> statisticData;
	private boolean sampling = false;

	private boolean headerAdded = false;

	private Map<String, BufferedWriter> fileWriterMap = new HashMap<String, BufferedWriter>();
	/** Current count of sampling. */
	private long samplingCount = 0;
	/** The count of ignoring sampling. */
	private int ignoreSampleCount;
	private boolean firstSampling = true;
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
		//if port is 0, it is Null singleConsole.
		if (port  == 0) {
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
		try {
			return StringUtils.defaultIfBlank(this.getConsoleProperties().getConsoleHost(), InetAddress.getLocalHost()
							.getHostAddress());
		} catch (UnknownHostException e) {
			return "127.0.0.1";
		}
	}

	/**
	 * Start console and wait until it's ready to get agent message.
	 */
	public void start() {

		synchronized (eventSyncCondition) {
			consoleFoundationThread = new Thread(new Runnable() {
				public void run() {
					consoleFoundation.run();
				}
			}, "SingleConsole on port " + getConsolePort());
			consoleFoundationThread.setDaemon(true);
			consoleFoundationThread.start();
			// 10 second is too big?
			eventSyncCondition.waitNoInterrruptException(10000);
		}
	}

	/**
	 * For test.
	 */
	public void startSync() {
		consoleFoundation.run();
	}

	/**
	 * Shutdown console and wait until underlying console logic is stop to run.
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
		final List<AgentIdentity> agentIdentities = new ArrayList<AgentIdentity>();
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
		setDistributionDirectory(filePath);
		distributFiles();
	}

	/**
	 * Distribute files on agents.
	 */
	public void distributFiles() {
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
		final FileDistributionHandler distributionHandler = fileDistribution.getHandler();
		// When cancel is called.. stop processing.
		while (!cancel) {
			try {
				final FileDistributionHandler.Result result = distributionHandler.sendNextFile();
				if (result == null) {
					break;
				}

			} catch (FileContents.FileContentsException e) {
				throw new NGrinderRuntimeException("Error while distribute files for " + getConsolePort());
			}
			// The cache status is updated asynchronously by agent reports.
			// If we have a listener, we wait for up to five seconds for all
			// agents to indicate that they are up to date.
			synchronized (cacheStateCondition) {
				for (int i = 0; i < 5 && shouldEnable(fileDistribution); ++i) {
					cacheStateCondition.waitNoInterrruptException(1000);
				}
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
				// However sometimes runningThread is over 0 but all processs is
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
		return new Date().getTime() - startTime;
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

	/**
	 * Get all expression views.
	 * 
	 * @return {@link ExpressionView} array
	 * @since 3.0.2
	 */
	public ExpressionView[] getExpressionView() {
		return modelView.getCumulativeStatisticsView().getExpressionViews();
	}

	/**
	 * Get detailed expression view.
	 * 
	 * @return {@link ExpressionView} array
	 * @since 3.0.2
	 */
	public ExpressionView[] getDetailedExpressionView() {
		return StatisticsServicesImplementation.getInstance().getDetailStatisticsView().getExpressionViews();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.grinder.console.model.SampleListener#update(net.grinder.statistics.StatisticsSet,
	 * net.grinder.statistics.StatisticsSet)
	 */
	@Override
	public void update(StatisticsSet intervalStatistics, StatisticsSet cumulativeStatistics) {
		if (!sampling) {
			return;
		}
		if (samplingCount++ < ignoreSampleCount) {
			return;
		}
		if (firstSampling) {
			firstSampling = false;
			informTestSamplingStart();
		}
		try {
			final StatisticsSet intervalStatisticsSnapshot = intervalStatistics.snapshot();
			final StatisticsSet cumulatedStatisticsSnapshot = cumulativeStatistics.snapshot();
			setTpsValue(sampleModel.getTPSExpression().getDoubleValue(intervalStatisticsSnapshot));
			checkTooLowTps(getTpsValues());
			updateStatistics();
			writeIntervalSummaryData(intervalStatisticsSnapshot);
			ModelTestIndex modelIndex = ((SampleModelImplementationEx) sampleModel).getModelTestIndex();
			writeIntervalCsvData(intervalStatisticsSnapshot, modelIndex);
			samplingLifeCycleListener.apply(new Informer<SamplingLifeCycleListener>() {
				@Override
				public void inform(SamplingLifeCycleListener listener) {
					listener.onSampling(getReportPath(), intervalStatisticsSnapshot, cumulatedStatisticsSnapshot);
				}
			});
			checkTooManyError(cumulativeStatistics);

		} catch (RuntimeException e) {
			LOGGER.error("Error occurs while update statistics " + e.getMessage(), e);
			throw e;
		}

	}

	private String createKeyFromExpression(ExpressionView expressionView) {
		return expressionView.getDisplayName().replaceAll("\\s+", "_");
	}

	/**
	 * Write total test interval data into file.
	 * 
	 * @param intervalStatistics
	 *            interval statistics
	 */
	public void writeIntervalSummaryData(StatisticsSet intervalStatistics) {
		for (ExpressionView eachView : getExpressionView()) {
			double doubleValue = eachView.getExpression().getDoubleValue(intervalStatistics);
			writeReportData(createKeyFromExpression(eachView) + REPORT_DATA,
							formatValue(getRealDoubleValue(doubleValue)));
		}
	}

	/**
	 * Write each test interval data into CSV.
	 * 
	 * @param intervalStatistics
	 *            interval statistics
	 * @param modelTestIndex
	 *            model containning all tests
	 */
	public void writeIntervalCsvData(StatisticsSet intervalStatistics, ModelTestIndex modelTestIndex) {
		if (modelTestIndex == null) {
			return;
		}
		StringBuilder csvLine = new StringBuilder();
		csvLine.append(DateUtil.dateToString(new Date()));
		ExpressionView[] expressionView = getExpressionView();
		for (ExpressionView eachView : expressionView) {
			if (!eachView.getDisplayName().equals("Peak TPS")) {
				double doubleValue = eachView.getExpression().getDoubleValue(intervalStatistics);
				csvLine.append(",").append(formatValue(getRealDoubleValue(doubleValue)));
			}
		}

		for (int i = 0; i < modelTestIndex.getNumberOfTests(); i++) {
			StatisticsSet lastSampleStatistics = modelTestIndex.getLastSampleStatistics(i);

			// get the key list from lastStatistic map, use list to keep the order
			Test test = modelTestIndex.getTest(i);
			String description = test.getDescription();
			csvLine.append(",").append(description);
			for (ExpressionView eachView : expressionView) {
				if (!eachView.getDisplayName().equals("Peak TPS")) {
					csvLine.append(",").append(
									formatValue(getRealDoubleValue(eachView.getExpression().getDoubleValue(
													lastSampleStatistics))));
				}
			}

		}

		// add header into csv file.
		if (!headerAdded) {
			StringBuilder csvHeader = new StringBuilder();
			csvHeader.append("DateTime");

			// get the key list from lastStatistic map, use list to keep the order
			for (ExpressionView each : expressionView) {
				if (!each.getDisplayName().equals("Peak TPS")) {
					csvHeader.append(",").append(createKeyFromExpression(each));
				}
			}

			for (int i = 0; i < modelTestIndex.getNumberOfTests(); i++) {
				csvHeader.append(",").append("Description");
				// get the key list from lastStatistic map, use list to keep the order
				for (ExpressionView each : expressionView) {
					if (!each.getDisplayName().equals("Peak TPS")) {
						csvHeader.append(",").append(createKeyFromExpression(each)).append("-").append(i);
					}
				}

			}
			writeCSVDataLine(csvHeader.toString());
			headerAdded = true;
		}
		writeCSVDataLine(csvLine.toString());
	}

	// In case of error..
	/**
	 * Check if the TPS is too low. the TPS is lower than 0.001 for 2 minutes, It notifies it to the
	 * {@link ConsoleShutdownListener}
	 * 
	 * @param tps
	 *            current TPS
	 */
	private void checkTooLowTps(double tps) {
		// If the tps is low that it's can be the agents or scripts goes wrong.
		if (tps < 0.001) {
			if (momentWhenTpsBeganToHaveVerySmall == null) {
				momentWhenTpsBeganToHaveVerySmall = new Date();
			} else if (new Date().getTime() - momentWhenTpsBeganToHaveVerySmall.getTime() >= TOO_LOW_TPS_TIME) {
				LOGGER.warn("Stop the test because its tps is less than 0.001 for more than {} minitue.",
								TOO_LOW_TPS_TIME / 60000);
				getListeners().apply(new Informer<ConsoleShutdownListener>() {
					public void inform(ConsoleShutdownListener listener) {
						listener.readyToStop(StopReason.TOO_LOW_TPS);
					}
				});
				momentWhenTpsBeganToHaveVerySmall = null;

			}
		} else {
			momentWhenTpsBeganToHaveVerySmall = null;
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
		long tpsSum = cumulativeStatistics.getCount(statisticsIndexMap.getLongSampleIndex("timedTests"));
		long errors = cumulativeStatistics.getValue(statisticsIndexMap.getLongIndex("errors"));
		if (((double) tpsSum / 2) < errors) {
			if (lastMomentWhenErrorsMoreThanHalfOfTotalTPSValue == null) {
				lastMomentWhenErrorsMoreThanHalfOfTotalTPSValue = new Date();
			} else if (isOverLowTpsThreshhold()) {
				LOGGER.warn("Stop the test because test error is more than half of total tps for last {} seconds.",
								TOO_MANY_ERROR_TIME / 1000);
				getListeners().apply(new Informer<ConsoleShutdownListener>() {
					public void inform(ConsoleShutdownListener listener) {
						listener.readyToStop(StopReason.TOO_MANY_ERRORS);
					}
				});
				lastMomentWhenErrorsMoreThanHalfOfTotalTPSValue = null;
			}
		}
	}

	private boolean isOverLowTpsThreshhold() {
		return new Date().getTime() - lastMomentWhenErrorsMoreThanHalfOfTotalTPSValue.getTime() >= TOO_MANY_ERROR_TIME;
	}

	/**
	 * To update statistics data while test is running.
	 */
	protected void updateStatistics() {
		Map<String, Object> result = new ConcurrentHashMap<String, Object>();
		result.put("test_time", getCurrentRunningTime() / 1000);
		List<Map<String, Object>> cumulativeStatistics = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> lastSampleStatistics = new ArrayList<Map<String, Object>>();
		ExpressionView[] views = modelView.getCumulativeStatisticsView().getExpressionViews();
		ModelTestIndex modelIndex = ((SampleModelImplementationEx) sampleModel).getModelTestIndex();

		if (modelIndex != null) {
			for (int i = 0; i < modelIndex.getNumberOfTests(); i++) {
				Map<String, Object> statistics = new HashMap<String, Object>();
				Map<String, Object> lastStatistics = new HashMap<String, Object>();
				Test test = modelIndex.getTest(i);

				statistics.put("testNumber", test.getNumber());
				statistics.put("testDescription", test.getDescription());
				// remove description from statistic, otherwise, it will be
				// saved in report data.
				// and the character like ',' in this field will affect the csv
				// file too.
				lastStatistics.put("testNumber", test.getNumber());
				lastStatistics.put("testDescription", test.getDescription());

				StatisticsSet set = modelIndex.getCumulativeStatistics(i);
				StatisticsSet lastSet = modelIndex.getLastSampleStatistics(i);
				for (ExpressionView expressionView : views) {
					statistics.put(expressionView.getDisplayName().replaceAll("\\s+", "_"),
									getRealDoubleValue(expressionView.getExpression().getDoubleValue(set)));
					lastStatistics.put(expressionView.getDisplayName().replaceAll("\\s+", "_"),
									getRealDoubleValue(expressionView.getExpression().getDoubleValue(lastSet)));
				}

				cumulativeStatistics.add(statistics);
				lastSampleStatistics.add(lastStatistics);
			}
		}

		StatisticsSet totalSet = sampleModel.getTotalCumulativeStatistics();
		Map<String, Object> totalStatistics = new HashMap<String, Object>();

		for (ExpressionView expressionView : views) {

			totalStatistics.put(expressionView.getDisplayName().replaceAll("\\s+", "_"),
							getRealDoubleValue(expressionView.getExpression().getDoubleValue(totalSet)));
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
	public interface SamplingLifeCycleListener {
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
	 * Listener interface to detect console shutdown condition.
	 * 
	 * @author JunHo Yoon
	 */
	public interface ConsoleShutdownListener {
		/**
		 * Called when the console should be shutdowned.
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
	 * @see net.grinder.console.communication.ProcessControl.Listener#update(net.grinder.console.
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
		} finally {
			noOp();
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
	 * specific value. Please refer {@link #updateStatistics()}
	 * 
	 * @return map which contains statistics data
	 */
	public Map<String, Object> getStatictisData() {
		return this.statisticData != null ? this.statisticData : getNullStatictisData();
	}

	private Map<String, Object> getNullStatictisData() {
		Map<String, Object> result = new ConcurrentHashMap<String, Object>(1);
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
		checkNotNull(reportPath, "report folder should not be empty!").mkdirs();
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
		this.ignoreSampleCount = ignoreSampleCount;
		this.sampling = true;
		LOGGER.info("Sampling is started");
		this.sampleModel = getConsoleComponent(SampleModelImplementationEx.class);
		this.sampleModel.addTotalSampleListener(this);
		this.sampleModel.start();

	}

	/**
	 * Stop sampling.
	 */
	public void unregisterSampling() {
		this.currentNotFinishedProcessCount = 0;
		this.sampling = false;
		this.sampleModel = getConsoleComponent(SampleModelImplementationEx.class);
		this.sampleModel.reset();
		this.sampleModel.stop();
		LOGGER.info("Sampling is stopped");
		informTestSamplingEnd();
	}

	private void informTestSamplingStart() {
		samplingLifeCycleListener.apply(new Informer<SamplingLifeCycleListener>() {
			@Override
			public void inform(SamplingLifeCycleListener listener) {
				listener.onSamplingStarted();
			}
		});
	}

	private void informTestSamplingEnd() {
		samplingLifeCycleListener.apply(new Informer<SamplingLifeCycleListener>() {
			@Override
			public void inform(SamplingLifeCycleListener listener) {
				listener.onSamplingEnded();
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
		return getCurrentRunningTime() > (duration + TEST_DURATION_CHECK_MARGIN);
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

	public SampleModel getSampleModel() {
		return sampleModel;
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

}
