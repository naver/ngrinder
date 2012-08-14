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
package net.grinder.console.communication;

import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.util.Map;

import net.grinder.GrinderConstants;
import net.grinder.common.processidentity.ProcessIdentity;
import net.grinder.common.processidentity.WorkerProcessReport;
import net.grinder.console.communication.ProcessStatusImplementation.AgentAndWorkers;

import org.ngrinder.common.util.ReflectionUtil;

/**
 * This class is used to get worker and thread information from grinder. We add
 * it in this package Because some class like AgentAndWorkers can only be
 * accessed within same package.
 * 
 * @author Mavlarn
 * 
 */
public final class NGrinderConsoleCommunicationService {

	/** Constructor. */
	private NGrinderConsoleCommunicationService() {

	}

	/**
	 * Get number of running worker processes and threads.
	 * 
	 * @param processControl
	 *            - The process control of Grinder
	 * @param result
	 *            - Result map to front end
	 */
	public static void collectWorkerAndThreadInfo(ProcessControl processControl, Map<String, Object> result) {
		Map<ProcessIdentity, AgentAndWorkers> agents = checkNotNull(getLiveAgents(processControl));

		int workerNumber = 0;
		int threadNumber = 0;

		for (AgentAndWorkers agent : agents.values()) {
			WorkerProcessReport[] reports = agent.getWorkerProcessReports();

			if (reports != null) {
				workerNumber += reports.length;

				for (WorkerProcessReport report : reports) {
					threadNumber += report.getNumberOfRunningThreads();
				}
			}
		}

		result.put(GrinderConstants.P_PROCESS, workerNumber);
		result.put(GrinderConstants.P_THREAD, threadNumber);
	}

	/**
	 * Retrieve current live agents from {@link ProcessControl}.
	 * 
	 * @param processControl
	 *            processControl instance from which the data will be retrieved.
	 * @return available agent map
	 */
	@SuppressWarnings("unchecked")
	public static Map<ProcessIdentity, AgentAndWorkers> getLiveAgents(ProcessControl processControl) {
		ProcessStatusImplementation processStatusSet = (ProcessStatusImplementation) ReflectionUtil.getFieldValue(
				processControl, "m_processStatusSet");
		Map<ProcessIdentity, AgentAndWorkers> result = (Map<ProcessIdentity, AgentAndWorkers>) ReflectionUtil
				.getFieldValue(processStatusSet, "m_agentIdentityToAgentAndWorkers");

		return result;
	}
}
