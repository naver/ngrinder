package com.nhncorp.ngrinder.grinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.grinder.common.processidentity.WorkerProcessReport;
import net.grinder.console.communication.ProcessControl;
import net.grinder.console.communication.ProcessControl.ProcessReports;


public final class ProcessControlListener implements ProcessControl.Listener {
	
	private static final Logger LOG = LoggerFactory.getLogger(ProcessControlListener.class);
	
	private int lastWP = 0;
	
	public void update(ProcessReports[] processReports) {
		int wp = 0;
	
		for (int i = 0; i < processReports.length; ++i) {
			final WorkerProcessReport[] workerProcessStatuses = processReports[i]
					.getWorkerProcessReports();
			wp += workerProcessStatuses.length;
		}
		if (lastWP != wp) {
			LOG.debug("Current worker process count:{}", wp);
		}
		//GrinderAPI.updateWorkerProcessCount(wp);
	}
}