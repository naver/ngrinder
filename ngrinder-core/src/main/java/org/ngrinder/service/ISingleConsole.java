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
package org.ngrinder.service;

import java.io.File;
import java.util.List;

import net.grinder.common.GrinderProperties;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.statistics.StatisticsIndexMap;

/**
 * {@link SingleConsole} interface for plugin use.
 *
 */
@SuppressWarnings("JavadocReference")
public interface ISingleConsole {

	/**
	 * Mark the cancel status.
	 */
	void cancel();

	/**
	 * Get current running time in ms.
	 *
	 * @return running time
	 */
	long getCurrentRunningTime();

	/**
	 * Get the current total execution count(test count + error count).
	 *
	 * @return current total execution count
	 */
	long getCurrentExecutionCount();

	/**
	 * Get statistics index map used.
	 *
	 * @return {@link StatisticsIndexMap}
	 */
	StatisticsIndexMap getStatisticsIndexMap();

	/**
	 * Get report path.
	 *
	 * @return report path
	 */
	File getReportPath();

	/**
	 * Get peak TPS.
	 *
	 * @return peak tps
	 */
	double getPeakTpsForGraph();

	/**
	 * Get the count of current running threads.
	 *
	 * @return running threads.
	 */
	int getRunningThread();

	/**
	 * Get the count of current running processes.
	 *
	 * @return running processes
	 */
	int getRunningProcess();

	/**
	 * Get the all agents attached in this processes.
	 *
	 * @return {@link AgentIdentity} list
	 * @since 3.1.2
	 */
	List<AgentIdentity> getAllAttachedAgents();

	/**
	 * Return the assigned console port.
	 *
	 * @return console port
	 */
	int getConsolePort();


	/**
	 * Get the associated grinder properties.
	 *
	 * @return grinder properties. null if the test is not started.
	 */
	GrinderProperties getGrinderProperties();
}
