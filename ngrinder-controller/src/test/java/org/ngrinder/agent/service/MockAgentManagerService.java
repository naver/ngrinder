package org.ngrinder.agent.service;


import org.ngrinder.agent.repository.AgentManagerRepository;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.schedule.ScheduledTaskService;
import org.ngrinder.perftest.service.AgentManager;

public class MockAgentManagerService extends AgentManagerService {

	public MockAgentManagerService(AgentManager agentManager, AgentManagerRepository agentManagerRepository, LocalAgentService cachedLocalAgentService, Config config, ScheduledTaskService scheduledTaskService) {
		super(agentManager, agentManagerRepository, cachedLocalAgentService, config, scheduledTaskService);
	}

	@Override
	public void checkAgentStatePeriodically() {
		// Do nothing.
	}
}
