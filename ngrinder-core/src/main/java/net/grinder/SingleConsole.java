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

import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import net.grinder.statistics.StatisticsSet;
import net.grinder.util.AllocateLowestNumber;
import net.grinder.util.ConsolePropertiesFactory;
import net.grinder.util.Directory;
import net.grinder.util.FileContents;
import net.grinder.util.ListenerSupport;
import net.grinder.util.ListenerSupport.Informer;
import net.grinder.util.thread.Condition;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableDouble;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.util.DateUtil;
import org.ngrinder.common.util.ReflectionUtil;
import org.picocontainer.MutablePicoContainer;
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
public class SingleConsole implements Listener, SampleListener {
	private final ConsoleProperties consoleProperties;
	private Thread thread;
	private ConsoleFoundationEx consoleFoundation;
	public static final Resources RESOURCE = new ResourcesImplementation(
					"net.grinder.console.common.resources.Console");
	public static final Logger LOGGER = LoggerFactory.getLogger(RESOURCE.getString("shortTitle"));

	private static final String REPORT_CSV = "output.csv";
	private static final String REPORT_DATA = ".data";

	private Condition m_eventSyncCondition = new Condition();
	private ProcessReports[] processReports;
	private boolean cancel = false;

	// for displaying tps graph in running page
	private double tpsValue = 0;
	// for displaying tps graph in running page
	private double peakTpsForGraph = 0;
	private SampleModel sampleModel;
	private SampleModelViews modelView;
	private long startTime = 0;
	private Date TPS_LESSTHAN_ZREO_TIME;
	private Date ERRORS_MORE_THAN_HALF_OF_TOTAL_TPS_TIME;
	private final ListenerSupport<ConsoleShutdownListener> m_shutdownListeners = new ListenerSupport<ConsoleShutdownListener>();

	private File reportPath;
	private NumberFormat formatter = new DecimalFormat("###.###");
	// private NumberFormat simpleFormatter = new DecimalFormat("###");

	private Map<String, Object> statisticData;

	private List<String> csvHeaderList = new ArrayList<String>();
	private boolean headerAdded = false;

	Map<String, BufferedWriter> fileWriterMap = new HashMap<String, BufferedWriter>();
	/** Current count of sampling */
	private long samplingCount = 0;
	/** The count of ignoring sampling */
	private int ignoreSampleCount;

	/**
	 * Currently running thread
	 */
	private int runningThread = 0;

	/**
	 * Currently running process
	 */
	private int runningProcess = 0;

	/**
	 * Currently not finished process count.
	 */
	private int currentNotFinishedProcessCount = 0;

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
		this.consoleProperties = consoleProperties;

		try {
			this.getConsoleProperties().setConsoleHost(ip);
			this.getConsoleProperties().setConsolePort(port);
			this.consoleFoundation = new ConsoleFoundationEx(RESOURCE, LOGGER, consoleProperties,
							m_eventSyncCondition);

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
			return StringUtils.defaultIfBlank(this.getConsoleProperties().getConsoleHost(), InetAddress
							.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			return "";
		}
	}

