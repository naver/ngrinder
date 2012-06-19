package net.grinder.console.communication;

import java.util.Map;

import net.grinder.common.processidentity.ProcessIdentity;
import net.grinder.common.processidentity.WorkerProcessReport;
import net.grinder.console.communication.ProcessStatusImplementation.AgentAndWorkers;

import org.springframework.test.util.ReflectionTestUtils;

import com.nhncorp.ngrinder.grinder.define.GrinderConstants;

/**
 * This class is used to get worker and thread information from grinder.
 * We add it in this package Because some class like AgentAndWorkers can only be accessed within same
 * package.
 * @author Mavlarn
 *
 */
public class NGrinderConsoleCommunicationService {
	
	/**
     * Get number of running worker processes and threads
     * @param processControl - The process control of Grinder
     * @param result - Result map to front end
     */
	public static void collectWorkerAndThreadInfo(ProcessControl processControl,
			Map<String, Object> result) {
		Map<ProcessIdentity, AgentAndWorkers> agents = getLiveAgents(processControl);

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

	@SuppressWarnings("unchecked")
	public static Map<ProcessIdentity, AgentAndWorkers> getLiveAgents(
			ProcessControl processControl) {
		ProcessStatusImplementation processStatusSet = (ProcessStatusImplementation) ReflectionTestUtils
				.getField(processControl, "m_processStatusSet");
		Map<ProcessIdentity, AgentAndWorkers> result = (Map<ProcessIdentity, AgentAndWorkers>) ReflectionTestUtils
				.getField(processStatusSet, "m_agentIdentityToAgentAndWorkers");

		return result;
	}
}
