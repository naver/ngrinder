package org.ngrinder.agent.service.autoscale;

import org.ngrinder.agent.service.AgentAutoScaleAction;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.schedule.ScheduledTaskService;
import org.ngrinder.perftest.service.AgentManager;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Created by junoyoon on 15. 7. 29.
 */
@Qualifier("null")
public class NullAgentAutoScaleAction extends AgentAutoScaleAction {

	private static final AgentAutoScaleAction NULL_AGENT_AUTO_SCALE_ACTION = new NullAgentAutoScaleAction();


	@Override
	public void init(Config config, AgentManager agentManager, ScheduledTaskService scheduledTaskService) {

	}

	@Override
	public void activateNodes(int count) {
	}

	@Override
	public int getMaxNodeCount() {
		return 0;
	}

	@Override
	public void touch(String name) {
	}


	@Override
	public String getDiagnosticInfo() {
		return "null";
	}

	@Override
	public void destroy() {

	}

}