	/**
	 * Start console and wait until it's ready to get agent message.
	 */
	public void start() {

		synchronized (m_eventSyncCondition) {
			thread = new Thread(new Runnable() {
				public void run() {
					consoleFoundation.run();
				}
			}, "SingleConsole on port " + getConsolePort());
			thread.setDaemon(true);
			thread.start();
			// 10 second is too big?
			m_eventSyncCondition.waitNoInterrruptException(10000);
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
				if (thread != null && !thread.isInterrupted()) {
					thread.interrupt();
					thread.join(1000);
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
		AllocateLowestNumber agentIdentity = (AllocateLowestNumber) checkNotNull(
						ReflectionUtil.getFieldValue((ProcessControlImplementation) consoleFoundation
										.getComponent(ProcessControl.class), "m_agentNumberMap"),
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

	public ConsoleProperties getConsoleProperties() {
		return consoleProperties;
	}

	public long startTest(GrinderProperties properties) {
		properties.setInt(GrinderProperties.CONSOLE_PORT, getConsolePort());
		getConsoleComponent(ProcessControl.class).startWorkerProcesses(properties);
		this.startTime = System.currentTimeMillis();
		return this.startTime;
	}

	public void setDistributionDirectory(File filePath) {
		final ConsoleProperties properties = (ConsoleProperties) getConsoleComponent(ConsoleProperties.class);
		Directory directory;
		try {
			directory = new Directory(filePath);
			properties.setAndSaveDistributionDirectory(directory);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new NGrinderRuntimeException(e.getMessage(), e);
		}
	}

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

	public void waitUntilAgentConnected(int size) {
		int trial = 1;
		while (trial++ < 10) {
			// when agent finished one test, processReports will be updated as
			// null
			if (processReports == null || this.processReports.length != size) {
				synchronized (m_eventSyncCondition) {
					m_eventSyncCondition.waitNoInterrruptException(1000);
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
			// However sometimes runningThread is over 0 but all processs is marked as FINISHED.. It can be treated as finished status as well.
			} else if (this.currentNotFinishedProcessCount == 0) {
				System.out.println(this.currentNotFinishedProcessCount);
				return true;
			}
			return false;
		}
	}

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

	public long getCurrentRunningTime() {
		return new Date().getTime() - startTime;
	}

	@Override
	public void update(StatisticsSet intervalStatistics, StatisticsSet cumulativeStatistics) {
		if (samplingCount++ < ignoreSampleCount) {
			return;
		}
		setTpsValue(sampleModel.getTPSExpression().getDoubleValue(intervalStatistics));
		checkTooLowTps(getTpsValues());
		updateStatistics();
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> lastSampleStatistics = (List<Map<String, Object>>) statisticData
						.get("lastSampleStatistics");

		if (lastSampleStatistics != null) {
			double tpsSum = 0;
			double errors = 0;

			StringBuilder csvLine = new StringBuilder();
			StringBuilder csvHeader = new StringBuilder();
			csvHeader.append("DateTime");

			Map<String, Object> valueMap = new HashMap<String, Object>();
			int testIndex = 0;
			for (Map<String, Object> lastStatistic : lastSampleStatistics) {
				testIndex++;
				tpsSum += (Double) lastStatistic.get("TPS");
				errors += (Double) lastStatistic.get("Errors");

				for (Entry<String, Object> each : lastStatistic.entrySet()) {
					if (!headerAdded) {
						// Peak TPS is not meaningfule for CSV report.
						if (!each.getKey().contains("Peak_TPS")) {
							csvHeaderList.add(each.getKey());
							csvHeader.append(",");
							csvHeader.append(each.getKey() + "-" + testIndex);
						}
					}
					Object val = each.getValue();
					LOGGER.debug("statistic data key:{}", each.getKey());
					LOGGER.debug("- value:{}, value type:{}", val, val != null ? val.getClass().getName()
									: null);
					// number value in lastStatistic is Double, we add every
					// test's double value
					// into valueMap, so we use
					// MutableDouble in valueMap, to avoid creating too many
					// objects.
					if (val instanceof Double) {
						// for debug, maybe there are some fields should not be
						// sum up.
						LOGGER.debug("Calculate sum for key:{} in statistic", each.getKey());
						MutableDouble mutableDouble = (MutableDouble) valueMap.get(each.getKey());
						if (mutableDouble == null) {
							mutableDouble = new MutableDouble(0);
							valueMap.put(each.getKey(), mutableDouble);
						}
						mutableDouble.add((Double) val);
					} else if (String.valueOf(val).equals("null")) {
						// if it is null, just assume it is 0. The value is a
						// String "null"
						// that the Double value in
						// one second is null. Now I treat this value as ZERO.
						// But maybe it is not
						// the most proper solution.
						valueMap.put(each.getKey(), new MutableDouble(0));
					} else { // there are some String type object like test
								// description.
						valueMap.put(each.getKey(), val);
					}
				}

				// add date time into csv
				// FIXME this date time interval should be 1 second.
				// but the system can not make sure about that.
				csvLine.append(DateUtil.dateToString(new Date()));

				// FIXME we should also save vuser number, to describe the
				// current vuser count in
				// this secons.
				for (String key : csvHeaderList) {
					if (!key.contains("Peak_TPS")) {
						csvLine.append(",");
						csvLine.append(formatValue(lastStatistic.get(key)));
					}
				}
			}
			try {
				// add header into csv file.
				if (!headerAdded) {
					for (Entry<String, Object> each : valueMap.entrySet()) {
						if (!each.getKey().contains("Peak_TPS")) {
							csvHeader.append(",");
							csvHeader.append(each.getKey());
						}
					}
					writeCSVDataLine(csvHeader.toString());
					headerAdded = true;
				}

				for (Entry<String, Object> each : valueMap.entrySet()) {
					writeReportData(each.getKey() + REPORT_DATA, formatValue(each.getValue()));
				}
				// add total test report into csv file.
				for (String key : csvHeaderList) {
					if (!key.contains("Peak_TPS")) {
						csvLine.append(",");
						csvLine.append(formatValue(valueMap.get(key)));
					}
				}

				writeCSVDataLine(csvLine.toString());

			} catch (IOException e) {
				LOGGER.error("Write report data failed : ", e);
			}
			// In case of error..
			checkTooManyError(tpsSum, errors);
		}

	}

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
			if (TPS_LESSTHAN_ZREO_TIME == null) {
				TPS_LESSTHAN_ZREO_TIME = new Date();
			} else if (new Date().getTime() - TPS_LESSTHAN_ZREO_TIME.getTime() >= 120000) {
				LOGGER.warn("Test has been forced to stop because of tps is less than 0.001 and sustain more than two minitue.");
				getListeners().apply(new Informer<ConsoleShutdownListener>() {
					public void inform(ConsoleShutdownListener listener) {
						listener.readyToStop(StopReason.TOO_LOW_TPS);
					}
				});
				TPS_LESSTHAN_ZREO_TIME = null;

			}
		} else {
			TPS_LESSTHAN_ZREO_TIME = null;
			// only if tps value is not too small ,It should be displayed
		}
	}

	/**
	 * Check if too many error occurs. If the half of total transaction is error for 10 sec. It
	 * notifies the {@link ConsoleShutdownListener}
	 * 
	 * @param tpsSum
	 *            sum of tps
	 * @param errors
	 *            count of errors.
	 */
	private void checkTooManyError(double tpsSum, double errors) {
		if (tpsSum / 2 < errors) {
			if (ERRORS_MORE_THAN_HALF_OF_TOTAL_TPS_TIME == null) {
				ERRORS_MORE_THAN_HALF_OF_TOTAL_TPS_TIME = new Date();
			} else if (new Date().getTime() - ERRORS_MORE_THAN_HALF_OF_TOTAL_TPS_TIME.getTime() >= 10000) {
				LOGGER.warn("Test has been forced to stop because of error is more than half of total tps and sustain more than ten second.");
				getListeners().apply(new Informer<ConsoleShutdownListener>() {
					public void inform(ConsoleShutdownListener listener) {
						listener.readyToStop(StopReason.TOO_MANY_ERRORS);
					}
				});
				ERRORS_MORE_THAN_HALF_OF_TOTAL_TPS_TIME = null;
			}
		}
	}

	/**
	 * To update statistics data while test is running
	 */
	private void updateStatistics() {
		Map<String, Object> result = new HashMap<String, Object>();
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
				// remove description from statistic, otherwise, it will be
				// saved in report data.
				// and the character like ',' in this field will affect the csv
				// file too.
				lastStatistics.put("testNumber", test.getNumber());

				StatisticsSet set = modelIndex.getCumulativeStatistics(i);
				StatisticsSet lastSet = modelIndex.getLastSampleStatistics(i);
				for (ExpressionView expressionView : views) {
					statistics.put(expressionView.getDisplayName().replaceAll("\\s+", "_"),
									getRealDoubleValue(expressionView.getExpression().getDoubleValue(set)));
					lastStatistics.put(
									expressionView.getDisplayName().replaceAll("\\s+", "_"),
									getRealDoubleValue(expressionView.getExpression().getDoubleValue(lastSet)));
				}

				cumulativeStatistics.add(statistics);
				lastSampleStatistics.add(lastStatistics);
			}
		}

		StatisticsSet totalSet = sampleModel.getTotalCumulativeStatistics();
		Map<String, Object> totalStatistics = new HashMap<String, Object>();

		for (ExpressionView expressionView : views) { // TODO : expressionView
														// == null ?
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

	private static Object getRealDoubleValue(Double doubleValue) {
		if (doubleValue.isInfinite() || doubleValue.isNaN()) {
			return null;
		}
		return doubleValue;
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
		return this.m_shutdownListeners;
	}

	/**
	 * Add {@link ConsoleShutdownListener} to get notified when console is shutdowned.
	 * 
	 * @param listener
	 *            listener to be used.
	 */
	public void addListener(ConsoleShutdownListener listener) {
		m_shutdownListeners.add(listener);
	}

	/**
	 * Update process status of agents. In this method
	 */
	@Override
	public void update(ProcessReports[] processReports) {
		synchronized (m_eventSyncCondition) {
			this.processReports = processReports;
			// The reason I passed porcessReport as parameter here is to prevent the synchronization
			// problem.
			updateCurrentProcessAndThread(processReports);
			m_eventSyncCondition.notifyAll();
		}
	}

	/**
	 * Update current processes and threads
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
			processCount += processReports.length;
			for (WorkerProcessReport processReport : agentReport.getWorkerProcessReports()) {
				// There might be the processes which is not finished but no running thread in it.
				if (processReport.getState() != 3) {
					notFinishedWorkerCount++;
				}
				threadCount += processReport.getNumberOfRunningThreads();
			}
		}
		synchronized (this) {
			this.runningProcess = processCount;
			this.runningThread = threadCount;
			this.currentNotFinishedProcessCount = notFinishedWorkerCount;
		}
	}

	private void writeReportData(String name, String value) throws IOException {
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
			// IOUtils.closeQuietly(write);
			// IOUtils.closeQuietly(bw);
		}
	}

	private void writeCSVDataLine(String line) throws IOException {
		writeReportData(REPORT_CSV, line);
	}

	private String formatValue(Object val) {
		if (val instanceof Double) {
			return formatter.format(val);
		} else if (val == null) {
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
		return this.statisticData;
	}

	/**
	 * Get report path
	 * 
	 * @return report path
	 */
	public File getReportPath() {
		return reportPath;
	}

	/**
	 * Set report path
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
		sampleModel = getConsoleComponent(SampleModelImplementationEx.class);
		sampleModel.addTotalSampleListener(this);
	}

	/**
	 * Stop sampling
	 */
	public void unregisterSampling() {
		SampleModel sampleModel = getConsoleComponent(SampleModelImplementationEx.class);
		sampleModel.reset();
	}

}
