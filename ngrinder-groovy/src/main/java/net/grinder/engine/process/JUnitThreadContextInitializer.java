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
package net.grinder.engine.process;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.grinder.common.GrinderProperties;
import net.grinder.common.SkeletonThreadLifeCycleListener;
import net.grinder.engine.common.EngineException;
import net.grinder.engine.process.GrinderProcess.ThreadContexts;
import net.grinder.engine.process.GrinderProcess.Times;
import net.grinder.engine.process.dcr.DCRContextImplementation;
import net.grinder.script.Grinder;
import net.grinder.script.InternalScriptContext;
import net.grinder.script.Statistics;
import net.grinder.scriptengine.Instrumenter;
import net.grinder.statistics.StatisticsServices;
import net.grinder.statistics.StatisticsServicesImplementation;
import net.grinder.util.Sleeper;
import net.grinder.util.SleeperImplementation;
import net.grinder.util.weave.agent.ExposeInstrumentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ThreadContext initializer in JUnit context.
 *
 * This class is responsible to setup the Grinder thread context when it's not executed in the
 * Grinder agent.
 *
 * @author JunHo Yoon
 * @since 3.2
 */
public class JUnitThreadContextInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(JUnitThreadContextInitializer.class);
	private ThreadContexts m_threadContexts = new ThreadContexts();
	private StatisticsServices m_statisticsServices;
	private Times m_times = new Times();

	/**
	 * Constructor.
	 */
	public JUnitThreadContextInitializer() {
	}

	/**
	 * Initialize thread context. This method is subject to be executed once in the total execution
	 * context.
	 */
	public void initialize() {
		m_statisticsServices = StatisticsServicesImplementation.getInstance();
		List<Instrumenter> list = new ArrayList<Instrumenter>();

		DCRContextImplementation context = DCRContextImplementation.create(LOGGER);
		if (context == null) {
			String file = ExposeInstrumentation.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			file = new File(file).getAbsolutePath();
			throw new RuntimeException("Please add \r\n-javaagent:" + file + "\r\nin 'Run As JUnit' vm argument.");
		}
		list.add(new JavaDCRInstrumenterEx(context));
		final Instrumenter instrumenter = new MasterInstrumenter(list);

		TestStatisticsHelperImplementation m_testStatisticsHelper = new TestStatisticsHelperImplementation(m_statisticsServices.getStatisticsIndexMap());

		TestRegistryImplementation m_testRegistryImplementation = new TestRegistryImplementation(m_threadContexts,
				m_statisticsServices.getStatisticsSetFactory(), m_testStatisticsHelper,
				m_times.getTimeAuthority());

		m_testRegistryImplementation.setInstrumenter(instrumenter);

		final Logger externalLogger = new ExternalLogger(LOGGER, m_threadContexts);

		Sleeper m_sleeper = new SleeperImplementation(m_times.getTimeAuthority(), externalLogger, 1.0d, 0.2d);

		final Statistics scriptStatistics = new ScriptStatisticsImplementation(m_threadContexts, m_statisticsServices,
				new NullSender());

		final InternalScriptContext scriptContext = new ScriptContextImplementation(new SimpleWorkerIdentity(
				"unit-test", 0), new SimpleWorkerIdentity("unit-test", 0), m_threadContexts, null,
				externalLogger, m_sleeper, new SSLControlImplementation(m_threadContexts), scriptStatistics,
				m_testRegistryImplementation, null, null, null, null);
		Grinder.grinder = scriptContext;

		new PluginRegistryImplementation(externalLogger, scriptContext, m_threadContexts, m_statisticsServices,
				m_times.getTimeAuthority());

	}

	/**
	 * Register the worker thread context.
	 */
	public void attachWorkerThreadContext() {
		try {
			if (m_threadContexts.get() == null) {
				GrinderProperties properties = new GrinderProperties();
				properties.setInt("grinder.agents", 1);
				properties.setInt("grinder.processes", 1);
				properties.setInt("grinder.threads", 1);
				properties.setBoolean("grinder.logData", false);
				final ThreadContextImplementation threadContext = new ThreadContextImplementation(properties,
						m_statisticsServices, 0, LOGGER);
				m_threadContexts.threadCreated(threadContext);
				m_threadContexts.threadStarted(threadContext);
				threadContext.registerThreadLifeCycleListener(new SkeletonThreadLifeCycleListener() {
					@Override
					public void endThread() {
						threadContext.reportPendingDispatchContext();
					}

					@Override
					public void endRun() {
					}
				});
			}
		} catch (EngineException e) {
			LOGGER.error("Error while initiating thread context", e);
		}
	}

	/**
	 * Detach the worker thread context.
	 */
	public void detachWorkerThreadContext() {
		ThreadContext threadContext = m_threadContexts.get();
		if (threadContext != null) {
			threadContext.fireEndThreadEvent();
			m_threadContexts.threadStarted(null);
		}
	}

	/**
	 * Get thread context updater.
	 *
	 * @return JUnitThreadContextUpdater
	 */
	public JUnitThreadContextUpdater getThreadContextUpdater() {
		return new JUnitThreadContextUpdater(m_threadContexts);
	}
}
